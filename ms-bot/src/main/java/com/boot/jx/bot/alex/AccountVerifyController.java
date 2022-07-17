
package com.boot.jx.bot.alex;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ContactCleanerService;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

//@BotController(name = "ALEX")
public class AccountVerifyController extends ChatController {

	@Autowired
	private ContactCleanerService contactCleanerService;

	@ChatMapping(key = AlexBotConstants.KEY.VERIFY_CIVIL_ID)
	public void verifyCivilIdPlain(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("Please Enter Your Valid Civil ID");
		next(AlexBotConstants.KEY.JUST_CIVIL_ID);
	}

	@ChatMapping(key = AlexBotConstants.KEY.ROUTE_NUMBER, pattern = "^route([ ]*)(\\d{5,15})([ ]*)$")
	public void routeNumber(InboxMessage inboxMessage, StringMatcher matcher) {
		contactCleanerService.addWhatsAppTest(inboxMessage.getFrom());
		reply(new OutboxMessage().template(inboxMessage.getFrom() + " is added to dev testing"));
	}

	@ChatMapping(key = AlexBotConstants.KEY.PING, pattern = "^PING$")
	public void onPing(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("PING");
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE, pattern = "^HI$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		String name = context().contact().getDoc().getName();
		if (ArgUtil.is(name)) {
			reply("Hello " + name);
			reply("Type menu to see options");
		} else {
			reply("Hello !there. If you tell me your name,I can update it in our records for future.");
			next(AlexBotConstants.KEY.SAVE_NAME_ONENTER);
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.SAVE_NAME_ONENTER)
	public void saveNameOnConfirm(InboxMessage inboxMessage, StringMatcher matcher) {
		String name = inboxMessage.getMessage();
		context().session().put("_name", name);

		ChatPromise x = require(AlexBotConstants.KEY.SAVE_NAME_CONFIRM);
		switch (x.getResult()) {
		case RESOLVED:
			reply("OK Now we can continue");
			reply("Type menu to see options");
			break;
		case REJECTED:
			reply("OK, if you do not want to tell us your name.");
			reply("Type menu to see options");
			break;
		default:
			return;
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.SAVE_NAME_CONFIRM)
	public void savenameOCnifmr(InboxMessage inboxMessage, StringMatcher matcher) {
		String name = ArgUtil.parseAsString(context().session().get("_name"));
		reply("Is your name '" + name + "' ? 'YES' to confirm. 'NO' to exit. or You can just type your name");
		next(AlexBotConstants.KEY.SAVE_NAME_CONFIRM_ONSELECT);
	}

	@ChatMapping(key = AlexBotConstants.KEY.SAVE_NAME_CONFIRM_ONSELECT)
	public void confirmName(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toUpperCase()) {
		case "YES":
			String _name = ArgUtil.parseAsString(context().session().get("_name"));
			context().contact().getDoc().setName(_name);
			context().contact().put("name", _name);
			reply("Hello " + _name + "! Your name has been updated");
			resolve(AlexBotConstants.KEY.SAVE_NAME_CONFIRM);
			resolve(AlexBotConstants.KEY.SAVE_NAME_CONFIRM_ONSELECT);
			break;
		case "NO":
			reject(AlexBotConstants.KEY.SAVE_NAME_CONFIRM);
			resolve(AlexBotConstants.KEY.SAVE_NAME_CONFIRM_ONSELECT);
			break;
		default:
			context().session().put("_name", inboxMessage.getMessage());
			reply("Is your name '" + inboxMessage.getMessage()
					+ "' ? 'YES' to confirm. 'NO' to exit. or You can just type your name");
			next(AlexBotConstants.KEY.SAVE_NAME_CONFIRM_ONSELECT);
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.MENU, pattern = "^MENU$")
	public void serviceMenu(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("1. ServiceA, 2.ServiceB 3.ServiceC 4.TalkToAgent");
		next(AlexBotConstants.KEY.SERVICE_SELECTOR);
	}

	@ChatMapping(key = AlexBotConstants.KEY.SERVICE_SELECTOR)
	public void serviceSelect(InboxMessage inboxMessage, StringMatcher matcher) {

		switch (inboxMessage.getMessage()) {
		case "1":
			reply("You Selected ServiceA");
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		case "2":
			reply("You Selected ServiceB");
			ChatPromise x = require(AlexBotConstants.KEY.CONFIRM_GENDER);
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
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		case "4":
			assignToDefaultAgent();
			break;
		case "5":
			send(new OutboxMessage().message("You Selected Rcpt Download"));
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		default:
			reply("You Selected Nothing, Please select again");
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.CONFIRM_GENDER)
	public void onLinkCivilId(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("M. Indian, F.Female X.Cancel");
		next(AlexBotConstants.KEY.CONFIRM_GENDER_ONSELECT);
	}

	@ChatMapping(key = AlexBotConstants.KEY.CONFIRM_GENDER_ONSELECT)
	public void genderSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage()) {
		case "M":
			reply("You Selected Male");
			resolve(AlexBotConstants.KEY.CONFIRM_GENDER);
			break;
		case "F":
			reply("You Selected Female");
			resolve(AlexBotConstants.KEY.CONFIRM_GENDER);
			break;
		case "X":
			reply("You Selected Cancel");
			reject(AlexBotConstants.KEY.CONFIRM_GENDER);
			break;
		default:
			reply("You Selected NoGender, Please select again");
			next(AlexBotConstants.KEY.CONFIRM_GENDER_ONSELECT);
			break;
		}
	}
}
