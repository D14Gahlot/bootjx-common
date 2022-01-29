package com.boot.jx.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

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
    private AgentChatHandler agentChatHandler;

//    @Autowired(required = false)
//    private ChatService chatService;

    public void reply(String message) {
//	try {
//	    if (ArgUtil.is(chatService)) {
//		chatService.reply(new OutboxMessage().message(message));
//	    }
//	} catch (InterruptedException e) {
//	    e.printStackTrace();
//	}
    }

    public void reply(OutboxMessage message) {
//	try {
//	    if (ArgUtil.is(chatService)) {
//		message.session().setAgent(chatService.getClientConfig().getDefaultSender());
//		chatService.reply(message);
//	    }
//	} catch (InterruptedException e) {
//	    e.printStackTrace();
//	}
    }

    @Override
    public void handle(InboxMessage inboxMessage) {
	if (ArgUtil.isEmpty(inboxMessage.session().getMode())) {
	    try {
		InboxMessage agentAssignResp = agentChatHandler.onAssign(inboxMessage);
		if (ArgUtil.is(agentAssignResp.session().getAgent())) {
		    PMConfigurationObject transferReply = pmEnvironment
			    .keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
		    if (transferReply.exists()) {
			reply(new OutboxMessage().templateId(transferReply.asString()));
		    } else {
			reply("Connecting you to one of our customer representatives. Give us a moment.");
		    }
		} else {
		    reply("All agents are busy or online, we will connect you whenever someone is available.");
		}
	    } catch (Exception e) {
		reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
		LOGGER.error("Erro while Connecting to Agent", e);
	    }

	}
	agentChatHandler.onMessageReceive(inboxMessage);
    }

    @Override
    public void handle(MessageReport messageReport) {
	LOGGER.debug("No Handling Required for Status on AgentSide");
    }

}
