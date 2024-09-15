package com.boot.jx.account.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.jx.account.AccountAuthService;
import com.boot.jx.account.doc.AccountMeta;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.account.doc.SignupContact;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.sso.service.CommonAuthenticator;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.javachinna.oauth2.user.OAuth2UserInfo;
import com.javachinna.oauth2.user.SocialEnums.ChannelPartner;

@Controller
@RequestMapping("/linq")
public class LinqContrller {

	@Autowired
	private CommonAuthenticator commonAuthenticator;

	@Autowired
	private AccountStore accountStore;

	@Autowired
	private AccountAuthService sessionService;

	@RequestMapping(value = { "/app/v1/connect/{provider}" }, method = { RequestMethod.GET })
	public String initSocialConnect(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String mode, @PathVariable(value = "provider") String provider,
			@RequestParam(required = false) String verificationId,
			@RequestParam(required = false, defaultValue = "ANY") ChannelPartner partner)
			throws IOException, URISyntaxException {

		String redirectUrl = commonAuthenticator.createAuthUrl(provider, partner);

//		if (ArgUtil.is(verificationId)) {
//			userSessionBean.postConnectVerificationId(verificationId);
//		}
		// System.out.println("redirectUrl" + redirectUrl);
		response.setHeader("Location", redirectUrl);
		response.setStatus(302);

		return "app-302";
	}

	private ApiResponse<OAuth2UserInfo, Object> connectSocialCallback(String provider, ChannelPartner partner,
			MapModel body, HttpServletRequest request) {
		OAuth2UserInfo info = commonAuthenticator.authenticate(provider, partner, body);
		if (ArgUtil.is(info)) {
			BusinessUserDoc accountDoc = accountStore.findUserByEmail(info.getEmail());
			if (ArgUtil.is(accountDoc)) {
				sessionService.login(accountDoc, request);
			} else {

				SignupContact signupContact = new SignupContact();
				signupContact.setEmail(info.getEmail());
				signupContact.setName(info.getName());
				signupContact.setPhone(info.getPhone());
				signupContact.setRole(info.getJobTitle());

				AccountMeta keys = new AccountMeta();
				keys.setEmailVerificationCode(UUID.randomUUID().toString());

				accountDoc = new BusinessUserDoc();
				accountDoc.setContact(signupContact);
				accountDoc.setMeta(keys);

				accountStore.save(accountDoc);
				sessionService.sendResetMail(accountDoc, "tenant-verify-email");
				sessionService.sendMailToSalesTeam(accountDoc, "new-customer-register-email");
			}

			return ApiResponse.buildResult(info).statusKey("AUTHORISED").redirectUrl("/partner/app/home");
		}
		return null;
	}

	@RequestMapping(
			value = { "/app/v1/connect/{provider}/callback", "/app/v1/connect/{path_partner}/{provider}/callback" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public String connectOutlookCallback(Model model, HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "provider") String provider,
			@PathVariable(required = false, value = "path_partner") ChannelPartner pathPartner,
			@RequestParam(required = false, defaultValue = "ANY") ChannelPartner partner)
			throws IOException, URISyntaxException {
		ApiResponse<OAuth2UserInfo, Object> resp = connectSocialCallback(provider, ArgUtil.anyOf(pathPartner, partner),
				null, request);
		response.setHeader("Location", resp.getRedirectUrl());
		response.setStatus(302);
		return "app-302";
	}
}
