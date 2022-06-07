
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.bot.chakli.DemoAlAamalController;
import com.boot.jx.common.doc.AppFaqDoc;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AppFaq", code = { "bot_faq" })
public class AppFaqBotController extends CommonBotController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoAlAamalController.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	
	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		if(assignEvent!=null) {
		 super.onSessionRoute(assignEvent);
		}
		ClientApp app = context().clientApp();
		String lang = ArgUtil.parseAsString(app.props().get("lang")==null?"en":app.props().get("lang"));
		List<AppFaqDoc>  faqLst = getParents(lang,null);
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		for (AppFaqDoc faq : faqLst) {
			String parentkey = faq.getParent();
			String code = faq.getCode();
			String value =null;
			Map<String,Object> mapKeyValue= faq.getTranslation();
			if(mapKeyValue.containsKey(lang)) {
				Map<String,Object> mkeyValue =(Map<String,Object>)mapKeyValue.get(lang);
				// Get keys and values
		        for (Map.Entry<String, Object> entry : mkeyValue.entrySet()) {
		            String k = entry.getKey();
		            String v = entry.getValue()==null?"":(String)entry.getValue();
			           if(ArgUtil.is(v) && k.contains("shortDesc")) {
		            	LOGGER.info("Key :"+k+"\t value :"+v);
		            	buttons.add(new TmplElement().name(code).label(v.toString()));
		            }
		        }
			}
		}
		buttons.add(new TmplElement().name("exit").label("Faq Menu"));
		reply(new OutboxMessage().message("Select Category").options("buttons", buttons));
		next("on_faq_parent_select");
		
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		ClientApp app = context().clientApp();
		String lang = ArgUtil.parseAsString(app.props().get("lang")==null?"en":app.props().get("lang"));
		String replay_id =toReplyEnum(inboxMessage); 
		LOGGER.info("Replay Id {===}"+replay_id);
		switch (replay_id) {
		case "exit":	
			onSessionRoute(null);
			return;
		case "m":
			routeSession("jazeera");
			return;		
		case "#":
			assignToDefaultAgent();
			reply(new OutboxMessage().template("ja_cs_to_contact"));
			return;
	default:		
		
		List<AppFaqDoc>  faqLst = getParents(lang,null);
		List<TmplElement> buttons = new ArrayList<TmplElement>();
			for (AppFaqDoc faq : faqLst) {
			String parentkey = faq.getParent();
			String code = faq.getCode();
			Map<String,Object> mapKeyValue= faq.getTranslation();
			if(mapKeyValue.containsKey(lang)) {
				Map<String,Object> mkeyValue =(Map<String,Object>)mapKeyValue.get(lang);
				// Get keys and values
		        for (Map.Entry<String, Object> entry : mkeyValue.entrySet()) {
		            String k = entry.getKey();
		            String v = entry.getValue()==null?"":(String)entry.getValue();
		             if(ArgUtil.is(v) && k.contains("shortDesc")) {
		            	 LOGGER.info("Key :"+k+"\t value :"+v);
		 	        	buttons.add(new TmplElement().name(code).label(v.toString()));
		            }
		        }
				
			}
		}
			buttons.add(new TmplElement().name("exit").label("FAQ Menu"));
		reply(new OutboxMessage().message("Select Category").options("buttons", buttons));
		next("on_faq_parent_select");
	}
	}

	@ChatMapping(key = "on_faq_parent_select")
	public void onAppSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		ClientApp app = context().clientApp();
		String lang = ArgUtil.parseAsString(app.props().get("lang")==null?"en":app.props().get("lang"));
		String replay_id = toReplyEnum(inboxMessage);
		switch (replay_id) {
		case "exit":	
			onSessionRoute(null);
			return;
		case "m":
			routeSession("jazeera");
			return;		
		case "#":
			assignToDefaultAgent();
			reply(new OutboxMessage().template("ja_cs_to_contact"));
			return;
	default:		
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		List<AppFaqDoc>  faqLst = getParents(lang,replay_id.toUpperCase());
		if(faqLst==null || faqLst.isEmpty()) {
			faqLst = getParents(lang,null);
		}
		for (AppFaqDoc faq : faqLst) {
			String parentkey = faq.getParent();
			String code = faq.getCode();
			String value =null;
			Map<String,Object> mapKeyValue= faq.getTranslation();
			if(mapKeyValue.containsKey(lang)) {
				Map<String,Object> mkeyValue =(Map<String,Object>)mapKeyValue.get(lang);
				// Get keys and values
		        for (Map.Entry<String, Object> entry : mkeyValue.entrySet()) {
		            String k = entry.getKey();
		            String v = entry.getValue()==null?"":(String)entry.getValue();
		             if(ArgUtil.is(v) && k.contains("shortDesc")) {
		            	LOGGER.info("Key :"+k+"\t value :"+v);
		            	buttons.add(new TmplElement().name(code).label(v.toString()));
		            }
		        }
				
			}
		}
		buttons.add(new TmplElement().name("exit").label("FAQ Menu"));
		reply(new OutboxMessage().message("Select Category").options("buttons", buttons));
		next("on_faq_child_select");
	}
	}		
	
	@ChatMapping(key = "on_faq_child_select")
	public void onChildSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		ClientApp app = context().clientApp();
		String lang = ArgUtil.parseAsString(app.props().get("lang")==null?"en":app.props().get("lang"));
		String replay_id = toReplyEnum(inboxMessage);
		LOGGER.info("replay_id {====}:"+replay_id);
		
		switch (replay_id) {
		case "exit":	
			onSessionRoute(null);
			return;
		case "m":
			routeSession("jazeera");
			return;		
		case "#":
			assignToDefaultAgent();
			reply(new OutboxMessage().template("ja_cs_to_contact"));
			return;
	default:
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		buttons.add(new TmplElement().name("exit").label("FAQ Menu"));
		List<AppFaqDoc>  faqChildValue = getChild(lang,replay_id.toUpperCase());
		for (AppFaqDoc faq : faqChildValue) {
			String parentkey = faq.getParent();
			String code = faq.getCode();
			String value =null;
			Map<String,Object> mapKeyValue= faq.getTranslation();
			Map<String,Object> mkeyValue =(Map<String,Object>)mapKeyValue.get(lang);
				// Get keys and values
		        for (Map.Entry<String, Object> entry : mkeyValue.entrySet()) {
		            String k = entry.getKey();
		            String v = entry.getValue()==null?"":(String)entry.getValue();
		             if(ArgUtil.is(v) && k.contains("body")) {
		     			reply(new OutboxMessage().message(v).options("buttons", buttons));
		            }
		        }
		}
    }
	}
	
	public List<AppFaqDoc> getParents(String lang,String parent){
		Query query = new Query();
		if(ArgUtil.is(lang) && ArgUtil.is(parent)) {
			query.addCriteria(Criteria.where("parent").is(parent).and("translation."+lang).exists(true));
		}else {
			query.addCriteria(Criteria.where("parent").is(""));
		}
		query.with(new Sort(new Order(Direction.ASC, "code"))); 
		List<AppFaqDoc> faqParentLst = commonMongoTemplate.find(query, AppFaqDoc.class);
	  	
		return faqParentLst;
	}
	
	public List<AppFaqDoc> getChild(String lang,String code){
		Query query = new Query();
		if(ArgUtil.is(lang) && ArgUtil.is(code)) {
			query.addCriteria(Criteria.where("code").is(code).and("translation."+lang).exists(true));
		}else {
			query.addCriteria(Criteria.where("code").is(code));
		}
		query.with(new Sort(new Order(Direction.ASC, "code"))); 
		List<AppFaqDoc> faqChildValue = commonMongoTemplate.find(query, AppFaqDoc.class);
		return  faqChildValue;
	}
	

}
