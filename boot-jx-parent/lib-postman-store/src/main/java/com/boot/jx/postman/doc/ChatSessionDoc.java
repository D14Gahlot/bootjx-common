package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.swagger.ApiMockModelProperty;

@Document(collection = "CHAT_SESSION")
public class ChatSessionDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String sessionId;

	@ApiMockModelProperty(example = "wa919930104050", required = false)
	private String contactId;

	private String assignedTo;

	private long lastInComingStamp;

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

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

}
