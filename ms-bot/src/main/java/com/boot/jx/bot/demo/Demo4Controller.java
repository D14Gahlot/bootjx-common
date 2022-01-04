
package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "app", "demo", "sandbox" })
public class Demo4Controller extends CommonBotController {

    private static final String CURRENT_DEMO = "current_menu";
    @Autowired
    private ChatContext chatContext;



    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("menu-5").put("name", chatContext.getContact().getName()));
	next("menu-5-dept");
    }

    @ChatMapping(key = "menu-5-dept")
    public void deptAsk(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("menu-5-dept"));
	next("menu-5-dept-onselect");
    }

    @ChatMapping(key = "menu-5-dept-onselect")
    public void panOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {

	switch (inboxMessage.getMessage().toLowerCase()) {

	case "*":
	    reply(new OutboxMessage().template("feedback").put("name", chatContext.getContact().getName()));
	    next("feedback-onselect");
	    break;
	case "#":
	    transferToAgent(inboxMessage, matcher);
	    break;
	case "h":
	case "need help":
	case "need help?":
	    this.botScore(0);
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    if (timeCheck()) {
		reply(new OutboxMessage().template("menu-5-dept-time-1"));
		send();
		next("menu-5-dept-onselect");
	    } else {
		reply(new OutboxMessage().template("menu-5-dept-time-2"));
		send();
		next("menu-5-dept-onselect");
	    }
	    break;
	}

    }

    @ChatMapping(key = "menu-4-8-talk2agent")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	chatContext.sessionData().data().remove(CURRENT_DEMO);
	commonTransferToAgent(inboxMessage, matcher);
    }

    public boolean timeCheck() {
	boolean isNowInRange = false;
	try {
	    LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
	    String isoTime = now.format(DateTimeFormatter.ISO_TIME);
	    LocalTime currTime = LocalTime.parse(isoTime, DateTimeFormatter.ISO_TIME);
	    LocalTime start = LocalTime.of(8, 0);
	    LocalTime stop = LocalTime.of(21, 0);

	    isNowInRange = (!currTime.isBefore(start)) && currTime.isBefore(stop);

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return isNowInRange;
    }

    public void send() {
    	Map<String,Object> data = new HashMap<String,Object>();
    	data.put("name", chatContext.getContact().getName());
    	data.put("phone", chatContext.getContact().getPhone());
    	System.out.println("data { }:"+data);
    	String templateCode="sales_inquiry_alert";
    	String lane="918828218374";
    	if (ArgUtil.is(AppContextUtil.getTenant())
    			&& !AppContextUtil.getTenant().equalsIgnoreCase("demo")){
    		lane="917304856205";
    	}
    	
    	OutboxMessage outboxMessage1 = new OutboxMessage();
    	outboxMessage1.hsm().setCode(templateCode);
    	outboxMessage1.contact().type(ContactType.WHATSAPP);
    	outboxMessage1.contact().setLane(lane);
    	//alert phone number
    	outboxMessage1.contact().setCsid("96551780410");
    	outboxMessage1.data(data);
    	send(outboxMessage1);
    	
    	OutboxMessage outboxMessage = new OutboxMessage();
    	outboxMessage.hsm().setCode(templateCode);
    	outboxMessage.contact().type(ContactType.WHATSAPP);
    	outboxMessage.contact().setLane(lane);
    	//alert phone number
    	outboxMessage.contact().setCsid("919619203759");
    	outboxMessage.data(data); 
    	send(outboxMessage);
    	
    	OutboxMessage outboxMessage2 = new OutboxMessage();
    	outboxMessage2.hsm().setCode(templateCode);
    	outboxMessage2.contact().type(ContactType.WHATSAPP);
    	outboxMessage2.contact().setLane(lane);
    	//alert phone number
    	outboxMessage2.contact().setCsid("918587874877");
    	outboxMessage2.data(data); 
    	send(outboxMessage2);
    	
    }
}
