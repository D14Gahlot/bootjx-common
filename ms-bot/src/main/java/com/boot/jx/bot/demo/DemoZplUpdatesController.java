
package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "app", "demo", "sandbox" })
public class DemoZplUpdatesController extends CommonBotController {


	@Autowired
	PMEnvironment pmEnvironment;

	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
		next("menu-on-select");
	}

	@ChatMapping(key = "menu-on-select")
	public void panOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
  	switch (inboxMessage.getMessage().toLowerCase()) {
		case "*":
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
			next("feedback-onselect");
			break;
		case "scoreboard":
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
			next("menu-on-select");
			break;
		case "points_table":
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
			next("menu-on-select");
			break;
		default:
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
		return;
	}
	}
	
	@ChatMapping(key = "feedback-onselect")
	public void feedback(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toUpperCase()) {
		case "HAPPY":
		case "YES":
		case "Y":
		case "1":
			botScore(10);
			reply("Thanks");
			resolveSession();
			closeSession();
			break;
		case "NOT HAPPY":
		case "NOTHAPPY":
		case "NO":
		case "N":
		case "2":
			botScore(0);
			//transferToAgent(inboxMessage, matcher);
			break;
		default:
			//handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "feedback-onselect");
			return;
		}
	}

}
