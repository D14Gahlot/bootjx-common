package com.boot.jx.postman.model;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class MessageDefinitions {

	@JsonDeserialize(as = ContactMeta.class, keyUsing = ContactMetaKeyDeserializer.class)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static interface Contact extends Serializable, JsonIgnoreNull, JsonIgnoreUnknown {
		public String getName();

		public void setName(String name);

		public String getEmail();

		public String phone();

		public void setPhone(String phone);

		public void phone(String phone);

		public void setEmail(String email);

		public default void copyFrom(Contact contactable) {
			this.setName(contactable.getName());
			this.phone(contactable.phone());
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
			this.phone(contactable.phone());
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
			if (ArgUtil.is(contactable.getName())) {
				this.setName(contactable.getName());
			}
			if (ArgUtil.is(contactable.phone())) {
				this.phone(contactable.phone());
			}
			if (ArgUtil.is(contactable.getEmail())) {
				this.setEmail(contactable.getEmail());
			}
			// ContactID
			if (ArgUtil.is(contactable.getContactId())) {
				this.setContactId(contactable.getContactId());
			}
			// Contactable
			if (ArgUtil.is(contactable.getContactType())) {
				this.setContactType(contactable.getContactType());
			}
			if (ArgUtil.is(contactable.getChannelType())) {
				this.setChannelType(contactable.getChannelType());
			}
			if (ArgUtil.is(contactable.getLane())) {
				this.setLane(contactable.getLane());
			}
			if (ArgUtil.is(contactable.getCsid())) {
				this.setCsid(contactable.getCsid());
			}
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

		public long getTimestamp();

		public void setTimestamp(long timestamp);

		public default void from(IMessageId message) {
			setMessageId(message.getMessageId());
			setMessageIdExt(message.getMessageIdExt());
			setMessageIdRef(message.getMessageIdRef());
//			setType(message.getType());
//			setTemplateId(message.getTemplateId());
//			setTemplateCode(message.getTemplateCode());
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

		public static final JsonPath AGENT_NAME = new JsonPath("session/agent/name");
		public static final JsonPath AGENT_CODE = new JsonPath("session/agent/code");
		public static final JsonPath TEAM_NAME = new JsonPath("session/team/name");
		public static final JsonPath TEAM_CODE = new JsonPath("session/team/code");

		public MessageSession session();

		public Contactable contact();

		public List<Object> trace();

		public MessageRouter route();
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

	}

	public static interface IMessageExtended extends IMessage {

		public List<String> to();

		String getFrom();

		BigDecimal getQueue();

		String getFromName();

		Message<?> replyMessage(String message);

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

	public interface TraceMessage extends SessionInfo {
		public List<Object> getTrace();

		public void setTrace(List<Object> trace);

		public default List<Object> trace() {
			if (this.getTrace() == null) {
				this.setTrace(new ArrayList<Object>());
			}
			return this.getTrace();
		}

		public String id();

		public void id(String id);
	}

	public static class ContactMetaKeyDeserializer extends KeyDeserializer {
		@Override
		public Object deserializeKey(String key, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			return JsonUtil.getMapper().readValue(key, ContactMeta.class);
		}
	}

	public static class ContactableDeserializer extends JsonDeserializer<Contactable> {
		@Override
		public Contactable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			JsonNode jsonNode = jp.getCodec().readTree(jp);
			String text = jsonNode.asText();
			return JsonUtil.getMapper().convertValue(text, ContactMeta.class);
		}
	}

	static {
		ObjectMapper objectMapper = JsonUtil.getMapper();
		SimpleModule module = new SimpleModule();
		module.addKeyDeserializer(ContactMeta.class, new ContactMetaKeyDeserializer());
		module.addDeserializer(Contactable.class, new ContactableDeserializer());
		objectMapper.registerModule(module);

	}
}
