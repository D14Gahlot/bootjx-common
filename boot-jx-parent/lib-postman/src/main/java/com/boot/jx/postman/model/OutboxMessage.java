package com.boot.jx.postman.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageOptions.WAMessageOptions;
import com.boot.utils.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxMessage extends Message<OutboxMessage> implements WAMessageOptions, IMessage {

	private static final long serialVersionUID = 3115992767625612005L;

	public static enum Channel implements IChannel {
		TWILIO, APIWHA, DEFAULT, GUPSHUP, DUMMY
	}

	private BigDecimal queue;
	private MessageSession session;
	public List<String> logs;

	public OutboxMessage(ContactType contactType) {
		super(contactType);
	}

	public OutboxMessage() {
		super();
	}

	public BigDecimal getQueue() {
		return queue;
	}

	public void setQueue(BigDecimal queue) {
		this.queue = queue;
	}

	public MessageSession getSession() {
		return session;
	}

	public void setSession(MessageSession session) {
		this.session = session;
	}

	@Override
	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}

	public List<String> logs() {
		if (this.logs == null) {
			this.logs = new ArrayList<String>();
		}
		return this.logs;
	}

	@Override
	@JsonIgnore
	public String forContact() {
		return CollectionUtil.getOne(this.to);
	}

}
