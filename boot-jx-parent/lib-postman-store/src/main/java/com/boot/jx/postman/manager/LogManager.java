package com.boot.jx.postman.manager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
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

    public MessageDoc event(IMessageExtended inboxMessage, String actorAgent, EVENTS eventName, String... logMessage) {
	MessageDoc doc = new MessageDoc();
	doc.setContactId(PostManUtil.createContactId(inboxMessage));
	doc.setType("L");
	doc.setTimestamp(System.currentTimeMillis());
	if (ArgUtil.is(logMessage)) {
	    for (String string : logMessage) {
		doc.logs().add(string);
	    }
	}
	doc.setAction(ArgUtil.parseAsString(eventName));
	doc.setSessionId(inboxMessage.getSessionId());
	doc.setAgent(actorAgent);
	messageStore.save(doc, inboxMessage.contact().type());
	return doc;
    }

    public MessageDoc event(IMessageExtended inboxMessage, EVENTS event, String... logs) {
	return event(inboxMessage, inboxMessage.session().getAgent(), event, logs);
    }

    private MessageDoc event(ChatSessionDoc sessionDoc, String auditAgent, EVENTS event, String... logs) {
	IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
	return event(inboxMessage, auditAgent, event, logs);
    }

    public MessageDoc event(ChatSessionDoc sessionDoc, EVENTS event, String... logs) {
	return event(sessionDoc, getCurrenUser(), event, logs);
    }

    public void error(InboxMessage inboxMessage, Exception e) {
	MessageDoc doc = new MessageDoc();
	doc.setSessionId(inboxMessage.getSessionId());
	doc.setMessageId(inboxMessage.getMessageId());
	doc.setMessageId(inboxMessage.getMessageId());
	doc.setMessageIdExt(inboxMessage.getMessageIdExt());
	doc.setMessageIdRef(inboxMessage.getMessageIdRef());
	doc.setContactId(PostManUtil.createContactId(inboxMessage));
	doc.setType("E");
	doc.setTimestamp(System.currentTimeMillis());
	doc.setTraceId(AppContextUtil.getTraceId());
	doc.setMessage(e.getMessage());

	StackTraceElement[] traces = e.getStackTrace();

	if (traces.length > 0 && traces[0].toString().length() > 0) {
	    for (StackTraceElement trace : traces) {
		doc.logs().add(trace.toString());
	    }
	}

	messageStore.save(doc, MessageStore.getCollectionName("LOGS"));
    }

}
