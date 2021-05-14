package com.boot.jx.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;

@Component
public class AdminLogoutHandler implements LogoutHandler {

	@Autowired
	AdminSessionService agentSessionService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (ArgUtil.is(authentication)) {
			agentSessionService.updateLogout(ArgUtil.parseAsString(authentication.getPrincipal()));
		}
	}

}
