
package com.boot.jx.bot.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "app", "demo", "sandbox", "customer" ,"chakli"})
public class Demo1Controller extends CommonBotController {

    private static final String CURRENT_DEMO = "current_menu";
    @Autowired
    private ChatContext chatContext;

    @Autowired
    Demo3Controller demo4Controller;

    @Autowired
    Demo2Controller demo3Controller;

    @Autowired
    Demo4Controller demo5Controller;
    
    @Autowired
    Demo5Controller demo6Controller;

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "menu", pattern = "^menu$")
    private void showDemoMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	String prevMenu = ArgUtil.parseAsString(chatContext.sessionData().data().get(CURRENT_DEMO), Constants.BLANK)
		.toLowerCase();

	if (ArgUtil.is(prevMenu)) {
	    switch (prevMenu) {
	    case "1":
		reply(new OutboxMessage().template("menu-1").put("name", chatContext.getContact().getName()));
		next("menu-1-onselect");
		return;
	    case "2":
		reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
		next("menu-2-onselect");
		return;
	    case "3":
		demo3Controller.start(inboxMessage, matcher);
		return;
	    case "4":
		demo4Controller.start(inboxMessage, matcher);
		return;
	    case "5":
		demo5Controller.start(inboxMessage, matcher);
		return;
	    default:
		break;
	    }
	}else if(AppContextUtil.getTenant().equalsIgnoreCase("chakli")) {
		demo6Controller.start(inboxMessage, matcher);
		return;
	}
	reply(new OutboxMessage().template("menu-0").put("name", chatContext.getContact().getName()));
	next("menu-0-onselect");
    }

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
	showDemoMenu(inboxMessage, matcher);
    }

    @ChatMapping(key = AlexBotConstants.KEY.PING, pattern = "^PING$")
    public void onPing(InboxMessage inboxMessage, StringMatcher matcher) {
	reply("PING");
    }

    @ChatMapping(key = "menu-0-onselect")
    public void menu1OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toUpperCase()) {
	case "menu":
	    showDemoMenu(inboxMessage, matcher);
	case "ASSET MANAGEMENT":
	case "ASSETMANAGMENT":
	case "1":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "1");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "RETAIL":
	case "2":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "2");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "REALSTATE":
	case "PROPERTYBKCMUMBAI":
	case "3":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "3");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "NEWACCOUNTOPEN":
	case "4":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "4");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "SALES INQUIRY":
	case "SALES":
	case "5":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "5");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "TALK TO AGENT":
	case "TALKTOAGENT":
	case "#":
	    transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-0-onselect");
	    return;
	}
    }

    @ChatMapping(key = "menu-1-onselect")
    public void menu2OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toLowerCase()) {
	case "1":
	    reply(new OutboxMessage().template("today-credits").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect");
	    break;
	case "2":
	    reply(new OutboxMessage().template("today-debits").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect");
	    break;
	case "3":
	    reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect");
	    break;
	case "4":
	    reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://www.mehery.com/wp-content/uploads/2021/02/Screenshot-2021-02-03-at-10.12.29-PM.png")
			    .mediaType(FileType.IMAGE.toString())));
	    next("more-onselect");
	    break;
	case "*":
	    reply(new OutboxMessage().template("feedback").put("name", chatContext.getContact().getName()));
	    next("feedback-onselect");
	    break;
	case "#":
	    transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-1-onselect");
	    break;
	}
    }

    /** menu2 for Retails **/

    @ChatMapping(key = "menu-2-onselect")
    public void menu3OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toLowerCase()) {
	case "1":
	    reply(new OutboxMessage().template("menu-2-today-offers").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect-menu-2");
	    break;
	case "2":
	    reply(new OutboxMessage().template("menu-2-weekly-offers").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect-menu-2");
	    break;
	case "3":
	    reply(new OutboxMessage().template("menu-2-retail-branch").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
			    .mediaType(FileType.DOCUMENT.toString())));
	    next("more-onselect-menu-2");
	    break;
	case "4":
	    reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName())
		    .attachment(new Attachment().mediaURL(
			    "https://www.mehery.com/wp-content/uploads/2021/02/Screenshot-2021-02-03-at-10.12.29-PM.png")
			    .mediaType(FileType.IMAGE.toString())));
	    next("more-onselect-menu-2");
	    break;
	case "*":
	    reply(new OutboxMessage().template("feedback").put("name", chatContext.getContact().getName()));
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
	switch (inboxMessage.getMessage().toUpperCase()) {
	case "YES":
	case "Y":
	case "1":
	    reply(new OutboxMessage().template("menu-1").put("name", chatContext.getContact().getName()));
	    next("menu-1-onselect");
	    break;
	case "NO":
	case "N":
	case "2":
	    reply(new OutboxMessage().template("feedback").put("name", chatContext.getContact().getName()));
	    next("feedback-onselect");
	    break;
	default:
	    handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "more-onselect");
	    return;
	}
    }

    @ChatMapping(key = "more-onselect-menu-2")
    public void moreonSelectRetailMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toUpperCase()) {
	case "YES":
	case "Y":
	case "1":
	    reply(new OutboxMessage().template("menu-2").put("name", chatContext.getContact().getName()));
	    next("menu-2-onselect");
	    break;
	case "NO":
	case "N":
	case "2":
	    reply(new OutboxMessage().template("feedback").put("name", chatContext.getContact().getName()));
	    next("feedback-onselect");
	    break;
	default:
	    handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "more-onselect");
	    return;
	}
    }

    @ChatMapping(key = "feedback-onselect")
    public void feedback(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (inboxMessage.getMessage().toUpperCase()) {
	case "HAPPY":
	case "YES":
	case "Y":
	case "1":
	    botScore(10);
	    reply("Thanks");
	    resolveSession();
	    closeSession();
	    break;
	case "NOT HAPPY":
	case "NOTHAPPY":
	case "NO":
	case "N":
	case "2":
	    botScore(0);
	    transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "feedback-onselect");
	    return;
	}
    }

    @ChatMapping(key = "transfer-to-agent")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	chatContext.sessionData().data().remove(CURRENT_DEMO);
	commonTransferToAgent(inboxMessage, matcher);
    }

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
	if (!handleGlobalOption(inboxMessage, matcher)) {
	    showDemoMenu(inboxMessage, matcher);
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
	String thisMessage = ArgUtil.nonEmpty(inboxMessage.getMessage(), Constants.BLANK).toUpperCase().replace(" ",
		"");

	if (ArgUtil.is(inboxMessage.getTags()) && ArgUtil.is(inboxMessage.getTags().getCategories())) {
	    if (inboxMessage.getTags().getCategories().indexOf("today-credits") > -1) {
		reply(new OutboxMessage().template("today-credits").put("name", chatContext.getContact().getName())
			.attachment(new Attachment().mediaURL(
				"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
				.mediaType(FileType.DOCUMENT.toString())));
		next("more-onselect");
		return true;
	    } else if (inboxMessage.getTags().getCategories().indexOf("today-debits") > -1) {
		reply(new OutboxMessage().template("today-debits").put("name", chatContext.getContact().getName())
			.attachment(new Attachment().mediaURL(
				"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
				.mediaType(FileType.DOCUMENT.toString())));
		next("more-onselect");
		return true;
	    } else if (inboxMessage.getTags().getCategories().indexOf("today-trnx") > -1) {
		reply(new OutboxMessage().template("today-trnx").put("name", chatContext.getContact().getName())
			.attachment(new Attachment().mediaURL(
				"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/sample-receipt/mehery-sample-template.pdf")
				.mediaType(FileType.DOCUMENT.toString())));
		next("more-onselect");
		return true;
	    } else if (inboxMessage.getTags().getCategories().indexOf("menu") > -1
		    || thisMessage.equalsIgnoreCase("menu")) {
		showDemoMenu(inboxMessage, matcher);
		return true;
	    } else if (inboxMessage.getTags().getCategories().indexOf("transfer-to-agent") > -1
		    || thisMessage.equalsIgnoreCase("#") || thisMessage.equalsIgnoreCase("TalkToAgent")) {
		transferToAgent(inboxMessage, matcher);
		return true;
	    }
	}
	switch (thisMessage) {
	case "MENU":
	    showDemoMenu(inboxMessage, matcher);
	    return true;

	case "/ASSETMANAGMENT":
	case "ASSETMANAGMENT":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "1");
	    showDemoMenu(inboxMessage, matcher);
	    return true;

	case "/RETAIL":
	case "RETAIL":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "2");
	    showDemoMenu(inboxMessage, matcher);
	    return true;

	case "/PROPERTYBKCMUMBAI":
	case "REALSTATE":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "3");
	    showDemoMenu(inboxMessage, matcher);
	    return true;

	case "/NEWACCOUNTOPEN":
	case "NEWACCOUNTOPEN":
	    chatContext.sessionData().data().put(CURRENT_DEMO, "4");
	    showDemoMenu(inboxMessage, matcher);
	    return true;

	case "#":
	case "TALK TO AGENT":
	case "TALKTOAGENT":
	    transferToAgent(inboxMessage, matcher);
	    return true;
	case "*":
	case "EXIT":
	case "/EXIT_CHAT":
	    chatContext.sessionData().data().remove(CURRENT_DEMO);
	    reply(new OutboxMessage().template("feedback").put("name",
		    ArgUtil.nonEmpty(chatContext.getContact().getName(), "WhatsApp User")));
	    next("feedback-onselect");
	    return true;
	default:
	    // System.out.println("NO Match");
	    break;
	}

	return false;
    }

}
