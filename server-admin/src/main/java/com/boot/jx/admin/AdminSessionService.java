package com.boot.jx.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.boot.jx.AppConfig;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.utils.ArgUtil;

@Component
public class AdminSessionService implements LogoutHandler, AuditDetailProvider {

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
		if (agent.isAdmin()) {
			adminSessionBean.addRole(PMConstants.USER_ROLE.ADMIN);
		}
		if (agent.isSuperAdmin()) {
			adminSessionBean.addRole(PMConstants.USER_ROLE.BUSINESS_USER);
		}
		if (agent.isDuperAdmin()) {
			adminSessionBean.addRole(PMConstants.USER_ROLE.DUPER_USER);
		}

		updateLogin(agent);
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
		commonHttpRequest.instance(request, response, appConfig).setCookie("JXSESSIONID", "JXSESSIONID", 0);
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

	@Override
	public AppAuthUser getAuthUser() {
		return this.adminSessionBean;
	}

}
