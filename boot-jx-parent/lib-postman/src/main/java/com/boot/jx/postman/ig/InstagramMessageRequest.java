package com.boot.jx.postman.ig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.utils.JsonPath;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramMessageRequest implements Serializable {

	private static final long serialVersionUID = 8865862314571412689L;

	public static final JsonPath MESSAGE_ATTACHMENT_TYPE = new JsonPath("attachment/type");
	public static final JsonPath MESSAGE_ATTACHMENT_URL = new JsonPath("attachment/payload/url");
	public static final JsonPath MESSAGE_ATTACHMENT_REUSABLE = new JsonPath("attachment/payload/is_reusable");

	@JsonProperty("message_type")
	private String messageType;
	private Map<String, String> recipient = new HashMap<String, String>();
	private Map<String, Object> message = new HashMap<String, Object>();

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

	public Map<String, Object> getMessage() {
		return message;
	}

	public void setMessage(Map<String, Object> message) {
		this.message = message;
	}

	public InstagramMessageRequest messageType(String messageType) {
		this.messageType = messageType;
		return this;
	}

	public InstagramMessageRequest recipientId(String recipientId) {
		this.getRecipient().put("id", recipientId);
		return this;
	}

	public InstagramMessageRequest messageText(String messageText) {
		this.getMessage().put("text", messageText);
		return this;
	}

	/**
	 * https://developers.facebook.com/docs/messenger-platform/send-messages/#types
	 * 
	 * @param type - image |video| audio | file | template
	 * @return
	 */
	public InstagramMessageRequest attachmentType(Object type) {
		MESSAGE_ATTACHMENT_TYPE.save(this.message, type);
		return this;
	}

	/**
	 * https://developers.facebook.com/docs/messenger-platform/send-messages/#types
	 * 
	 * @return
	 */
	public InstagramMessageRequest attachmentUrl(Object type) {
		MESSAGE_ATTACHMENT_URL.save(this.message, type);
		MESSAGE_ATTACHMENT_REUSABLE.save(this.message, false);
		return this;
	}

}
