package com.boot.jx.connectors;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(ContactType.EMPTY)
public class DummyConnector implements DefaultConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(DummyConnector.class);
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

	@Override
	public void sendReply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.DUMMY)) {
			if (redisson == null) {
				try {
					messageQueue.enqueue(outboxMessage);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				LOGGER.info("sendReply to " + inboxMessage.getFrom());
				RBlockingQueue<OutboxMessage> messageQueue = redisson
						.getBlockingQueue("DUMMY_USER" + "_" + inboxMessage.getFrom());
				messageQueue.add(outboxMessage);
			}

		}
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		this.sendReply(inboxMessage, new OutboxMessage().message("Call us @ " + gupShupConfig.getGupShupWaNumber()));
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
		RBlockingQueue<OutboxMessage> messageQueue = redisson.getBlockingQueue("DUMMY_USER" + "_" + number);
		return messageQueue.poll(5, TimeUnit.SECONDS);
	}

}
