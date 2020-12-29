package com.boot.jx.connectors;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.MessageQueue;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(ContactType.EMPTY)
public class DummyConnector implements DefaultConnector {

	@Autowired
	protected GupShupConfig gupShupConfig;

	private MessageQueue messageQueue = new MessageQueue(100);

	@Override
	public void sendReply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.DUMMY)) {
			if (redisson == null) {
				throw new PostManException("No Redisson Avaialble");
			}
			RBlockingQueue<OutboxMessage> messageQueue = redisson
					.getBlockingQueue("DUMMY_USER" + "_" + inboxMessage.getFrom());
			messageQueue.add(outboxMessage);
		}
	}

	@Override
	public void assignToAgent(InboxMessage inboxMessage, String deptName) {
		if (redisson == null) {
			throw new PostManException("No Redisson Avaialble");
		}
		RBlockingQueue<OutboxMessage> messageQueue = redisson
				.getBlockingQueue("DUMMY_USER" + "_" + inboxMessage.getFrom());
		messageQueue.add(new OutboxMessage().message("Call us @ " + gupShupConfig.getGupShupWaNumber()));
	}

	@Autowired(required = false)
	RedissonClient redisson;

	public OutboxMessage pollUnreadMessage(String number) throws InterruptedException {
		if (redisson == null) {
			throw new PostManException("No Redisson Avaialble");
		}
		RBlockingQueue<OutboxMessage> messageQueue = redisson.getBlockingQueue("DUMMY_USER" + "_" + number);
		return messageQueue.poll(5, TimeUnit.SECONDS);
	}

}
