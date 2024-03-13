package com.boot.jx.common.config;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.service.SessionEventTimer;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.inbound.InBound.MessageEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMConstants.PROPERTIES;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.CommonMsgText.InBoundMsgText;
import com.boot.jx.postman.model.ext.InBoundContact;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.model.ext.InBoundMeta;
import com.boot.jx.postman.model.ext.InBoundMsg;
import com.boot.jx.postman.model.ext.InBoundMsgMedia;
import com.boot.jx.postman.model.ext.InBoundMsgStatus;
import com.boot.jx.postman.model.ext.InBoundWrapper;
import com.boot.jx.postman.model.ext.MsgSession;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

public abstract class DefaultChatBoundHandler implements InBoundHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChatBoundHandler.class);

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMDomainConfig pmDomainConfig;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private RestService restService;

	@Autowired
	private RestHookService restHookService;

	@Autowired
	protected ChatLogger logManager;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	private ChatArchiveBuilder chatArchiveBuilder;

	@Autowired
	private MitelClient mitelClient;

	@Autowired
	private SessionEventTimer sessionEventTimer;

	@Autowired(required = false)
	private ChatService chatService;

	@Lazy
	@Autowired(required = false)
	private ChatSessionService chatSessionService;

	@Lazy
	@Autowired(required = false)
	private MessageEvents messageEvents;

	@Autowired(required = false)
	private MessageContext messageContext;

	@Override
	public MessageContext context() {
		return messageContext;
	}

	@Override
	public CHAT_MODE mode() {
		return CHAT_MODE.NONE;
	}

	@Override
	public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {

		ClientApp defaultClient = context().clientApp(inboxMessage.session().getQueue(), inboxMessage.contact());
		if (ArgUtil.is(defaultClient)) {
			try {

				APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());

				// WEBHOOOK HANDLING
				if (CHAT_MODE.WEBHOOK.equals(appType.getMode())) {
					LOGGER.debug("Forwarding InboxMessage to Xternal Queue ");
					if (ArgUtil.is(defaultClient.getForward()) && ArgUtil.is(inboxMessage.getOriginalMessage())) {
						// This code is only for local debugging for inbounds will not execute in
						// production
						inboxMessage.setOriginalMessage(null);
						chatClient.forward(defaultClient.getForward() + PATH.INBOUND_FRWRD, inboxMessage);
					} else if (ArgUtil.is(defaultClient.getWebhook())) {
						forward2Webhook(inboxMessage, defaultClient.getWebhook(), defaultClient.getId(), true);
					} else {
						// if (APP_TYPE.APP_SCRIPT.equals(appType)) {
						forward2Webhook(inboxMessage, pmCommonConfig.getScriptusUrl() + PATH.APP_SCRIPT_FRWRD,
								defaultClient.getId());
						// } else {
						// ApiResponseUtil.throwException("Forward URL missing");
						// }
					}

					updateStatus(inboxMessage, Status.FORWARDED);
					return;
				}

				// INTERNAL AGENT HANDLING
				if (CHAT_MODE.AGENT.equals(appType.getMode()) && ArgUtil.is(pmCommonConfig.getAgentUrl())) {
					LOGGER.debug("Forwarding InboxMessage to internal Agent ");
					chatClient.forward(pmCommonConfig.getAgentUrl() + PATH.INBOUND_FRWRD, inboxMessage);
					return;
				}

				// INTERNAL BOT HANDLING
				if (CHAT_MODE.BOT.equals(appType.getMode()) && ArgUtil.is(pmCommonConfig.getBotUrl())) {
					LOGGER.debug("Forwarding InboxMessage to internal Bot ");
					chatClient.forward(pmCommonConfig.getBotUrl()
							// "http://127.0.0.1:8084/bot"
							+ PATH.INBOUND_FRWRD, inboxMessage);
					return;
				}

			} catch (Exception e) {
				updateStatus(inboxMessage, Status.FORWARD_ERR, e);
			}
			return;
		}

		if ("AGENT".equalsIgnoreCase(inboxMessage.session().getMode())
				&& ArgUtil.isEmptyValue(inboxMessage.session().isResolved())) {
			chatClient.forward(pmCommonConfig.getAgentUrl() + PATH.INBOUND_FRWRD, inboxMessage);
		} else {
			chatClient.forward(pmCommonConfig.getBotUrl() + PATH.INBOUND_FRWRD, inboxMessage);
		}
	}

	@Override
	public void afterMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
		if (messageEvents != null) {
			messageEvents.postMessageInBound(inboxMessage);
		}
	}

	private void updateStatus(InboxMessage inboxMessage, Status status, Exception e) {
		MessageReport messageReport = new MessageReport();
		messageReport.contact().copyFrom(inboxMessage.getContact());
		messageReport.setChangeStamp(System.currentTimeMillis());
		messageReport.setMessageId(inboxMessage.getMessageId());
		messageReport.setMessageIdExt(inboxMessage.getMessageIdExt());
		messageReport.setMessageIdRef(inboxMessage.getMessageIdRef());
		messageReport.setStatus(status);

		if (ArgUtil.is(e)) {
			messageReport.setReason(e.getMessage());
		}
		messageStore.updateStatus(messageReport);

		if (ArgUtil.is(e)) {
			logManager.error(inboxMessage, status, e);
		}
	}

	private void updateStatus(InboxMessage inboxMessage, Status status) {
		updateStatus(inboxMessage, status, null);
	}

	private void forward2Webhook(InboxMessage inboxMessage, String forwardUrl, String clientAppId) {
		forward2Webhook(inboxMessage, forwardUrl, clientAppId, false);
	}

	private void forward2Webhook(InboxMessage inboxMessage, String forwardUrl, String clientAppId,
			boolean externalTimeout) {
		InBoundContact contact = InBoundContact.from(inboxMessage.contact());

		InBoundMsg msg = new InBoundMsg();
		msg.messageId = inboxMessage.getMessageId();
		msg.messageIdExt = inboxMessage.getMessageIdExt();
		msg.contactFrom = ArgUtil.nonEmpty(inboxMessage.contact().getPhone(), inboxMessage.contact().getEmail(),
				inboxMessage.contact().getCsid());
		msg.contactId = contact.contactId;
		msg.session = new MsgSession();
		msg.session.sessionId = inboxMessage.getSessionId();
		msg.session.routingId = inboxMessage.getSession().getRoutingId();

		msg.timestamp = inboxMessage.getTimestamp();
		msg.tags = inboxMessage.getTags();
		msg.input = inboxMessage.form();

		if (ArgUtil.is(inboxMessage.getAttachments())) {
			Attachment atth = inboxMessage.attachments().get(0);
			InBoundMsgMedia media = InBoundMsgMedia.from(atth);
			if (MESSAGE_FORMAT_TYPE.IMAGE.equals(inboxMessage.getFormatType())) {
				msg.type = MESSAGE_FORMAT_TYPE.IMAGE;
				msg.image = media;
			} else if (MESSAGE_FORMAT_TYPE.STICKER.equals(inboxMessage.getFormatType())) {
				msg.type = MESSAGE_FORMAT_TYPE.STICKER;
				msg.sticker = media;
			} else if (MESSAGE_FORMAT_TYPE.VIDEO.equals(inboxMessage.getFormatType())) {
				msg.video = media;
				msg.type = MESSAGE_FORMAT_TYPE.VIDEO;
			} else if (MESSAGE_FORMAT_TYPE.AUDIO.equals(inboxMessage.getFormatType())) {
				msg.audio = media;
				msg.type = MESSAGE_FORMAT_TYPE.AUDIO;
			} else if (MESSAGE_FORMAT_TYPE.VOICE.equals(inboxMessage.getFormatType())) {
				msg.voice = media;
				msg.type = MESSAGE_FORMAT_TYPE.VOICE;
			} else {
				msg.type = MESSAGE_FORMAT_TYPE.DOCUMENT;
				msg.document = media;
			}
			msg.attachments = new ArrayList<InBoundMsgMedia>();
			for (Attachment thisAttach : inboxMessage.attachments()) {
				InBoundMsgMedia thisMedia = InBoundMsgMedia.from(thisAttach);
				msg.attachments.add(thisMedia);
			}
		} else {
			msg.type = MESSAGE_FORMAT_TYPE.TEXT;
			msg.text = new InBoundMsgText();
			msg.text.type = inboxMessage.getFormatSubType();
			msg.text.setBody(inboxMessage.getMessage());
		}

		InBoundWrapper wrap = new InBoundWrapper().type("messages");
		wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
				.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString())
				.appId(clientAppId)
				.debug(pmEnvironment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_DEBUG_CONTACT).is(contact.contactId));
		wrap.contacts = CollectionUtil.asList(contact);
		wrap.messages = CollectionUtil.asList(msg);

		try {
			(externalTimeout ? restHookService.ajax(forwardUrl) : restService.ajax(forwardUrl))
					.cookie(ParamKeys.X_API_ID, clientAppId).post(wrap).asNone();
		} catch (Exception e) {
			ApiResponseUtil.addError("Connection Error:" + forwardUrl);
			throw e;
		}

	}

	@Override
	public void doHandle(MessageReport messageReport) {

		ClientApp defaultClient = context().clientApp(messageReport.session().getQueue(), messageReport.contact());

		if (ArgUtil.is(defaultClient)) {
			if (ArgUtil.areEqual(CHAT_MODE.WEBHOOK.toString(), defaultClient.getAppType())) {
				LOGGER.debug("Forwarding MessageReport to Xternal Service ");
				try {
					if (ArgUtil.is(defaultClient.getWebhook())) {
						InBoundContact contact = InBoundContact.from(messageReport.contact());
						InBoundMsgStatus status = new InBoundMsgStatus();
						status.contactId = contact.contactId;
						status.messageId = messageReport.getMessageId();
						status.messageIdExt = messageReport.getMessageIdExt();
						status.timestamp = messageReport.getChangeStamp();
						status.status = messageReport.getStatus();
						status.errors = messageReport.getErrors();

						InBoundWrapper wrap = new InBoundWrapper().type("statuses");
						wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
								.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString())
								.appId(defaultClient.getId());
						wrap.contacts = CollectionUtil.asList(contact);
						wrap.statuses = CollectionUtil.asList(status);
						restHookService.ajax(defaultClient.getWebhook()).post(wrap).asNone();
					}

				} catch (Exception e) {
					logManager.error(messageReport, e);
				}
				return;
			}

		}
		stompTunnelService.sendToAll("/message/update/status", messageReport);
	}

	@Override
	public NodeEntry<InBoundEvent> assignSessionToAgent(PMArgs params, ChatSessionDoc session) {
		return new NodeEntry<InBoundEvent>().value(
				chatClient.assignToAgentV2(new PMArgs().sessionId(session.getSessionId()).contact(session.contact())
						.assignToDeptCode(params.getAssignToDeptCode()).assignToAgentCode(params.getAssignToAgentCode())
						.assignToSkillCodes(params.getAssignToSkillCodes())));
	}

	@Override
	public InBoundEvent onSessionEvent(InBoundEvent event, PMArgs pmArgs) {
		context().setInBoundEvent(event);
		if (InBoundEvent.SESSION_ROUTED.equals(event.eventCode)) {
			ChatSessionDoc sessionDoc = context().session().getDoc();
			this.onSessionRouteSync(event, sessionDoc, pmArgs);
		}
		return event;
	}

	@Override
	public void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		logManager.addTrace(inBoundEvent, EVENTS.ON_SESSION_ROUTE, "NO_ACTION");
	}

	@Override
	public void afterSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		if (sessionEventTimer != null) {
			ClientApp defaultClient = context().clientApp(inBoundEvent.sessionRouted.targetQueue);
			sessionEventTimer.setChatOutIdleTimeout(inBoundEvent.getSessionId(), defaultClient);
		}
	}

	@Override
	public void onSessionRouteWrapper(InBoundEvent event, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		if (InBoundEvent.SESSION_ROUTED.equals(event.eventCode)) {
			ClientApp targetAppQueue = context().clientApp(event.sessionRouted.targetQueue, null);
			if (ArgUtil.is(targetAppQueue)) {
				APP_TYPE appType = APP_TYPE.from(targetAppQueue.getAppType());
				if (appType.is(mode())) {
					context().setInBoundEvent(event);
					context().session(sessionDoc);
					this.onSessionRoute(event, sessionDoc, pmArgs);
					sessionEventTimer.setChatOnRoute(sessionDoc.getSessionId(), targetAppQueue);
				} else if (appType.is(CHAT_MODE.WEBHOOK)) {
					sendEventWebhook(event, targetAppQueue);
					return;
				} else if (appType.is(CHAT_MODE.AGENT)) {
					chatClient.sessionEvent(pmCommonConfig.getAgentUrl(), event, pmArgs);
				} else if (appType.is(CHAT_MODE.BOT)) {
					chatClient.sessionEvent(pmCommonConfig.getBotUrl(), event, pmArgs);
				}
			}
		}
	}

	@Override
	public void onSessionInit(InBoundEvent event, ChatSessionDoc chatSessionDoc) {

	}

	@Override
	public void onSessionResolve(InBoundEvent event, ChatSessionDoc chatSessionDoc) {
		PMConfigurationObject feedbackQueue = pmEnvironment.keyEntry(PROPERTIES.POSTMAN_CHAT_FEEDBACK_QUEUE);
		if (feedbackQueue.exists()) {
			chatSessionService.routeSession(chatSessionDoc, new PMArgs().assignToQueueCode(feedbackQueue.asString()));
		} else {
			PMConfigurationObject resolvedReply = pmEnvironment
					.keyEntry(PROPERTIES.POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED);
			if (resolvedReply.exists()) {
				chatService.send(chatSessionDoc, new OutboxMessage().template(resolvedReply.asString()));
			}
			chatSessionService.closeSession(chatSessionDoc);
		}
	}

	@Override
	public void onSessionClose(InBoundEvent event, ChatSessionDoc sessionDoc) {

		ClientApp defaultClient = context().clientApp(sessionDoc.getAssignedToQueue(), null);
		if (ArgUtil.is(defaultClient)) {
			APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());
			if (APP_TYPE.WEBHOOK.equals(appType)) {
				sendEventWebhook(event, defaultClient);
				return;
			} else if (APP_TYPE.MITEL.equals(appType)) {
				try {
					MapModel meta = new MapModel(sessionDoc.getMeta());
					MapPathEntry omid = meta.pathEntry("mitel.omid");
					if (omid.exists()) {
						mitelClient.openMediaAction(defaultClient, omid.asString(), "Complete");
					}
				} catch (Exception e) {
					logManager.error(event, e);
				}
			}
		}
		stompTunnelService.sendToAll(PostManUtil.ON_DEPT_ASSIGN_TOPIC(sessionDoc.getAssignedToDept()),
				chatArchiveBuilder.sessionDTO().from(sessionDoc).withContact()
						.isAssigned(sessionDoc.getAssignedToAgent()).get());
	}

	private void sendEventWebhook(InBoundEvent event, ClientApp defaultClient) {
		LOGGER.debug("Forwarding Session Routing Event to Xternal Service ");
		try {
			APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());

			String webhookUrl = APP_TYPE.APP_SCRIPT.equals(appType)
					? (pmCommonConfig.getScriptusUrl() + PATH.APP_SCRIPT_FRWRD)
					: defaultClient.getWebhook();

			if (ArgUtil.is(webhookUrl)) {
				InBoundContact contact = InBoundContact.from(event.contact());

				InBoundWrapper wrap = new InBoundWrapper().type("events");
				wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
						.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString())
						.appId(defaultClient.getId()).debug(pmEnvironment
								.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_DEBUG_CONTACT).is(contact.contactId));
				wrap.contacts = CollectionUtil.asList(contact);
				wrap.events = CollectionUtil.asList(event);
				restHookService.ajax(webhookUrl).cookie(ParamKeys.X_API_ID, defaultClient.getId()).post(wrap).asNone();
			}
		} catch (Exception e) {
			logManager.error(event, e);
		}
	}

}
