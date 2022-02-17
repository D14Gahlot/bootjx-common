package com.boot.jx.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.DefaultInBoundHandler;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;

@Component
public class AgentInBoundHandler extends DefaultInBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInBoundHandler.class);

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    public PMDomainConfig pmDomainConfig;

    @Autowired
    public PMCommonConfig pmCommonConfig;

    @Autowired
    private AgentChatHandler agentChatHandler;

    @Autowired(required = false)
    private ChatService chatService;

    @Override
    public void doHandle(InboxMessage inboxMessage) {
	if (ArgUtil.isEmpty(inboxMessage.session().getMode())) {
	    try {
		InboxMessage agentAssignResp = agentChatHandler.onAssign(inboxMessage);
		if (ArgUtil.is(agentAssignResp.session().getAgent())) {
		    PMConfigurationObject transferReply = pmEnvironment
			    .keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
		    if (transferReply.exists()) {
			chatService.reply(inboxMessage, new OutboxMessage().templateId(transferReply.asString()));
		    } else {
			chatService.reply(inboxMessage, new OutboxMessage()
				.message("Connecting you to one of our customer representatives. Give us a moment."));
		    }
		} else {
		    chatService.reply(inboxMessage, new OutboxMessage().message(
			    "All agents are busy or online, we will connect you whenever someone is available."));
		}
	    } catch (Exception e) {
		LOGGER.error("Error ONE while Connecting to Agent", e);
		try {
		    chatService.reply(inboxMessage, new OutboxMessage().message(
			    "We are having some issues trying connect you to one of our customer representatives. Please be patient"));
		} catch (InterruptedException e1) {
		    LOGGER.error("Error TWO  while Sending Failure", e1);
		}
	    }
	}
	agentChatHandler.onMessageReceive(inboxMessage);
    }

    @Override
    public void doHandle(MessageReport messageReport) {
	LOGGER.debug("No Handling Required for Status on AgentSide");
    }

    @Override
    public void doHandle(InBoundEvent inBoundEvent) {
	super.doHandle(inBoundEvent);
    }

}
