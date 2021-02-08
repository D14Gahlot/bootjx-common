package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.chat.ChatClient;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class AgentSessionService {

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
	private AgentSessionBean agentSessionBean;

	public void updateSession() {
		AgentSessionDoc agentSessionDoc = mongoTemplate.findById(agentSessionBean.getAgentCode(),
				AgentSessionDoc.class);
		if (ArgUtil.isEmpty(agentSessionDoc)) {
			agentSessionDoc = new AgentSessionDoc();
		}
		agentSessionDoc.setAgentCode(agentSessionBean.getAgentCode());
		agentSessionDoc.setAgentDept(agentSessionBean.getAgentDept());
		agentSessionDoc.setLoggedIn(agentSessionBean.isLoggedIn());
		agentSessionDoc.setOnline(agentSessionBean.isOnline());
		agentSessionDoc.setLastOnlineStamp(agentSessionBean.getLastOnlineStamp());
		mongoTemplate.save(agentSessionDoc);
	}

	/**
	 * Refreshes online status for currently logged in agent
	 */
	public void refreshOnline() {
		if (TimeUtils.isExpired(agentSessionBean.getLastOnlineStamp(), chatClient.getChatOnlholdTimeout())) {
			agentSessionBean.setLastOnlineStamp(System.currentTimeMillis());
			this.updateSession();
		}
	}

	public void setOnline(boolean isOnline) {
		agentSessionBean.setOnline(isOnline);
		this.updateSession();
	}

	/**
	 * Refreshes login status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogin(String username) {
		agentSessionBean.setLoggedIn(true);
		agentSessionBean.setOnline(true);
		agentSessionBean.setAgentCode(username);
		agentSessionBean.setAgentDept("ONLINE");
		agentSessionBean.setLastOnlineStamp(System.currentTimeMillis());
		this.updateSession();
	}

	/**
	 * Refreshes logout status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogout(String username) {
		agentSessionBean.setLoggedIn(false);
		agentSessionBean.setOnline(false);
		agentSessionBean.setAgentCode(username);
		agentSessionBean.setAgentDept("ONLINE");
		this.updateSession();
	}

}
