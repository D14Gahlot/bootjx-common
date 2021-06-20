package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MessageDefinitions {

	public static class MESSAGE_BOUND_TYPE {
		public static final String INBOUND = "I";
		public static final String INBOUND_IMPORTED = "Ii";

		public static final String OUTBOUND = "O";
		public static final String OUTBOUND_IMPORTED = "Oi";
	}

	public static class MESSAGE_CHANNLES {
		public static final String GUPSHUPW = "GUPSHUPW";
	}

	@JsonDeserialize(as = ContactInfo.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface Contactable extends Serializable {
		public String getContactType();

		public String getChannel();

		public String getLane();

		public String getCsid();

		public String getEmail();

		public String getPhone();

		public String getContactId();

		public void setCsid(String createCsid);

		public void setContactId(String contactId);

		public void setContactType(String contactType);

		public void setChannel(String channel);

		public void setLane(String lane);

		public default ContactType type() {
			return ArgUtil.parseAsEnumT(getContactType(), ContactType.class);
		}

		public default void type(ContactType contactType) {
			this.setContactType(ArgUtil.parseAsString(contactType));
		}

	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageExternal extends Serializable {
		// External attributes
	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageInternal extends Serializable {
		// Internal attributes
		public String getSessionId();

		public void setSessionId(String sessionId);

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessage extends IMessageExternal, IMessageInternal {

		public long getTimestamp();

		public String forContact();

		public MessageSession session();

		public Contactable contact();

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
