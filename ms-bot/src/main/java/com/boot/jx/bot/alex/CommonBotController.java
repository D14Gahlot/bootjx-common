package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.bot.ChatController;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.StringUtils.StringMatcher;

public class CommonBotController extends ChatController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonBotController.class);

	public void commonTransferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		try {
			assignToDefaultAgent();
			routeSession(PMConstants.DEFAULT.AGENT_QUEUE_CODE);
		} catch (Exception e) {
			reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
			LOGGER.error("Erro while Connecting to Agent", e);
		}
	}
}
