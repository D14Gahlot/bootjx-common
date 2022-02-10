package com.boot.jx.postman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.manager.LogManager;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
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

@Component
public class PostManInBoundHandler implements InBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostManInBoundHandler.class);

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

    @Override
    public void handle(InboxMessage inboxMessage) {

	String assignedQueue = inboxMessage.session().getQueue();

	if (!ArgUtil.is(assignedQueue)) {
	    assignedQueue = pmDomainConfig.getDefaultInboundQueue(inboxMessage.contact());
	}

	if (ArgUtil.is(assignedQueue)) {
	    ClientApp defaultClient = pmEnvironment.local().clientApiKey(assignedQueue);
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
		    if (ArgUtil.areEqual(CHAT_MODE.AGENT.toString(), defaultClient.getAppType())) {
			LOGGER.debug("Forwarding InboxMessage to internal Agent ");
			chatClient.forward(pmCommonConfig.getAgentUrl() + PATH.INBOUND_FRWRD, inboxMessage);
			return;
		    }

		    // INTERNAL BOT HANDLING
		    if (ArgUtil.areEqual(CHAT_MODE.BOT.toString(), defaultClient.getAppType())) {
			LOGGER.debug("Forwarding InboxMessage to internal Bot ");
			chatClient.forward(pmCommonConfig.getBotUrl() + PATH.INBOUND_FRWRD, inboxMessage);
			return;
		    }

		} catch (Exception e) {
		    updateStatus(inboxMessage, Status.FORWARD_ERR, e);
		}

	    }

	}

//	PMConfigurationObject webhookEntry = pmEnvironment
//		.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_WEBHOOK);
//	if (webhookEntry.exists()) {
//	    LOGGER.debug("Forwarding InboxMessage to Xternal Service ");
//	    try {
//		forward2Webhook(inboxMessage, webhookEntry.asString(), null);
//		updateStatus(inboxMessage, Status.FORWARDED);
//	    } catch (Exception e) {
//		updateStatus(inboxMessage, Status.FORWARD_ERR, e);
//	    }
//	} else {
	chatClient.forward(inboxMessage);
//	}

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

	String assignedQueue = messageReport.session().getQueue();

	if (!ArgUtil.is(assignedQueue)) {
	    assignedQueue = pmDomainConfig.getDefaultInboundQueue(messageReport.contact());
	}

	if (ArgUtil.is(assignedQueue)) {
	    ClientApp defaultClient = pmEnvironment.local().clientApiKey(assignedQueue);
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
	}
	stompTunnelService.sendToAll("/message/update/status", messageReport);
    }

    @Override
    public void handle(InBoundEvent inBoundEvent) {

	String assignedQueue;
	if (InBoundEvent.SESSION_ROUTED.equals(inBoundEvent.eventCode)) {
	    assignedQueue = inBoundEvent.sessionRouted.targetQueue;
	    if (!ArgUtil.is(assignedQueue)) {
		ClientApp defaultClient = pmEnvironment.local().clientApiKey(assignedQueue);
		if (ArgUtil.is(defaultClient)) {
		    if (ArgUtil.areEqual(CHAT_MODE.WEBHOOK.toString(), defaultClient.getAppType())) {
			LOGGER.debug("Forwarding Session Routing Event to Xternal Service ");
			try {
			    InBoundContact contact = InBoundContact.from(inBoundEvent.contact());

			    InBoundWrapper wrap = new InBoundWrapper();
			    wrap.meta = new InBoundMeta()
				    .domain(AppContextUtil.getTenant()).server(pmEnvironment
					    .keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString())
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
}
