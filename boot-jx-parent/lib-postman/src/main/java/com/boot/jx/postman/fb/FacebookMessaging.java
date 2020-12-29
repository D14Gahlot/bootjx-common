package com.boot.jx.postman.fb;

import java.io.Serializable;
import java.util.Map;

public class FacebookMessaging implements Serializable {
	private static final long serialVersionUID = 4410460271776560521L;
	private Map<String, String> sender;
	private Map<String, String> recipient;
	private Long timestamp;
	private FacebookMessage message;

	public Map<String, String> getSender() {
		return sender;
	}

	public void setSender(Map<String, String> sender) {
		this.sender = sender;
	}

	public Map<String, String> getRecipient() {
		return recipient;
	}

	public void setRecipient(Map<String, String> recipient) {
		this.recipient = recipient;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public FacebookMessage getMessage() {
		return message;
	}

	public void setMessage(FacebookMessage message) {
		this.message = message;
	}
}
