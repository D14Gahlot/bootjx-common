package com.boot.jx.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatClient;
import com.boot.utils.TimeUtils;

@Component
public class AdminSessionService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatClient chatClient;

	/*
	 * Below APIs are
	 * 
	 * APIs for currently logged in user only
	 */

	@Autowired
	private AdminSessionBean adminSessionBean;

	public void updateSession() {
	}

	/**
	 * Refreshes online status for currently logged in agent
	 */
	public void refreshOnline() {
		if (TimeUtils.isExpired(adminSessionBean.getLastOnlineStamp(), chatClient.getChatOnlholdTimeout())) {
			adminSessionBean.setLastOnlineStamp(System.currentTimeMillis());
			this.updateSession();
		}
	}

	public void setOnline(boolean isOnline) {
		adminSessionBean.setOnline(isOnline);
		this.updateSession();
	}

	/**
	 * Refreshes login status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogin(String username) {
		adminSessionBean.setLoggedIn(true);
		adminSessionBean.setOnline(true);
		adminSessionBean.setAgentCode(username);
		adminSessionBean.setAgentDept("ONLINE");
		adminSessionBean.setLastOnlineStamp(System.currentTimeMillis());
		this.updateSession();
	}

	/**
	 * Refreshes logout status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogout(String username) {
		adminSessionBean.setLoggedIn(false);
		adminSessionBean.setOnline(false);
		adminSessionBean.setAgentCode(username);
		adminSessionBean.setAgentDept("ONLINE");
		this.updateSession();
	}

}
