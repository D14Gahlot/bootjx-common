package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.inbound.InBound.MessageEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.model.MapModel.NodeEntry;

@Component
public class MessageEventsImpl implements MessageEvents {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private SessionEventTimer sessionEventTimer;

	@Autowired
	private MessageContext messageContext;

	@Override
	public NodeEntry<InBoundEvent> postMessageInBound(InboxMessage message) {
		ClientApp app = messageContext.clientApp();
		if (app.isAgentApp()) {
			sessionEventTimer.setChatOutIdleTimeout(message.getSessionId());
		}
		return null;
	}

	@Override
	public NodeEntry<InBoundEvent> preMessageOutBound(OutboxMessage message) {
		return null;
	}

	@Override
	public NodeEntry<InBoundEvent> onMessageOutbound(OutboxMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeEntry<InBoundEvent> postMessageOutBound(OutboxMessage message) {
		ClientApp app = messageContext.clientApp();
		if (app.isAgentApp()) {
			sessionEventTimer.setChatInIdleTimeout(message.getSessionId());
		}
		return null;
	}

}
