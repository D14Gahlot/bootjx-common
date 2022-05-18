package com.boot.jx.bot.chakli;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_cafebazzabot" })
public class DemoCafeBazzaController extends DefaultChakliController {

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("cb_welcome_msg");
	}

	@ChatMapping(key = "select-language")
	public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String lang = toReplyEnum(inboxMessage);
		if (!ArgUtil.is(lang)) {
			lang = ArgUtil.parseAsString(context().contact().getLang());
		}

		if(!timeCheckV1("cb_switch","cb_start_time","cb_end_time")) {
			 reply(new OutboxMessage().template("cb_working_hours_update"));
		}else {
		if (lang.equalsIgnoreCase("english") || (lang != null && lang.equalsIgnoreCase("en"))) {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("cb_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("cb_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("cb_question"));
			next("select-question");
		}
	 }
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("cb_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "cb_new_order":
			reply(new OutboxMessage().template("cb_new_order_ans"));
			next("next_menu");
			break;
		case "cb_edit_order":
		case "cb_order_follow_up":
		case "cb_reservation":
		case "cb_edit_reservation":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		case "cb_location":
			reply(new OutboxMessage().template("cb_location_ans"));
			next("next_menu");
			break;
		case "cb_working_hours":
			reply(new OutboxMessage().template("cb_working_hrs_ans"));
			next("next_menu");
			break;
		case "cb_catering":
			reply(new OutboxMessage().template("cb_catering_ans"));
			next("next_menu");
			break;
		case "cb_help":
			//reply(new OutboxMessage().template("cb_help_ans"));
			//next("next_menu");
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		default:
			reply(new OutboxMessage().template("cb_invalid_option"));
			reply(new OutboxMessage().template("cb_question"));
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
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		default:
			this.goToMainMenu(inboxMessage, matcher);
			break;

		}
	}

	public void goToMainMenu(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("cb_question"));
		next("select-question");
	}

	@ChatMapping(key = "cb_invalid_input")
	public void invalidinput(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "*":
			reply(new OutboxMessage().template("select-question"));
			next("select-question");
			break;
		case "#":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		default:
			reply(new OutboxMessage().template("cb_question"));
			next("select-question");
			break;
		}
	}

}
