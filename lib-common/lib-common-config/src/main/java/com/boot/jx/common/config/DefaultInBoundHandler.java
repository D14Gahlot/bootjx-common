package com.boot.jx.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.manager.LogManager;
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
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

public class DefaultInBoundHandler implements InBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInBoundHandler.class);

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
    private StompTunnelService stompTunnelService;

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
    public void handle(InboxMessage inboxMessage) {

	ClientApp defaultClient = getDefaultInboundApp(inboxMessage.session().getQueue(), inboxMessage.contact());
	if (ArgUtil.is(defaultClient)) {
	    try {

		// WEBHOOOK HANDLING
		if (ArgUtil.areEqual(CHAT_MODE.WEBHOOK.toString(), defaultClient.getAppType())) {
		    LOGGER.debug("Forwarding InboxMessage to Xternal Queue ");
		    String forwardUrl = defaultClient.getWebhook();
		    forward2Webhook(inboxMessage, forwardUrl, defaultClient.getId());
		    updateStatus(inboxMessage, Status.FORWARDED);
		    return;
		}

		// INTERNAL AGENT HANDLING
		if (ArgUtil.areEqual(CHAT_MODE.AGENT.toString(), defaultClient.getAppType())
			&& ArgUtil.is(pmCommonConfig.getAgentUrl())) {
		    LOGGER.debug("Forwarding InboxMessage to internal Agent ");
		    chatClient.forward(pmCommonConfig.getAgentUrl() + PATH.INBOUND_FRWRD, inboxMessage);
		    return;
		}

		// INTERNAL BOT HANDLING
		if (ArgUtil.areEqual(CHAT_MODE.BOT.toString(), defaultClient.getAppType())
			&& ArgUtil.is(pmCommonConfig.getBotUrl())) {
		    LOGGER.debug("Forwarding InboxMessage to internal Bot ");
		    chatClient.forward(pmCommonConfig.getBotUrl() + PATH.INBOUND_FRWRD, inboxMessage);
		    return;
		}

	    } catch (Exception e) {
		updateStatus(inboxMessage, Status.FORWARD_ERR, e);
	    }
	    return;

	}

	chatClient.forward(inboxMessage);

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
    public void handle(MessageReport messageReport) {

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
    public void handle(InBoundEvent inBoundEvent) {

	if (InBoundEvent.SESSION_ROUTED.equals(inBoundEvent.eventCode)) {

	    ClientApp defaultClient = getDefaultInboundApp(inBoundEvent.sessionRouted.targetQueue, null);
	    if (ArgUtil.is(defaultClient)) {
		if (ArgUtil.areEqual(CHAT_MODE.WEBHOOK.toString(), defaultClient.getAppType())) {
		    LOGGER.debug("Forwarding Session Routing Event to Xternal Service ");
		    try {
			InBoundContact contact = InBoundContact.from(inBoundEvent.contact());

			InBoundWrapper wrap = new InBoundWrapper();
			wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
				.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString())
				.appId(defaultClient.getId());
			wrap.contacts = CollectionUtil.asList(contact);
			wrap.events = CollectionUtil.asList(inBoundEvent);
			restService.ajax(defaultClient.getWebhook()).post(wrap).asNone();
		    } catch (Exception e) {
			logManager.error(inBoundEvent, e);
		    }
		    return;
		}
	    }
	}
    }

}
