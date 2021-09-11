package com.boot.jx.admin;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.boot.jx.common.dto.AgentResponseAuthDto;

@Component
public class AdminSessionService {

    /*
     * Below APIs are
     * 
     * APIs for currently logged in user only
     */

    @Autowired
    private AdminSessionBean adminSessionBean;

    @Autowired
    private AdminAuthProvider adminAuthProvider;

    public void updateSession() {
    }

    /**
     * Refreshes login status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogin(AgentResponseAuthDto agent) {
	adminSessionBean.setProfile(agent);
	this.updateSession();
    }

    /**
     * Refreshes logout status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogout(String username) {
	adminSessionBean.setProfile(null);
	this.updateSession();
    }

    public void login(HttpServletRequest request, AgentResponseAuthDto agent, String passhash) {
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(agent.getAgent_code(),
		passhash);
	token.setDetails(new WebAuthenticationDetails(request));
	Authentication authentication = adminAuthProvider.authenticate(token);
	SecurityContextHolder.getContext().setAuthentication(authentication);
	updateLogin(agent);
    }

}
