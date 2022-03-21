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
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

//@BotController(name = "DemoBot", code = { "chakli" })
@BotController(name = "DemoBot", code = { "chakli_dietcareclinicbot" })
public class Demo5Controller extends CommonBotController {
	
	public static final String REPLY_ID = "reply_id";	
	
	public static final String TALK_TO_AGENT = "";	

	
	    @Autowired
	    PMEnvironment pmEnvironment;
	   
	    @Autowired
		MongoTemplate mongoTemplate;
	    
	    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		
	    	reply(new OutboxMessage().template("dc_welcome_message").put("name", context().contact().getName()));
	    	next("select-language");
	
	    }
	    
	    
	    @ChatMapping(key = "select-language")
	    public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	
	    String lang =toReplyEnum(inboxMessage); 
	 	  if(!ArgUtil.is(lang)) {
	 		 lang= ArgUtil.parseAsString(context().contact().getLang());
	 	  }
	 	 
	     if(!timeCheck()) {
	    	 reply(new OutboxMessage().template("working_hours_update"));
	     }else {
		    if(lang.equalsIgnoreCase("english") || (lang!=null && lang.equalsIgnoreCase("en"))) {
		    	 context().contact().setLang("en");
		    	 context().commit();
		    	 reply(new OutboxMessage().template("dc_services"));
		    	 next("select-service");
		    }else if(lang.equalsIgnoreCase("العربية") || (lang!=null &&  lang.equalsIgnoreCase("ar"))) {
		    	 context().contact().setLang("ar");
		    	 context().commit();
		    	reply(new OutboxMessage().template("dc_services"));
		    	 next("select-service");
		    } else{
		    	context().contact().setLang("en");
		    	 context().commit();
		    	reply(new OutboxMessage().template("dc_services"));
		    	next("select-service");
		    }
	     } 
	    }
	    
	    @ChatMapping(key = "select-service")
	    public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	checkValue("dc_services",toReplyEnum(inboxMessage));
	    	switch(toReplyEnum(inboxMessage)) {
	  
		case "memberships":
		case "الاشتراكات":
		    reply(new OutboxMessage().template("dc_membership_options"));
		    next("memberships-onselect");
		    break;
		case "appointments":
		case "حجز المواعيد":
		    reply(new OutboxMessage().template("dc_appointments_opt"));
		    next("appointments-onselect");
		    break;    
		case "customer_service":
		case "customer service":	
		case "خدمة العملاء":
		 	 this.transferToAgent(inboxMessage, matcher);	
		    break;    
		case "menu_selection":
		case "menu selection":	
		case "المنيوخيارات":
			 this.transferToAgent(inboxMessage, matcher);	
		    break;
		case "clinic_locations":
		case "clinic locations":	
		case "مواقع العيادات":
		    reply(new OutboxMessage().template("dc_location_option"));
		    next("clinics-onselect");
		    break;
		case "*":
			reply(new OutboxMessage().template("dc_services_rechoose"));
			next("select-service-rechoose");
			break;
		case "#":
			  this.transferToAgent(inboxMessage, matcher);
		    break;   
		default :
		    reply(new OutboxMessage().template("invalid_input_response_std"));
		    reply(new OutboxMessage().template("dc_services"));
		    break; 
	   
	    }
	   }
	    
	    @ChatMapping(key = "memberships-onselect")
	    public void membershipsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch(toReplyEnum(inboxMessage)) {
			case "new_member":
			case "new member":	
			case "مشترك جديد":
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "current_member":
			case "current member":	
			case "مشترك حالي":
			    reply(new OutboxMessage().template("dc_current_member_options"));
			    next("currentmember-onselect");
			    break;
			case "previous_member":
			case "previous member":	
			case "مشترك سابق":
				this.transferToAgent(inboxMessage, matcher);
			    break; 
			case "*":
				this.goToMainMenu(inboxMessage, matcher);
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break; 
			default :
			    reply(new OutboxMessage().template("invalid_input_response_std"));
			    reply(new OutboxMessage().template("dc_membership_options"));
			    next("memberships-onselect");
			    break; 
		   
		    }	    
			    
	    }

	    
	    @ChatMapping(key = "currentmember-onselect")
	    public void currentmemberOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch(toReplyEnum(inboxMessage)) {
	    	case "renew_membership":
	    	case "renew membership":	
			case "تجديد نوع الحالي":
			case "تجديد نفس الاشتراك":	
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "change_membership":
			case "change membership":	
			case "تغيير نوع الاشتراك":
				this.transferToAgent(inboxMessage, matcher);
			    break;
			case "*":
				this.goToMainMenu(inboxMessage, matcher);
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;  
			default :
			    reply(new OutboxMessage().template("invalid_input_response_std"));
			    reply(new OutboxMessage().template("dc_current_member_options"));
			    next("currentmember-onselect");
			    break;     
	    	}
	   
	    
	    }
	    
	    
	    @ChatMapping(key = "clinics-onselect")
	    public void clinicOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch(toReplyEnum(inboxMessage)) {
			case "sharq":
			case "شرق":	
			    reply(new OutboxMessage().template("dc_location_link_timing_sharq"));
			    next("dc_location_link_timing");
			    break;
			case "bairaq_mall":
			case "bairaq mall":	
			case "البيرق مجمع":
			    reply(new OutboxMessage().template("dc_location_link_timing_bairaq_mall"));
			    next("dc_location_link_timing");
			    break;
			case "salmiya":
			case "السالمية":
			    reply(new OutboxMessage().template("dc_location_link_timing_salmiya"));
			   // next("clinics-onselect");
			    next("dc_location_link_timing");
			    break;
			    
			case "jahra":
			case "الجهراء":	
			    reply(new OutboxMessage().template("dc_location_link_timing_jahra"));
			    next("dc_location_link_timing");
			    break;
			    
			case "360_mall":
			case "360 mall":	
			case "360 مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_360mall"));
			    next("dc_location_link_timing");
			    break;
			case "avenues_mall":
			case "avenues mall":	
			case "الأفنيوز مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_avenues"));
			    next("dc_location_link_timing");
			    break;
			case "aqaila":
			case "العقيلة":	
			    reply(new OutboxMessage().template("dc_location_link_timing_aqaila"));
			    next("dc_location_link_timing");
			    break;  
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
	    
	  
	    @ChatMapping(key = "appointments-onselect")
	    public void appointmentOptionsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch(toReplyEnum(inboxMessage)) {
			case "new_client":
			case "new client":	
			case "عميل جديد":	
			    reply(new OutboxMessage().template("dc_appt_diet_location_opt"));
			    next("newclient-onselect");
			    break;
			case "existing_client":
			case "current_client":
			case "current client":	
			case "عميل حالي":	
			    reply(new OutboxMessage().template("dc_date_and_time_request"));
				next("dc_cs_to_contact");
			    break;  
			    
			case "previous_client":
			case "previous client":	
			case "عميل سابق":	
			    reply(new OutboxMessage().template("dc_date_and_time_request"));
				next("dc_cs_to_contact");
			    break; 
			case "*":
				this.goToMainMenu(inboxMessage, matcher);
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;
			default :
			    reply(new OutboxMessage().template("invalid_input_response_std"));
			    reply(new OutboxMessage().template("dc_appointments_opt"));
			    next("appointments-onselect");
			    break; 
	    	}
	    }
	    
	    @ChatMapping(key = "newclient-onselect")
	    public void newclientOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	checkValue("dc_dietitian_list_feb2022",toReplyEnum(inboxMessage));
	    	switch (toReplyEnum(inboxMessage)) {
			case "dieticians":
			case "dietitians":
			case "اختيار الأخصائي":
			    reply(new OutboxMessage().template("dc_dietitian_list_feb2022"));
			    next("select-dietician");
			    break;
			case "locations":
			case "branch":
			case "اختيار الموقع":	
			    reply(new OutboxMessage().template("dc_location_option"));
			    next("select-location-withdatetime");
			    break; 
			case "*":
				this.goToMainMenu(inboxMessage, matcher);
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;   
			default :
			    reply(new OutboxMessage().template("invalid_input_response_std"));
			    reply(new OutboxMessage().template("dc_appt_diet_location_opt"));
			    next("newclient-onselect");
			    break; 
	    	}
	    }
	    
