
package com.boot.jx.bot.alex;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ContactCleanerService;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot")
public class DemoController extends ChatController {

	@Autowired
	private ChatContext chatContext;

	@Autowired
	private ContactCleanerService contactCleanerService;

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
		reply(new OutboxMessage().template("menu-1").put("name", chatContext.getContact().getName()));
		next("menu-1-onselect");
	}

	@ChatMapping(key = "menu-1-onselect")
	public void menu1OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toLowerCase()) {
		case "2":
			reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
			next("menu-2-onselect");
			break;
		case "1":
			transferToAgent(inboxMessage, matcher);
			break;
		default:
			reply("Invalid Option");
			next("menu-1-onselect");
			return;
		}
	}

	@ChatMapping(key = "menu-2-onselect")
	public void menu2OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toLowerCase()) {
		case "1":
			reply(new OutboxMessage().template("today-credits").put("name", chatContext.getContact().getName()));
			next("more-onselect");
			break;
		case "2":
			reply(new OutboxMessage().template("today-debits").put("name", chatContext.getContact().getName()));
			next("more-onselect");
			break;
		case "3":
			reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName()));
			next("more-onselect");
			break;
		case "*":
			reply(new OutboxMessage().template("feedback"));
			next("feedback-onselect");
			break;
		case "#":
		default:
			transferToAgent(inboxMessage, matcher);
			break;
		}
	}

	@ChatMapping(key = "more-onselect")
	public void moreonSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toLowerCase()) {
		case "yes":
		case "y":
		case "1":
			reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
			next("menu-2-onselect");
			break;
		case "no":
		case "n":
		case "2":
			reply(new OutboxMessage().template("feedback"));
			next("feedback-onselect");
			break;
		default:
			reply("Invalid Option");
			next("feedback-onselect");
			return;
		}
	}

	@ChatMapping(key = "feedback-onselect")
	public void feedback(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toLowerCase()) {
		case "happy":
		case "yes":
		case "y":
		case "1":
			reply("Thanks - Conversation Closed");
			break;
		case "not happy":
		case "nothappy":
		case "no":
		case "n":
		case "2":
			transferToAgent(inboxMessage, matcher);
			break;
		default:
			reply("Invalid Option");
			next("feedback-onselect");
			return;
		}
	}

	@ChatMapping(key = "transfer-to-agent")
	public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		try {
			InboxMessage agentAssignResp = assignToAgent().getResult();
			if (ArgUtil.is(agentAssignResp.session().getAgent())) {
				reply("One of our agent will attend you shortly");
			} else {
				reply("All agents are busy or online, we will connect you whenever someone is available.");
			}
		} catch (Exception e) {
			reply("Some Tech Issues");
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE, pattern = "^*$")
	public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
		if (ArgUtil.is(inboxMessage.getTags()) && ArgUtil.is(inboxMessage.getTags().getCategories())) {
			if (inboxMessage.getTags().getCategories().indexOf("today-credits") > -1) {
				reply(new OutboxMessage().template("today-credits").put("name", chatContext.getContact().getName()));
				next("more-onselect");
			} else if (inboxMessage.getTags().getCategories().indexOf("today-debits") > -1) {
				reply(new OutboxMessage().template("today-debits").put("name", chatContext.getContact().getName()));
				next("more-onselect");
			} else if (inboxMessage.getTags().getCategories().indexOf("today-trnx") > -1) {
				reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName()));
				next("more-onselect");
			} else if (inboxMessage.getTags().getCategories().indexOf("menu") > -1) {
				reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
				next("menu-2-onselect");
			} else if (inboxMessage.getTags().getCategories().indexOf("transfer-to-agent") > -1) {
				transferToAgent(inboxMessage, matcher);
			}
		}

	}

}
