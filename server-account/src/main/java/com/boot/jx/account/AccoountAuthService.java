package com.boot.jx.account;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.boot.jx.account.api.AccountDoc;

@Component
public class AccoountAuthService {

    /*
     * Below APIs are
     * 
     * APIs for currently logged in user only
     */

    @Autowired
    private AccountSessionBean adminSessionBean;

    @Autowired
    private AccountAuthProvider adminAuthProvider;

    public void updateSession() {
    }

    /**
     * Refreshes login status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogin(AccountDoc account) {
	adminSessionBean.setAccount(account);
	this.updateSession();
    }

    /**
     * Refreshes logout status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogout(String username) {
	adminSessionBean.setAccount(null);
	this.updateSession();
    }

    public void login(AccountDoc account, HttpServletRequest request) {
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
		account.getContact().getEmail(), account.getMeta().getPasswordHash());
	token.setDetails(new WebAuthenticationDetails(request));
	Authentication authentication = adminAuthProvider.authenticate(token);
	SecurityContextHolder.getContext().setAuthentication(authentication);
	updateLogin(account);
    }

}
