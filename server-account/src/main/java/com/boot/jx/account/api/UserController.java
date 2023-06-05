package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import com.boot.jx.common.dto.UserAuthToken;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.others.OAClient;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;

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
		return ApiResponse.buildData(loginToken);
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
