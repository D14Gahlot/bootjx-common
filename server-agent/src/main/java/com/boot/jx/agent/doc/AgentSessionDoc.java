package com.boot.jx.agent.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "AGENT_SESSION")
@TypeAlias("AgentSessionDoc")
public class AgentSessionDoc {

	@Id
	private String agentCode;

	private boolean isLoggedIn;

	private boolean isOnline;

	private long lastOnlineStamp;

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
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public long getLastOnlineStamp() {
		return lastOnlineStamp;
	}

	public void setLastOnlineStamp(long lastOnlineStamp) {
		this.lastOnlineStamp = lastOnlineStamp;
	}
}
