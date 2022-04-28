package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.model.InboxMessage;

@Document(collection = MessageHold.COLLECTION_NAME)
@TypeAlias("MessageHold")
public class MessageHold implements Serializable {

	private static final long serialVersionUID = -1916969779141145310L;

	public static final String COLLECTION_NAME = "MESSAGE_HOLD";
	public static final String COLLECTION_REJECTED = "MESSAGE_REJECTED";

	@Id
	private String tempId;

	@Indexed
	private String contactId;

	@Indexed
	private String sessionId;

	private long timestamp;

	private InboxMessage inboxMessage;

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public InboxMessage getInboxMessage() {
		return inboxMessage;
	}

	public void setInboxMessage(InboxMessage inboxMessage) {
		this.inboxMessage = inboxMessage;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
