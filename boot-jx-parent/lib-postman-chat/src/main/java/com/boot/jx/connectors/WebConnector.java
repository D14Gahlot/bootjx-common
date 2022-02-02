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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.connectors.AbstractConnector.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.WebPlugin;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.WEBSITE)
public class WebConnector extends DefaultConnector<WebConfigDetails, WebPlugin> {

    private static final String WEB_USER_MESSAGE_STR = "WEB_USER_MESSAGE_STR_";
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConnector.class);

    @Value("${app.stomp}")
    boolean stompEnabled;

    @Override
    public WebPlugin getPlugin() {
	return ChannelPluginProvider.WEB;
    }

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
    public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	String contactId = outboxMessage.contact().getContactId();

	template(channelConfig, chatContactDoc, outboxMessage);

	if (redisson == null) {
	    try {
		messageQueue.enqueue(outboxMessage);
		outboxMessage.updateStatus(OutboxMessage.Status.SENT);
	    } catch (InterruptedException e) {
		outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
		outboxMessage.logs().add(e.getMessage());
		e.printStackTrace();
	    }
	} else if (stompEnabled) {
	    stompTunnelService.sendToTag(contactId, "/message/receive/new", outboxMessage);
	} else {
	    LOGGER.debug("sendReply to " + contactId);
	    RBlockingQueue<String> messageQueue = redisson.getBlockingQueue(WEB_USER_MESSAGE_STR + contactId);
	    messageQueue.add(JsonUtil.toJson(outboxMessage));
	}
    }

    @Override
    public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	this.reply(null, null, new OutboxMessage().message("Call us"), inboxMessage);
	return inboxMessage;
    }

    @Autowired(required = false)
    RedissonClient redisson;

    @Autowired
    private StompTunnelService stompTunnelService;

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
	    reply(null, null, (OutboxMessage) inboxMessage.replyMessage("Please fill below inputs to continue")
		    .option("inputs", inputs), inboxMessage);
	    return false;
	}

	if (ArgUtil.isEmpty(chatContactDoc.getEmail())) {
	    inputs.add(new TmplElement().name("email").label("Email").type("EMAIL"));
	    reply(null, null, (OutboxMessage) inboxMessage.replyMessage("Please fill below inputs to continue")
		    .option("inputs", inputs), inboxMessage);
	    return false;
	}

	return true;
    }

    public InboxMessage toInboxMessage(ChannelConfig channelConfig, MapModel map) {
	// Create Default Message from Channel
	InboxMessage inboxMessage = this.createInboxMessage(channelConfig, map.as(InboxMessage.class));
	inboxMessage.setSessionId(null);
	inboxMessage.setMessageId(null);

	inboxMessage.contact().setCsid(inboxMessage.getFrom());
	inboxMessage.session().setAgent(null);
	inboxMessage.session().setDept(null);

	return inboxMessage;
    }

    @Override
    public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
	    MessageBoxEvent messageBoxEvent) {
	return messageBoxEvent.addInboxMessage(toInboxMessage(channelConfig, requestMap));
    }

}
