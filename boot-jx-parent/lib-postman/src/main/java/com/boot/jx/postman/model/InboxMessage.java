package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.MESSAGE_BOUND_TYPE;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InboxMessage implements Serializable, SessionMessage {

	private static final long serialVersionUID = -4488174520614920589L;

	private String messageId;
	private String messageIdExt;
	protected List<String> to;
	private String from;
	private String fromName;
	private String sessionId;

	private Contactable contact;
	private BigDecimal queue;

	private long timestamp;
	private String message;

	@JsonIgnore
	private StringMatcher matcher;

	private String checksum;

	private Object originalMessage;
	private MessageSession session;

	protected Map<String, Object> form = new HashMap<String, Object>();
	protected Map<String, Object> data = new HashMap<String, Object>();
	protected TagDocument tags;
	private List<Attachment> attachments = null;

	public InboxMessage() {
		this.timestamp = System.currentTimeMillis();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BigDecimal getQueue() {
		return queue;
	}

	public void setQueue(BigDecimal queue) {
		this.queue = queue;
	}

	public WAMessage replyWAMessage(String message) {
		WAMessage reply = new WAMessage();
		reply.setQueue(this.getQueue());
		reply.addTo(this.getFrom());
		reply.setMessage(message);
		return reply;
	}

	public Message<?> replyMessage(String message) {
		if (ContactType.WHATSAPP.toString().equals(this.contact().getContactType())) {
			WAMessage reply = new WAMessage();
			reply.setQueue(this.getQueue());
			reply.contact().setChannel(this.contact().getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			return reply;
		} else if (ContactType.TELEGRAM.toString().equals(this.contact().getContactType())) {
			TGMessage reply = new TGMessage();
			reply.setQueue(this.getQueue());
			reply.contact().setChannel(this.contact().getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			return reply;
		} else {
			OutboxMessage reply = new OutboxMessage();
			reply.setQueue(this.getQueue());
			reply.contact().setChannel(this.contact().getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			reply.contact().setContactType(this.contact().getContactType());
			return reply;
		}
	}

	// Builder Functions
	public InboxMessage to(String to) {
		this.to().add(to);
		return this;
	}

	public InboxMessage from(String from) {
		this.setFrom(from);
		return this;
	}

	public InboxMessage message(String message) {
		this.setMessage(message);
		return this;
	}

	@JsonIgnore
	public StringMatcher getMatcher() {
		return matcher;
	}

	@JsonIgnore
	public void setMatcher(StringMatcher matcher) {
		this.matcher = matcher;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getMessageIdExt() {
		return messageIdExt;
	}

	public void setMessageIdExt(String messageIdExt) {
		this.messageIdExt = messageIdExt;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Map<String, Object> getForm() {
		return form;
	}

	public void setForm(Map<String, Object> form) {
		this.form = form;
	}

	public Map<String, Object> form() {
		if (form == null) {
			this.form = new HashMap<String, Object>();
		}
		return this.form;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Map<String, Object> data() {
		if (data == null) {
			this.data = new HashMap<String, Object>();
		}
		return this.data;
	}

	public Object getOriginalMessage() {
		return originalMessage;
	}

	public void setOriginalMessage(Object originalMessage) {
		this.originalMessage = originalMessage;
	}

	public TagDocument getTags() {
		return tags;
	}

	public void setTags(TagDocument tags) {
		this.tags = tags;
	}

	public MessageSession getSession() {
		return session;
	}

	public void setSession(MessageSession session) {
		this.session = session;
	}

	@Override
	public List<String> to() {
		if (to == null) {
			this.to = new ArrayList<String>();
		}
		return this.to;
	}

	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

	@Override
	public String forContact() {
		if (ArgUtil.is(this.contact().getCsid())) {
			return this.contact().getCsid();
		}
		return this.from;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<Attachment> attachments() {
		if (this.attachments == null) {
			this.attachments = new ArrayList<Attachment>();
		}
		return attachments;
	}

	public InboxMessage attachment(Attachment... attachments) {
		for (Attachment file : attachments) {
			this.attachments().add(file);
		}
		return this;
	}

	@Override
	public String getType() {
		return MESSAGE_BOUND_TYPE.INBOUND;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public Contactable getContact() {
		return contact;
	}

	public void setContact(Contactable contact) {
		this.contact = contact;
	}

	public Contactable contact() {
		if (this.contact == null) {
			this.contact = new ContactInfo();
		}
		return this.contact;
	}

	@Override
	public String toString() {
		return String.format("[messageId:%s]", this.messageId);
	}
}
