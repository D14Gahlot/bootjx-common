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

public class InBoundEvent implements LoggableEntity, SessionInfo, TraceMessage {

	private static final long serialVersionUID = -8470839812429749401L;

	public static final String SESSION_ROUTED = "SESSION_ROUTED";

	public static final String SESSION_INIT = "SESSION_INIT";
	public static final String SESSION_CLOSED = "SESSION_CLOSED";
	public static final String SESSION_STATUS = "SESSION_STATUS";

	public static final String SESSION_ASSIGNED = "SESSION_ASSIGNED";

	public static final String CONTACT_UPDATE = "CONTACT_UPDATE";

	public String eventId;

	@ApiMockModelProperty(example = "SESSION_ROUTED", value = "Event Triggered by App/Service")
	public String eventCode;

	private String checksum;

	public String sessionId;
	public String contactId;

	public InBoundEvent eventCode(String eventCode) {
		this.eventCode = eventCode;
		return this;
	}

	public static class SessionRouted {
		public String routingId;
		public boolean sessionStart;
		public String sourceQueue;
		public String targetQueue;

		@ApiMockModelProperty(value = "Additional params sent by Router")
		public Object params;
	}

	public static class SessionAssigned {
		public String oldDept;
		public String newDept;
		public String oldAgent;
		public String newAgent;
		public String oldBot;
		public String newBot;
	}

	public SessionRouted sessionRouted;

	public SessionAssigned sessionAssigned;

	public Contactable contact;
	private MessageSession session;
	private MessageRouter route;
	private List<Object> trace;

	public Contactable contact() {
		if (this.contact == null) {
			this.contact = new ContactMeta();
		}
		return this.contact;
	}

	public SessionAssigned sessionAssigned() {
		if (this.sessionAssigned == null) {
			this.sessionAssigned = new SessionAssigned();
		}
		return this.sessionAssigned;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String getContactId() {
		if (ArgUtil.is(this.contactId)) {
			return this.contactId;
		}
		return PostManUtil.CONTACT_ID(this.contact());
	}

	@Override
	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

	public MessageSession getSession() {
		return session;
	}

	public void setSession(MessageSession session) {
		this.session = session;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public List<Object> getTrace() {
		return trace;
	}

	public void setTrace(List<Object> trace) {
		this.trace = trace;
	}

	@Override
	public String id() {
		return this.getEventId();
	}

	@Override
	public void id(String id) {
		this.setEventId(id);
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
}