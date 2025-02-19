package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.inbound.InBound.MessageEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.model.ext.SessionBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;

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

		SessionBoundEvent inboundEVent = new SessionBoundEvent();
		inboundEVent.setTriggerType(SessionBoundEvent.TRIGGER_TYPE.MESSAGE);
		inboundEVent.setType(SessionBoundEvent.EVENT_TYPE.MESSAGE_INBOUND);
		inboundEVent.from(message);

		sessionEventTimer.setChatOutIdleTimeout(message.getSessionId(), app, inboundEVent);
		sessionEventTimer.setMitelRoutingCheck(message.getSessionId(), app);
		sessionEventTimer.setMitelClosingCheck(message.getSessionId(), app, false);
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

		SessionBoundEvent inboundEVent = new SessionBoundEvent();
		inboundEVent.setTriggerType(SessionBoundEvent.TRIGGER_TYPE.MESSAGE);
		inboundEVent.setType(SessionBoundEvent.EVENT_TYPE.MESSAGE_OUTBOUND);
		inboundEVent.from(message);
		sessionEventTimer.setChatInIdleTimeout(message.getSessionId(), app, inboundEVent);
		sessionEventTimer.setMitelClosingCheck(message.getSessionId(), app, false);
		return null;
	}

	/**
	 * After Status of Outbound Message has been Recieved
	 * 
	 */
	@Override
	public NodeEntry<InBoundEvent> postMessageStatus(MessageReport messageReport) {
		ClientApp app = messageContext.clientApp();
		MessageDoc m = messageContext.getMessageDoc();
		if (ArgUtil.is(m) && ArgUtil.is(m.getTimeout())) {
			SessionBoundEvent inboundEVent = new SessionBoundEvent();
			inboundEVent.setTriggerType(SessionBoundEvent.TRIGGER_TYPE.STATUS);
			inboundEVent.setType(StringUtils.toUpperCase(ArgUtil.parseAsString(messageReport.getStatus())));
			inboundEVent.from(messageReport);
			sessionEventTimer.setChatStatusTimeout(messageReport.getSessionId(), app, inboundEVent);
		}
		return null;
	}

}
