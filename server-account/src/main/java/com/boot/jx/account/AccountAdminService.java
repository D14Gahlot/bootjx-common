package com.boot.jx.account;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.account.doc.DomainUserDoc;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;

@Component
public class AccountAdminService {

    /*
     * Below APIs are
     * 
     * APIs for currently logged in user only
     */

    @Autowired
    private AccountSessionBean sessionBean;

    @Autowired
    private AccountAuthProvider adminAuthProvider;

    @Autowired
    private RestService restService;

    @Value("${mry.app.url}")
    private String appServiceUrl;

    public void updateSession() {
    }

    /**
     * Refreshes login status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogin(DomainUserDoc account) {
	sessionBean.domainUser(account);
	sessionBean.setRole("DOMAIN_ADMIN");
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

    public void login(DomainUserDoc account, HttpServletRequest request) {
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
		account.getContact().getEmail(), account.getMeta().getPassword());
	token.setDetails(new WebAuthenticationDetails(request));
	Authentication authentication = adminAuthProvider.authenticate(token);
	SecurityContextHolder.getContext().setAuthentication(authentication);
	updateLogin(account);
    }

    public boolean validateCpanelUser(String domainId, String domainToken, HttpServletRequest request) {
	if (ArgUtil.is(domainId) && ArgUtil.is(domainToken)) {
	    ApiResponse<DomainUserDoc, String> resp = restService.ajax(appServiceUrl).path("/account/pub/auth")
		    .header("tnt", "app").field("tnt", "app").field("domain", AppContextUtil.getTenant())
		    .field("domainId", domainId).field("domainToken", domainToken).post()
		    .as(new ParameterizedTypeReference<ApiResponse<DomainUserDoc, String>>() {
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

    public void sendResetMail(DomainUserDoc accountDoc, String emailTemplate) {
	postManClient.send(new MessageBox().push(new Email().to(accountDoc.getContact().getEmail())
		.template(emailTemplate).put("logo", pmEnvironment.get("mry.prop.logo.bg-x-icon").asString())
		.put("website", pmEnvironment.get("mry.prop.service.website").asString())
		.put("service", pmEnvironment.get("mry.prop.service.name").asString())
		.put("link",
			String.format("https://app.%s/partner/auth/verify-link?code=%s&account=%s",
				pmEnvironment.get("mry.prop.service.domain"),
				accountDoc.getMeta().getEmailVerificationCode(), accountDoc.getId()))
		.put("name", accountDoc.getContact().getName())));
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

}
