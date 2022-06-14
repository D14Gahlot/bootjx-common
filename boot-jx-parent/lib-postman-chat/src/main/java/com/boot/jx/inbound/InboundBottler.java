package com.boot.jx.inbound;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.cache.CacheBox;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.def.ICacheBox;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.doc.MessageHold;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class InboundBottler extends ATaskLimiter {

	private CacheBox<String> holdManager;

	@Autowired(required = false)
	private RedissonClient redisson;

	public ICacheBox<String> hold() {
		if (holdManager == null) {
			this.holdManager = CacheBox.getInstance("BotEngine-Hold-v2", redisson);
		}
		return this.holdManager;
	}

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private MessageStore messageStore;

	public void push(InboxMessage inboxMessage) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessage.contact());
		String onhold = hold().get(contactId);

		if (ArgUtil.isEqual(onhold, "QUEUING")) {
			queue(contactId, new MessageHold().inboxMessage(inboxMessage));
			throttle(new TunnelTask().name("MESSAGE_DEQUEUE").id(contactId).intervalSeconds(1));
		} else {
			hold().put(contactId, "QUEUING");
			inBoundService.invokeMethods(inboxMessage);
			hold().put(contactId, "DEQUEUING");
		}
		onhold = hold().get(contactId);
		if (!ArgUtil.isEqual(onhold, "QUEUING")) {
			this.dequeue(contactId);
		}

	}

	public InBoundEvent sessionEvent(InBoundEvent event, PMArgs pmArgs) {
		String contactId = PostManUtil.CONTACT_ID(event.contact());
		String onhold = hold().get(contactId);

		if (ArgUtil.isEqual(onhold, "QUEUING")) {
			queue(contactId, new MessageHold().event(event).pmArgs(pmArgs));
			throttle(new TunnelTask().name("MESSAGE_DEQUEUE").id(contactId).intervalSeconds(1));
		} else {
			hold().put(contactId, "QUEUING");
			chatSessionService.sessionEvent(event, pmArgs);
			hold().put(contactId, "DEQUEUING");
		}
		onhold = hold().get(contactId);
		if (!ArgUtil.isEqual(onhold, "QUEUING")) {
			this.dequeue(contactId);
		}

		return event;
	}

	private void dequeue(String contactId) {
		CommonMongoQueryBuilder builder2 = new CommonMongoQueryBuilder();
		builder2.with(Criteria.where("contactId").is(contactId).and("appType").is(appConfig.getAppType()))
				.sortBy("timestamp", Direction.ASC).limit(1);
		MessageHold docs = CollectionUtil.first(
				messageStore.findAllAndRemove(builder2.getQuery(), MessageHold.class, MessageHold.COLLECTION_QUEUED));
		if (ArgUtil.is(docs)) {
			if (ArgUtil.is(docs.getInboxMessage())) {
				inBoundService.invokeMethods(docs.getInboxMessage());
			} else if (ArgUtil.is(docs.getEvent())) {
				chatSessionService.sessionEvent(docs.getEvent(), docs.getPmArgs());
			}
		}

	}

	private void queue(String contactId, MessageHold hold) {
		hold.setContactId(contactId);
		hold.setTimestamp(System.currentTimeMillis());
		hold.setAppType(appConfig.getAppType());
		messageStore.save(hold, MessageHold.COLLECTION_QUEUED);
	}

	@Override
	public void doTask(TunnelTask task) {
		if ("MESSAGE_DEQUEUE".equals(task.getName())) {
			String contactId = task.getId();
			hold().put(contactId, "DEQUEUING");
			this.dequeue(contactId);
		}
	}

}
