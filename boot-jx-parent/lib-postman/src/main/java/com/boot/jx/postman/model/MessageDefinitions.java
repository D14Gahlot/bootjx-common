package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class MessageDefinitions {

	public static class MESSAGE_BOUND_TYPE {
		public static final String INBOUND = "I";
		public static final String INBOUND_IMPORTED = "Ii";

		public static final String OUTBOUND = "O";
		public static final String OUTBOUND_IMPORTED = "Oi";
	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageExternal extends Serializable {
		// External attributes
		public String getChannel();

		public ContactType getContactType();

		public String getLane();

		public String getCsid();
	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageInternal extends Serializable {
		// Internal attributes
		public String getSessionId();

		public void setSessionId(String sessionId);

		public String getContactId();

		public void setContactId(String contactId);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessage extends IMessageExternal, IMessageInternal {

		public long getTimestamp();

		public String forContact();

		public MessageSession session();

		public String getType();

	}

	public static interface SessionMessage extends IMessage {

		public List<String> to();

		String getFrom();

		void setSessionId(String sessionId);

		BigDecimal getQueue();

		String getFromName();

		Message<?> replyMessage(String message);

	}
}