@ChatMapping(key = "select-dietician")	    
public void selectDateTime(InboxMessage inboxMessage, StringMatcher matcher) {
	String reply=toReplyEnum(inboxMessage);
	switch (reply) {
	case "*":
		this.goToMainMenu(inboxMessage, matcher);
		break; 
	case "#"	:
		this.transferToAgent(inboxMessage, matcher);
	    break;   
	default :
		if(checkValue("dc_dietitian_list_feb2022", reply)) {
		reply(new OutboxMessage().template("dc_date_and_time_request"));
		next("dc_cs_to_contact");
		}else {
			reply(new OutboxMessage().template("invalid_input_response_std"));
			reply(new OutboxMessage().template("dc_dietitian_list_feb2022"));
		    next("select-dietician");
		    break; 
		}
	}
}

@ChatMapping(key = "select-location-withdatetime")	    
public void selectLocationDateTime(InboxMessage inboxMessage, StringMatcher matcher) {
	String reply=toReplyEnum(inboxMessage);
	switch (reply) {
	case "*":
		this.goToMainMenu(inboxMessage, matcher);
		break; 
	case "#"	:
		this.transferToAgent(inboxMessage, matcher);
	    break;   
	default :
		if(checkValue("dc_location_option", reply)) {
		reply(new OutboxMessage().template("dc_date_and_time_request"));
		next("dc_cs_to_contact");
		}else {
			reply(new OutboxMessage().template("invalid_input_response_std"));
			reply(new OutboxMessage().template("dc_location_option"));
		    next("select-location-withdatetime");
		    break; 
		}
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
	   
	    
	    @ChatMapping(key = "dc_date_time")
	    public void specifyDateAndTime(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (toReplyEnum(inboxMessage)) {
	    	case "*":
				this.goToMainMenu(inboxMessage, matcher);
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;   
			default :
				reply(new OutboxMessage().template("dc_date_and_time_request"));
				next("dc_cs_to_contact");
	    	}
	    }
	    
	    @ChatMapping(key = "dc_cs_to_contact")
	    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	    	routeSession("agendsk");
	    	//reply(new OutboxMessage().template("dc_cs_to_contact"));
	    	//commonTransferToAgent(inboxMessage, matcher);
	    }
	    
	    public void goToMainMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	    	reply(new OutboxMessage().template("dc_services_rechoose"));
			next("select-service-rechoose");
	    }
	    
	    
	    
	    @ChatMapping(key = "invalid_input")
	    public void invalidinput(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (toReplyEnum(inboxMessage)) {
	    	case "*":
				reply(new OutboxMessage().template("dc_services_rechoose"));
				next("select-service-rechoose");
				break; 
			case "#"	:
				this.transferToAgent(inboxMessage, matcher);
			    break;   
			default :
				reply(new OutboxMessage().template("dc_services_rechoose"));
				next("select-service-rechoose");
			    break;    
	    	}
	    	}
	    
	    @ChatMapping(key = "select-service-rechoose")
	    public void seviceOnSelectRechoose(InboxMessage inboxMessage, StringMatcher matcher) {
	    switch (toReplyEnum(inboxMessage)) {
		case "memberships":
		case "الاشتراكات":
		    reply(new OutboxMessage().template("dc_membership_options"));
		    next("memberships-onselect");
		    break;
		case "appointments":
		case "حجز المواعيد":
		    reply(new OutboxMessage().template("dc_appointments_opt"));
		    next("appointments-onselect");
		    break;    
		case "customer_service":
		case "customer service":	
		case "خدمة العملاء":
			 this.transferToAgent(inboxMessage, matcher);	
		    break;    
		
		case "menu_selection":
		case "menu selection":	
		case "المنيوخيارات":
			 this.transferToAgent(inboxMessage, matcher);	
		    break;
		case "clinic_locations":
		case "clinic locations":	
		case "مواقع العيادات":
		    reply(new OutboxMessage().template("dc_location_option"));
		    next("clinics-onselect");
		    break;
		case "*":
			this.goToMainMenu(inboxMessage, matcher);
			break; 
		case "#"	:
			this.transferToAgent(inboxMessage, matcher);
		    break;   
		default :
			reply(new OutboxMessage().template("dc_services_rechoose"));
			next("select-service-rechoose");
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
	    	 String lang= ArgUtil.parseAsString(context().contact().getLang());
	    	Query query = new Query();
			query.addCriteria(Criteria.where("code").is(tmplCode).and("lang").is(lang));
			HSMTemplateDoc hsmTmpl =mongoTemplate.findOne(query,HSMTemplateDoc.class,"DICT_HSM_TEMPLATES");
			if(ArgUtil.is(hsmTmpl)) {
				Map<String, Object> options = hsmTmpl.getOptions();
				if(ArgUtil.is(options)) {
					List<Map<String, Object>> extTemCom =(List<Map<String, Object>>) options.get("buttons");//.asListOfMap();
					 booValue = extTemCom.stream().anyMatch(map -> map.containsValue(userInput));
				}
			}
	    	return booValue;
	    	
	    }


}
