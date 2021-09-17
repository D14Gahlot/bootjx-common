
package com.boot.jx.bot.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "app", "demo", "sandbox" })
public class Demo2Controller extends ChatController {

    private static final String CURRENT_DEMO = "current_menu";
    @Autowired
    private ChatContext chatContext;

    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("menu-3").put("name", chatContext.getContact().getName()));
	// reply(new OutboxMessage().template("menu-3-1").put("name",
	// chatContext.getContact().getName()));
	next("menu-3-1-onselect");
    }

    @ChatMapping(key = "menu-3-1-onselect")
    public void option1(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("menu-3-1-resp").put("name", chatContext.getContact().getName())
		.attachment(new Attachment().mediaURL(
			"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/zen-residence-compressed.pdf")
			.mediaCaption("Floor Plan").mediaType(FileType.DOCUMENT.toString())));
	// reply(new OutboxMessage().template("menu-3-2-1"));
	next("menu-3-2-1-onselect");
    }

    @ChatMapping(key = "menu-3-2-1-onselect")
    public void option2(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toLowerCase()) {
	case "yes":
	case "y":
	    reply(new OutboxMessage().template("menu-3-2-2-yes"));
	    next("menu-3-2-2-onselect");
	    break;
	default:
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	}

    }

    @ChatMapping(key = "menu-3-2-2-onselect")
    public void option3(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toLowerCase()) {
	case "yes":
	case "y":
	    reply(new OutboxMessage().template("menu-3-3-1").attachment(new Attachment().mediaURL(
		    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/zen-residence-compressed.pdf")
		    .mediaCaption("Amenities").mediaType(FileType.DOCUMENT.toString())));
	    next("menu-3-3-1-onselect");
	    break;
	default:
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	}
    }

    @ChatMapping(key = "menu-3-3-1-onselect")
    public void option4(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toLowerCase()) {
	case "yes":
	case "y":
	    reply(new OutboxMessage().template("menu-3-3-2-yes"));
	default:
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	}
    }

    @ChatMapping(key = "menu-4-8-talk2agent")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	try {
	    chatContext.getSession().data().remove(CURRENT_DEMO);
	    InboxMessage agentAssignResp = assignToAgent().getResult();
	    if (ArgUtil.is(agentAssignResp.session().getAgent())) {
		reply(new OutboxMessage().template("menu-4-8-talk2agent"));
	    } else {
		reply("All the agents are busy or online, we will connect you whenever someone is available.");
	    }
	} catch (Exception e) {
	    reply("Some Tech Issues");
	}
    }
}
