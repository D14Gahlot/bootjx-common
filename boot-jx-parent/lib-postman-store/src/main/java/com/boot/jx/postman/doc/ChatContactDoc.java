package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "CHAT_CONTACT")
public class ChatContactDoc implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@ApiMockModelProperty(example = "wa919930104050", required = false)
	@JsonProperty("contactId")
	private String contactId;

	private String contactType;

	private long lastInComingStamp;

	private long lastOutGoingStamp;

	private String sessionId;

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public long getLastInComingStamp() {
		return lastInComingStamp;
	}

	public void setLastInComingStamp(long lastInComingStamp) {
		this.lastInComingStamp = lastInComingStamp;
	}

	public long getLastOutGoingStamp() {
		return lastOutGoingStamp;
	}

	public void setLastOutGoingStamp(long lastOutGoingStamp) {
		this.lastOutGoingStamp = lastOutGoingStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
