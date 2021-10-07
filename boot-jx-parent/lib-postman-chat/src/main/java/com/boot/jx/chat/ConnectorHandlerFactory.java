package com.boot.jx.chat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.common.ScopedBeanFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class ConnectorHandlerFactory extends ScopedBeanFactory<String, ConnectorHandler> {

    private static final long serialVersionUID = 4007091611441725719L;

    public static Logger LOGGER = LoggerService.getLogger(ConnectorHandlerFactory.class);

    public interface ConnectorHandler {
	default public void reply(IMessageExtended inboxMessage, OutboxMessage outboxMessage) {
	    outboxMessage.addTo(inboxMessage.getFrom());
	    outboxMessage.contact().setLane(inboxMessage.contact().getLane());
	    this.send(outboxMessage);
	}

	default public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	    outboxMessage.addTo(chatContactDoc.getCsid());
	    outboxMessage.contact().setLane(chatContactDoc.getLane());
	    this.send(outboxMessage);
	}

	default public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	    return inboxMessage;
	}

	default public boolean initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
	    return true;
	}

	default public boolean initSession(ChatContactQuery contactQuery, ChatSessionDoc session,
		OutboxMessage outboxMessage) {
	    return true;
	}

	default public void message(String messageType, ChatContactDoc chatContactDoc, IMessageExtended inboxMessage,
		OutboxMessage outboxMessage) {
	    LOGGER.debug("message(String {}, ChatContactDoc {}, SessionMessage {}, OutboxMessage {})", messageType,
		    chatContactDoc, inboxMessage, outboxMessage);
	    try {
		switch (messageType) {
		case "SEND":
		    outboxMessage.messageMetaWrapper().composeType("N"); // is a New Message
		    this.send(chatContactDoc, outboxMessage);
		    outboxMessage.updateStatus(Message.Status.SENT);
		    break;
		case "REPLY":
		    outboxMessage.messageMetaWrapper().composeType("R"); // Its a Reply
		    this.reply(inboxMessage, outboxMessage);
		    outboxMessage.updateStatus(Message.Status.SENT);
		    break;
		default:
		    break;
		}
	    } catch (Exception e) {
		outboxMessage.updateStatus(Message.Status.SENT_ERR);
		outboxMessage.logs().add(e.getMessage());
		LOGGER.error("SEND ERROR", e);
	    }

	}

	void send(OutboxMessage outboxMessage);

	default void registerWebHook(ChannelConfig channelConfig) {
	    LOGGER.error("WEBHOOK REGISTRATION NOT FOUND ");
	}

	default InboxMessage createInboxMessage(ChannelConfig channelConfig) {
	    InboxMessage inboxMessage = new InboxMessage();
	    if (ArgUtil.is(channelConfig)) {
		inboxMessage.contact().type(channelConfig.getContactType());
		inboxMessage.contact().setChannel(channelConfig.getChannelType());
		inboxMessage.contact().setLane(channelConfig.getLane());
	    }
	    return inboxMessage;
	}

    }

    public static abstract class AbstractConnector implements ConnectorHandler {
	@Autowired
	protected MessageContext messageContext;
    }

    public static abstract class DefaultConnector extends AbstractConnector {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Lazy
    public @interface ConnectorMapping {
	ContactType[] contactType();

	String[] channel() default "DEFAULT";
    }

    public ConnectorHandlerFactory(List<ConnectorHandler> libs) {
	super(libs);
    }

    @Override
    public String[] getKeys(ConnectorHandler lib) {
	ConnectorMapping annotation = lib.getClass().getAnnotation(ConnectorMapping.class);
	List<String> zoom = new ArrayList<String>();
	if (annotation != null) {
	    for (ContactType contactType : annotation.contactType()) {
		for (String channel : annotation.channel()) {
		    zoom.add(String.format("%s_%s", contactType, channel));
		}
	    }
	    return zoom.toArray(new String[0]);
	}
	return null;
    }

    public ConnectorHandler get(ContactType contactType, String channel) {
	LOGGER.debug("get(ContactType {}, String {})", contactType, channel);
	String precisedKey = String.format("%s_%s", contactType, channel);
	ConnectorHandler x = this.get(precisedKey);
	if (ArgUtil.is(x)) {
	    return x;
	}
	precisedKey = String.format("%s_DEFAULT", contactType);
	return this.get(precisedKey);
    }

    public ConnectorHandler get(ChannelConfig channelConfig) {
	if (ArgUtil.is(channelConfig)) {
	    ConnectorHandler connector = get(channelConfig.getContactType(), channelConfig.getChannelType());
	    if (ArgUtil.is(connector)) {
		return connector;
	    }
	}
	return defaultConnector;
    }

    @Autowired(required = false)
    private DefaultConnector defaultConnector;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private StompTunnelService stompTunnelService;

    @Autowired
    public CommonMongoTemplate commonMongoTemplate;

    @Autowired
    private PMEnvironment environment;

    /**
     * 
     * @param channelType
     * @param lane
     */
    public void registerWebHook(String channelType, String lane) {
	String channelId = PostManUtil.CHANNEL_ID(channelType, lane);
	PMConfiguration config = environment.config();
	ChannelConfig channelConfig = config.channels(channelId);
	registerWebHook(channelConfig);
    }

    public void registerWebHook(ChannelConfig channelConfig) {
	ConnectorHandler connector = get(channelConfig.getContactType(), channelConfig.getChannelType());
	if (ArgUtil.is(connector)) {
	    connector.registerWebHook(channelConfig);
	}
    }

    /**
     * 
     * Should always be last method or not changes in chatContactDoc or
     * outboxMessage after this;
     * 
     * @param messageType
     * @param chatContactDoc
     * @param inboxMessage
     * @param outboxMessage
     */
    @Async
    public void message(String messageType, ChatContactDoc chatContactDoc, IMessageExtended inboxMessage,
	    OutboxMessage outboxMessage) {
	LOGGER.debug("message(String {}, ChatContactDoc {}, SessionMessage {}, OutboxMessage {})", messageType,
		chatContactDoc, inboxMessage, outboxMessage);

	String channelId = PostManUtil.CHANNEL_ID(outboxMessage.contact());
	ChannelConfig channelConfig = environment.config().channels(channelId);

	try {
	    ConnectorHandler connector = get(channelConfig);
	    if (ArgUtil.is(connector)) {
		connector.message(messageType, chatContactDoc, inboxMessage, outboxMessage);
	    }
	} catch (Exception e) {
	    LOGGER.error(messageType, e);
	}

	MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);

	if (ArgUtil.isEqual(messageType, "REPLY", "SEND")) {
	    ChatContactQuery chatContactQuery = new ChatContactQuery(outboxMessage.contact().getContactId());
	    ChatSessionQuery chatSessionQuery = new ChatSessionQuery(outboxMessage.getSessionId());
	    long now = System.currentTimeMillis();
	    chatContactQuery.setLastOutBoundStamp(now);
	    chatSessionQuery.setLastOutGoingStamp(now);

	    switch (messageType) {
	    case "REPLY":
		chatContactQuery.setLastReplyStamp(now);
		chatSessionQuery.setLastResponseStamp(now);
		break;
	    case "SEND":
		chatContactQuery.setLastPushStamp(now);
		break;
	    default:
		break;
	    }
	    commonMongoTemplate.updateFirst(chatSessionQuery);
	    commonMongoTemplate.updateFirst(chatContactQuery);
	}

	if (PMConstants.CHAT_MODE.AGENT.toString().equals(outboxMessage.session().getMode())
		&& ArgUtil.is(outboxMessage.session().getDept())) {
	    ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
	    stompTunnelService.sendToTag(outboxMessage.session().getDept(), "/message/sent/new", messageDto);
	}
    }

}
