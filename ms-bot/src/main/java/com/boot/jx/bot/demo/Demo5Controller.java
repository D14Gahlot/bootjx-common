package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

<<<<<<< HEAD
import com.boot.jx.AppContextUtil;
=======
>>>>>>> staging
import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", tenant = { "chakli" })
public class Demo5Controller extends CommonBotController {
	
	    private static final String CURRENT_DEMO = "current_menu";
	    @Autowired
	    private ChatContext chatContext;
	
	    @Autowired
	    PMEnvironment pmEnvironment;
	    
	    //String lang = "en";

	    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("dc_welcome_message").put("name", chatContext.contact().getName()));
		next("select-language");
	    }
	    
	    
	    @ChatMapping(key = "select-language")
	    public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    String language = inboxMessage.getMessage().toLowerCase();
	    String lang = ArgUtil.parseAsString(chatContext.contact().getLang());
	    
	    // if(!timeCheck()) {
	    //	 reply(new OutboxMessage().template("working_hours_update").lang("en"));
	    //	 this.transferToAgent(inboxMessage, matcher);
	    	 
	   //  }
	     
		    if(language.equalsIgnoreCase("english") || (lang!=null && lang.equalsIgnoreCase("en"))) {
		    	 chatContext.contact().setLang("en");
		    	 reply(new OutboxMessage().template("dc_services"));
		    	 next("select-service");
		    }else if(language.equalsIgnoreCase("العربية") || (lang!=null &&  lang.equalsIgnoreCase("ar"))) {
		    	chatContext.contact().setLang("ar");
		    	reply(new OutboxMessage().template("dc_services"));
		    	 next("select-service");
		    } else{
		    	reply(new OutboxMessage().template("dc_services"));
		    	 next("select-service");
		    }
	    }
	    
	    @ChatMapping(key = "select-service")
	    public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    String lang = ArgUtil.parseAsString(chatContext.session().get("lang"));	
	    switch (inboxMessage.getMessage().toLowerCase().trim()) {
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
		case "customer service":
		case "خدمة العملاء":
		   // reply(new OutboxMessage().template("dc_cs_to_contact"));
		    //next("dc_cs_to_contact");
			 this.transferToAgent(inboxMessage, matcher);	
		    break;    
		
		case "menu selection":
		case "المنيوخيارات":
			 this.transferToAgent(inboxMessage, matcher);	
		    break;
		case "clinic locations":
		case "مواقع العيادات":
		    reply(new OutboxMessage().template("dc_location_option"));
		    next("clinics-onselect");
		    break;
		case "*":
		    reply(new OutboxMessage().template("end_chat_message"));
		    next("feedback-onselect");
		    break;    
		default :
			 // reply(new OutboxMessage().template("dc_cs_to_contact"));
			  this.transferToAgent(inboxMessage, matcher);
		    break;    
	   
	    }
	   }
	    
	    @ChatMapping(key = "memberships-onselect")
	    public void membershipsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "new member":
			case "مشترك جديد":
			   // reply(new OutboxMessage().template("dc_cs_to_contact"));
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "current member":
			case "مشترك حالي":
			    reply(new OutboxMessage().template("dc_current_member_options"));
			    next("currentmember-onselect");
			    break;
			case "previous member":
			case "مشترك سابق":
			    this.transferToAgent(inboxMessage, matcher);
			    break;      
			    
	    }
	    
	    }
	    
	    
	    @ChatMapping(key = "currentmember-onselect")
	    public void currentmemberOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
	    	case "renew membership":
			case "تجديد نوع الحالي":
			   // reply(new OutboxMessage().template("dc_cs_to_contact"));
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "change membership":
			case "تغيير نوع الاشتراك":
				  this.transferToAgent(inboxMessage, matcher);
			    break;
	    	}
	   
	    
	    }
	    
	    
	    @ChatMapping(key = "clinics-onselect")
	    public void clinicOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "sharq":
			case "شرق":	
			    reply(new OutboxMessage().template("dc_location_link_timing_sharq"));
			    next("select-language");
			    break;
			case "bairaq mall":
			case "البيرق مجمع":
			    reply(new OutboxMessage().template("dc_location_link_timing_bairaq_mall"));
			    next("select-language");
			    break;
			case "salmiya":
			case "السالمية":
			    reply(new OutboxMessage().template("dc_location_link_timing_salmiya"));
			    next("clinics-onselect");
			    break;
			    
			case "jahra":
			case "الجهراء":	
			    reply(new OutboxMessage().template("dc_location_link_timing_jahra"));
			    next("select-language");
			    break;
			    
			case "360 mall":
			case "360 مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_360mall"));
			    next("select-language");
			    break;
			case "avenues mall":
			case "الأفنيوز مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_avenues"));
			    next("select-language");
			    break;
			case "aqaila":
			case "العقيلة":	
			    reply(new OutboxMessage().template("dc_location_link_timing_aqaila"));
			    //next("clinics-onselect");
			    next("select-language");
			    break;    
			      
			
	    }
	    
	    }
	    
	  
	    @ChatMapping(key = "appointments-onselect")
	    public void appointmentOptionsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "new client":
			case "عميل جديد":	
			    reply(new OutboxMessage().template("dc_appt_diet_location_opt"));
			    next("newclient-onselect");
			    break;
			case "existing client":
			case "عميل حالي":	
			    reply(new OutboxMessage().template("dc_date_and_time_request"));
				next("dc_cs_to_contact");
			    break;  
			    
			case "previous client":
			case "عميل سابق":	
			    reply(new OutboxMessage().template("dc_date_and_time_request"));
				next("dc_cs_to_contact");
			    break;   
	    	}
	    }
	    
	    @ChatMapping(key = "newclient-onselect")
	    public void newclientOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "dietician":
			case "اختيار الأخصائي":
			    reply(new OutboxMessage().template("dc_dietitian_list_feb2022"));
			    next("dc_date_time");
			    break;
			case "location":
			case "branch":
			case "اختيار الموقع":	
			    reply(new OutboxMessage().template("dc_location_option"));
			    next("dc_date_time");
			    break;  
	    	}
	    }
	    

	   
	    
	    @ChatMapping(key = "dc_date_time")
	    public void specifyDateAndTime(InboxMessage inboxMessage, StringMatcher matcher) {
	    	reply(new OutboxMessage().template("dc_date_and_time_request"));
	    	next("dc_cs_to_contact");
	    }
	    
	    @ChatMapping(key = "dc_cs_to_contact")
	    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
		commonTransferToAgent(inboxMessage, matcher);
	    }
	    
	    public boolean timeCheck() {
	    	boolean isNowInRange = false;
	    	try {
	    	    LocalTime now = LocalTime.now(ZoneId.of("Asia/Kuwait"));
	    	    String isoTime = now.format(DateTimeFormatter.ISO_TIME);
	    	    LocalTime currTime = LocalTime.parse(isoTime, DateTimeFormatter.ISO_TIME);
	    	    LocalTime start = LocalTime.of(9, 0);
	    	    LocalTime stop = LocalTime.of(23, 0);

	    	    isNowInRange = (!currTime.isBefore(start)) && currTime.isBefore(stop);

	    	} catch (Exception e) {
	    	    e.printStackTrace();
	    	}
	    	return isNowInRange;
	        }
	    
}
