package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.bot.ChatController;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

public class CommonBotController extends ChatController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonBotController.class);
	public static final String REPLY_ID = "reply_id";

	public void commonTransferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		try {
			assignToDefaultAgent();
		} catch (Exception e) {
			reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
			LOGGER.error("Erro while Connecting to Agent", e);
		}
	}

	public String toReplyEnum(InboxMessage inboxMessage) {
		String codeValue = inboxMessage.form().get(REPLY_ID) == null ? inboxMessage.getMessage()
				: inboxMessage.form().get(REPLY_ID).toString();
		if (ArgUtil.is(codeValue)) {
			codeValue = codeValue.toLowerCase().trim();
		}
		return codeValue;
	}
}
