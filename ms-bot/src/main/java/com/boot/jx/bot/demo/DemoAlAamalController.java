package com.boot.jx.bot.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
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
	 private ChatContext chatContext;
	
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
	
	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
    	reply(new OutboxMessage().template("alaamal_menu").put("name", chatContext.contact().getName()));
    	next("menu-0-onselect");

    }
	
    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "menu", pattern = "^menu$")
    private void showDemoMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	String prevMenu = ArgUtil.parseAsString(chatContext.session().get(CURRENT_DEMO), Constants.BLANK).toUpperCase();

	if (ArgUtil.is(prevMenu)) {
	    switch (prevMenu) {	   
	    case "1":
	    case "JIHAN":
	    jihanController.start(inboxMessage, matcher);
		return;
	    case "2":
	    case "ALMARSA":
	    marsaController.start(inboxMessage, matcher);
		return;
	    case "3":
	    case "DUKKANBURGER"	:
	    dukkanBurController.start(inboxMessage, matcher);
		return;
	    case "4":
	    case "GREENSKWT"	:
	    greensKwtController.start(inboxMessage, matcher);
		return;
	    case "5":
	    case "JAIPUR"	:
	    jaipurController.start(inboxMessage, matcher);
		return;
	    case "8":
	    case "CAFEBAZZA"	:
	    cafeBazzaController.start(inboxMessage, matcher);
		return;
	    case "9":
	    case "dietcaredlv"	:
	    	dietCareDlvController.start(inboxMessage, matcher);
		return;
	    case "10":
	    case "DIETCARECLINIC"	:
	    	routeSession("dietcareclinicbot");
	    	//dietCareController.start(inboxMessage, matcher);
		return;
	    default:
		break;
	    }
	}
	reply(new OutboxMessage().template("alaamal-menu").put("name", chatContext.contact().getName()));
	next("menu-0-onselect");
    }
    @ChatMapping(key = "menu-0-onselect")
    public void menu1OnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (toReplyEnum(inboxMessage)) {
	case "menu":
	    showDemoMenu(inboxMessage, matcher);
	case "jihan":
	case "1":
	    chatContext.session().put(CURRENT_DEMO, "1");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "almarsa":
	case "2":
	    chatContext.session().put(CURRENT_DEMO, "2");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "dukkanburger":
	case "3":
	    chatContext.session().put(CURRENT_DEMO, "3");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "arabi":
	case "4":
	    chatContext.session().put(CURRENT_DEMO, "4");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "greenskwt":
	case "5":
	    chatContext.session().put(CURRENT_DEMO, "5");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "jaipur":
	case "6":
	    chatContext.session().put(CURRENT_DEMO, "6");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "leroyal":
	case "7":
	    chatContext.session().put(CURRENT_DEMO, "7");
	    showDemoMenu(inboxMessage, matcher);
	    break;  
	case "cafebazza":
	case "8":
	    chatContext.session().put(CURRENT_DEMO, "8");
	    showDemoMenu(inboxMessage, matcher);
	    break;
	case "dietcaredlv":
	case "9":
	    chatContext.session().put(CURRENT_DEMO, "9");
	    showDemoMenu(inboxMessage, matcher);
	    break; 
	case "dietcareclinic":
	case "10":
	    chatContext.session().put(CURRENT_DEMO, "10");
	    showDemoMenu(inboxMessage, matcher);
	    break;   
	case "TALK TO AGENT":
	case "TALKTOAGENT":
	case "#":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    //handleGlobalOptionOrInvalidAndNext(inboxMessage, matcher, "menu-0-onselect");
	    return;
	}
    }
    
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
    	routeSession("agendsk");
    }
    
    
    public String toReplyEnum(InboxMessage inboxMessage) {
    	String codeValue = inboxMessage.form().get(REPLY_ID)==null?inboxMessage.getMessage():
	    	 inboxMessage.form().get(REPLY_ID).toString();
    	if(ArgUtil.is(codeValue)) {
    		codeValue=codeValue.toLowerCase().trim(); 
    	}
    	System.out.println("codeValue :"+codeValue);
    	return codeValue ;
    }

}
