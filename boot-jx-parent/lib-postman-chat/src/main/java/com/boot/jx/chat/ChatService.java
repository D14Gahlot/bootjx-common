package com.boot.jx.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatContextDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class ChatService {

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

	private boolean actionIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		if (!ArgUtil.is(outboxMessage.getAction())) {
			return false;
		}
		outboxMessage.setStatus(Message.Status.INIT);
		messageStore.create(outboxMessage);
		if (ArgUtil.is(chatContactDoc)) {
			outboxMessage.setContactType(ArgUtil.parseAsEnumT(chatContactDoc.getContactType(), ContactType.class));
			outboxMessage.setContactId(chatContactDoc.getContactId());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.message("ACTION", chatContactDoc, null, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.message("ACTION", chatContactDoc, null, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			}
		}
		messageStore.update(outboxMessage);
		return true;
	}

	private void replyIntenal(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		outboxMessage.setStatus(Message.Status.INIT);
		messageStore.create(outboxMessage);
		if (ArgUtil.is(inboxMessage)) {
			outboxMessage.setContactType(inboxMessage.getContactType());
			outboxMessage.setChannel(inboxMessage.getChannel());
			outboxMessage.setQueue(inboxMessage.getQueue());
			outboxMessage.addTo(inboxMessage.getFrom());
			outboxMessage.setSessionId(inboxMessage.getSessionId());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.message("REPLY", null, inboxMessage, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.message("REPLY", null, inboxMessage, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			}
		}
		messageStore.update(outboxMessage);
	}

	private void sendIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		outboxMessage.setStatus(Message.Status.INIT);
		messageStore.create(outboxMessage);
		if (ArgUtil.is(chatContactDoc)) {
			outboxMessage.setContactType(ArgUtil.parseAsEnumT(chatContactDoc.getContactType(), ContactType.class));
			outboxMessage.setContactId(chatContactDoc.getContactId());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.message("SEND", chatContactDoc, null, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.message("SEND", chatContactDoc, null, outboxMessage);
				outboxMessage.setStatus(Message.Status.SENT);
			}
		}
		messageStore.update(outboxMessage);
	}

	public void reply(OutboxMessage outboxMessage) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		ChatContactDoc chatContactDoc = sessionStore.getContact(inboxMessage.getContactId());

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(chatClient.getDefaultSender());
		}
		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));
		replyIntenal(inboxMessage, outboxMessage);
	}

	public void reply(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		InboxMessage inboxMessage = sessionStore.toInboxMessage(sessionDoc);
		ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
		}
		outboxMessage.model().put("contact", ChatDTOUtil.getContactDTO(chatContactDoc));

		replyIntenal(inboxMessage, outboxMessage);
	}

	public void send(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());

		// Action Only
		if (actionIntenal(chatContactDoc, outboxMessage)) {
			return;
		}

		if (ArgUtil.isEmpty(outboxMessage.session().getAgent())) {
			outboxMessage.session().setAgent(sessionDoc.getAssignedToAgent());
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

			sessionDoc.setMode(inboxMessage.session().getMode());
			sessionDoc.setAssignedToAgent(inboxMessage.session().getAgent());
			sessionStore.save(sessionDoc);
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
			session.setContactName(contact.getName());
			session = sessionStore.initSession(session);
		}
		return session.isInitd();
	}

	public boolean resolveSession(ChatSessionDoc session) {
		if (!ArgUtil.isNone(session.getResolveSessionStamp())) {
			return true;
		}
		session = sessionStore.resolveSession(session);
		return true;
	}

	public boolean closeSession(ChatSessionDoc session) {
		if (!session.isActive()) {
			return true;
		}
		session = sessionStore.closeSession(session);
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
}
