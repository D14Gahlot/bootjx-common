package com.boot.jx.postman.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Component
public class LogManager {

    @Autowired(required = false)
    private AuditDetailProvider auditDetailProvider;

    public String getCurrenUser() {
	return ArgUtil.is(auditDetailProvider) ? auditDetailProvider.getAuditUser() : "_SYSTEM_";
    }

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private MessageStore messageStore;

    public MessageDoc note(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
	outboxMessage.contact().setContactType(sessionDoc.getContactType());
	outboxMessage.contact().setChannelType(sessionDoc.getChannel());
	outboxMessage.contact().setLane(sessionDoc.getLane());
	outboxMessage.contact().setContactId(sessionDoc.getContactId());
	outboxMessage.contact().copyFrom(sessionDoc.getContact());
	
	outboxMessage.setSessionId(sessionDoc.getSessionId());
	outboxMessage.setType("N");
	return messageStore.note(outboxMessage, getCurrenUser());
    }

    public MessageDoc log(IMessageExtended inboxMessage, String auditAgent, EVENTS event, String... logs) {
	return messageStore.log(inboxMessage, auditAgent, event, logs);
    }

    public MessageDoc log(IMessageExtended inboxMessage, EVENTS event, String... logs) {
	return log(inboxMessage, inboxMessage.session().getAgent(), event, logs);
    }

    private MessageDoc log(ChatSessionDoc sessionDoc, String auditAgent, EVENTS event, String... logs) {
	IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
	return log(inboxMessage, auditAgent, event, logs);
    }

    public MessageDoc log(ChatSessionDoc sessionDoc, EVENTS event, String... logs) {
	return log(sessionDoc, getCurrenUser(), event, logs);
    }

}
