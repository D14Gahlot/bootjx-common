package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.UserLoginTokenDoc;
import com.boot.jx.common.dto.UserLoginToken;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
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

	@Autowired
	private OAClient oaClient;

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
	public ApiResponse<UserLoginToken, Object> agentLogin(@RequestParam String username, @RequestParam String password,
			@RequestParam(required = false) String app, @RequestParam String tnt, @RequestParam String domainId,
			@RequestParam(required = false) String otp, @RequestParam(required = false) String otpNounce,
			@RequestParam(required = false) String tokenId) throws NoSuchAlgorithmException {

		UserLoginToken loginToken = empAuthService.createAgentLoginToken(username, username, password, tnt, domainId,
				app);
		if (ArgUtil.is(tokenId)) {
			UserLoginTokenDoc loginDoc = mongoTemplate.findById(tokenId, UserLoginTokenDoc.class);
			if (!new OTPDetails().yin(otpNounce).yang(loginDoc.getOtpNounce()).genrate(username, app).validate(otp,
					loginDoc.getOtpHash())) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("otp"));
			}
		} else {
			OTPDetails otpDetails = OTPUtils.genrateBasicOTP(username, app);

			ChannelConfig channel = pmEnvironment.config().channel("oa:mehery");
			OutboxMessage ob = new OutboxMessage();
			ob.contact().setPhone(loginToken.getDomainUserPhone());
			ob.setTemplateExt(new HSMTemplate3rdParty().code("login_otp"));
			ob.model().put("prefix", otpDetails.getPrefix());
			ob.model().put("value", otpDetails.getOtp());
			ob.model().put("data",
					MapModel.createInstance().put("panel", ArgUtil.nonEmpty(app, Constants.BLANK)).toMap());

			oaClient.sendMessage(channel, ob);

			// Details to SHOW/MASK to UI
			loginToken.setOtpPrefix(otpDetails.getPrefix());
			loginToken.setOtpNounce(otpDetails.getYin());
			loginToken.setDomainToken(null);

			// Details to SAVE in DB
			UserLoginTokenDoc loginDoc = EntityDtoUtil.dtoToEntity(loginToken, new UserLoginTokenDoc());
			loginDoc.setOtpNounce(otpDetails.getYang());
			loginDoc.setOtpHash(otpDetails.getHash());
			mongoTemplate.save(loginDoc);

			// Details to SHOW/MASK to UI
			loginToken.setTokenId(loginDoc.getTokenId());
		}
		return ApiResponse.buildData(loginToken);
	}

}
