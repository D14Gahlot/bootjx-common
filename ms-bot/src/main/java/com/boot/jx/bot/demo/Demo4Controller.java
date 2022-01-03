
package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.UserClient.Channel;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "app", "demo", "sandbox" })
public class Demo4Controller extends CommonBotController {

    private static final String CURRENT_DEMO = "current_menu";
    @Autowired
    private ChatContext chatContext;
    
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageStore messageStore;
    
    @Autowired
    private SessionStore sessionStore;



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
		//this.transferToAgent(inboxMessage,matcher);
		send();
		next("menu-5-dept-onselect");
	    } else {
		reply(new OutboxMessage().template("menu-5-dept-time-2"));
		//this.transferToAgent(inboxMessage,matcher);
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
    	boolean isNowInRange=false;
    	try {
    		 LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));
    	     String isoTime = now.format(DateTimeFormatter.ISO_TIME); 
    	    LocalTime currTime = LocalTime.parse(isoTime,DateTimeFormatter.ISO_TIME);
            LocalTime start = LocalTime.of( 8 , 0 );
            LocalTime stop = LocalTime.of( 21 , 0 );
           
             isNowInRange = ( ! currTime.isBefore( start ) ) && currTime.isBefore( stop ) ;
    		
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return isNowInRange;
    }
    
    public void send() {
    	
    	OutboxMessage outboxMessage = new OutboxMessage();
    	
    	outboxMessage.template().setCode("sales_inquiry");
    	//outboxMessage.setMessage("Hi, a prospect {{contact.name}}, using {{contact.phone}}, \\nhas reached out to us. \\nPlease log into customer.mehery.com and respond to the customer ASAP.\\n\",");
    	outboxMessage.contact().type(ContactType.WHATSAPP);
    	outboxMessage.contact().setLane("918828218374");
    	//outboxMessage.contact().setPhone("96551780410");
    	outboxMessage.contact().setCsid("919619203759");
    	send(outboxMessage);
    	
    	OutboxMessage outboxMessage1 = new OutboxMessage();
    	outboxMessage1.template().setCode("sales_inquiry");
    	outboxMessage1.contact().type(ContactType.WHATSAPP);
    	outboxMessage1.contact().setLane("918828218374");
    	//outboxMessage.contact().setPhone("96551780410");
    	outboxMessage1.contact().setCsid("96551780410");
    	send(outboxMessage1);
    	
    	
    }
}
