package com.boot.jx.bot.chakli;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "chakli")
public class DefaultChakliController extends CommonBotController {

	private void resolveLanguage(String tmplCode) {
		reply(new OutboxMessage().template(tmplCode).put("name", context().contact().getName()));
		next("select-language");
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		resolveLanguage(null);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage(null);
	}

}
