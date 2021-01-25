package com.boot.jx.postman.model;

import java.math.BigDecimal;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageOptions.WAMessageOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxMessage extends Message<OutboxMessage> implements WAMessageOptions {

	private static final long serialVersionUID = 3115992767625612005L;

	public static enum Channel implements IChannel {
		TWILIO, APIWHA, DEFAULT, GUPSHUP, DUMMY
	}

	protected Channel channel = Channel.DEFAULT;
	private BigDecimal queue;
	private String agent;

	public OutboxMessage(ContactType contactType) {
		super(contactType);
	}

	public OutboxMessage() {
		super();
	}

	public void setIChannel(Channel channel) {
		this.channel = channel;
	}

	public BigDecimal getQueue() {
		return queue;
	}

	public void setQueue(BigDecimal queue) {
		this.queue = queue;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

}
