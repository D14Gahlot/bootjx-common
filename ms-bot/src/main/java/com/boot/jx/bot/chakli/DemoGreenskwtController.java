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

@BotController(name = "almamaalholding", code = { "chakli_greenskwtbot" })
public class DemoGreenskwtController extends DefaultChakliController {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("gr_welcome_msg");
	}

	@ChatMapping(key = "select-language")
	public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String lang = toReplyEnum(inboxMessage);
		if (!ArgUtil.is(lang)) {
			lang = ArgUtil.parseAsString(context().contact().getLang());
		}

		if(!timeCheckV1("gr_switch","gr_start_time","gr_end_time")) {
			 reply(new OutboxMessage().template("gr_working_hours_update"));
		}else {
		if (lang.equalsIgnoreCase("english") || (lang != null && lang.equalsIgnoreCase("en"))) {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("gr_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("gr_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("gr_question"));
			next("select-question");
		}
	 }
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("gr_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "gr_new_order":
			reply(new OutboxMessage().template("gr_new_order_ans"));
			next("next_menu");
			break;
		case "gr_edit_order":
		case "gr_ord_follow_up":
			this.transferToAgent(inboxMessage, matcher);
			break;
		case "gr_location":
			reply(new OutboxMessage().template("gr_our_location_ans"));
			next("next_menu");
			break;
		case "gr_working_hrs":
			reply(new OutboxMessage().template("gr_working_hrs_ans"));
			next("next_menu");
			break;
		case "gr_catering":
			reply(new OutboxMessage().template("gr_catering_ans"));
			next("next_menu");
			break;
		case "gr_help":
			//reply(new OutboxMessage().template("gr_help_ans"));
			//next("next_menu");
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.transferToAgent(inboxMessage, matcher);
			break;
		default:
			reply(new OutboxMessage().template("gr_invalid_option"));
			reply(new OutboxMessage().template("gr_question"));
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
		reply(new OutboxMessage().template("gr_question"));
		next("select-question");
	}

	@ChatMapping(key = "gr_invalid_input")
	public void invalidinput(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "*":
			reply(new OutboxMessage().template("select-question"));
			next("select-question");
			break;
		case "#":
			this.transferToAgent(inboxMessage, matcher);
			break;
		default:
			this.goToMainMenu(inboxMessage, matcher);
			break;
		}
	}

}
