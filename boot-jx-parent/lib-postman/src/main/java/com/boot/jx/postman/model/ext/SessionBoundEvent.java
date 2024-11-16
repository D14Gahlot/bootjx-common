package com.boot.jx.postman.model.ext;

import java.util.List;

import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.LoggableEntity;
import com.boot.jx.postman.model.MessageDefinitions.SessionInfo;
import com.boot.jx.postman.model.MessageDefinitions.TraceMessage;
import com.boot.jx.postman.model.MessageRouter;
import com.boot.jx.postman.model.MessageSession;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

public class SessionBoundEvent implements SessionInfo, TraceMessage, LoggableEntity {

	private static final long serialVersionUID = 5398462708633545227L;
	private MessageSession session;
	public Contactable contact;
	private MessageRouter route;
	public String contactId;
	protected List<Object> trace;

	public String sessionId;
	public String messageId;
	public String eventId;
	private String checksum;

	@ApiMockModelProperty(example = "SESSION_ROUTED", value = "Event Triggered by App/Service")
	public String eventCode;

	@ApiMockModelProperty(example = "INBOUND", value = "Bound Type of Event Triggered by App/Service",
			allowableValues = "INBOUND,OUTBOUND,ACTION")
	public String type;

	public MessageSession getSession() {
		return session;
	}

	public void setSession(MessageSession session) {
		this.session = session;
	}

	@Override
	public String getSessionId() {
		return this.sessionId;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

	@Override
	public Contactable contact() {
		if (this.contact == null) {
			this.contact = new ContactMeta();
		}
		return this.contact;
	}

	public MessageRouter getRoute() {
		return route;
	}

	public void setRoute(MessageRouter route) {
		this.route = route;
	}

	@Override
	public MessageRouter route() {
		if (route == null) {
			this.route = new MessageRouter();
		}
		return this.route;
	}

	@Override
	public List<Object> getTrace() {
		return trace;
	}

	@Override
	public void setTrace(List<Object> trace) {
		this.trace = trace;
	}

	@Override
	public String getContactId() {
		if (ArgUtil.is(this.contactId)) {
			return this.contactId;
		}
		return PostManUtil.CONTACT_ID(this.contact());
	}

	public String eventId() {
		if (!ArgUtil.is(this.eventId)) {
			this.eventId = String.format("%s/%s", this.messageId, this.eventCode);
		}
		return this.eventId;
	}

	@Override
	public String id() {
		return this.eventId;
	}

	@Override
	public void id(String id) {
		this.eventId = id;
	}

	public Contactable getContact() {
		return contact;
	}

	public void setContact(Contactable contact) {
		this.contact = contact;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
