package com.boot.jx.common.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "AGENT_SESSION")
@TypeAlias("AgentSessionDoc")
public class AgentSessionDoc {

    @Id
    private String agentCode;

    private String agentDept;

    private Boolean isLoggedIn;

    private Boolean isOnline;
    
    private Boolean isAway;

    private Boolean isEnabled;

    private Long lastOnlineStamp;

    private Long lastAssignStamp;

    public String getAgentCode() {
	return agentCode;
    }

    public void setAgentCode(String agentCode) {
	this.agentCode = agentCode;
    }

    public void setLoggedIn(Boolean isLoggedIn) {
	this.isLoggedIn = isLoggedIn;
    }

    public void setOnline(Boolean isOnline) {
	this.isOnline = isOnline;
    }

    public Long getLastOnlineStamp() {
	return lastOnlineStamp;
    }

    public void setLastOnlineStamp(Long lastOnlineStamp) {
	this.lastOnlineStamp = lastOnlineStamp;
    }

    public String getAgentDept() {
	return agentDept;
    }

    public void setAgentDept(String agentDept) {
	this.agentDept = agentDept;
    }

    public Long getLastAssignStamp() {
	return lastAssignStamp;
    }

    public void setLastAssignStamp(Long lastAssignStamp) {
	this.lastAssignStamp = lastAssignStamp;
    }

    public Boolean getIsEnabled() {
	return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
	this.isEnabled = isEnabled;
    }

    public Boolean getIsLoggedIn() {
	return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean isLoggedIn) {
	this.isLoggedIn = isLoggedIn;
    }

    public Boolean getIsOnline() {
	return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
	this.isOnline = isOnline;
    }

    public String toString() {
	return String.format("AgentSessionDoc(%s,%s,%s)", this.agentCode, this.isLoggedIn, this.isOnline);
    }

    public Boolean getIsAway() {
        return isAway;
    }

    public void setIsAway(Boolean isAway) {
        this.isAway = isAway;
    }

}
