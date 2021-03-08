package com.boot.jx.postman.fb;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookMessageResp implements Serializable {

	private static final long serialVersionUID = -8675624188026661184L;

	@JsonProperty("recipient_id")
	private String recipientId;

	@JsonProperty("message_id")
	private String messageId;

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

}
