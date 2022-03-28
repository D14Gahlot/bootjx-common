package com.boot.jx.postman.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageDefinitions.LogMessage;
import com.boot.jx.postman.model.MessageOptions.WAMessageOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxMessage extends Message<OutboxMessage>
	implements WAMessageOptions, IMessage, LogMessage, IMessageExtended {

    private static final long serialVersionUID = 3115992767625612005L;

    public static final OutboxMessage NO_MESSAGE = new OutboxMessage();

    private BigDecimal queue;
    private MessageSession session;
    private MessageRoute route;
    private MessagePrompt prompt;
    private List<String> logs;

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

    public String getCsid() {
	return this.contact().getCsid();
    }

    public void setCsid(String csid) {
	this.contact().setCsid(csid);
    }

    public MessagePrompt getPrompt() {
	return prompt;
    }

    public void setPrompt(MessagePrompt prompt) {
	this.prompt = prompt;
    }

    public OutboxMessage prompt(MessagePrompt prompt) {
	this.prompt = prompt;
	return this;
    }

    public MessageRoute getRoute() {
	return route;
    }

    public void setRoute(MessageRoute route) {
	this.route = route;
    }

    @Override
    public MessageRoute route() {
	if (route == null) {
	    this.route = new MessageRoute();
	}
	return this.route;
    }

    @Override
    public String getFrom() {
	return null;
    }

    @Override
    public String getFromName() {
	return null;
    }

    @Override
    public Message<?> replyMessage(String message) {
	return null;
    }
}
