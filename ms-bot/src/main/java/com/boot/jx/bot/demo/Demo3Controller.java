
package com.boot.jx.bot.demo;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "app", "demo", "sandbox", "customer" })
public class Demo3Controller extends CommonBotController {

    private static final String CURRENT_DEMO = "current_menu";
    @Autowired
    private ChatContext chatContext;

    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
    	reply(new OutboxMessage().template("menu-4").put("name", chatContext.getContact().getName()));
	// reply(new OutboxMessage().template("menu-4-1-email-1-ask").put("name",
	// chatContext.getContact().getName()));
	next("menu-4-1-email-onselect");
    }

    @ChatMapping(key = "menu-4-1-email-onselect")
    public void emailAsk(InboxMessage inboxMessage, StringMatcher matcher) {
	String regex = "^(.+)@(.+)$";
	matcher = ArgUtil.is(matcher) ? matcher : new StringMatcher(inboxMessage.getMessage());

	if (ArgUtil.is(matcher) && matcher.match(Pattern.compile(regex))) {
	    reply(new OutboxMessage().template("menu-4-1-email-2-0"));
	    // reply(new OutboxMessage().template("menu-4-2-pan-1-ask"));
	    next("menu-4-2-pan-onselect");
	} else {
	    reply("Enter valid email");
	    next("menu-4-1-email-onselect");
	}
    }

    @ChatMapping(key = "menu-4-2-pan-onselect")
    public void panOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
 	switch (inboxMessage.getMessage().toLowerCase()) {

	case "h":
	case "need help":
	case "need help?":
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;

	default:
	    if (inboxMessage.getMessage().length() == 12) {
		reply(new OutboxMessage().template("menu-4-3-pan-2-0"));
		// reply(new OutboxMessage().template("menu-4-4-date-1-ask"));
		next("menu-4-2-date-onselect");
	    } else {
		reply(new OutboxMessage().template("menu-4-3-pan-1-nok"));
		next("menu-4-2-pan-onselect");
	    }
	    break;
	}

    }

    @ChatMapping(key = "menu-4-2-date-onselect")
    public void dateOnSelect(InboxMessage inboxMessage, StringMatcher matcher) throws InterruptedException {  
	switch (inboxMessage.getMessage().toLowerCase()) {

	case "h":
	case "need help":
	case "need help?":
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;

	default:
	    if (inboxMessage.getMessage().length() == 8) {
		reply(new OutboxMessage().template("menu-4-5-payment-0"));
		// reply(new OutboxMessage().template("menu-4-5-payment-1"));
		Thread.sleep(2000);

		reply(new OutboxMessage().template("menu-4-6-payment-0"));
		// reply(new OutboxMessage().template("menu-4-6-payment-1-done"));
		// Thread.sleep(2000);

		// reply(new OutboxMessage().template("menu-4-7-welcome"));
		chatContext.sessionData().data().remove(CURRENT_DEMO);
	    } else {
		reply(new OutboxMessage().template("menu-4-4-date-1-nok"));
		next("menu-4-2-date-onselect");
	    }
	    break;
	}

    }

    @ChatMapping(key = "menu-4-8-talk2agent")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	chatContext.sessionData().data().remove(CURRENT_DEMO);
	commonTransferToAgent(inboxMessage, matcher);
    }
}
