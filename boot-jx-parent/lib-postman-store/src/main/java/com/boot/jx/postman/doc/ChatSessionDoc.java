package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.swagger.ApiMockModelProperty;

@Document(collection = "CHAT_SESSION")
@TypeAlias("ChatSessionDoc")
public class ChatSessionDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String sessionId;

	@ApiMockModelProperty(example = "wa919930104050", required = false)
	private String contactId;

	private String assignedToDept;
	private String assignedToAgent;

	private long lastInComingStamp;

	private boolean active;
	private boolean initd;

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

}
