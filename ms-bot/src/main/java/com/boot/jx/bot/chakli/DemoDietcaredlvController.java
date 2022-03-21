package com.boot.jx.bot.chakli;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_dietcaredlv" })
public class DemoDietcaredlvController extends CommonBotController {

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("jd_welcome_msg").put("name", context().contact().getName()));
	next("select-language");

    }
}
