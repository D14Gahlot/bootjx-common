package com.boot.jx.bot.chakli;

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
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_greenskwtbot" })
public class DemoGreenskwtController extends CommonBotController {

    public static final String REPLY_ID = "reply_id";

    public static final String TALK_TO_AGENT = "";

    @Autowired
    PMEnvironment pmEnvironment;

    @Autowired
    MongoTemplate mongoTemplate;

    private void resolveLanguage() {
		reply(new OutboxMessage().template("gr_welcome_msg").put("name", context().contact().getName()));
		next("select-language");
	}
    
	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		resolveLanguage();
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		resolveLanguage();
	}


    @ChatMapping(key = "select-language")
    public void languageOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	String lang = toReplyEnum(inboxMessage);
	if (!ArgUtil.is(lang)) {
	    lang = ArgUtil.parseAsString(context().contact().getLang());
	}

	// if(!timeCheck()) {
	// reply(new OutboxMessage().template("working_hours_update"));
	// }else {
	if (lang.equalsIgnoreCase("english") || (lang != null && lang.equalsIgnoreCase("en"))) {
	    context().contact().setLang("en");
	    context().commit();
	    reply(new OutboxMessage().template("gr_question"));
	    next("select-question");
	} else if (lang.equalsIgnoreCase("العربية") || (lang != null && lang.equalsIgnoreCase("ar"))) {
	    context().contact().setLang("ar");
	    context().commit();
	    reply(new OutboxMessage().template("gr_question"));
	    next("select-question");
	} else {
	    context().contact().setLang("en");
	    context().commit();
	    reply(new OutboxMessage().template("gr_question"));
	    next("select-question");
	}
	// }
    }

    @ChatMapping(key = "select-question")
    public void seviceOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
	checkValue("gr_question", toReplyEnum(inboxMessage));
	switch (toReplyEnum(inboxMessage)) {
	case "gr_new_order":
	    reply(new OutboxMessage().template("gr_new_order_ans"));
	    next("next_menu");
	    break;
	case "gr_edit_order":
	case "gr_ord_follow_up":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	case "gr_location":
	    reply(new OutboxMessage().template("gr_our_location_ans"));
	    next("next_menu");
	    break;
	case "gr_working_hrs":
	    reply(new OutboxMessage().template("gr_working_hrs_ans"));
	    next("next_menu");
	    break;
	case "gr_catering":
	    reply(new OutboxMessage().template("gr_catering_ans"));
	    next("next_menu");
	    break; 
	case "gr_help":
	    reply(new OutboxMessage().template("gr_help_ans"));
	    next("next_menu");
	    break;
	case "*":
	    this.goToMainMenu(inboxMessage, matcher);
	    break;
	case "#":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    reply(new OutboxMessage().template("gr_invalid_option"));
	    reply(new OutboxMessage().template("gr_question"));
	    break;

	}
    }

    @ChatMapping(key = "next_menu")
    public void locationLinkTiming(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (toReplyEnum(inboxMessage)) {
	case "*":
	    this.goToMainMenu(inboxMessage, matcher);
	    break;
	case "#":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    this.goToMainMenu(inboxMessage, matcher);
	    break;

	}
    }

    @ChatMapping(key = "jd_cs_to_contact")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	routeSession("agendsk");
    }

    public void goToMainMenu(InboxMessage inboxMessage, StringMatcher matcher) {
	reply(new OutboxMessage().template("gr_question"));
	next("select-question");
    }

    @ChatMapping(key = "gr_invalid_input")
    public void invalidinput(InboxMessage inboxMessage, StringMatcher matcher) {
	switch (toReplyEnum(inboxMessage)) {
	case "*":
	    reply(new OutboxMessage().template("select-question"));
	    next("select-question");
	    break;
	case "#":
	    this.transferToAgent(inboxMessage, matcher);
	    break;
	default:
	    this.goToMainMenu(inboxMessage, matcher);
	    break;
	}
    }

    public boolean timeCheck() {
	SafeKeyHashMap<Object> globalVars = pmEnvironment.local().globalVars();
	String officeTimeFlag = globalVars.keyEntry("office_time_msg").asString();
	boolean isNowInRange = false;
	if (officeTimeFlag.equalsIgnoreCase("true")) {
	    String startTime = globalVars.keyEntry("office_start_time").asString();
	    String endTime = globalVars.keyEntry("office_start_time").asString();

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
	} else {
	    isNowInRange = true;
	}
	return isNowInRange;
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

    @SuppressWarnings("unchecked")
    private Boolean checkValue(String tmplCode, String userInput) {
	Boolean booValue = false;
	String lang = ArgUtil.parseAsString(context().contact().getLang());
	Query query = new Query();
	query.addCriteria(Criteria.where("code").is(tmplCode).and("lang").is(lang));
	HSMTemplateDoc hsmTmpl = mongoTemplate.findOne(query, HSMTemplateDoc.class, "DICT_HSM_TEMPLATES");
	if (ArgUtil.is(hsmTmpl)) {
	    Map<String, Object> options = hsmTmpl.getOptions();
	    if (ArgUtil.is(options)) {
		List<Map<String, Object>> extTemCom = (List<Map<String, Object>>) options.get("buttons");
		booValue = extTemCom.stream().anyMatch(map -> map.containsValue(userInput));
	    }
	}
	return booValue;
    }
    
    public void next(String key) {
		String handelrName = key;
		if (ArgUtil.is(this.controllerName)) {
			handelrName = this.controllerName + "#" + key;
		}
		context().meta().setNextHandler(handelrName);
	}

}
