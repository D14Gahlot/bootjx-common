package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.ChatController;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

public class CommonBotController extends ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBotController.class);

    @Autowired
    private PMEnvironment pmEnvironment;

    public void commonTransferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	try {
	    InBoundEvent assignEvent = assignToAgent().value();
	    if (ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {
		PMConfigurationObject transferReply = pmEnvironment
			.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
		if (transferReply.exists()) {
		    reply(new OutboxMessage().template(transferReply.asString()));
		} else {
		    reply("Connecting you to one of our customer representatives. Give us a moment.");
		}
	    } else {
		PMConfigurationObject noAgentReply = pmEnvironment
			.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT);
		if (noAgentReply.exists()) {
		    reply(new OutboxMessage().template(noAgentReply.asString()));
		} else {
		    reply("All agents are busy or online, we will connect you whenever someone is available.");
		}
	    }
	} catch (Exception e) {
	    reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
	    LOGGER.error("Erro while Connecting to Agent", e);
	}
    }
}
