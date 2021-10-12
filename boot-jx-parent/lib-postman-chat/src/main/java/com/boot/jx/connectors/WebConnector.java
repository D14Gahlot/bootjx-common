package com.boot.jx.connectors;

import java.util.ArrayList;
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
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.WEBSITE)
public class WebConnector extends DefaultConnector {

    private static final String WEB_USER_MESSAGE_STR = "WEB_USER_MESSAGE_STR_";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConnector.class);

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

    public OutboxMessage process(OutboxMessage outboxMessage) {
	if (ArgUtil.is(outboxMessage.getTemplate())) {
	    QuickMedia mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
	    if (ArgUtil.is(mediaReply)) {
		if ("image".equalsIgnoreCase(mediaReply.getType())) {
		    outboxMessage.attachment(
			    new Attachment().mediaURL(mediaReply.getUrl()).mediaType(FileType.IMAGE.toString()));
		}
	    } else {
		tmplClient.process(outboxMessage);
	    }
	}
	return outboxMessage;
    }

    @Override
    public void send(OutboxMessage outboxMessage) {
	String to = CollectionUtil.getOne(outboxMessage.getTo());

	process(outboxMessage);
	if (redisson == null) {
	    try {
		messageQueue.enqueue(outboxMessage);
		outboxMessage.updateStatus(OutboxMessage.Status.SENT);
	    } catch (InterruptedException e) {
		outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
		outboxMessage.logs().add(e.getMessage());
		e.printStackTrace();
	    }
	} else {
	    LOGGER.debug("sendReply to " + to);
	    RBlockingQueue<String> messageQueue = redisson.getBlockingQueue(WEB_USER_MESSAGE_STR + to);
	    messageQueue.add(JsonUtil.toJson(outboxMessage));
	}
    }

    @Override
    public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	this.reply(inboxMessage, new OutboxMessage().message("Call us"));
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
	RBlockingQueue<String> messageQueue = redisson.getBlockingQueue(WEB_USER_MESSAGE_STR + number);
	String x = messageQueue.poll(5, TimeUnit.SECONDS);

	if (ArgUtil.is(x)) {
	    return JsonUtil.parse(x, OutboxMessage.class);
	}
	return null;
    }

    @Override
    public boolean initSession(ChatSessionDoc session, InboxMessage inboxMessage) {

	ChatContactQuery contactQuery = messageContext.getChatContactQuery();
	ChatContactDoc chatContactDoc = messageContext.getChatContactDoc();

	if (ArgUtil.is(inboxMessage.getForm())) {
	    if (ArgUtil.is(inboxMessage.getForm().get("name"))) {
		contactQuery.setName(ArgUtil.parseAsString(inboxMessage.getForm().get("name")));
	    }
	    if (ArgUtil.is(inboxMessage.getForm().get("email"))) {
		contactQuery.setEmail(ArgUtil.parseAsString(inboxMessage.getForm().get("email")));
	    }
	}

	List<TmplElement> inputs = new ArrayList<TmplElement>();
	if (ArgUtil.isEmpty(chatContactDoc.getName())) {
	    inputs.add(new TmplElement().name("name").label("Name").type("TEXT"));
	    reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage("Please fill below inputs to continue")
		    .option("inputs", inputs));
	    return false;
	}

	if (ArgUtil.isEmpty(chatContactDoc.getEmail())) {
	    inputs.add(new TmplElement().name("email").label("Email").type("EMAIL"));
	    reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage("Please fill below inputs to continue")
		    .option("inputs", inputs));
	    return false;
	}

	return true;
    }

}
