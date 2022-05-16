
package com.boot.jx.bot.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "demo_zplu" })
public class DemoZplUpdatesController extends CommonBotController {

	@Autowired
	PMEnvironment pmEnvironment;

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
		next("menu-on-select");
	}

	@ChatMapping(key = "menu-on-select")
	public void panOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "scoreboard":
			reply(new OutboxMessage()
					.attachment(new Attachment()
							.mediaURL(
									"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/zpl/scoreboard.jpg")
							.mediaType(FileType.IMAGE.toString())));
			break;
		case "points_table":
			reply(new OutboxMessage()
					.attachment(new Attachment().mediaURL(
							"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/zpl/points_table.jpg")
							.mediaType(FileType.IMAGE.toString())));
			break;
		default:
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
			return;
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
