package com.boot.jx.postman.model.ext;

import com.boot.jx.postman.model.MessageDefinitions.LoggableEntity;
import com.boot.jx.postman.model.MessageDefinitions.SessionInfo;
import com.boot.jx.postman.model.MessageDefinitions.TraceMessage;
import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundEvent extends SessionBoundEvent implements LoggableEntity, SessionInfo, TraceMessage {

	private static final long serialVersionUID = -8470839812429749401L;

	public static final String SESSION_ROUTED = "SESSION_ROUTED";

	public static final String SESSION_INIT = "SESSION_INIT";
	public static final String SESSION_CLOSED = "SESSION_CLOSED";
	public static final String SESSION_STATUS = "SESSION_STATUS";

	public static final String SESSION_ASSIGNED = "SESSION_ASSIGNED";

	public static final String CONTACT_UPDATE = "CONTACT_UPDATE";

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

	public SessionAssigned sessionAssigned() {
		if (this.sessionAssigned == null) {
			this.sessionAssigned = new SessionAssigned();
		}
		return this.sessionAssigned;
	}

}