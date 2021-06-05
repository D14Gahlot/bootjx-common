package com.boot.jx.chat;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.logger.LoggerService;
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
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
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
	private MessageStore messageStore;

	@Autowired
	private SessionStore sessionStore;

	@Autowired(required = false)
	private AuditDetailProvider auditDetailProvider;

	public InboxMessage getInboxMessage() {
		return chatContext.getInboxMessage();
	}

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired(required = false)
	private DefaultConnector defaultConnector;

	public ChatContext getChatContext() {
		return chatContext;
	}

	public ChatClient getClient() {
		return chatClient;
	}

	public String getCurrenUser() {
		return ArgUtil.is(auditDetailProvider) ? auditDetailProvider.getAuditUser() : "_SYSTEM_";
	}

	private void message(String messageType, ChatContactDoc chatContactDoc, InboxMessage inboxMessage,
			OutboxMessage outboxMessage) {
		try {
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.message(messageType, chatContactDoc, inboxMessage, outboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.message(messageType, chatContactDoc, inboxMessage, outboxMessage);
			}
		} catch (Exception e) {
			LOGGER.error(messageType, e);
		}
		messageStore.createOrUpdate(outboxMessage);
	}

	private boolean actionIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		if (!ArgUtil.is(outboxMessage.getAction())) {
			return false;
		}

		if (!ArgUtil.is(chatContactDoc)) {
			throw new PostManException("Destination Not Specified : chatContactDoc Empty");
		}

		outboxMessage.updateStatus(Message.Status.INIT);
		outboxMessage.setContactType(ArgUtil.parseAsEnumT(chatContactDoc.getContactType(), ContactType.class));
		outboxMessage.setContactId(chatContactDoc.getContactId());

		messageStore.createOrUpdate(outboxMessage);
		connectorHandlerFactory.message("ACTION", chatContactDoc, null, outboxMessage);
		return true;
	}

	private void replyIntenal(InboxMessage inboxMessage, OutboxMessage outboxMessage) {

		if (!ArgUtil.is(inboxMessage)) {
			throw new PostManException("Destination Not Specified : inboxMessage Empty");
		}

		outboxMessage.updateStatus(Message.Status.INIT);
		outboxMessage.setContactType(inboxMessage.getContactType());
		outboxMessage.setChannel(inboxMessage.getChannel());
		outboxMessage.setLane(inboxMessage.getLane());
		outboxMessage.setQueue(inboxMessage.getQueue());
		outboxMessage.addTo(inboxMessage.getFrom());
		outboxMessage.setContactId(inboxMessage.getContactId());
		outboxMessage.setSessionId(inboxMessage.getSessionId());

		messageStore.createOrUpdate(outboxMessage);
		connectorHandlerFactory.message("REPLY", null, inboxMessage, outboxMessage);
	}

	private void sendIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {

		if (!ArgUtil.is(chatContactDoc)) {
			throw new PostManException("Destination Not Specified : chatContactDoc Empty");
		}

		outboxMessage.updateStatus(Message.Status.INIT);
		outboxMessage.setContactType(ArgUtil.parseAsEnumT(chatContactDoc.getContactType(), ContactType.class));
		outboxMessage.setChannel(chatContactDoc.getChannelType());
		outboxMessage.setLane(chatContactDoc.getLane());
		outboxMessage.setContactId(chatContactDoc.getContactId());
		outboxMessage.setSessionId(chatContactDoc.getSessionId());

		messageStore.createOrUpdate(outboxMessage);

		connectorHandlerFactory.message("SEND", chatContactDoc, null, outboxMessage);
	}

	public void reply(OutboxMessage outboxMessage) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		ChatContactDoc chatContactDoc = sessionStore.getContact(inboxMessage.getContactId());

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(chatClient.getDefaultSender());
		}

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
		replyIntenal(inboxMessage, outboxMessage);
	}

	public void reply(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		InboxMessage inboxMessage = sessionStore.toInboxMessage(sessionDoc);
		ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
		}

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));

		replyIntenal(inboxMessage, outboxMessage);
	}

	public MessageDoc note(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		outboxMessage.setContactType(ArgUtil.parseAsEnumT(sessionDoc.getContactType(), ContactType.class));
		outboxMessage.setChannel(sessionDoc.getChannel());
		outboxMessage.setLane(sessionDoc.getLane());
		outboxMessage.setContactId(sessionDoc.getContactId());
		outboxMessage.setSessionId(sessionDoc.getSessionId());
		outboxMessage.setType("N");
		return messageStore.note(outboxMessage, getCurrenUser());
	}

	public void log(InboxMessage inboxMessage, String agent, EVENTS event, String... logs) {
		messageStore.log(inboxMessage, agent, event, logs);
	}

	public void log(InboxMessage inboxMessage, EVENTS event, String... logs) {
		log(inboxMessage, inboxMessage.session().getAgent(), event, logs);
	}

	public void log(ChatSessionDoc sessionDoc, String agent, EVENTS event, String... logs) {
		InboxMessage inboxMessage = sessionStore.toInboxMessage(sessionDoc);
		log(inboxMessage, getCurrenUser(), event, logs);
	}

	public void log(ChatSessionDoc sessionDoc, EVENTS event, String... logs) {
		log(sessionDoc, getCurrenUser(), event, logs);
	}

	public void send(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
		}

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
		sendIntenal(chatContactDoc, outboxMessage);
	}

	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(chatClient.getDefaultSender());
		}

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
		sendIntenal(chatContactDoc, outboxMessage);
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
			inboxMessage.session().setMode("BOT");
			inboxMessage.session().setAgent(chatClient.getDefaultSender());

			sessionStore.assignToBot(sessionDoc, chatClient.getDefaultSender());
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
		ConnectorHandler connector = connectorHandlerFactory.get(inboxMessage.getContactType(),
				inboxMessage.getChannel());

		ChatContactDoc contact = sessionStore.getContact(inboxMessage);
		if (ArgUtil.is(connector)) {
			initd = connector.initSession(contact, session, inboxMessage);
			sessionStore.save(contact);
		}
		if (initd) {
			session = sessionStore.initSession(session, contact);
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
		if (!ArgUtil.isNone(session.getResolveSessionStamp())) {
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
