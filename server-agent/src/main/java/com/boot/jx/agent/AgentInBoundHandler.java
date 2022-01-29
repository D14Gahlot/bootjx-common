package com.boot.jx.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
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
import com.boot.jx.postman.model.ext.MsgSession;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class AgentInBoundHandler implements InBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInBoundHandler.class);

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    public PMDomainConfig pmDomainConfig;

    @Autowired
    public PMCommonConfig pmCommonConfig;

    @Autowired
    private RestService restService;
    
    @Autowired
    private AgentChatHandler agentChatHandler;

    @Override
    public void handle(InboxMessage inboxMessage) {
	agentChatHandler.onMessageReceive(inboxMessage);
    }

    @Override
    public void handle(MessageReport messageReport) {
	PMConfigurationObject webhookEntry = pmEnvironment
		.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_WEBHOOK);

	if (webhookEntry.exists()) {
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
			.server(pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_DOMAIN).asString());
		wrap.contacts = CollectionUtil.asList(contact);
		wrap.statuses = CollectionUtil.asList(status);
		restService.ajax(webhookEntry.asString()).post(wrap).asMapModel();
	    } catch (Exception e) {
		LOGGER.error("Error while Trying to HIT " + webhookEntry.asString(), e);
	    }

	}
    }

}
