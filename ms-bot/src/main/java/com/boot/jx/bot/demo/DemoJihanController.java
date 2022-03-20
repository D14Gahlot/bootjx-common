package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_jihanbot" })
public class DemoJihanController extends CommonBotController {
	
	public static final String REPLY_ID = "reply_id";	
	
	public static final String TALK_TO_AGENT = "";	

	  
	    @Autowired
	    private ChatContext chatContext;
	
	    @Autowired
	    PMEnvironment pmEnvironment;
	   
	    @Autowired
		MongoTemplate mongoTemplate;
	    
	    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
	   	reply(new OutboxMessage().template("jd_welcome_msg").put("name", chatContext.contact().getName()));
	    	next("select-language");
	    }
	    
	    
	    @ChatMapping(key = "select-language")
	    public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    String lang =toReplyEnum(inboxMessage); 
	 	  if(!ArgUtil.is(lang)) {
	 		 lang= ArgUtil.parseAsString(chatContext.contact().getLang());
	 	  }
	 	 
	     //if(!timeCheck()) {
	    //	 reply(new OutboxMessage().template("working_hours_update"));
	    // }else {
		    if(lang.equalsIgnoreCase("english") || (lang!=null && lang.equalsIgnoreCase("en"))) {
		    	 chatContext.contact().setLang("en");
		    	 chatContext.commitContact();
		    	 reply(new OutboxMessage().template("jd_question"));
		    	 next("select-question");
		    }else if(lang.equalsIgnoreCase("العربية") || (lang!=null &&  lang.equalsIgnoreCase("ar"))) {
		    	 chatContext.contact().setLang("ar");
		    	 chatContext.commitContact();
		    	reply(new OutboxMessage().template("jd_question"));
		    	 next("select-question");
		    } else{
		    	chatContext.contact().setLang("en");
		    	 chatContext.commitContact();
		    	reply(new OutboxMessage().template("jd_question"));
		    	next("select-question");
		    }
	     //} 
	    }
	  
	    @ChatMapping(key = "select-question")
	    public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    checkValue("jd_question",toReplyEnum(inboxMessage));
	    switch(toReplyEnum(inboxMessage)) {
		case "jd_new_order":
			    reply(new OutboxMessage().template("jd_new_order_ans"));
			    break;
		case "jd_edit_order":
		case "jd_ord_follow_up":
		case "jd_reserv":
		case "jd_edit_reserv":	
			 this.transferToAgent(inboxMessage, matcher);	
			 break; 
		case "jd_location":
		    reply(new OutboxMessage().template("jd_our_location_ans"));
		    next("our_location");
		    break;
		case "jd_working_hrs":
		    reply(new OutboxMessage().template("jd_working_hrs_ans"));
		    next("working_hours");
		    break;
		case "for_catering":
		    reply(new OutboxMessage().template("jd_catering_ans"));
		    next("for_catering");
		    break;
		case "help":
		    reply(new OutboxMessage().template("jd_help_ans"));
		    next("help");
		    break; 
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break;  
		case "#":
			  this.transferToAgent(inboxMessage, matcher);
		    break;   
		default :
		    reply(new OutboxMessage().template("jd_invalid_option"));
		    reply(new OutboxMessage().template("jd_question"));
		    break; 
	   
	    }
	   }
	  
	@ChatMapping(key = "dc_location_link_timing")
	public void locationLinkTiming(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break; 
		case "#"	:
			this.transferToAgent(inboxMessage, matcher);
		    break;   
		default :
			reply(new OutboxMessage().template("invalid_input_response_std"));
			reply(new OutboxMessage().template("dc_location_option"));
			next("clinics-onselect");	
		    break;     
	
		}
	}
		   
		
	    @ChatMapping(key = "jd_cs_to_contact")
	    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	    	routeSession("agendsk");
	    }
	    
	    public void goToMainMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	    	reply(new OutboxMessage().template("jd_question"));
			next("select-service-rechoose");
	    }
	    
	    
	    
	    @ChatMapping(key = "jd_invalid_input")
	    public void invalidinput(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (toReplyEnum(inboxMessage)) {
	    	case "*":
				reply(new OutboxMessage().template("select-question"));
				next("select-question");
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;   
			default :
				reply(new OutboxMessage().template("dc_services_rechoose"));
				next("select-question");
			    break;    
	    	}
	    	}
	    
	    
	    public boolean timeCheck() {
	    	SafeKeyHashMap<Object> globalVars = pmEnvironment.local().globalVars();
	    	String officeTimeFlag = globalVars.keyEntry("office_time_msg").asString();
	    	boolean isNowInRange = false;
	    	if(officeTimeFlag.equalsIgnoreCase("true")) {
	    		String startTime =globalVars.keyEntry("office_start_time").asString();
	    		String endTime =globalVars.keyEntry("office_start_time").asString();
	    		
		    	try {
		    	    LocalTime now = LocalTime.now(ZoneId.of("Asia/Kuwait"));
		    	    String isoTime = now.format(DateTimeFormatter.ISO_TIME);
		    	    LocalTime currTime = LocalTime.parse(isoTime, DateTimeFormatter.ISO_TIME);
		    	    LocalTime start = LocalTime.of(Integer.valueOf(startTime), 0);
		    	    LocalTime stop = LocalTime.of(Integer.valueOf(endTime), 0);
		    	    
		    	    isNowInRange = (!currTime.isBefore(start)) && currTime.isBefore(stop);
	
		    	} catch (Exception e) {
		    	    e.printStackTrace();
		    	}
	    	}else {
	    		isNowInRange=true;
	    	}
	    	return isNowInRange;
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
	    @SuppressWarnings("unchecked")
	    private Boolean checkValue(String tmplCode,String userInput) {
	    	Boolean booValue=false;
	    	 String lang= ArgUtil.parseAsString(chatContext.contact().getLang());
	    	Query query = new Query();
			query.addCriteria(Criteria.where("code").is(tmplCode).and("lang").is(lang));
			HSMTemplateDoc hsmTmpl =mongoTemplate.findOne(query,HSMTemplateDoc.class,"DICT_HSM_TEMPLATES");
			if(ArgUtil.is(hsmTmpl)) {
				Map<String, Object> options = hsmTmpl.getOptions();
				if(ArgUtil.is(options)) {
					List<Map<String, Object>> extTemCom =(List<Map<String, Object>>) options.get("buttons");
					 booValue = extTemCom.stream().anyMatch(map -> map.containsValue(userInput));
				}
			}
	    	return booValue;
	    }
}
