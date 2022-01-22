package com.boot.jx.postman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.ext.CommonMsgText.InBoundMsgText;
import com.boot.jx.postman.model.ext.InBoundContact;
import com.boot.jx.postman.model.ext.InBoundMeta;
import com.boot.jx.postman.model.ext.InBoundMsg;
import com.boot.jx.postman.model.ext.InBoundMsgMedia;
import com.boot.jx.postman.model.ext.InBoundMsgStatus;
import com.boot.jx.postman.model.ext.InBoundWrapper;
import com.boot.jx.rest.RestService;
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
    private RestService restService;

    @Override
    public void handle(InboxMessage inboxMessage) {
	PMConfigurationObject webhookEntry = pmEnvironment
		.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_WEBHOOK);

	if (webhookEntry.exists()) {
	    LOGGER.debug("Forwarding InboxMessage to Xternal Service ");
	    try {

		InBoundContact contact = InBoundContact.from(inboxMessage.contact());

		InBoundMsg msg = new InBoundMsg();
		msg.messageId = inboxMessage.getMessageId();
		msg.messageIdExt = inboxMessage.getMessageIdExt();
		msg.contactFrom = ArgUtil.nonEmpty(inboxMessage.contact().getPhone(),
			inboxMessage.contact().getEmail());
		msg.contactId = contact.contactId;

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
			.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString());
		wrap.contacts = CollectionUtil.asList(contact);
		wrap.messages = CollectionUtil.asList(msg);
		restService.ajax(webhookEntry.asString()).post(wrap).asMapModel();
	    } catch (Exception e) {
		LOGGER.error("Error while Trying to HIT " + webhookEntry.asString(), e);
	    }
	} else {
	    chatClient.forward(inboxMessage);
	}

    }

    @Override
    public void handle(MessageReport messageReport) {
	PMConfigurationObject webhookEntry = pmEnvironment
		.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_WEBHOOK);

	if (webhookEntry.exists()) {
	    LOGGER.debug("Forwarding MessageReport to Xternal Service ");
	    try {
		InBoundMsgStatus status = new InBoundMsgStatus();
		status.contactId = messageReport.contact().getContactId();
		status.messageId = messageReport.getMessageId();
		status.messageIdExt = messageReport.getMessageIdExt();
		status.timestamp = messageReport.getChangeStamp();
		status.status = messageReport.getStatus();
		status.errors = messageReport.getErrors();

		InBoundWrapper wrap = new InBoundWrapper();
		wrap.meta = new InBoundMeta().domain(AppContextUtil.getTenant())
			.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString());
		wrap.contacts = CollectionUtil.asList(InBoundContact.from(messageReport.contact()));
		wrap.statuses = CollectionUtil.asList(status);
		restService.ajax(webhookEntry.asString()).post(wrap).asMapModel();
	    } catch (Exception e) {
		LOGGER.error("Error while Trying to HIT " + webhookEntry.asString(), e);
	    }

	}
    }

}
