package com.boot.jx.postman.model;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MessageDefinitions {

	@JsonDeserialize(as = ContactMeta.class, keyUsing = ContactMetaKeyDeserializer.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static interface Contact extends Serializable {
		public String getName();

		public void setName(String name);

		public String getEmail();

		public String getPhone();

		public void setPhone(String phone);

		public void setEmail(String email);

		public default void copyFrom(Contact contactable) {
			this.setName(contactable.getName());
			this.setPhone(contactable.getPhone());
			this.setEmail(contactable.getEmail());
		}

		public static Contact instance() {
			return new ContactMeta();
		}
	}

	@JsonDeserialize(as = ContactMeta.class, keyUsing = ContactMetaKeyDeserializer.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static interface ContactID extends Contact {
		public void setContactId(String contactId);

		public String getContactId();

		public default void copyFrom(ContactID contactable) {
			// Contact
			this.setName(contactable.getName());
			this.setPhone(contactable.getPhone());
			this.setEmail(contactable.getEmail());
			// ContactID
			this.setContactId(contactable.getContactId());
		}

		public static ContactID instance() {
			return new ContactMeta();
		}

	}

	@JsonDeserialize(as = ContactMeta.class, keyUsing = ContactMetaKeyDeserializer.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static interface Contactable extends ContactID {
		public String getContactType();

		public String getLane();

		public String getCsid();

		public void setCsid(String createCsid);

		public void setContactType(String contactType);

		public void setLane(String lane);

		public default ContactType type() {
			return ArgUtil.parseAsEnumT(getContactType(), ContactType.class);
		}

		public String getChannelType();

		public void setChannelType(String channelType);

		public default void type(ContactType contactType) {
			this.setContactType(ArgUtil.parseAsString(contactType));
		}

		public default void copyFrom(Contactable contactable) {
			// Contact
			this.setName(contactable.getName());
			this.setPhone(contactable.getPhone());
			this.setEmail(contactable.getEmail());
			// ContactID
			this.setContactId(contactable.getContactId());
			// Contactable
			this.setContactType(contactable.getContactType());
			this.setChannelType(contactable.getChannelType());
			this.setLane(contactable.getLane());
			this.setCsid(contactable.getCsid());
		}

		public static Contactable instance() {
			return new ContactMeta();
		}

	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageExternal extends Serializable {

	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageInternal extends Serializable {
	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessageId extends Serializable {
		public String getMessageId();

		public void setMessageId(String messageId);

		public String getMessageIdExt();

		public void setMessageIdExt(String messageIdExt);

		public String getMessageIdRef();

		public void setMessageIdRef(String messageIdRef);

		public String getReplyId();

		public String getReplyIdExt();

		public default void from(IMessageId message) {
			setMessageId(message.getMessageId());
			setMessageIdExt(message.getMessageIdExt());
			setMessageIdRef(message.getMessageIdRef());
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface SessionId extends Serializable {
		// Internal attributes
		public String getSessionId();

		public void setSessionId(String sessionId);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface SessionInfo extends SessionId, Serializable {
		public MessageSession session();

		public Contactable contact();
	}

	// External attributes
	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface SessionMessage extends SessionInfo, SessionId, IMessageId {
		public String getSubject();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface IMessage extends IMessageExternal, IMessageInternal, SessionMessage, IMessageId {

		public long getTimestamp();

		public String getType();

		public String toString();

		public String getFormatType();

		public String getFormatSubType();

		public MessageRouter route();

	}

	public static interface IMessageExtended extends IMessage {

		public List<String> to();

		String getFrom();

		BigDecimal getQueue();

		String getFromName();

		Message<?> replyMessage(String message);

	}

	public class ContactMetaKeyDeserializer extends KeyDeserializer {
		@Override
		public Object deserializeKey(String key, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			return JsonUtil.getMapper().readValue(key, ContactMeta.class);
		}
	}

	public interface LoggableEntity {
		public String getSessionId();

		public String getContactId();
	}

	public interface LogMessage extends SessionMessage, IMessageId {
		public List<Object> getLogs();

		public void setLogs(List<Object> logs);

		public default List<Object> logs() {
			if (this.getLogs() == null) {
				this.setLogs(new ArrayList<Object>());
			}
			return this.getLogs();
		}
	}
}
