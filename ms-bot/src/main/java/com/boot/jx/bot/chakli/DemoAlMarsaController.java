package com.boot.jx.bot.chakli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_marsabot" })
public class DemoAlMarsaController extends DefaultChakliController {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static final String TALK_TO_AGENT = "";

	@Autowired
	PMEnvironment pmEnvironment;

	@Autowired
	MongoTemplate mongoTemplate;

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("ma_welcome_msg");
	}

	@ChatMapping(key = KEY_SELECT_LANGUAGE)
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
			reply(new OutboxMessage().template("ma_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("ma_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("ma_question"));
			next("select-question");
		}
		// }
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("ma_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "ma_new_order":
			reply(new OutboxMessage().template("ma_new_order_ans"));
			next("next_menu");
			break;
		case "ma_edit_order":
		case "ma_order_follow_up":
		case "ma_reservation":
		case "ma_edit_reservation":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		case "ma_location":
			reply(new OutboxMessage().template("ma_location_ans"));
			next("next_menu");
			break;
		case "ma_working_hours":
			reply(new OutboxMessage().template("ma_working_hrs_ans"));
			next("next_menu");
			break;
		case "ma_catering":
			reply(new OutboxMessage().template("ma_catering_ans"));
			next("next_menu");
			break;
		case "ma_help":
			reply(new OutboxMessage().template("ma_help_ans"));
			next("next_menu");
			break;
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;
		case "#":
			this.commonTransferToAgent(inboxMessage, matcher);
			break;
		default:
			reply(new OutboxMessage().template("ma_question"));
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
		reply(new OutboxMessage().template("ma_question"));
		next("select-question");
	}

	@ChatMapping(key = "ma_invalid_input")
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
			reply(new OutboxMessage().template("ma_question"));
			next("select-question");
			break;
		}
	}

}
