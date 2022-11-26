package com.boot.jx.contak;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.PMCommonConfigImpl;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class ContakAuthService implements LogoutHandler, AuditDetailProvider {

	/*
	 * Below APIs are
	 * 
	 * APIs for currently logged in user only
	 */

	@Autowired
	private ContakSessionBean sessionBean;

	@Autowired
	private ContakAuthProvider adminAuthProvider;

	@Autowired
	private RestService restService;

	@Autowired
	private PMCommonConfigImpl appCommonConfig;

	@Value("${mry.app.url}")
	private String appServiceUrl;

	public void updateSession() {
	}

	/**
	 * Refreshes login status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogin(ContakUserDoc account) {
		sessionBean.domainUser(account);
		sessionBean.addRole(CollectionUtil.asArray(account.getRole()));
		sessionBean.addRole(PMConstants.USER_ROLE.BUSINESS_USER);
		if (ArgUtil.areEqual(appCommonConfig.getDuperEmail(), account.getEmail())) {
			sessionBean.addRole(PMConstants.USER_ROLE.DUPER_USER);
		}
		this.updateSession();
	}

	/**
	 * Refreshes logout status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogout(String username) {
		sessionBean.domainUser(null);
		SecurityContextHolder.getContext().setAuthentication(null);
		this.updateSession();
	}

	public void login(ContakUserDoc account, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account.getEmail(),
				account.getMeta().getPassword());
		token.setDetails(new WebAuthenticationDetails(request));
		Authentication authentication = adminAuthProvider.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		updateLogin(account);
	}

	public boolean validateCpanelUser(String domainId, String domainToken, HttpServletRequest request) {
		if (ArgUtil.is(domainId) && ArgUtil.is(domainToken)) {
			ApiResponse<ContakUserDoc, String> resp = restService.ajax(appServiceUrl).path("/account/pub/auth")
					.header("tnt", "app").field("tnt", "app").field("domain", AppContextUtil.getTenant())
					.field("domainId", domainId).field("domainToken", domainToken).post()
					.as(new ParameterizedTypeReference<ApiResponse<ContakUserDoc, String>>() {
					});
			login(resp.getResult(), request);
		}

		if (!ArgUtil.is(sessionBean.domainUser())) {
			return true;
		}

		return false;
	}

	@Autowired
	private PostManClient postManClient;

	@Autowired
	private PMEnvironment pmEnvironment;

	public void sendResetMail(ContakUserDoc accountDoc, String emailTemplate) {
		postManClient.send(new MessageBox().push(new Email().to(accountDoc.getEmail()).template(emailTemplate)
				.put("logo", pmEnvironment.keyEntry("mry.prop.logo.bg-x-icon").asString())
				.put("website", pmEnvironment.keyEntry("mry.prop.service.website").asString())
				.put("service", pmEnvironment.keyEntry("mry.prop.service.name").asString())
				.put("servicedomain", pmEnvironment.keyEntry("mry.prop.service.server").asString())
				.put("link",
						String.format("https://app.%s/partner/auth/verify-link?code=%s&account=%s",
								pmEnvironment.keyEntry("mry.prop.service.domain").asString(),
								accountDoc.getMeta().getEmailVerificationCode(), accountDoc.getId()))
				.put("contactName", accountDoc.getName())));

	}

	public static Authentication getAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth) && auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
			return auth;
		}
		return null;
	}

	public static boolean isAuthenticated() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth) && auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
			return true;
		}
		return false;
	}

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (ArgUtil.is(authentication)) {
			updateLogout(ArgUtil.parseAsString(authentication.getPrincipal()));
		}
		commonHttpRequest.instance(request, response, appConfig).setCookie("ACCTSESSIONID", "ACCTSESSIONID", 0);
	}

	@Override
	public AppAuthUser getAuthUser() {
		return this.sessionBean;
	}

	@Override
	public String getAuditUser() {
		if (RequestContextHolder.getRequestAttributes() != null) {
			if (ArgUtil.is(getAuthUser())) {
				return getAuthUser().getAuthUser();
			}
		}
		return PMConstants.DEFAULT.NO_USER;
	}

}
