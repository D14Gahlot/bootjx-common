package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class ChatSessionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSessionFactory.class);

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private PMDomainConfig pmDomainConfig;

    public ChatSessionDoc getChatSessionByContactId(String contactId) {

	if (!ArgUtil.is(contactId)) {
	    return null;
	}

	ChatContactDoc chatContactDoc = sessionStore.getContact(contactId);

	String sessionId = chatContactDoc.getSessionId();

	ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);

	if (sessionStore.isSessionValid(chatSessionDoc)) {
	    return chatSessionDoc;
	}

	return null;
    }

    public ChatSessionDoc getChatSession(String sessionId) {

	if (!ArgUtil.is(sessionId)) {
	    return null;
	}

	ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);

	if (sessionStore.isSessionValid(chatSessionDoc)) {
	    return chatSessionDoc;
	}

	if (!ArgUtil.is(chatSessionDoc)) {
	    return null;
	}

	return getChatSessionByContactId(chatSessionDoc.getContactId());
    }

    public ChatSessionDoc getChatSession(SessionMessage sessionMessage) {

	// SESSION FIND BY SESSION_ID
	ChatSessionDoc chatSessionDoc = getChatSession(sessionMessage.getSessionId());

	if (ArgUtil.is(chatSessionDoc)) {
	    return chatSessionDoc;
	}

	Contactable contact = PostManUtil.getContactMeta(sessionMessage.contact());

	if (!ArgUtil.is(contact.getContactId())) {
	    // CONTACT CONNANOT BE FOUND
	    return null;
	}

	// CONTACT FIND BY SESSION_ID
	ChatContactDoc chatContactDoc = sessionStore.getContact(contact.getContactId());

	// CONTACT CREATION
	if (ArgUtil.isEmpty(chatContactDoc)) {
	    // System.out.println("CONTACT CREATION");
	    ChatContactQuery chatContactQuery = new ChatContactQuery(contact.getContactId());
	    chatContactQuery.update(contact);
	    chatContactQuery.updateCreatedStamp();
	    // chatContactDoc = sessionStore.save(chatContactQuery.getDoc());
	    chatContactDoc = sessionStore.save(chatContactQuery);
	}

	// SESSION FUND BY CONTACT_ID
	chatSessionDoc = getChatSessionByContactId(contact.getContactId());

	if (ArgUtil.is(chatSessionDoc)) {
	    return chatSessionDoc;
	}

	sessionStore.closeAllPreviousSessions(contact.getContactId());

	// SESSION CREATION
	// System.out.println("SESSION CREATION");
	chatSessionDoc = new ChatSessionDoc();
	chatSessionDoc.setContactId(contact.getContactId());
	chatSessionDoc.setContactType(sessionMessage.contact().getContactType());
	chatSessionDoc.setChannel(sessionMessage.contact().getChannelType());
	chatSessionDoc.setLane(sessionMessage.contact().getLane());
	chatSessionDoc.setActive(true);
	chatSessionDoc.setPrimary(true);
	chatSessionDoc.contact().setName(chatContactDoc.getName());
	chatSessionDoc.contact().copyFrom(chatContactDoc);
	return sessionStore.saveSession(chatSessionDoc);
    }

    public ChatSessionDoc linkSession(ChatSessionDoc chatSessionDoc, IMessage inboxMessage) {
	if (!ArgUtil.is(chatSessionDoc)) {
	    return null;
	}

	if (PostManUtil.isInBound(inboxMessage)) {
	    chatSessionDoc.setLastInComingStamp(inboxMessage.getTimestamp());
	    // Query Update for Session
	    ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);
	    chatSessionDocQuery.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());

	    if (ArgUtil.isEmptyValue(chatSessionDoc.getFirstInComingStamp())) {
		chatSessionDocQuery.setFirstInComingStamp(inboxMessage.getTimestamp());
	    }

	    // Assign Queue
	    if (ArgUtil.isEmptyValue(chatSessionDoc.getAssignedToQueue())) {
		String defaultQueue = pmDomainConfig.getDefaultInboundQueue(inboxMessage.contact());
		if (ArgUtil.is(defaultQueue)) {
		    chatSessionDocQuery.setQueue(defaultQueue);
		}
	    }

	    sessionStore.updateFirst(chatSessionDocQuery);

	    // Query Update for Contact
	    ChatContactQuery chatContactQuery = new ChatContactQuery(chatSessionDoc.getContactId());
	    chatContactQuery.setLastInBoundStamp(inboxMessage.getTimestamp());
	    chatContactQuery.setSessionId(chatSessionDoc.getSessionId());
	    chatContactQuery.update(inboxMessage.contact());
	    sessionStore.updateFirst(chatContactQuery);
	} else if (PostManUtil.isOutBound(inboxMessage)) {
	    // Query Update for Contact
	    ChatContactQuery chatContactQuery = new ChatContactQuery(chatSessionDoc.getContactId());
	    chatContactQuery.setSessionId(chatSessionDoc.getSessionId());
	    sessionStore.updateFirst(chatContactQuery);
	}

	if (ArgUtil.isEmpty(inboxMessage.contact().getName())) {
	    inboxMessage.contact().setName(chatSessionDoc.contact().getName());
	}
	inboxMessage.contact().setContactId(chatSessionDoc.getContactId());
	inboxMessage.setSessionId(chatSessionDoc.getSessionId());
	inboxMessage.session().setQueue(chatSessionDoc.getAssignedToQueue());
	inboxMessage.session().setAgent(chatSessionDoc.getAssignedToAgent());
	inboxMessage.session().setDept(chatSessionDoc.getAssignedToDept());
	inboxMessage.session().setMode(chatSessionDoc.getMode());
	inboxMessage.session().setResolved(chatSessionDoc.isResolved());

	return chatSessionDoc;
    }

    public void push(MessageDoc msgDoc, IMessage iMessage) {
	if (PostManUtil.isInBound(msgDoc.getType()) || PostManUtil.isOutBound(msgDoc.getType())) {
	    try {
		ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(msgDoc.getSessionId());
		if (PostManUtil.isInBound(msgDoc.getType())) {
		    chatSessionDocQuery.setLastInBoundMsg(msgDoc, iMessage.contact().getContactType());
		} else if (PostManUtil.isOutBound(msgDoc.getType())) {
		    chatSessionDocQuery.setLastOutBoundMsg(msgDoc, iMessage.contact().getContactType());
		}
		chatSessionDocQuery.setLastMsg(msgDoc, iMessage.contact().getContactType());
		sessionStore.updateFirst(chatSessionDocQuery);
	    } catch (Exception e) {
		LOGGER.error("SessionStore.push", e);
	    }
	}
    }

}
