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

	private void replyIntenal(OutboxMessage outboxMessage) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
			outboxMessage.setContactType(inboxMessage.getContactType());
			outboxMessage.setQueue(inboxMessage.getQueue());
			outboxMessage.addTo(inboxMessage.getFrom());
			outboxMessage.setSessionId(inboxMessage.getSessionId());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.reply(inboxMessage, outboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.reply(inboxMessage, outboxMessage);
			}
		}
		messageStore.create(outboxMessage);
	}

	public void reply(OutboxMessage outboxMessage) throws InterruptedException {
		outboxMessage.option("isViaAgent", "true");
		replyIntenal(outboxMessage);
	}

	private void sendIntenal(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.is(chatContactDoc)) {
			outboxMessage.setContactType(ArgUtil.parseAsEnumT(chatContactDoc.getContactType(), ContactType.class));
			outboxMessage.setSessionId(outboxMessage.getSessionId());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.send(chatContactDoc, outboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.send(chatContactDoc, outboxMessage);
			}
		}
		messageStore.create(outboxMessage);
	}

	public void send(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		ChatContactDoc chatContactDoc = sessionStore.getContact(sessionDoc.getContactId());
		sendIntenal(chatContactDoc, outboxMessage);
	}

	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		sendIntenal(chatContactDoc, outboxMessage);
	}

	public boolean beforeMessageHandler() {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
		}
		return true;
	}

	public ChatContext loadChatContext(String contactId, InboxMessage inboxMessage) {
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

	public ChatContext getChatContext() {
		return chatContext;
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

		if (ArgUtil.is(connector)) {
			initd = connector.initSession(inboxMessage, session);
		}
		if (initd) {
			sessionStore.initSession(session);
		}
		return session.isInitd();
	}

}
