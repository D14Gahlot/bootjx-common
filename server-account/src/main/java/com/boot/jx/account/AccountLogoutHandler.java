package com.boot.jx.account;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;

@Component
public class AccountLogoutHandler implements LogoutHandler {

	@Autowired
	AccoountAuthService agentSessionService;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (ArgUtil.is(authentication)) {
			agentSessionService.updateLogout(ArgUtil.parseAsString(authentication.getPrincipal()));
		}
		commonHttpRequest.setCookie("JXSESSIONID", "JXSESSIONID", 0);
	}

}
