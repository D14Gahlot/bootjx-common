package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.ChatSessionEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Component
public class ChatSessionEventsImpl implements ChatSessionEvents {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Lazy
	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public NodeEntry<InBoundEvent> onSessionIdleOutBound(ChatSessionDoc session) {
		PMConfigurationObject frwrdQueue = pmEnvironment
				.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE);
		assignToQueue(session, frwrdQueue);
		return null;
	}

	@Override
	public NodeEntry<InBoundEvent> onSessionIdleInBound(ChatSessionDoc session) {
		PMConfigurationObject frwrdQueue = pmEnvironment
				.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE);
		assignToQueue(session, frwrdQueue);
		return null;
	}

	private void assignToQueue(ChatSessionDoc session, PMConfigurationObject frwrdQueue) {
		if (frwrdQueue.exists()) {
			ClientApp targetApp = pmEnvironment.config().clientApiKey(frwrdQueue.asString());
			String targetDept = null;
			if (targetApp.isAgentApp()) {
				targetDept = ArgUtil.parseAsString(targetApp.props().get("deptCode"), session.getAssignedToDept());
				sessionStore.assignToAgent(session, null, null);
			}
			chatSessionService.routeSession(session,
					new PMArgs().assignToQueueCode(frwrdQueue.asString()).assignToDeptCode(targetDept));
		}
	}

}
