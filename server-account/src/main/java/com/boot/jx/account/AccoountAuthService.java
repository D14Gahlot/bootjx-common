package com.boot.jx.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.common.dto.AgentResponseAuthDto;

@Component
public class AccoountAuthService {

	/*
	 * Below APIs are
	 * 
	 * APIs for currently logged in user only
	 */

	@Autowired
	private AccountSessionBean adminSessionBean;

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

}
