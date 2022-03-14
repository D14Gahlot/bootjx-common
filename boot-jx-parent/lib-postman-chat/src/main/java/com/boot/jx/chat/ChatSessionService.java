package com.boot.jx.chat;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.inbound.InBound.SessionAssginHandler;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO.ChatUserProfileRequest;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMParams;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;

@Component
public class ChatSessionService {

    private static final Logger LOGGER = LoggerService.getLogger(ChatSessionService.class);

    @Autowired
    private PMDomainConfig pmDomainConfig;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageContext messageContext;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private ChatClient chatClient;

    @Autowired(required = false)
    private InBoundHandler inBoundHandler;

    public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
	boolean initd = session.isInitd();
	if (initd) {
	    return true;
	}
	ConnectorHandler connector = connectorHandlerFactory.get(inboxMessage.contact().type(),
		inboxMessage.contact().getChannelType());

	if (ArgUtil.is(connector)) {
	    OutboxMessage reply = connector.initSession(session, inboxMessage);
	    if (ArgUtil.is(reply)) {
		try {
		    if (!OutboxMessage.NO_MESSAGE.equals(reply))
			chatService.reply(inboxMessage, reply);
		    initd = false;
		} catch (InterruptedException e) {
		    LOGGER.error("Errror While Replying To Sesion Init Message", e);
		}
	    } else {
		initd = true;
	    }
	    messageContext.commitChatContactQuery();
	}

	if (initd) {
	    InBoundEvent sessionInitEvent = chatSessionManager.initSession(inboxMessage, session);
	    if (ArgUtil.is(inBoundHandler)) {
		inBoundHandler.onSessionInit(sessionInitEvent, session);
	    }
	    this.routeSession(session);
	}

	inboxMessage.session().setQueue(session.getAssignedToQueue());
	inboxMessage.session().setDept(session.getAssignedToDept());
	inboxMessage.session().setAgent(session.getAssignedToAgent());
	inboxMessage.session().setBot(session.getAssignedToBot());

	return session.isInitd();
    }

    public boolean initSession(OutboxMessage outboxMessage, ChatSessionDoc session) {
	boolean initd = session.isInitd();
	if (initd) {
	    return true;
	}
	ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.contact().type(),
		outboxMessage.contact().getChannelType());

	messageContext.setMessage(outboxMessage);
	ChatContactQuery contactQuery = messageContext.contact();
	if (ArgUtil.is(connector)) {
	    initd = connector.initSession(contactQuery, session, outboxMessage);
	    // TODO:-- Validate if saving is required in case of outbound
	    // sessionStore.save(contact);
	}
	if (initd) {
	    session = sessionStore.initSession(session);
	}
	return session.isInitd();
    }

    @Async
    public void initSessionPost(InboxMessage inboxMessage, ChatSessionDoc session) {
	ChatContactDoc contact = sessionStore.getContact(inboxMessage);
	try {
	    ChatUserProfileRequest chatUserProfileRequest = new ChatUserProfileRequest();
	    chatUserProfileRequest.setEmail(contact.getEmail());
	    chatUserProfileRequest.setMobile(contact.getPhone());
	    chatUserProfileRequest.setContactId(contact.getContactId());
	    chatUserProfileRequest.setContactType(contact.getContactType());
	    chatUserProfileRequest.setLane(contact.getLane());
	    chatUserProfileRequest.setProfileId(contact.getProfileId());
	    ChatUserProfileDTO profile = chatClient.fetchContactDetails(chatUserProfileRequest);

	    contact = sessionStore.getContact(inboxMessage);
	    if (ArgUtil.is(profile.getProfileId())) {
		sessionStore.save(profile);
		contact.setProfileId(profile.getProfileId());
	    } else {
		contact.setProfile(profile);
	    }
	    sessionStore.save(contact);
	} catch (Exception e) {

	}
    }

    public InBoundEvent routeSession(ChatSessionDoc sessionDoc, String queue, Object params) {
	InBoundEvent event = chatSessionManager.assignToQueue(sessionDoc, queue);
	event.sessionRouted.params = params;
	if (ArgUtil.is(inBoundHandler)) {
	    inBoundHandler.onSessionRouteAsync(event, sessionDoc);
	}
	return event;
    }

    public InBoundEvent routeSession(ChatSessionDoc session) {
	if (ArgUtil.isEmptyValue(session.getAssignedToQueue()) || ArgUtil.isEmptyValue(session.getMode())) {
	    String defaultQueue = pmDomainConfig.getDefaultInboundQueue(session.contact());
	    return routeSession(session, defaultQueue, null);
	}
	return null;
    }

    public InBoundEvent routeSession(String sessionId, String queue, Object params) {
	ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
	return routeSession(sessionDoc, queue, params);
    }

    public NodeEntry<InBoundEvent> updateSessionStatus(ChatSessionDoc sessionDoc, CHAT_STATUS status) {
	NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();

	if (!ArgUtil.is(status)) {
	    return eventEntry;
	}

	String oldStatus = sessionDoc.getStatus();
	if (status.toString().equalsIgnoreCase(oldStatus)) {
	    return eventEntry;
	}
	if (status == PMConstants.CHAT_STATUS.RESOLVED) {
	    NodeEntry<InBoundEvent> eventEntry2 = chatSessionManager.resolveSession(sessionDoc);
	    if (ArgUtil.is(inBoundHandler)) {
		inBoundHandler.onSessionResolveAsync(eventEntry2.getValue(), sessionDoc);
	    }
	    return eventEntry2;
	} else if (status == PMConstants.CHAT_STATUS.CLOSED) {
	    InBoundEvent event = chatSessionManager.closeSession(sessionDoc);
	    if (ArgUtil.is(inBoundHandler)) {
		inBoundHandler.onSessionCloseAsync(event, sessionDoc);
	    }
	    return eventEntry.value(event);
	} else {
	    InBoundEvent event = chatSessionManager.updateStatus(sessionDoc, status);
	    return eventEntry.value(event);
	}
    }

    public NodeEntry<InBoundEvent> closeSession(ChatSessionDoc chatSessionDoc) {
	if (!chatSessionDoc.isResolved()) {
	    updateSessionStatus(chatSessionDoc, PMConstants.CHAT_STATUS.RESOLVED);
	}
	return updateSessionStatus(chatSessionDoc, PMConstants.CHAT_STATUS.CLOSED);
    }

    public NodeEntry<InBoundEvent> closeSession(String sessionId) {
	ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
	return closeSession(sessionDoc);
    }

    public NodeEntry<InBoundEvent> assignSessionToAgent(PMParams params) {
	return inBoundHandler.assignSessionToAgent(params);
    }

    public NodeEntry<InBoundEvent> assignSessionToAgent(ChatSessionDoc sessionDoc, String agentDept, String agentCode) {
	return inBoundHandler.assignSessionToAgent(sessionDoc, agentDept, agentCode);
    }

}
