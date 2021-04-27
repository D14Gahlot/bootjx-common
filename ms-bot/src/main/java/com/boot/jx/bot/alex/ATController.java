
package com.boot.jx.bot.alex;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "Aerin", tenant = "aertrip")
public class ATController extends ChatController {

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
			reply("Some Tech Issues");
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
		transferToAgent(inboxMessage, matcher);
	}

}
