
package com.boot.jx.bot.alex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppContextUtil;
import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.bot.ChatController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "FatherBot")
public class DefaultChatController extends CommonBotController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChatController.class);

    @Autowired
    private ChatContext chatContext;
    
    @Autowired
    private PMEnvironment pmEnvironment;

    @ChatMapping(key = "transfer-to-agent")
    public void transferToAgent(InboxMessage inboxMessage, StringMatcher matcher) {
	try {
	    InboxMessage agentAssignResp = assignToAgent().getResult();
	    if (ArgUtil.is(agentAssignResp.session().getAgent())) {
		PMConfigurationObject transferReply = pmEnvironment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
		if(transferReply.exists()) {
		    reply(new OutboxMessage().templateId(transferReply.asString()));
		} else if(ArgUtil.is(AppContextUtil.getTenant()) && AppContextUtil.getTenant().equalsIgnoreCase("tathkarah")) {
	    		reply("، عميلنا العزيز\r\n"
	    		 		+ "\r\n"
	    		 		+ "مرحباً بك في تطبيق تذكره!\r\n"
	    		 		+ "\r\n"
	    		 		+ "لحظات وسيتم توصيلك بأحد ممثلي خدمة العملاء. \r\n"
	    		 		+ "\r\n"
	    		 		+ " …..شكرا لانتظارك");
	    	}else {
	    	 reply("Connecting you to one of our customer representatives. Give us a moment.");
	    	}
	    } else {
	    	reply("All agents are busy or online, we will connect you whenever someone is available.");
	    }
	} catch (Exception e) {
	    reply("We are having some issues trying connect you to one of our customer representatives. Please be patient");
	    LOGGER.error("Erro while Connecting to Agent", e);
	}
    }

    @ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
    public void defaultHandler(InboxMessage inboxMessage, StringMatcher matcher) {
	commonTransferToAgent(inboxMessage, matcher);
    }

}
