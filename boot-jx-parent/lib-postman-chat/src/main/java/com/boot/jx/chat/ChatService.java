package com.boot.jx.chat;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatContextDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO.ChatUserProfileRequest;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_MODE;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class ChatService {

    public static Logger LOGGER = LoggerService.getLogger(ChatService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatContext chatContext;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private MessageContext messageContext;

    @Autowired
    private SessionStore sessionStore;

    @Autowired(required = false)
    private AuditDetailProvider auditDetailProvider;

    public InboxMessage getInboxMessage() {
	return chatContext.getInboxMessage();
    }

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

    public ChatContext getChatContext() {
	return chatContext;
    }

    public ChatClient getClient() {
	return chatClient;
    }

    public PMClientConfig getClientConfig() {
	return chatClientConfig;
    }

    public String getCurrenUser() {
	return ArgUtil.is(auditDetailProvider) ? auditDetailProvider.getAuditUser() : "_SYSTEM_";
    }

    private MessageDoc actionIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	if (!ArgUtil.is(outboxMessage.getAction())) {
	    return null;
	}

	if (!ArgUtil.is(chatContactDoc)) {
	    throw new PostManException("Destination Not Specified : chatContactDoc Empty");
	}

	outboxMessage.updateStatus(Message.Status.INIT);
	outboxMessage.contact().setContactType(chatContactDoc.getContactType());
	outboxMessage.contact().setContactId(chatContactDoc.getContactId());

	MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);
	connectorHandlerFactory.message("ACTION", chatContactDoc, null, outboxMessage);

	return messageDoc;
    }

    private MessageDoc replyIntenal(IMessageExtended inboxMessage, OutboxMessage outboxMessage) {

	if (!ArgUtil.is(inboxMessage)) {
	    throw new PostManException("Destination Not Specified : inboxMessage Empty");
	}

	outboxMessage.updateStatus(Message.Status.INIT);
	outboxMessage.contact().setContactType(inboxMessage.contact().getContactType());
	outboxMessage.contact().setChannel(inboxMessage.contact().getChannel());
	outboxMessage.contact().setLane(inboxMessage.contact().getLane());
	outboxMessage.contact().setCsid(inboxMessage.contact().getCsid());
	outboxMessage.contact().setContactId(inboxMessage.contact().getContactId());
	outboxMessage.setQueue(inboxMessage.getQueue());
	outboxMessage.addTo(inboxMessage.getFrom());
	outboxMessage.setSessionId(inboxMessage.getSessionId());

	if (!ArgUtil.is(outboxMessage.session().getMode())) {
	    outboxMessage.session().setMode(inboxMessage.session().getMode());
	}

	MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);
	connectorHandlerFactory.message("REPLY", null, inboxMessage, outboxMessage);
	return messageDoc;
    }

    private MessageDoc sendIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	LOGGER.debug("sendIntenal(ChatContactDoc {}, OutboxMessage {})", chatContactDoc, outboxMessage);

	if (!ArgUtil.is(chatContactDoc)) {
	    throw new PostManException("Destination Not Specified : chatContactDoc Empty");
	}

	outboxMessage.updateStatus(Message.Status.INIT);
	outboxMessage.contact().setContactType(chatContactDoc.getContactType());
	outboxMessage.contact().setChannel(chatContactDoc.getChannel());
	outboxMessage.contact().setLane(chatContactDoc.getLane());
	outboxMessage.contact().setCsid(chatContactDoc.getCsid());
	outboxMessage.contact().setContactId(chatContactDoc.getContactId());
	outboxMessage.setSessionId(chatContactDoc.getSessionId());

	MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);
	connectorHandlerFactory.message("SEND", chatContactDoc, null, outboxMessage);
	return messageDoc;
    }

    public MessageDoc reply(OutboxMessage outboxMessage) throws InterruptedException {
	InboxMessage inboxMessage = chatContext.getInboxMessage();
	ChatContactDoc chatContactDoc = sessionStore.getContact(inboxMessage.contact().getContactId());

	if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
	    outboxMessage.session().setAgent(chatClientConfig.getDefaultSender());
	}

	// Action Only
	MessageDoc actionDco = actionIntenal(chatContactDoc, outboxMessage);
	if (ArgUtil.is(actionDco)) {
	    return actionDco;
	}

	outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
	return replyIntenal(inboxMessage, outboxMessage);
    }

    public MessageDoc reply(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
	IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
	ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

	if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
	    outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
	}

	if (ArgUtil.isEmpty(outboxMessage.session().getDept())) {
	    outboxMessage.session().setAgent(sessionDoc.getAssignedToDept());
	}

	// Action Only
	MessageDoc actionDto = actionIntenal(chatContactDoc, outboxMessage);
	if (ArgUtil.is(actionDto)) {
	    return actionDto;
	}

	outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));

	return replyIntenal(inboxMessage, outboxMessage);
    }

    public MessageDoc note(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
	outboxMessage.contact().setContactType(sessionDoc.getContactType());
	outboxMessage.contact().setChannel(sessionDoc.getChannel());
	outboxMessage.contact().setLane(sessionDoc.getLane());
	outboxMessage.contact().setContactId(sessionDoc.getContactId());
	outboxMessage.setSessionId(sessionDoc.getSessionId());
	outboxMessage.setType("N");
	return messageStore.note(outboxMessage, getCurrenUser());
    }

    public void log(IMessageExtended inboxMessage, String agent, EVENTS event, String... logs) {
	messageStore.log(inboxMessage, agent, event, logs);
    }

    public void log(IMessageExtended inboxMessage, EVENTS event, String... logs) {
	log(inboxMessage, inboxMessage.session().getAgent(), event, logs);
    }

    public void log(ChatSessionDoc sessionDoc, String agent, EVENTS event, String... logs) {
	IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
	log(inboxMessage, agent, event, logs);
    }

    public void log(ChatSessionDoc sessionDoc, EVENTS event, String... logs) {
	log(sessionDoc, getCurrenUser(), event, logs);
    }

    public MessageDoc send(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
	LOGGER.debug("send(ChatSessionDoc {}, OutboxMessage {})", sessionDoc, outboxMessage);
	ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

	if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
	    outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
	}

	if (ArgUtil.isEmpty(outboxMessage.session().getDept())) {
	    outboxMessage.session().setAgent(sessionDoc.getAssignedToDept());
	}

	// Action Only
	// Action Only
	MessageDoc actionDto = actionIntenal(chatContactDoc, outboxMessage);
	if (ArgUtil.is(actionDto)) {
	    return actionDto;
	}

	outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
	return sendIntenal(chatContactDoc, outboxMessage);
    }

    public MessageDoc send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
	    outboxMessage.session().setAgent(chatClientConfig.getDefaultSender());
	}

	// Action Only
	// Action Only
	MessageDoc actionDto = actionIntenal(chatContactDoc, outboxMessage);
	if (ArgUtil.is(actionDto)) {
	    return actionDto;
	}

	outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
	return sendIntenal(chatContactDoc, outboxMessage);
    }

    public boolean beforeMessageHandler() {
	InboxMessage inboxMessage = chatContext.getInboxMessage();
	if (ArgUtil.is(inboxMessage)) {
	}
	return true;
    }

    public ChatContext loadChatContext(String contactId, InboxMessage inboxMessage) {

	if (!ArgUtil.is(inboxMessage.session().getMode())) {
	    ChatSessionDoc sessionDoc = sessionStore.getSession(inboxMessage.getSessionId());

	    if (!ArgUtil.is(sessionDoc)) {
		LOGGER.error("No Session Found for {}/{}", contactId, inboxMessage.getSessionId());
	    }

	    inboxMessage.session().setMode(CHAT_MODE.BOT.toString());
	    inboxMessage.session().setAgent(chatClientConfig.getDefaultSender());

	    sessionStore.assignToBot(sessionDoc, chatClientConfig.getDefaultSender());
	}

	ChatContextDoc doc = mongoTemplate.findById(contactId, ChatContextDoc.class);
	if (ArgUtil.is(doc)) {
	    chatContext.getStore().loadUser(doc.getUser());
	    if (ArgUtil.is(doc.getMeta()) && !TimeUtils.isExpired(doc.getMeta().getUpdateStamp(), "5min")) {
		chatContext.setMeta(doc.getMeta());
		chatContext.getStore().loadSession(doc.getSession());
	    } else {
		chatContext.getStore().loadSession(null);
		chatContext.setMeta(new ChatMeta());
	    }
	} else {
	    chatContext.getStore().loadUser(null);
	    chatContext.getStore().loadSession(null);
	    chatContext.setMeta(new ChatMeta());
	}
	chatContext.setInboxMessage(inboxMessage);
	// messageStore.create(inboxMessage);
	return chatContext;
    }

    public void commitChatContext(String contactId, String prevHandler, InboxMessage inboxMessage) {
	ChatContextDoc doc = new ChatContextDoc();
	doc.setContactId(contactId);
	chatContext.meta().setPrevHandler(prevHandler);
	chatContext.meta().setUpdateStamp(System.currentTimeMillis());

	doc.setUser(chatContext.getStore().getUser());
	doc.setSession(chatContext.getStore().getSession());
	doc.setMeta(chatContext.getMeta());

	if (ArgUtil.is(prevHandler)) {
	    messageStore.setHandler(inboxMessage, prevHandler);
	}
	mongoTemplate.save(doc);
	chatContext.commitContact();
    }

    public InboxMessage forward() {
	return chatClient.forward(getInboxMessage()).getResult();
    }

    public InboxMessage forward(InboxMessage inboxMessage) {
	return chatClient.forward(inboxMessage).getResult();
    }

    public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
	boolean initd = session.isInitd();
	if (initd) {
	    return true;
	}
	ConnectorHandler connector = connectorHandlerFactory.get(inboxMessage.contact().type(),
		inboxMessage.contact().getChannel());

	if (ArgUtil.is(connector)) {
	    initd = connector.initSession(session, inboxMessage);
	    messageContext.commitChatContactQuery();
	}
	if (initd) {
	    session = sessionStore.initSession(session);
	}
	return session.isInitd();
    }

    public boolean initSession(OutboxMessage outboxMessage, ChatSessionDoc session) {
	boolean initd = session.isInitd();
	if (initd) {
	    return true;
	}
	ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.contact().type(),
		outboxMessage.contact().getChannel());

	messageContext.setMessage(outboxMessage);
	ChatContactQuery contactQuery = messageContext.getChatContactQuery();
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

    public boolean resolveSession(ChatSessionDoc session) {
	if (!ArgUtil.isEmptyValue(session.getResolveSessionStamp())) {
	    return false;
	}
	session = sessionStore.resolveSession(session);
	log(session, EVENTS.STATUS_CHANGED, session.getStatus(), CHAT_STATUS.RESOLVED.toString());
	return true;
    }

    public boolean closeSession(ChatSessionDoc session) {
	if (!session.isActive()) {
	    return false;
	}
	session = sessionStore.closeSession(session);
	log(session, EVENTS.STATUS_CHANGED, session.getStatus(), CHAT_STATUS.CLOSED.toString());
	return true;
    }

    public boolean updateSessionStatus(ChatSessionDoc sessionDoc, CHAT_STATUS status) {
	String oldStatus = sessionDoc.getStatus();
	if (status.toString().equalsIgnoreCase(oldStatus)) {
	    return false;
	}
	if (status == CHAT_STATUS.RESOLVED) {
	    return this.resolveSession(sessionDoc);
	} else if (status == CHAT_STATUS.CLOSED) {
	    return this.closeSession(sessionDoc);
	} else {
	    sessionStore.changeStatus(sessionDoc, status);
	    log(sessionDoc, EVENTS.STATUS_CHANGED, oldStatus, status.toString());
	}
	return true;
    }

    public boolean botScore(ChatSessionDoc session, Integer botScore) {
	session = sessionStore.botScore(session, botScore);
	return true;
    }

    public boolean agentScore(ChatSessionDoc session, Integer botScore) {
	session = sessionStore.agentScore(session, botScore);
	return true;
    }

    @Autowired
    private ChatStatusReportService chatStatusReportService;

    public void updateMessageStatus(List<MessageReport> updateDeliveryStatus) {
	chatStatusReportService.offer(updateDeliveryStatus);
	chatStatusReportService.process(null);
    }
}
