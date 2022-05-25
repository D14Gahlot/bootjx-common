
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.common.doc.AppFaqDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AppFaq", code = { "bot_faq" })
public class AppFaqBotController extends CommonBotController {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	
	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		// List Main menu with parent null
		//List<DepartmentDoc> teams = commonMongoTemplate.findAll(DepartmentDoc.class);
		
		//List<AppFaqDoc> faqs = commonMongoTemplate.
		
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {

	}

	@ChatMapping(key = "on_faq_select")
	public void onAppSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		
	}

}
