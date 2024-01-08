package com.boot.jx.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.boot.jx.connectors.AbstractConnector.DefaultConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.inbound.InBound.MessageEvents;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.MESSAGE_COMPOSE_TYPE;
import com.boot.jx.postman.PMConstants.MESSAGE_SEND_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class ConnectorHandlerFactory extends ScopedBeanFactory<String, ConnectorHandler> {

	private static final long serialVersionUID = 4007091611441725719L;

	public static Logger LOGGER = LoggerService.getLogger(ConnectorHandlerFactory.class);

	public interface ConnectorHandler {

		public static final long DEFAULT_SESISON_PERIOD = TimeUtils.toMillis("24h");

		default public void reply(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
				OutboxMessage outboxMessage, IMessageExtended inboxMessage) {
			if (!ArgUtil.is(channelConfig)) {
				channelConfig = getChannelConfig(outboxMessage);
			}

			if (!ArgUtil.is(chatContactDoc)) {
				chatContactDoc = getChatContact(outboxMessage);
			}

			outboxMessage.addTo(inboxMessage.getFrom());
			outboxMessage.contact().setLane(inboxMessage.contact().getLane());
			this.onSend(channelConfig, chatContactDoc, outboxMessage);
		}

		/**
		 * Message to be send while initiating new session
		 * 
		 * @param channelConfig
		 * @param chatContactDoc
		 * @param outboxMessage
		 */
		default public void send(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
				OutboxMessage outboxMessage) {
			if (!ArgUtil.is(channelConfig)) {
				channelConfig = getChannelConfig(outboxMessage);
			}

			if (!ArgUtil.is(chatContactDoc)) {
				chatContactDoc = getChatContact(outboxMessage);
			}
			outboxMessage.addTo(chatContactDoc.getCsid());
			outboxMessage.contact().setLane(chatContactDoc.getLane());
			this.onSend(channelConfig, chatContactDoc, outboxMessage);
		}

		default public InboxMessage assignToAgent(InboxMessage inboxMessage) {
			return inboxMessage;
		}

		default public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
			return null;
		}

		default public boolean initSession(ChatContactQuery contactQuery, ChatSessionDoc session,
				OutboxMessage outboxMessage) {
			return true;
		}

		default public void meta(ChannelConfig channelConfig, String messageType, ChatContactDoc chatContactDoc,
				IMessageExtended inboxMessage, OutboxMessage outboxMessage) {
			if (TimeUtils.isExpired(chatContactDoc.getLastInBoundStamp(), DEFAULT_SESISON_PERIOD)) {
				outboxMessage.messageMetaWrapper().sendType(MESSAGE_SEND_TYPE.PUSH_MESSAGE); // Push Message
			} else {
				outboxMessage.messageMetaWrapper().sendType(MESSAGE_SEND_TYPE.SESSION_MESSAGE); // Session Message
			}
		}

		default public void message(ChannelConfig channelConfig, String messageType, ChatContactDoc chatContactDoc,
				OutboxMessage outboxMessage, IMessageExtended inboxMessage) {
			LOGGER.debug("message(String {}, ChatContactDoc {}, SessionMessage {}, OutboxMessage {})", messageType,
					chatContactDoc, inboxMessage, outboxMessage);
			try {
				switch (messageType) {
				case MESSAGE_COMPOSE_TYPE.SEND:
					outboxMessage.messageMetaWrapper().composeType(MESSAGE_COMPOSE_TYPE.SEND_CODE); // is a New Message
					this.meta(channelConfig, messageType, chatContactDoc, inboxMessage, outboxMessage);
					this.send(channelConfig, chatContactDoc, outboxMessage);
					outboxMessage.updateStatus(Message.Status.SENT);
					break;
				case MESSAGE_COMPOSE_TYPE.REPLY:
					outboxMessage.messageMetaWrapper().composeType(MESSAGE_COMPOSE_TYPE.REPLY_CODE); // Its a Reply
					this.meta(channelConfig, messageType, chatContactDoc, inboxMessage, outboxMessage);
					this.reply(channelConfig, chatContactDoc, outboxMessage, inboxMessage);
					outboxMessage.updateStatus(Message.Status.SENT);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				onException(channelConfig, chatContactDoc, outboxMessage, e);
			}

		}

		default public void onException(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
				OutboxMessage outboxMessage, Exception e) {
			outboxMessage.updateStatus(Message.Status.SENT_EXC);
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		}

		void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage);

		default void onChannelUpdate(ChannelConfig channelConfig) {
			LOGGER.error("WEBHOOK REGISTRATION NOT FOUND ");
		}

		default InboxMessage createInboxMessage(ChannelConfig channelConfig, InboxMessage inboxMessage) {
			if (ArgUtil.is(channelConfig)) {
				inboxMessage.contact().type(channelConfig.getContactType());
				inboxMessage.contact().setChannelType(channelConfig.getChannelType());
				inboxMessage.contact().setLane(channelConfig.getLane());
			}
			return inboxMessage;
		}

		default InboxMessage createInboxMessage(ChannelConfig channelConfig) {
			InboxMessage inboxMessage = new InboxMessage();
			createInboxMessage(channelConfig, inboxMessage);
			return inboxMessage;
		}

		default MessageReport createMessageReport(ChannelConfig channelConfig) {
			MessageReport messageReport = new MessageReport();
			if (ArgUtil.is(channelConfig)) {
				messageReport.contact().type(channelConfig.getContactType());
				messageReport.contact().setChannelType(channelConfig.getChannelType());
				messageReport.contact().setLane(channelConfig.getLane());
			}
			return messageReport;
		}

		/**
		 * 
		 * This method is invoked when message from ChannelProvider is recvd and is to
		 * be formatted into MessageBoxEvent format Basically this method should be
		 * implemented to convert channel message formats to internal formats
		 * 
		 * @param channelConfig
		 * @param requestMap
		 * @param messageBoxEvent TODO
		 * @return
		 */
		public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
				MessageBoxEvent messageBoxEvent);

		default List<InboxMessage> beforeReceiveInboxMessage(List<InboxMessage> inboxMessages) {
			return inboxMessages;
		}

		/**
		 * This method is invoked after message from ChannelProvider has been processed
		 * 
		 * @param inboxMessages
		 * 
		 * @return
		 */
		default List<InboxMessage> onReceiveInboxMessage(List<InboxMessage> inboxMessages) {
			return inboxMessages;
		}

		default void onMessageReports(List<MessageReport> messageReports) {
			// DO Nothing this method is optional
		}

		public ChannelConfig getChannelConfig(IMessage outboxMessage);

		public ChatContactDoc getChatContact(IMessage outboxMessage);

		public OutboxMessage template(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
				OutboxMessage outboxMessage);

		boolean optin(ChannelConfig channelConfig, ChatContactDoc chatContactDoc);

		void prompt(InboxMessage inboxMessage);

		void linkProfile(ChatSessionDoc session, InboxMessage inboxMessage);

		CommonFile reloadMedia(ChannelConfig channelConfig, MessageDoc msg, Integer index)
				throws FileNotFoundException, IOException;

		void reloadMedia(ChannelConfig channelConfig, MessageDoc msg) throws FileNotFoundException, IOException;

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
	private DefaultConnector<?, ?> defaultConnector;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	public CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	protected MessageContext messageContext;

	@Lazy
	@Autowired(required = false)
	private MessageEvents messageEvents;

	/**
	 * 
	 * @param channelType
	 * @param lane
	 */
	public void onChannelUpdate(String channelType, String lane) {
		String channelId = PostManUtil.CHANNEL_ID(channelType, lane);
		PMConfiguration config = environment.local();
		ChannelConfig channelConfig = config.channel(channelId);
		onChannelUpdate(channelConfig);
	}

	public void onChannelUpdate(ChannelConfig channelConfig) {
		if (ArgUtil.is(channelConfig)) {
			ConnectorHandler connector = get(channelConfig.getContactType(), channelConfig.getChannelType());
			if (ArgUtil.is(connector)) {
				try {
					connector.onChannelUpdate(channelConfig);
				} catch (Exception e) {
					LOGGER.error("error onChannelUpdate " + channelConfig, e);
				}
			}
		}
	}

	/**
	 * 
	 * Should always be last method or not changes in chatContactDoc or
	 * outboxMessage after this;
	 * 
	 * @param messageType
	 * @param chatContactDoc
	 * @param outboxMessage
	 * @param inboxMessage
	 */
	@Async
	public void message(MessageContext context, String messageType, ChatContactDoc chatContactDoc,
			OutboxMessage outboxMessage, IMessageExtended inboxMessage) {
		messageContext.from(context);
		LOGGER.debug("message(String {}, ChatContactDoc {}, IMessageExtended {}, OutboxMessage {})", messageType,
				chatContactDoc, inboxMessage, outboxMessage);

		messageByConnector(messageType, chatContactDoc, outboxMessage, inboxMessage);

		MessageDoc messageDoc = messageStore.createOrUpdate(outboxMessage);

		if (ArgUtil.isEqual(messageType, "REPLY", "SEND")) {
			ChatContactQuery contactQuery = ArgUtil.is(chatContactDoc) ? new ChatContactQuery(chatContactDoc)
					: new ChatContactQuery(outboxMessage.contact().getContactId());
			ChatSessionQuery sessionQuery = new ChatSessionQuery(outboxMessage.getSessionId());
			long now = System.currentTimeMillis();
			contactQuery.setLastOutBoundStamp(now);
			sessionQuery.setLastOutGoingStamp(now);

			if (ArgUtil.is(chatContactDoc)) {
				if (ArgUtil.isEmptyValue(chatContactDoc.getFirstOutBoundStamp())) {
					contactQuery.setFirstOutBoundStamp(now);
				}
			}

			switch (messageType) {
			case MESSAGE_COMPOSE_TYPE.REPLY:
				contactQuery.setLastReplyStamp(now);
				sessionQuery.setLastResponseStamp(now);
				break;
			case MESSAGE_COMPOSE_TYPE.SEND:
				contactQuery.setLastPushStamp(now);
				break;
			default:
				break;
			}

			if (ArgUtil.is(inboxMessage) && ArgUtil.is(inboxMessage.contact())) {
				contactQuery.update(inboxMessage.contact());
			}

			sessionQuery.setLastMsg(ChatDTOUtil.getChatMessageDTO(messageDoc));
			commonMongoTemplate.updateFirst(sessionQuery);
			commonMongoTemplate.updateFirst(contactQuery);
		}

		if (PMConstants.CHAT_MODE.AGENT.toString().equals(outboxMessage.session().getMode())
				&& ArgUtil.is(outboxMessage.session().getDept())) {
			ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
			stompTunnelService.sendToTag(outboxMessage.session().getDept(), "/message/sent/new", messageDto);
		}

		if (messageEvents != null) {
			messageEvents.postMessageOutBound(outboxMessage);
		}
	}

	private void messageByConnector(String messageType, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage,
			IMessageExtended inboxMessage) {
		String channelId = PostManUtil.CHANNEL_ID(outboxMessage.contact());
		ChannelConfig channelConfig = environment.config().channel(channelId);

		try {
			if (ArgUtil.is(channelConfig) || ContactType.WEBSITE.equals(outboxMessage.contact().type())) {
				ConnectorHandler connector = get(channelConfig);
				if (ArgUtil.is(connector)) {
					connector.message(channelConfig, messageType, chatContactDoc, outboxMessage, inboxMessage);
				} else {
					outboxMessage.logs().add(String.format("Connector not defined for %s", channelId));
				}
			} else {
				outboxMessage.logs().add(String.format("ChannelConfig not found for %s", channelId));
			}

		} catch (Exception e) {
			LOGGER.error(messageType, e);
		}
	}

	/**
	 * This method does not store it in db
	 * 
	 * Currently not used anywhere
	 * 
	 * @param context
	 * @param messageType
	 * @param outboxMessage
	 * @param inboxMessage
	 */
	public void messageNoStore(MessageContext context, String messageType, OutboxMessage outboxMessage,
			IMessageExtended inboxMessage) {
		LOGGER.debug("message(String {}, ChatContactDoc {}, IMessageExtended {}, OutboxMessage {})", messageType, null,
				inboxMessage, outboxMessage);
		messageByConnector(messageType, null, outboxMessage, inboxMessage);
	}
}
