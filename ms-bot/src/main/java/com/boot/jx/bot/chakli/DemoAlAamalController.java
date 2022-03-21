package com.boot.jx.bot.chakli;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli" })
public class DemoAlAamalController extends CommonBotController {

    private static final String CURRENT_DEMO = "current_menu";

    public static final String REPLY_ID = "reply_id";

    @Autowired
    Demo5Controller dietCareController;

    @Autowired
    DemoJihanController jihanController;

    @Autowired
    DemoAlMarsaController marsaController;

    @Autowired
    DemoDukkanBurController dukkanBurController;

    @Autowired
    DemoGreenskwtController greensKwtController;

    @Autowired
    DemoJaipurController jaipurController;

    @Autowired
    DemoDietcaredlvController dietCareDlvController;

    @Autowired
    DemoCafeBazzaController cafeBazzaController;

    @Autowired
    DemoArabiController arabiController;

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
    context().session().remove(CURRENT_DEMO);
    reply(new OutboxMessage().template("alaamal_menu").put("name", context().contact().getName()));
	next("menu-0-onselect");
    }

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "menu", pattern = "^menu$")
    private void showDemoMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	String prevMenu = ArgUtil.parseAsString(context().session().get(CURRENT_DEMO), Constants.BLANK).toUpperCase();

	if (ArgUtil.is(prevMenu)) {
	    switch (prevMenu) {
	    case "1":
	    case "JIHAN":
		routeSession("jihanbot");
		return;
	    case "2":
	    case "ALMARSA":
		routeSession("marsabot");
		return;
	    case "3":
	    case "DUKKANBURGER":
		routeSession("dukkanburgerbot");
		return;
	    case "4":
	    case "ARABI":
		routeSession("arabibot");
		return;
	    case "5":
	    case "GREENSKWT":
		routeSession("greenskwtbot");
		return;
	    case "6":
	    case "JAIPUR":
		routeSession("jaipurbot");
		return;
	    case "8":
	    case "CAFEBAZZA":
		routeSession("cafebazzabot");
		return;
	    case "9":
	    case "dietcaredlv":
		routeSession("cafebazzabot");
		return;
	    case "10":
	    case "DIETCARECLINIC":
		routeSession("dietcareclinicbot");
		//dietCareController.start(inboxMessage, matcher);
		return;
	    default:
		break;
	    }
	}
	reply(new OutboxMessage().template("alaamal-menu").put("name", context().contact().getName()));
	next("menu-0-onselect");
    }

    @ChatMapping(key = "menu-0-onselect")
    public void menu1OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (toReplyEnum(inboxMessage)) {
	case "menu":
	    showDemoMenu(inboxMessage, matcher);
	case "jihan":
	case "1":
	    context().session().put(CURRENT_DEMO, "1");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "almarsa":
	case "2":
	    context().session().put(CURRENT_DEMO, "2");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "dukkanburger":
	case "3":
	    context().session().put(CURRENT_DEMO, "3");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "arabi":
	case "4":
	    context().session().put(CURRENT_DEMO, "4");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "greenskwt":
	case "5":
	    context().session().put(CURRENT_DEMO, "5");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "jaipur":
	case "6":
	    context().session().put(CURRENT_DEMO, "6");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "leroyal":
	case "7":
	    context().session().put(CURRENT_DEMO, "7");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "cafebazza":
	case "8":
	    context().session().put(CURRENT_DEMO, "8");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "dietcaredlv":
	case "9":
	    context().session().put(CURRENT_DEMO, "9");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "dietcareclinic":
	case "10":
	    context().session().put(CURRENT_DEMO, "10");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "TALK TO AGENT":
	case "TALKTOAGENT":
	case "#":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    // handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-0-onselect");
	    return;
	}
    }

    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	routeSession("agendsk");
    }

    public String toReplyEnum(InboxMessage inboxMessage) {
	String codeValue = inboxMessage.form().get(REPLY_ID) == null ? inboxMessage.getMessage()
		: inboxMessage.form().get(REPLY_ID).toString();
	if (ArgUtil.is(codeValue)) {
	    codeValue = codeValue.toLowerCase().trim();
	}
	System.out.println("codeValue :" + codeValue);
	return codeValue;
    }
    
    public void next(String key) {
		String handelrName = key;
		if (ArgUtil.is(this.controllerName)) {
			handelrName = this.controllerName + "#" + key;
		}
		context().meta().setNextHandler(handelrName);
	}

}
