package com.boot.jx.bot.chakli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_jaipurbot" })
public class DemoJaipurController extends DefaultChakliController {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("ja_welcome_msg");
	}

	@ChatMapping(key = "select-language")
	public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String lang = toReplyEnum(inboxMessage);
		if (!ArgUtil.is(lang)) {
			lang = ArgUtil.parseAsString(context().contact().getLang());
		}

		// if(!timeCheck()) {
		// reply(new OutboxMessage().template("working_hours_update"));
		// }else {
		if (lang.equalsIgnoreCase("english") || (lang != null && lang.equalsIgnoreCase("en"))) {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("ja_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("ja_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("ja_question"));
			next("select-question");
		}
		// }
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("ja_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "ja_new_order":
			reply(new OutboxMessage().template("ja_new_order_ans"));
			next("next_menu");
			break;
		case "ja_edit_order":
		case "ja_ord_follow_up":
			this.transferToAgent(inboxMessage, matcher);
			break;
		case "ja_location":
			reply(new OutboxMessage().template("ja_our_location_ans"));
			next("next_menu");
			break;
		case "ja_working_hrs":
			reply(new OutboxMessage().template("ja_working_hrs_ans"));
			next("next_menu");
			break;
		case "ja_catering":
			reply(new OutboxMessage().template("ja_catering_ans"));
			next("next_menu");
			break;
		case "ja_help":
			reply(new OutboxMessage().template("ja_help_ans"));
			next("next_menu");
			break;
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.transferToAgent(inboxMessage, matcher);
			break;
		default:
			reply(new OutboxMessage().template("ja_invalid_option"));
			reply(new OutboxMessage().template("ja_question"));
			break;

		}
	}

	@ChatMapping(key = "next_menu")
	public void locationLinkTiming(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.transferToAgent(inboxMessage, matcher);
			break;
		default:
			this.goToMainMenu(inboxMessage, matcher);
			break;

		}
	}

	@ChatMapping(key = "jd_cs_to_contact")
	public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		commonTransferToAgent(inboxMessage, matcher);
	}

	public void goToMainMenu(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("ja_question"));
		next("select-question");
	}

}
