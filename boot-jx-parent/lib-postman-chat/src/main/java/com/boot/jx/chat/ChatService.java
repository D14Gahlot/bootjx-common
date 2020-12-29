package com.boot.jx.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.doc.ChatContextDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class ChatService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatContext chatContext;

	@Autowired
	PostManClient postManClient;

	@Autowired
	private MessageStore messageStore;

	private MessageQueue messageQueue = new MessageQueue(100);

	public InboxMessage getInboxMessage() {
		return chatContext.getInboxMessage();
	}

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired(required = false)
	private DefaultConnector defaultConnector;

	private void sendIntenal(OutboxMessage outboxMessage) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
			outboxMessage.setContactType(inboxMessage.getContactType());
			outboxMessage.setQueue(inboxMessage.getQueue());
			outboxMessage.addTo(inboxMessage.getFrom());
			ConnectorHandler connector = connectorHandlerFactory.get(outboxMessage.getContactType(),
					outboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.sendReply(inboxMessage, outboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.sendReply(inboxMessage, outboxMessage);
			}
		}
		messageStore.create(outboxMessage);
	}

	public void reply(OutboxMessage outboxMessage) throws InterruptedException {
		outboxMessage.option("isViaAgent", "true");
		sendIntenal(outboxMessage);
	}

	public void send(OutboxMessage outboxMessage) throws InterruptedException {
		sendIntenal(outboxMessage);
	}

	public void assignToAgent(String deptName) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
			ConnectorHandler connector = connectorHandlerFactory.get(inboxMessage.getContactType(),
					inboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.assignToAgent(inboxMessage, deptName);
			} else if (ArgUtil.is(defaultConnector)) {
				chatContext.meta().setAgentEnabled(true);
				defaultConnector.assignToAgent(inboxMessage, deptName);
			}
		}
	}

	public boolean beforeMessageHandler() {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
		}
		return true;
	}

	public Message<?> lastMessage() throws InterruptedException {
		return messageQueue.dequeue();
	}

	public ChatContext loadChatContext(String contactId, InboxMessage inboxMessage) {
		ChatContextDoc doc = mongoTemplate.findById(contactId, ChatContextDoc.class);
		if (ArgUtil.is(doc)) {
			chatContext.getStore().loadUser(doc.getUser());
			if (ArgUtil.is(doc.getMeta()) && !TimeUtils.isExpired(doc.getMeta().getUpdateStamp(), "5min")) {
				chatContext.setMeta(doc.getMeta());
				chatContext.getStore().loadSession(doc.getSession());
				chatContext.getNextHandler();
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
		messageStore.create(inboxMessage);
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
	}

	public ChatContext getChatContext() {
		return chatContext;
	}

	public InboxMessage forward() {
		return postManClient.forward(getInboxMessage()).getResult();
	}
}
