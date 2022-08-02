package com.boot.jx.bot.chakli;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_jihanbot" })
public class DemoJihanController extends DefaultChakliController {

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("jd_welcome_msg");
	}

	@ChatMapping(key = "select-language")
	public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String lang = toReplyEnum(inboxMessage);
		if (!ArgUtil.is(lang)) {
			lang = ArgUtil.parseAsString(context().contact().getLang());
		}
		
		if(!timeCheckV1("jd_switch","jd_start_time","jd_end_time")) {
			 reply(new OutboxMessage().template("jd_working_hours_update"));
		}else {
		if (lang.equalsIgnoreCase("english") || (lang != null && lang.equalsIgnoreCase("en"))) {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("jd_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("jd_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("jd_question"));
			next("select-question");
		}
	}
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("jd_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "jd_new_order":
			reply(new OutboxMessage().template("jd_new_order_ans"));
			next("next_menu");
			break;
		case "jd_reserv":	
			reply(new OutboxMessage().template("jd_date_time"));
			next("next_datetime");
			break;
		case "jd_edit_order":
		case "jd_ord_follow_up":
		case "jd_edit_reserv":
			this.transferToAgent(inboxMessage, matcher);
			break;
		case "jd_location":
			reply(new OutboxMessage().template("jd_our_location_ans"));
			next("next_menu");
			break;
		case "jd_working_hrs":
			reply(new OutboxMessage().template("jd_working_hrs_ans"));
			next("next_menu");
			break;
		case "jd_catering":
			reply(new OutboxMessage().template("jd_catering_ans"));
			next("next_menu");
			break;
		case "jd_help":
			//reply(new OutboxMessage().template("jd_help_ans"));
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
			reply(new OutboxMessage().template("jd_invalid_option"));
			reply(new OutboxMessage().template("jd_question"));
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
		reply(new OutboxMessage().template("jd_question"));
		next("select-question");
	}

	@ChatMapping(key = "jd_invalid_input")
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

	@ChatMapping(key = "next_datetime")
	public void specifyDateAndTime(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		default:
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		}
	}
	
}
