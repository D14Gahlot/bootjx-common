package com.boot.jx.postman.fb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookMessageResponse implements Serializable {

	private static final long serialVersionUID = 8865862314571412689L;
	@JsonProperty("message_type")
	private String messageType;
	private Map<String, String> recipient = new HashMap<String, String>();
	private Map<String, String> message = new HashMap<String, String>();

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Map<String, String> getRecipient() {
		return recipient;
	}

	public void setRecipient(Map<String, String> recipient) {
		this.recipient = recipient;
	}

	public Map<String, String> getMessage() {
		return message;
	}

	public void setMessage(Map<String, String> message) {
		this.message = message;
	}
}
