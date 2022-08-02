
package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "FatherBot")
public class DefaultChatController extends CommonBotController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChatController.class);

	@ChatMapping(key = "transfer-to-agent")
	public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		commonTransferToAgent(inboxMessage, matcher);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
		commonTransferToAgent(inboxMessage, matcher);
	}

}
