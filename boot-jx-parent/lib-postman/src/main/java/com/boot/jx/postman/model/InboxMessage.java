package com.boot.jx.postman.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.boot.jx.dict.ContactType;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

	private String assignedToDept;
	private String assignedToAgent;

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
		if (ArgUtil.is(ContactType.WHATSAPP.equals(this.contactType))) {
			WAMessage reply = new WAMessage();
			reply.setQueue(this.getQueue());
			reply.setChannel(this.getChannel());
			reply.addTo(this.getFrom());
			reply.setMessage(message);
			return reply;
		} else if (ArgUtil.is(ContactType.TELEGRAM.equals(this.contactType))) {
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

	public String getAssignedToDept() {
		return assignedToDept;
	}

	public void setAssignedToDept(String assignedToDept) {
		this.assignedToDept = assignedToDept;
	}

	public String getAssignedToAgent() {
		return assignedToAgent;
	}

	public void setAssignedToAgent(String assignedToAgent) {
		this.assignedToAgent = assignedToAgent;
	}
}
