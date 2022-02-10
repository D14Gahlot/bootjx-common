package com.boot.jx.bot.demo;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
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
	    
	    String lang = null;

	    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("dc_welcome_message").put("name", chatContext.getContact().getName()));
		next("select-language");
	    }
	    
	    
	    @ChatMapping(key = "select-language")
	    public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    String language = inboxMessage.getMessage().toLowerCase();
	     chatContext.sessionData().put("lang", "eng");
	    // if(!timeCheck()) {
	    //	 reply(new OutboxMessage().template("working_hours_update").lang("eng"));
	    //	 this.transferToAgent(inboxMessage, matcher);
	    	 
	   //  }
	     
		    if(language.equalsIgnoreCase("english")) {
		    	 chatContext.sessionData().put("lang", "eng");
		    	 reply(new OutboxMessage().template("dc_services").lang("eng"));
		    	 next("select-service");
		    }else if(language.equalsIgnoreCase("العربية")) {
		    	chatContext.sessionData().put("lang", "ara");
		    	reply(new OutboxMessage().template("dc_services").lang("ara"));
		    	 next("select-service");
		    } else{
		    	reply(new OutboxMessage().template("dc_services").lang("eng"));
		    	 next("select-service");
		    }
	    }
	    
	    @ChatMapping(key = "select-service")
	    public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    lang = ArgUtil.parseAsString(chatContext.sessionData().get("lang"));	
	    switch (inboxMessage.getMessage().toLowerCase().trim()) {
		case "memberships":
		case "الاشتراكات":
		    reply(new OutboxMessage().template("dc_membership_options").lang(lang));
		    next("memberships-onselect");
		    break;
		case "appointments":
		case "حجز المواعيد":
		    reply(new OutboxMessage().template("dc_appointments_opt").lang(lang));
		    next("appointments-onselect");
		    break;    
		case "customer service":
		case "خدمة العملاء":
		    reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
		    next("customer-onselect");
		    break;    
		
		case "menu selection":
		case "المنيوخيارات":
		    reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
		    next("menu-onselect");
		    break;
		case "clinic locations":
		case "مواقع العيادات":
		    reply(new OutboxMessage().template("dc_location_option").lang(lang));
		    next("clinics-onselect");
		    break;
		case "*":
		    reply(new OutboxMessage().template("end_chat_message").lang(lang));
		    next("feedback-onselect");
		    break;    
		default :
			  reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
			  this.transferToAgent(inboxMessage, matcher);
		    break;    
	   
	    }
	   }
	    
	    @ChatMapping(key = "memberships-onselect")
	    public void membershipsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "new member":
			case "مشترك جديد":
			    reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "current member":
			case "مشترك حالي":
			    reply(new OutboxMessage().template("dc_current_member_options").lang(lang));
			    next("currentmember-onselect");
			    break;
			case "previous member":
			case "مشترك سابق":
			    reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
			    this.transferToAgent(inboxMessage, matcher);
			    break;      
			    
	    }
	    
	    }
	    
	    
	    @ChatMapping(key = "currentmember-onselect")
	    public void currentmemberOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
	    	case "renew membership":
			case "تجديد نوع الحالي":
			    reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
			    this.transferToAgent(inboxMessage, matcher);
			    break;
			case "change membership":
			case "تغيير نوع الاشتراك":
				  reply(new OutboxMessage().template("dc_cs_to_contact").lang(lang));
				  this.transferToAgent(inboxMessage, matcher);
			    break;
	    	}
	   
	    
	    }
	    
	    
	    @ChatMapping(key = "clinics-onselect")
	    public void clinicOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "sharq":
			case "شرق":	
			    reply(new OutboxMessage().template("dc_location_link_timing_sharq").lang(lang));
			    next("clinics-onselect");
			    break;
			case "bairaq_mall":
			case "البيرق مجمع":
			    reply(new OutboxMessage().template("dc_location_link_timing_bairaq_mall").lang(lang));
			    next("clinics-onselect");
			    break;
			case "salmiya":
			case "السالمية":
			    reply(new OutboxMessage().template("dc_location_link_timing_salmiya").lang(lang));
			    next("clinics-onselect");
			    break;
			    
			case "jahra":
			case "الجهراء":	
			    reply(new OutboxMessage().template("dc_location_link_timing_jahra").lang(lang));
			    next("clinics-onselect");
			    break;
			    
			case "360mall":
			case "360 مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_360mall").lang(lang));
			    next("clinics-onselect");
			    break;
			case "avenues":
			case "الأفنيوز مجمع":	
			    reply(new OutboxMessage().template("dc_location_link_timing_avenues").lang(lang));
			    next("clinics-onselect");
			    break;    
			
	    }
	    
	    }
	    
	  
	    @ChatMapping(key = "appointments-onselect")
	    public void appointmentOptionsOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "new client":
			case "عميل جديد":	
			    reply(new OutboxMessage().template("dc_appt_diet_location_opt").lang(lang));
			    next("newclient-onselect");
			    break;
			case "existing client":
			case "عميل حالي":	
			    reply(new OutboxMessage().template("dc_date_and_time_request").lang(lang));
			    next("dc_date_time");
			    break;  
			    
			case "previous client":
			case "عميل سابق":	
			    reply(new OutboxMessage().template("dc_date_and_time_request").lang(lang));
			    next("dc_date_time");
			    break;   
	    	}
	    }
	    
	    @ChatMapping(key = "newclient-onselect")
	    public void newclientOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	    	switch (inboxMessage.getMessage().toLowerCase().trim()) {
			case "dietitian":
			case "اختيار الأخصائي":
			    reply(new OutboxMessage().template("dc_dietitian_list_feb2022").lang(lang));
			    next("dc_date_time");
			    break;
			case "location":
			case "branch":
			case "اختيار الموقع":	
			    reply(new OutboxMessage().template("dc_location_option").lang(lang));
			    next("dc_date_time");
			    break;  
	    	}
	    }
	    

	   
	    
	    @ChatMapping(key = "dc_date_time")
	    public void specifyDateAndTime(InboxMessage inboxMessage, StringMatcher matcher) {
	    	reply(new OutboxMessage().template("dc_date_and_time_request").lang(lang));
	    	next("talk2agent");
	    }
	    
	    @ChatMapping(key = "talk2agent")
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
