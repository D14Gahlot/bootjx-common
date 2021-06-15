package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.boot.jx.dict.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface IMessage extends Serializable {

	public String getContactId();

	public ContactType getContactType();

	public String getLane();

	public String forContact();

	public String getSessionId();

	public MessageSession session();

	public static interface SessionMessage extends IMessage {

		void setContactId(String contactId);

		String getChannel();

		String getFrom();

		void setSessionId(String sessionId);

		BigDecimal getQueue();

		String getFromName();

		String getTo();

		Message<?> replyMessage(String message);

	}

}
