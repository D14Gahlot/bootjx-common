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

	private Boolean active;
	private Boolean initd;
	private Boolean resolved;

	private Long startSessionStamp;
	private Long fistResponseStamp;

	private Long lastInComingStamp;

	private Long assignedDeptStamp;
	private Long assignedAgentStamp;

	private Long lastResponseStamp;
	private Long resolveSessionStamp;
	private Long closeSessionStamp;

	private Integer agentScore;
	private Integer botScore;

	private String mode;

	public Long getLastInComingStamp() {
		return lastInComingStamp;
	}

	public void setLastInComingStamp(Long lastInComingStamp) {
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

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
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

	public Boolean isInitd() {
		return initd;
	}

	public void setInitd(Boolean initd) {
		this.initd = initd;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getStartSessionStamp() {
		return startSessionStamp;
	}

	public void setStartSessionStamp(Long startSessionStamp) {
		this.startSessionStamp = startSessionStamp;
	}

	public Long getCloseSessionStamp() {
		return closeSessionStamp;
	}

	public void setCloseSessionStamp(Long closeSessionStamp) {
		this.closeSessionStamp = closeSessionStamp;
	}

	public Long getAssignedDeptStamp() {
		return assignedDeptStamp;
	}

	public void setAssignedDeptStamp(Long assignedDeptStamp) {
		this.assignedDeptStamp = assignedDeptStamp;
	}

	public Long getAssignedAgentStamp() {
		return assignedAgentStamp;
	}

	public void setAssignedAgentStamp(Long assignedAgentStamp) {
		this.assignedAgentStamp = assignedAgentStamp;
	}

	public Long getFistResponseStamp() {
		return fistResponseStamp;
	}

	public void setFistResponseStamp(Long fistResponseStamp) {
		this.fistResponseStamp = fistResponseStamp;
	}

	public Long getLastResponseStamp() {
		return lastResponseStamp;
	}

	public void setLastResponseStamp(Long lastResponseStamp) {
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

	public Long getResolveSessionStamp() {
		return resolveSessionStamp;
	}

	public void setResolveSessionStamp(Long resolveSessionStamp) {
		this.resolveSessionStamp = resolveSessionStamp;
	}

	public Boolean isResolved() {
		return resolved;
	}

	public void setResolved(Boolean resolved) {
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

}
