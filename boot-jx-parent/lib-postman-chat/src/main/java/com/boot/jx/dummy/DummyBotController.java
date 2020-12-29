package com.boot.jx.dummy;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(lane = "DUMMY")
public class DummyBotController extends ChatController {

	@ChatMapping(key = DummyBotConstants.KEY.CONFIRM_GENDER)
	public void onLinkCivilId(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("M. Indian, F.Female X.Cancel");
		next(DummyBotConstants.KEY.CONFIRM_GENDER_ONSELECT);
	}

	@ChatMapping(key = DummyBotConstants.KEY.CONFIRM_GENDER_ONSELECT)
	public void serviceSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage()) {
		case "M":
			reply("You Selected Male");
			resolve(DummyBotConstants.KEY.CONFIRM_GENDER);
			break;
		case "F":
			reply("You Selected Female");
			resolve(DummyBotConstants.KEY.CONFIRM_GENDER);
			break;
		case "X":
			reply("You Selected Cancel");
			reject(DummyBotConstants.KEY.CONFIRM_GENDER);
			break;
		default:
			reply("You Selected NoGender, Please select again");
			next(DummyBotConstants.KEY.CONFIRM_GENDER_ONSELECT);
			break;
		}
	}

	@ChatMapping(key = DummyBotConstants.KEY.PING, pattern = "^HI$")
	public void onPing(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("Hello There!");
	}

	@ChatMapping(key = DummyBotConstants.KEY.MENU, pattern = "^MENU$")
	public void serviceMenu(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("1. ServiceA, 2.ServiceB 3.ServiceC 4.TalkToAgent");
		next(DummyBotConstants.KEY.SERVICE_SELECTOR);
	}

	@ChatMapping(key = DummyBotConstants.KEY.SERVICE_SELECTOR)
	public void serviceSelect2(InboxMessage inboxMessage, StringMatcher matcher) {

		switch (inboxMessage.getMessage()) {
		case "1":
			reply("You Selected ServiceA");
			next(DummyBotConstants.KEY.SERVICE_SELECTOR);
			break;
		case "2":
			reply("You Selected ServiceB, it requires Gender Confirmation.");
			ChatPromise x = require(DummyBotConstants.KEY.CONFIRM_GENDER);
			switch (x.getResult()) {
			case RESOLVED:
				reply("OK You Can continue with Service B");
				break;
			case REJECTED:
				reply("Sorry Gender was required for Service B");
				break;
			default:
				return;
			}
			break;
		case "3":
			reply("You Selected ServiceC");
			next(DummyBotConstants.KEY.SERVICE_SELECTOR);
			break;
		case "5":
			reply("Sorry! This feature is in Progress");
			break;
		default:
			reply("You Selected Nothing, Please select again");
			next(DummyBotConstants.KEY.SERVICE_SELECTOR);
			break;
		}
	}
}
