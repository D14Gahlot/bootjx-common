package com.boot.jx.chat;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatContextDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO.ChatUserProfileRequest;
import com.boot.jx.postman.manager.LogManager;
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

    @Autowired
    private LogManager logManager;

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
	connectorHandlerFactory.message("ACTION", chatContactDoc, outboxMessage, null);
	sessionStore.push(messageDoc, outboxMessage);
	return messageDoc;
    }

    private MessageDoc replyIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage,
	    IMessageExtended inboxMessage) {
	LOGGER.debug("replyIntenal(ChatContactDoc {}, OutboxMessage {})", inboxMessage, outboxMessage);

	if (!ArgUtil.is(inboxMessage)) {
	    throw new PostManException("Destination Not Specified : inboxMessage Empty");
	}

	outboxMessage.updateStatus(Message.Status.INIT);
	outboxMessage.contact().setContactType(inboxMessage.contact().getContactType());
	outboxMessage.contact().setChannelType(inboxMessage.contact().getChannelType());
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
	connectorHandlerFactory.message("REPLY", chatContactDoc, outboxMessage, inboxMessage);
	sessionStore.push(messageDoc, outboxMessage);
	return messageDoc;
    }

    private MessageDoc sendIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	LOGGER.debug("sendIntenal(ChatContactDoc {}, OutboxMessage {})", chatContactDoc, outboxMessage);

	if (!ArgUtil.is(chatContactDoc)) {
	    throw new PostManException("Destination Not Specified : chatContactDoc Empty");
	}

	outboxMessage.updateStatus(Message.Status.INIT);
	outboxMessage.contact().setContactType(chatContactDoc.getContactType());
	outboxMessage.contact().setChannelType(chatContactDoc.getChannelType());
	outboxMessage.contact().setLane(chatContactDoc.getLane());
	outboxMessage.contact().setCsid(chatContactDoc.getCsid());
	outboxMessage.contact().setContactId(chatContactDoc.getContactId());
	outboxMessage.setSessionId(chatContactDoc.getSessionId());

	MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);
	connectorHandlerFactory.message("SEND", chatContactDoc, outboxMessage, null);
	sessionStore.push(messageDoc, outboxMessage);
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
	return replyIntenal(chatContactDoc, outboxMessage, inboxMessage);
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

	return replyIntenal(chatContactDoc, outboxMessage, inboxMessage);
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

	    inboxMessage.session().setMode(PMConstants.CHAT_MODE.BOT.toString());
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
		inboxMessage.contact().getChannelType());

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
		outboxMessage.contact().getChannelType());

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

    public boolean updateTagCategoryStatus(ChatSessionDoc sessionDoc, String tagCategory) {
	String oldTagCategory = null;
	if (ArgUtil.is(sessionDoc.getTagCategory())) {
	    oldTagCategory = sessionDoc.getTagCategory();
	}
	sessionStore.updateQuickTag(sessionDoc, tagCategory);
	logManager.log(sessionDoc, EVENTS.TAG_ADDED, oldTagCategory, tagCategory);
	return true;
    }

}
