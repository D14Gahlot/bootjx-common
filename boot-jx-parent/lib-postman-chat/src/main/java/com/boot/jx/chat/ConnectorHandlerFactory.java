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
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_MODE;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;

@Component
public class ConnectorHandlerFactory extends ScopedBeanFactory<String, ConnectorHandler> {

	private static final long serialVersionUID = 4007091611441725719L;

	public static Logger LOGGER = LoggerService.getLogger(ConnectorHandlerFactory.class);

	public interface ConnectorHandler {
		default public void reply(SessionMessage inboxMessage, OutboxMessage outboxMessage) {
			outboxMessage.addTo(inboxMessage.getFrom());
			outboxMessage.contact().setLane(inboxMessage.contact().getLane());
			this.send(outboxMessage);
		}

		default public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
			outboxMessage.addTo(chatContactDoc.getCsid());
			outboxMessage.contact().setLane(chatContactDoc.getLane());
			this.send(outboxMessage);
		}

		public InboxMessage assignToAgent(InboxMessage inboxMessage);

		default public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
			return true;
		}

		default public boolean initSession(ChatContactDoc contact, ChatSessionDoc session,
				OutboxMessage outboxMessage) {
			return true;
		}

		default public void message(String messageType, ChatContactDoc chatContactDoc, SessionMessage inboxMessage,
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

	}

	public interface DefaultConnector extends ConnectorHandler {
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
		LOGGER.debug("get(ContactType {}, String {})",contactType,channel);
		String precisedKey = String.format("%s_%s", contactType, channel);
		ConnectorHandler x = this.get(precisedKey);
		if (ArgUtil.is(x)) {
			return x;
		}
		precisedKey = String.format("%s_DEFAULT", contactType);
		return this.get(precisedKey);
	}

	@Autowired(required = false)
	private DefaultConnector defaultConnector;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	public CommonMongoTemplate commonMongoTemplate;

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
	public void message(String messageType, ChatContactDoc chatContactDoc, SessionMessage inboxMessage,
			OutboxMessage outboxMessage) {
		LOGGER.debug("message(String {}, ChatContactDoc {}, SessionMessage {}, OutboxMessage {})", messageType,
				chatContactDoc, inboxMessage, outboxMessage);

		try {
			ConnectorHandler connector = get(outboxMessage.contact().type(), outboxMessage.contact().getChannel());
			if (ArgUtil.is(connector)) {
				connector.message(messageType, chatContactDoc, inboxMessage, outboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				defaultConnector.message(messageType, chatContactDoc, inboxMessage, outboxMessage);
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

		if (CHAT_MODE.AGENT.toString().equals(outboxMessage.session().getMode())
				&& ArgUtil.is(outboxMessage.session().getDept())) {
			ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
			stompTunnelService.sendToTag(outboxMessage.session().getDept(), "/message/sent/new", messageDto);
		}
	}

}
