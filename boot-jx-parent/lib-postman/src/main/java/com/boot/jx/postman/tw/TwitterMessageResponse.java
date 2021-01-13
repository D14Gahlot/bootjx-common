package com.boot.jx.postman.tw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.postman.model.InboxMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterMessageResponse implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7126671857642101120L;
	@JsonProperty("message_type")
	private String messageType;
	private Map<String, String> recipient = new HashMap<String, String>();
	private Map<String, String> message = new HashMap<String, String>();
	List<TwitterMessage> lstOfMsg = new ArrayList<TwitterMessage>();
	List<InboxMessage> inboxMsg = new ArrayList<InboxMessage>();
	
	

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

	public List<TwitterMessage> getLstOfMsg() {
		return lstOfMsg;
	}

	public void setLstOfMsg(List<TwitterMessage> lstOfMsg) {
		this.lstOfMsg = lstOfMsg;
	}

	public List<InboxMessage> getInboxMsg() {
		return inboxMsg;
	}

	public void setInboxMsg(List<InboxMessage> inboxMsg) {
		this.inboxMsg = inboxMsg;
	}

	
}
