package com.boot.jx.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.manager.LogManager;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.ext.CommonMsgText.InBoundMsgText;
import com.boot.jx.postman.model.ext.InBoundContact;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.model.ext.InBoundMeta;
import com.boot.jx.postman.model.ext.InBoundMsg;
import com.boot.jx.postman.model.ext.InBoundMsgMedia;
import com.boot.jx.postman.model.ext.InBoundMsgStatus;
import com.boot.jx.postman.model.ext.InBoundWrapper;
import com.boot.jx.postman.model.ext.MsgSession;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

public abstract class DefaultChatBoundHandler implements InBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChatBoundHandler.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    public PMDomainConfig pmDomainConfig;

    @Autowired
    public PMCommonConfig pmCommonConfig;

    @Autowired
    private RestService restService;

    @Autowired
    private LogManager logManager;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private StompTunnelService stompTunnelService;

    @Autowired
    private ChatArchiveBuilder chatArchiveBuilder;

    @Autowired
    MitelClient mitelClient;

    public ClientApp getDefaultInboundApp(String assignedQueue, Contactable contactable) {
	ClientApp defaultClient = null;
	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.local().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }

	}

	if (!ArgUtil.is(contactable)) {
	    return defaultClient;
	}
	assignedQueue = pmDomainConfig.getDefaultInboundQueue(contactable);

	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.local().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }
	}

	return defaultClient;
    }

    @Override
    public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {

	ClientApp defaultClient = getDefaultInboundApp(inboxMessage.session().getQueue(), inboxMessage.contact());
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
			forward2Webhook(inboxMessage, defaultClient.getWebhook(), defaultClient.getId());
		    } else {
			ApiResponseUtil.throwException("Forward URL missing");
		    }
		    updateStatus(inboxMessage, Status.FORWARDED);
		    return;
		}

		// INTERNAL AGENT HANDLING
		if (CHAT_MODE.AGENT.equals(appType.getMode()) && ArgUtil.is(pmCommonConfig.getAgentUrl())) {
		    LOGGER.debug("Forwarding InboxMessage to internal Agent ");
		    chatClient.forward(pmCommonConfig.getAgentUrl() + PATH.INBOUND_FRWRD, inboxMessage);

		    if (ArgUtil.is(session) && APP_TYPE.MITEL.equals(appType)) {
			MapModel meta = new MapModel(session.getMeta());
			String omid = meta.pathEntry("mitel.omid").asString();
			if (ArgUtil.is(omid)) {
			    MapModel mitel = mitelClient.resend(defaultClient, session.contact(),
				    session.getSessionId(), omid);
			    String newomid = mitel.getString("id");
			    if (!ArgUtil.areEqual(newomid, omid)) {
				ChatSessionQuery q = new ChatSessionQuery(session);
				q.set("meta.mitel.omid", newomid).set("meta.mitel.queue_id",
					mitel.getString("queueId"));
				sessionStore.updateFirst(q);
			    }
			}
		    }
		    return;
		}

		// INTERNAL BOT HANDLING
		if (CHAT_MODE.BOT.equals(appType.getMode()) && ArgUtil.is(pmCommonConfig.getBotUrl())) {
		    LOGGER.debug("Forwarding InboxMessage to internal Bot ");
		    chatClient.forward(pmCommonConfig.getBotUrl() + PATH.INBOUND_FRWRD, inboxMessage);
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
	    logManager.error(inboxMessage, e);
	}
    }

    private void updateStatus(InboxMessage inboxMessage, Status status) {
	updateStatus(inboxMessage, status, null);
    }

    private void forward2Webhook(InboxMessage inboxMessage, String forwardUrl, String clientAppId) {
	InBoundContact contact = InBoundContact.from(inboxMessage.contact());

	InBoundMsg msg = new InBoundMsg();
	msg.messageId = inboxMessage.getMessageId();
	msg.messageIdExt = inboxMessage.getMessageIdExt();
	msg.contactFrom = ArgUtil.nonEmpty(inboxMessage.contact().getPhone(), inboxMessage.contact().getEmail());
	msg.contactId = contact.contactId;
	msg.session = new MsgSession();
	msg.session.sessionId = inboxMessage.getSessionId();

	msg.timestamp = inboxMessage.getTimestamp();
	msg.tags = inboxMessage.getTags();
	msg.input = inboxMessage.form();

	if (ArgUtil.is(inboxMessage.getAttachments())) {
	    Attachment atth = inboxMessage.attachments().get(0);
	    InBoundMsgMedia media = InBoundMsgMedia.from(atth);
	    if (MESSAGE_FORMAT_TYPE.IMAGE.equals(inboxMessage.getFormatType())) {
		msg.image = media;
	    } else if (MESSAGE_FORMAT_TYPE.STICKER.equals(inboxMessage.getFormatType())) {
		msg.sticker = media;
	    } else if (MESSAGE_FORMAT_TYPE.VIDEO.equals(inboxMessage.getFormatType())) {
		msg.video = media;
	    } else if (MESSAGE_FORMAT_TYPE.AUDIO.equals(inboxMessage.getFormatType())) {
		msg.audio = media;
	    } else if (MESSAGE_FORMAT_TYPE.VOICE.equals(inboxMessage.getFormatType())) {
		msg.voice = media;
	    } else {
		msg.document = media;
	    }
	} else {
	    msg.type = MESSAGE_FORMAT_TYPE.TEXT;
	    msg.text = new InBoundMsgText();
	    msg.text.type = inboxMessage.getFormatSubType();
	    msg.text.setBody(inboxMessage.getMessage());
	}

	InBoundWrapper wrap = new InBoundWrapper();
	wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
		.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString())
		.appId(clientAppId);

	wrap.contacts = CollectionUtil.asList(contact);
	wrap.messages = CollectionUtil.asList(msg);
	restService.ajax(forwardUrl).post(wrap).asNone();
    }

    @Override
    public void doHandle(MessageReport messageReport) {

	ClientApp defaultClient = getDefaultInboundApp(messageReport.session().getQueue(), messageReport.contact());

	if (ArgUtil.is(defaultClient)) {
	    if (ArgUtil.areEqual(CHAT_MODE.WEBHOOK.toString(), defaultClient.getAppType())) {
		LOGGER.debug("Forwarding MessageReport to Xternal Service ");
		try {
		    InBoundContact contact = InBoundContact.from(messageReport.contact());

		    InBoundMsgStatus status = new InBoundMsgStatus();
		    status.contactId = contact.contactId;
		    status.messageId = messageReport.getMessageId();
		    status.messageIdExt = messageReport.getMessageIdExt();
		    status.timestamp = messageReport.getChangeStamp();
		    status.status = messageReport.getStatus();
		    status.errors = messageReport.getErrors();

		    InBoundWrapper wrap = new InBoundWrapper();
		    wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
			    .server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString())
			    .appId(defaultClient.getId());
		    wrap.contacts = CollectionUtil.asList(contact);
		    wrap.statuses = CollectionUtil.asList(status);
		    restService.ajax(defaultClient.getWebhook()).post(wrap).asNone();
		} catch (Exception e) {
		    logManager.error(messageReport, e);
		}
		return;
	    }

	}
	stompTunnelService.sendToAll("/message/update/status", messageReport);
    }

    @Override
    public void onSessionRoute(InBoundEvent event, ChatSessionDoc sessionDoc) {

	if (InBoundEvent.SESSION_ROUTED.equals(event.eventCode)) {

	    ClientApp defaultClient = getDefaultInboundApp(event.sessionRouted.targetQueue, null);
	    if (ArgUtil.is(defaultClient)) {
		APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());
		if (APP_TYPE.WEBHOOK.equals(appType)) {
		    sendEventWebhook(event, defaultClient);
		    return;
		} else if (APP_TYPE.MITEL.equals(appType)) {
		    try {
			MapModel mitel = mitelClient.send(defaultClient, sessionDoc.contact(),
				sessionDoc.getSessionId());
			ChatSessionQuery q = new ChatSessionQuery(sessionDoc);
			q.set("meta.mitel.omid", mitel.getString("id")).set("meta.mitel.queue_id",
				mitel.getString("queueId"));
			sessionStore.updateFirst(q);
		    } catch (Exception e) {
			logManager.error(event, e);
		    }
		}
	    }
	}
    }

    @Override
    public void onSessionInit(InBoundEvent event, ChatSessionDoc chatSessionDoc) {

    }

    @Override
    public void onSessionClose(InBoundEvent event, ChatSessionDoc sessionDoc) {

	ClientApp defaultClient = getDefaultInboundApp(sessionDoc.getAssignedToQueue(), null);
	if (ArgUtil.is(defaultClient)) {
	    APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());
	    if (APP_TYPE.WEBHOOK.equals(appType)) {
		sendEventWebhook(event, defaultClient);
		return;
	    } else if (APP_TYPE.MITEL.equals(appType)) {
		try {
		    MapModel meta = new MapModel(sessionDoc.getMeta());
		    mitelClient.openMediaAction(defaultClient, meta.pathEntry("mitel.omid").asString(), "Complete");
		} catch (Exception e) {
		    logManager.error(event, e);
		}
	    }
	}
	stompTunnelService.sendToAll(PostManUtil.ON_DEPT_ASSIGN_TOPIC(sessionDoc.getAssignedToDept()),
		chatArchiveBuilder.buildChatSessionDTO().from(sessionDoc).withContact()
			.isAssigned(sessionDoc.getAssignedToAgent()).get());
    }

    private void sendEventWebhook(InBoundEvent event, ClientApp defaultClient) {
	LOGGER.debug("Forwarding Session Routing Event to Xternal Service ");
	try {
	    InBoundContact contact = InBoundContact.from(event.contact());

	    InBoundWrapper wrap = new InBoundWrapper();
	    wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
		    .server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString())
		    .appId(defaultClient.getId());
	    wrap.contacts = CollectionUtil.asList(contact);
	    wrap.events = CollectionUtil.asList(event);
	    restService.ajax(defaultClient.getWebhook()).post(wrap).asNone();
	} catch (Exception e) {
	    logManager.error(event, e);
	}
    }

}
