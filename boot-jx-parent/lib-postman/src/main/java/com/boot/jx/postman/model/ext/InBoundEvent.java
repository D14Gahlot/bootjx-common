package com.boot.jx.postman.model.ext;

import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundEvent {

    public static final String SESSION_ROUTED = "SESSION_ROUTED";

    public static final String SESSION_INIT = "SESSION_INIT";

    @ApiMockModelProperty(example = "SESSION_ROUTED", value = "Event Triggered by App/Service")
    public String eventCode;

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

    public SessionRouted sessionRouted;

    private Contactable contact;

    public Contactable contact() {
	if (this.contact == null) {
	    this.contact = new ContactMeta();
	}
	return this.contact;
    }
}