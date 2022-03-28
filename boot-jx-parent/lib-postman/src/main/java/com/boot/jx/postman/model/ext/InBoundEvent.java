package com.boot.jx.postman.model.ext;

import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundEvent {

    public static final String SESSION_ROUTED = "SESSION_ROUTED";

    public static final String SESSION_INIT = "SESSION_INIT";
    public static final String SESSION_CLOSED = "SESSION_CLOSED";
    public static final String SESSION_STATUS = "SESSION_STATUS";

    public static final String SESSION_ASSIGNED = "SESSION_ASSIGNED";

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

    private Contactable contact;

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
}