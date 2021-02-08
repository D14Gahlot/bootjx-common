package com.boot.jx.connectors;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(ContactType.WEBSITE)
public class WebConnector implements DefaultConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebConnector.class);
	@Autowired
	protected GupShupConfig gupShupConfig;

	public static class MessageQueue<T> {

		private List<T> queue = new LinkedList<T>();
		private int limit = 10;

		public MessageQueue(int limit) {
			this.limit = limit;
		}

		public synchronized void enqueue(T item) throws InterruptedException {
			while (this.queue.size() == this.limit) {
				wait();
			}
			this.queue.add(item);
			if (this.queue.size() == 1) {
				notifyAll();
			}
		}

		public synchronized T dequeue() throws InterruptedException {
			while (this.queue.size() == 0) {
				wait();
			}
			if (this.queue.size() == this.limit) {
				notifyAll();
			}

			return this.queue.remove(0);
		}
	}

	private MessageQueue<OutboxMessage> messageQueue = new MessageQueue<OutboxMessage>(100);

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void send(String lane, String csid, OutboxMessage outboxMessage) {
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if (ArgUtil.is(mediaReply)) {
				if ("image".equalsIgnoreCase(mediaReply.getType())) {
					outboxMessage.addFile(new File().url(mediaReply.getUrl()).fileType(File.FileType.IMAGE));
				}
			} else {
				tmplClient.process(outboxMessage);
			}
		}

		if (redisson == null) {
			try {
				messageQueue.enqueue(outboxMessage);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			LOGGER.debug("sendReply to " + csid);
			RBlockingQueue<OutboxMessage> messageQueue = redisson.getBlockingQueue("WEB_USER_MESSAGE" + "_" + csid);
			messageQueue.add(outboxMessage);
		}
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		send(inboxMessage.getLane(), inboxMessage.getFrom(), outboxMessage);
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		send(chatContactDoc.getLane(), chatContactDoc.getCsid(), outboxMessage);
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		this.reply(inboxMessage, new OutboxMessage().message("Call us @ " + gupShupConfig.getGupShupWaNumber()));
		return inboxMessage;
	}

	@Autowired(required = false)
	RedissonClient redisson;

	public OutboxMessage pollUnreadMessage(String number) throws InterruptedException {
		if (redisson == null) {
			try {
				return messageQueue.dequeue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		RBlockingQueue<OutboxMessage> messageQueue = redisson.getBlockingQueue("WEB_USER_MESSAGE" + "_" + number);
		return messageQueue.poll(5, TimeUnit.SECONDS);
	}

	@Autowired
	private SessionStore sessionStore;

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		ChatContactDoc contact = sessionStore.getContact(inboxMessage);

		if (ArgUtil.is(inboxMessage.getForm())) {
			if (ArgUtil.is(inboxMessage.getForm().get("name"))) {
				contact.setName(ArgUtil.parseAsString(inboxMessage.getForm().get("name")));
			}
			if (ArgUtil.is(inboxMessage.getForm().get("email"))) {
				contact.setEmail(ArgUtil.parseAsString(inboxMessage.getForm().get("email")));
			}
			sessionStore.save(contact);
		}

		if (ArgUtil.isEmpty(contact.getName())) {
			reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage(null).template("pm-user-login-form"));
			return false;
		}

		if (ArgUtil.isEmpty(contact.getEmail())) {
			reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage(null).template("pm-user-login-form"));
			return false;
		}

		return true;
	}

}
