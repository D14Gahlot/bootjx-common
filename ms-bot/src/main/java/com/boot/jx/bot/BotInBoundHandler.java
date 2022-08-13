package com.boot.jx.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.DefaultChatBoundHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;

import ch.qos.logback.core.Context;

@Component
public class BotInBoundHandler extends DefaultChatBoundHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BotInBoundHandler.class);

	@Autowired
	private BotEngine botEngine;

	@Override
	public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
		logManager.trace(inboxMessage, "BotInBoundHandler:onMessage");
		botEngine.invokeMethodsAsync(inboxMessage);
	}

	@Override
	public void doHandle(MessageReport messageReport) {
		LOGGER.debug("No Handling Required for Status on BotSide");
	}

	@Override
	public void onSessionRoute(InBoundEvent event, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		ClientApp defaultClient = context().clientApp(event.sessionRouted.targetQueue, null);
		if (ArgUtil.is(defaultClient)) {
			APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());
			if (CHAT_MODE.BOT.equals(appType.getMode())) {
				context().setInBoundEvent(event);
				context().session(sessionDoc);
				botEngine.invokeMethods(event);
			} else {
				super.onSessionRoute(event, sessionDoc, pmArgs);
			}
		}

	}

}
