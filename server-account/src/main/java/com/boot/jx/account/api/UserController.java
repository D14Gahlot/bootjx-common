package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.UserAuthTokenDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.dto.UserAuthToken;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.UniqueID;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private EmpAuthService empAuthService;

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@RequestMapping(value = { "/auth/**", "/app/**" }, method = { RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		model.addAllAttributes(appCommonConfig.appAttributes());

		Authentication auth = AccountAuthService.getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_ROLE", "ACCOUNT_ADMIN");
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_ROLE", "GUEST");
		}

		model.addAttribute("APP", "account");

		return "app-account";
	}

	@ResponseBody
	@RequestMapping(value = "/pub/login", method = { RequestMethod.POST })
	public ApiResponse<UserAuthToken, Object> agentLogin(@RequestParam String username, @RequestParam String password,
			@RequestParam(required = false) String app, @RequestParam String tnt, @RequestParam String domainId,
			@RequestParam(required = false) String otp, @RequestParam(required = false) String otpNounce,
			@RequestParam(required = false) String tokenId) throws NoSuchAlgorithmException {

		UserAuthToken loginToken = empAuthService.createAgentLoginToken(username, username, password, tnt, domainId,
				app, "LOGIN");
		if (ArgUtil.is(tokenId)) {
			UserAuthTokenDoc loginDoc = mongoTemplate.findById(tokenId, UserAuthTokenDoc.class);
			if (!new OTPDetails().yin(otpNounce).yang(loginDoc.getOtpNounce()).genrate(username, app).validate(otp,
					loginDoc.getOtpHash())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("otp")
						.codeKey("ValidCredentials").description("Invalid OTP"));
			}
		} else if (ArgUtil.is(loginToken.getDomainUserPhone())) {
			empAuthService.sendOTP(loginToken);
		}

		if (ArgUtil.is(loginToken.getDomainToken())) {
			tokenId = ArgUtil.nonEmpty(tokenId, loginToken.getTokenId());
			UserAuthTokenDoc loginDoc = null;
			if (ArgUtil.is(tokenId)) {
				loginDoc = mongoTemplate.findById(tokenId, UserAuthTokenDoc.class);
			}
			if (!ArgUtil.is(loginDoc)) {
				loginDoc = EntityDtoUtil.dtoToEntity(loginToken, new UserAuthTokenDoc());
			}
			loginDoc.setSsoToken(UniqueID.generateString62());
			mongoTemplate.save(loginDoc);
			commonHttpRequest.setCookie("tokenId", loginDoc.getTokenId(), "/user", 3600 * 8);
			commonHttpRequest.setCookie("ssoToken", loginDoc.getSsoToken(), "/user", 3600 * 8);
		}
		return ApiResponse.buildData(loginToken);
	}

	@ResponseBody
	@RequestMapping(value = "/pub/logout", method = { RequestMethod.GET })
	public ApiResponse<UserAuthToken, Object> ssoLogout(@CookieValue(required = false) String tokenId)
			throws NoSuchAlgorithmException {
		UserAuthTokenDoc loginDoc = mongoTemplate.findById(tokenId, UserAuthTokenDoc.class);
		if (ArgUtil.is(loginDoc)) {
			loginDoc.setInvalid(true);
			mongoTemplate.save(loginDoc);
		} else {
			loginDoc = null;
		}
		return ApiResponse.buildResult(loginDoc);
	}

	@ResponseBody
	@RequestMapping(value = "/pub/sso", method = { RequestMethod.GET })
	public ApiResponse<AgentResponseAuthDto, UserAuthToken> ssoLogin(@CookieValue(required = false) String tokenId,
			@CookieValue(required = false) String ssoToken) throws NoSuchAlgorithmException {
		UserAuthTokenDoc loginDoc = mongoTemplate.findById(tokenId, UserAuthTokenDoc.class);
		AgentResponseAuthDto agent = null;
		if (ArgUtil.is(loginDoc) && ArgUtil.is(loginDoc.getSsoToken(), ssoToken)) {
			agent = empAuthService.loginByDomainToken(loginDoc);
		} else {
			loginDoc = null;
		}
		return ApiResponse.buildResult(agent, loginDoc);
	}

	@ResponseBody
	@RequestMapping(value = "/pub/resetpass/{flow}", method = { RequestMethod.POST })
	public ApiResponse<?, ?> resetPass(@PathVariable String flow, @RequestParam String username,
			@RequestParam(required = false) String password, @RequestParam(required = false) String newpassword,
			@RequestParam(required = false) String app, @RequestParam String tnt, @RequestParam String domainId,
			@RequestParam(required = false) String otp, @RequestParam(required = false) String otpNounce,
			@RequestParam(required = false) String tokenId) throws NoSuchAlgorithmException {

		if ("FORGOTPASS".equalsIgnoreCase(flow)) {
			return empAuthService.agentResetPass(username, ArgUtil.is(app, "admin"));
		}

		UserAuthToken loginToken = empAuthService.createAgentLoginToken(username, username, password, tnt, domainId,
				app, "RESETPASS");
		if (ArgUtil.is(tokenId)) {
			UserAuthTokenDoc loginDoc = mongoTemplate.findById(tokenId, UserAuthTokenDoc.class);
			if (!new OTPDetails().yin(otpNounce).yang(loginDoc.getOtpNounce()).genrate(username, app).validate(otp,
					loginDoc.getOtpHash())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("otp")
						.codeKey("ValidCredentials").description("Invalid OTP"));
			}
			empAuthService.agentSetPass(username, password, newpassword, ArgUtil.is(app, "admin"));
		} else {
			empAuthService.sendOTP(loginToken);
		}
		return ApiResponse.buildData(loginToken);
	}

}
