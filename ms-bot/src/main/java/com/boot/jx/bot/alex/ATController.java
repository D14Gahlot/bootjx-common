
package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.inbound.InBoundControllerWA;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "Aerin", tenant = "aertrip")
public class ATController extends ChatController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ATController.class);

	@Autowired
	private ChatContext chatContext;

	@ChatMapping(key = "transfer-to-agent")
	public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		try {
			InboxMessage agentAssignResp = assignToAgent().getResult();
			if (ArgUtil.is(agentAssignResp.session().getAgent())) {
				reply("Connecting you to one of our customer representatives. Give us a moment.");
			} else {
				reply("All agents are busy or online, we will connect you whenever someone is available.");
			}
		} catch (Exception e) {
			reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
			LOGGER.error("Erro while Connecting to Agent", e);
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
		transferToAgent(inboxMessage, matcher);
	}

}
