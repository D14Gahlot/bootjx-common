package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.ChatSessionEvents;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.model.MapModel.NodeEntry;

@Component
public class ChatSessionEventsImpl implements ChatSessionEvents {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Lazy
	@Autowired
	private ChatSessionService chatSessionService;

	@Override
	public NodeEntry<InBoundEvent> onSessionIdleOutBound(ChatSessionDoc session) {
		PMConfigurationObject frwrdQueue = pmEnvironment
				.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE);
		if (frwrdQueue.exists()) {
			chatSessionService.routeSession(session, new PMArgs().assignToQueueCode(frwrdQueue.asString()));
		}
		return null;
	}

	@Override
	public NodeEntry<InBoundEvent> onSessionIdleInBound(ChatSessionDoc session) {
		PMConfigurationObject frwrdQueue = pmEnvironment
				.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE);
		if (frwrdQueue.exists()) {
			chatSessionService.routeSession(session, new PMArgs().assignToQueueCode(frwrdQueue.asString()));
		}
		return null;
	}

}
