package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;
import com.boot.utils.TimeUtils.TimeUnits;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AgentSessionBean {

	private String agentCode;
	private String agentDept;

	private boolean isLoggedIn;

	private boolean isOnline;

	private long lastOnlineStamp;

	private boolean isDirty;

	public String getAgentCode() {
		return agentCode;
	}

	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		this.isDirty = true;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
		this.isDirty = true;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public long getLastOnlineStamp() {
		return lastOnlineStamp;
	}

	public void setLastOnlineStamp(long lastOnlineStamp) {
		this.lastOnlineStamp = lastOnlineStamp;
		this.isDirty = true;
	}

	public String getAgentDept() {
		return agentDept;
	}

	public void setAgentDept(String agentDept) {
		this.agentDept = agentDept;
	}

	@Autowired
	private MongoTemplate mongoTemplate;

	public void update() {
		AgentSessionDoc agentSessionDoc = mongoTemplate.findById(this.agentCode, AgentSessionDoc.class);
		if (ArgUtil.isEmpty(agentSessionDoc)) {
			agentSessionDoc = new AgentSessionDoc();
		}
		agentSessionDoc.setAgentCode(agentCode);
		agentSessionDoc.setAgentDept(agentDept);
		agentSessionDoc.setLoggedIn(isLoggedIn);
		agentSessionDoc.setOnline(isOnline);
		agentSessionDoc.setLastOnlineStamp(lastOnlineStamp);
		mongoTemplate.save(agentSessionDoc);
	}

	public void login(String username) {
		this.setLoggedIn(true);
		this.setOnline(true);
		this.setAgentCode(username);
		this.setAgentDept("ONLINE");
		this.setLastOnlineStamp(System.currentTimeMillis());
	}

	@Value("${postman.chat.onhold.timeout}")
	String chatOnlholdTimeout;
	
	public void refreshOnline() {
		if (TimeUtils.isExpired(this.lastOnlineStamp, chatOnlholdTimeout)) {
			this.setLastOnlineStamp(System.currentTimeMillis());
			this.update();
		}
	}

}
