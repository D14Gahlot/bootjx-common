package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.dict.ContactType;
import com.boot.utils.StringUtils.StringMatcher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InboxMessage implements Serializable {

	private static final long serialVersionUID = -4488174520614920589L;

	private String messageId;
	private String messageIdExt;
	private String to;
	private String from;
	private String fromName;
	private String sessionId;

	private String message;
	private ContactType contactType;
	private String contactId;
	private String channel;

	private BigDecimal queue;
	private String lane;
	private StringMatcher matcher;

	private String checksum;

	private Object originalMessage;
	private MessageSession session;

	protected Map<String, Object> form = new HashMap<String, Object>();
	protected Map<String, Object> data = new HashMap<String, Object>();
	protected TagDocument tags;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
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
		if (ContactType.WHATSAPP.equals(this.contactType)) {
			WAMessage reply = new WAMessage();
			reply.setQueue(this.getQueue());
			reply.setChannel(this.getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			return reply;
		} else if (ContactType.TELEGRAM.equals(this.contactType)) {
			TGMessage reply = new TGMessage();
			reply.setQueue(this.getQueue());
			reply.setChannel(this.getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			return reply;
		} else {
			OutboxMessage reply = new OutboxMessage();
			reply.setQueue(this.getQueue());
			reply.setChannel(this.getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			reply.setContactType(this.contactType);
			return reply;
		}
	}

	// Builder Functions
	public InboxMessage to(String to) {
		this.setTo(to);
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

	public StringMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(StringMatcher matcher) {
		this.matcher = matcher;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
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

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
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

	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

}
