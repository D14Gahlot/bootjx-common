package com.boot.jx.bot.demo;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "almamaalholding", code = { "chakli_jaipur" })
public class DemoJaipurController extends CommonBotController {
	 
	@Autowired
	  private ChatContext chatContext;
	 
	
	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	    public void start(InboxMessage inboxMessage, StringMatcher matcher) {
	  	reply(new OutboxMessage().template("jd_welcome_msg").put("name", chatContext.contact().getName()));
	    	next("select-language");
	
	    }
}
