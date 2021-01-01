
package com.boot.jax.bot.alex;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ContactCleanerService;
import com.boot.jx.postman.store.MessageStore;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "ALEX")
public class AccountVerifyController extends ChatController {

	@Autowired
	ChatContext chatContext;

	@Autowired
	MessageStore messageStore;

	@Autowired
	ContactCleanerService contactCleanerService;

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

	@ChatMapping(key = AlexBotConstants.KEY.MENU)
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
			send(new OutboxMessage().message("You Selected Rcpt Download"));
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		case "5":
			assignToAgent();
			break;
		default:
			reply("You Selected Nothing, Please select again");
			next(AlexBotConstants.KEY.SERVICE_SELECTOR);
			break;
		}
	}

}
