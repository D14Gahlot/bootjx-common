package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.APatchableIndexed;
import com.boot.jx.swagger.ApiMockModelProperty;

@Document(collection = "CHAT_SESSION")
@TypeAlias("ChatSessionDoc")
public class ChatSessionDoc extends APatchableIndexed<ChatSessionDoc, String> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String sessionId;

	@Version
	private Long version;

	@ApiMockModelProperty(example = "wa919930104050", required = false)
	private String contactId;
	private String contactType;
	private String channel;
	private String lane;

	private String contactName;

	private String assignedToDept;
	private String assignedToAgent;

	private boolean active;
	private boolean initd;
	private boolean resolved;
	private boolean expired;

	private long startSessionStamp;
	private long fistResponseStamp;

	private long agentSessionStamp;

	private long lastInComingStamp;
	private long lastOutGoingStamp;

	private long assignedDeptStamp;
	private long assignedAgentStamp;

	private long lastResponseStamp;
	private long resolveSessionStamp;
	private long closeSessionStamp;

	private Integer agentScore;
	private Integer botScore;

	private String mode;
	private String status;

	public long getLastInComingStamp() {
		return lastInComingStamp;
	}

	public void setLastInComingStamp(long lastInComingStamp) {
		this.lastInComingStamp = lastInComingStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAssignedToDept() {
		return assignedToDept;
	}

	public void setAssignedToDept(String assignedToDept) {
		this.assignedToDept = assignedToDept;
	}

	public String getAssignedToAgent() {
		return assignedToAgent;
	}

	public void setAssignedToAgent(String assignedToAgent) {
		this.assignedToAgent = assignedToAgent;
	}

	public boolean isInitd() {
		return initd;
	}

	public void setInitd(boolean initd) {
		this.initd = initd;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public long getStartSessionStamp() {
		return startSessionStamp;
	}

	public void setStartSessionStamp(long startSessionStamp) {
		this.startSessionStamp = startSessionStamp;
	}

	public long getCloseSessionStamp() {
		return closeSessionStamp;
	}

	public void setCloseSessionStamp(long closeSessionStamp) {
		this.closeSessionStamp = closeSessionStamp;
	}

	public long getAssignedDeptStamp() {
		return assignedDeptStamp;
	}

	public void setAssignedDeptStamp(long assignedDeptStamp) {
		this.assignedDeptStamp = assignedDeptStamp;
	}

	public long getAssignedAgentStamp() {
		return assignedAgentStamp;
	}

	public void setAssignedAgentStamp(long assignedAgentStamp) {
		this.assignedAgentStamp = assignedAgentStamp;
	}

	public long getFistResponseStamp() {
		return fistResponseStamp;
	}

	public void setFistResponseStamp(long fistResponseStamp) {
		this.fistResponseStamp = fistResponseStamp;
	}

	public long getLastResponseStamp() {
		return lastResponseStamp;
	}

	public void setLastResponseStamp(long lastResponseStamp) {
		this.lastResponseStamp = lastResponseStamp;
	}

	public Integer getAgentScore() {
		return agentScore;
	}

	public void setAgentScore(Integer agentScore) {
		this.agentScore = agentScore;
	}

	public Integer getBotScore() {
		return botScore;
	}

	public void setBotScore(Integer botScore) {
		this.botScore = botScore;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public long getResolveSessionStamp() {
		return resolveSessionStamp;
	}

	public void setResolveSessionStamp(long resolveSessionStamp) {
		this.resolveSessionStamp = resolveSessionStamp;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	@Override
	public ChatSessionDoc newInstance() {
		return new ChatSessionDoc();
	}

	@Override
	public void id(String id) {
		this.sessionId = id;
	}

	@Override
	public String id() {
		return this.sessionId;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getAgentSessionStamp() {
		return agentSessionStamp;
	}

	public void setAgentSessionStamp(long agentSessionStamp) {
		this.agentSessionStamp = agentSessionStamp;
	}

	public long getLastOutGoingStamp() {
		return lastOutGoingStamp;
	}

	public void setLastOutGoingStamp(long lastOutGoingStamp) {
		this.lastOutGoingStamp = lastOutGoingStamp;
	}

}
