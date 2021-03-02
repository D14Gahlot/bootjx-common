
package com.boot.jx.bot.alex;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot")
public class DemoController extends ChatController {

	@Autowired
	private ChatContext chatContext;

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "menu", pattern = "^menu$")
	private void showMenu(InboxMessage inboxMessage, StringMatcher matcher) {
		if (chatContext.getSession().data().containsKey("isMenu1Shown")) {
			reply(new OutboxMessage().template("menu-1").put("name", chatContext.getContact().getName()));
			next("menu-1-onselect");
			chatContext.getSession().data().put("isMenu1Shown", true);
		} else {
			reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
			next("menu-2-onselect");
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "hi", pattern = "^HI$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		showMenu(inboxMessage, matcher);
	}

	@ChatMapping(key = AlexBotConstants.KEY.PING, pattern = "^PING$")
	public void onPing(InboxMessage inboxMessage, StringMatcher matcher) {
		reply("PING");
	}

	@ChatMapping(key = "menu-1-onselect")
	public void menu1OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (inboxMessage.getMessage().toLowerCase()) {
		case "menu":
		case "Banking":
		case "2":
			showMenu(inboxMessage, matcher);
			break;
		case "talktoagent":
		case "1":
			transferToAgent(inboxMessage, matcher);
			break;
		default:
			handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-1-onselect");
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
			transferToAgent(inboxMessage, matcher);
			break;
		default:
			handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-2-onselect");
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
			handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "more-onselect");
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
			reply("Thanks");
			break;
		case "not happy":
		case "nothappy":
		case "no":
		case "n":
		case "2":
			transferToAgent(inboxMessage, matcher);
			break;
		default:
			handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "feedback-onselect");
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

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
		if (!handleGlobalOption(inboxMessage, matcher)) {
			showMenu(inboxMessage, matcher);
		}
	}

	private boolean handleGlobalOptionOrInvalidAndNext(InboxMessage inboxMessage, StringMatcher matcher,
			String nextHandler) {
		if (!handleGlobalOptionOrInvalid(inboxMessage, matcher)) {
			next(nextHandler);
			return false;
		}
		return true;
	}

	private boolean handleGlobalOptionOrInvalid(InboxMessage inboxMessage, StringMatcher matcher) {
		if (!handleGlobalOption(inboxMessage, matcher)) {
			reply(new OutboxMessage().template("invalid-options"));
			return false;
		}
		return true;
	}

	private boolean handleGlobalOption(InboxMessage inboxMessage, StringMatcher matcher) {
		String thisMessage = inboxMessage.getMessage().toLowerCase().replace(" ", "");

		if (ArgUtil.is(inboxMessage.getTags()) && ArgUtil.is(inboxMessage.getTags().getCategories())) {
			if (inboxMessage.getTags().getCategories().indexOf("today-credits") > -1) {
				reply(new OutboxMessage().template("today-credits").put("name", chatContext.getContact().getName())
						.attachment(new Attachment().mediaURL("http://www.africau.edu/images/default/sample.pdf")
								.mediaType(File.FileType.DOCUMENT.toString())));
				next("more-onselect");
				return true;
			} else if (inboxMessage.getTags().getCategories().indexOf("today-debits") > -1) {
				reply(new OutboxMessage().template("today-debits").put("name", chatContext.getContact().getName())
						.attachment(new Attachment().mediaURL("http://www.africau.edu/images/default/sample.pdf")
								.mediaType(File.FileType.DOCUMENT.toString())));
				next("more-onselect");
				return true;
			} else if (inboxMessage.getTags().getCategories().indexOf("today-trnx") > -1) {
				reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName())
						.attachment(new Attachment().mediaURL("http://www.africau.edu/images/default/sample.pdf")
								.mediaType(File.FileType.DOCUMENT.toString())));
				next("more-onselect");
				return true;
			} else if (inboxMessage.getTags().getCategories().indexOf("menu") > -1
					|| thisMessage.equalsIgnoreCase("menu")) {
				showMenu(inboxMessage, matcher);
				return true;
			} else if (inboxMessage.getTags().getCategories().indexOf("transfer-to-agent") > -1
					|| thisMessage.equalsIgnoreCase("#") || thisMessage.equalsIgnoreCase("TalkToAgent")) {
				transferToAgent(inboxMessage, matcher);
				return true;
			}
		}
		switch (thisMessage) {
		case "menu":
			showMenu(inboxMessage, matcher);
			return true;
		case "#":
		case "TalkToAgent":
			transferToAgent(inboxMessage, matcher);
			return true;
		case "*":
		case "exit":
		case "/exit_chat":
			reply(new OutboxMessage().template("feedback"));
			next("feedback-onselect");
			return true;
		default:
			// System.out.println("NO Match");
			break;
		}

		return false;
	}

}
