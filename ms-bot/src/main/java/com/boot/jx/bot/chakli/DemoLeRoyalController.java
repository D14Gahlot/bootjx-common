package com.boot.jx.bot.chakli;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_hotelleroyalbot" })
public class DemoLeRoyalController extends DefaultChakliController {

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage("lr_welcome_msg");
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
			reply(new OutboxMessage().template("lr_question"));
			next("select-question");
		} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
			context().contact().setLang("ar");
			context().commit();
			reply(new OutboxMessage().template("lr_question"));
			next("select-question");
		} else {
			context().contact().setLang("en");
			context().commit();
			reply(new OutboxMessage().template("lr_question"));
			next("select-question");
		}
		// }
	}

	@ChatMapping(key = "select-question")
	public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		checkValue("lr_question", toReplyEnum(inboxMessage));
		switch (toReplyEnum(inboxMessage)) {
		case "lr_hotel_room":
			//reply(new OutboxMessage().template("lr_hotel_room_ans"));
			//next("next_menu");
			this.transferToAgent(inboxMessage, matcher);
			break;
		case "lr_reserv":
			reply(new OutboxMessage().template("lr_date_time"));
			next("next_datetime");
			break;
		case "lr_edit_reserv":
			this.transferToAgent(inboxMessage, matcher);
			break;
		case "lr_location":
			reply(new OutboxMessage().template("lr_our_location_ans"));
			next("next_menu");
			break;
		case "lr_hotel_serv":
			reply(new OutboxMessage().template("lr_hotel_serv_ans"));
			next("next_menu");
			break;
		case "lr_help":
			//reply(new OutboxMessage().template("lr_help_ans"));
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
			reply(new OutboxMessage().template("lr_invalid_option"));
			reply(new OutboxMessage().template("lr_question"));
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
		reply(new OutboxMessage().template("lr_question"));
		next("select-question");
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
