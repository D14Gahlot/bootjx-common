package com.boot.jx.postman.model;

import java.io.Serializable;

import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.SessionId;
import com.boot.model.MapModel;

public class PMArgs implements SessionId, Serializable {
    private static final long serialVersionUID = -1002441635191227105L;

    private String checksum;
    private String sessionId;
    private String assignToQueueCode;
    private String assignToDeptCode;
    private String assignToAgentCode;
    private Contactable contact;
    private MapModel data;
    private Object params;

    public MapModel data() {
	if (this.data == null) {
	    this.data = MapModel.createInstance();
	}
	return this.data;
    }

    public PMArgs params(Object params) {
	this.params = params;
	return this;
    }

    public PMArgs sessionId(String sessionId) {
	this.sessionId = sessionId;
	return this;
    }

    public PMArgs assignToQueueCode(String queueCode) {
	this.assignToQueueCode = queueCode;
	return this;
    }

    public PMArgs assignToDeptCode(String deptCode) {
	this.assignToDeptCode = deptCode;
	return this;
    }

    public PMArgs assignToAgentCode(String agentCode) {
	this.assignToAgentCode = agentCode;
	return this;
    }

    public PMArgs contact(Contactable contact) {
	this.contact = contact;
	return this;
    }

    public Contactable contact() {
	if (this.contact == null) {
	    this.contact = new ContactMeta();
	}
	return this.contact;
    }

    public Contactable getContact() {
	return contact;
    }

    public void setContact(Contactable contact) {
	this.contact = contact;
    }

    public MapModel getData() {
	return data;
    }

    public void setData(MapModel data) {
	this.data = data;
    }

    public String getAssignToDeptCode() {
	return assignToDeptCode;
    }

    public void setAssignToDeptCode(String assignToDeptCode) {
	this.assignToDeptCode = assignToDeptCode;
    }

    public String getAssignToAgentCode() {
	return assignToAgentCode;
    }

    public void setAssignToAgentCode(String assignToAgentCode) {
	this.assignToAgentCode = assignToAgentCode;
    }

    public String getSessionId() {
	return sessionId;
    }

    public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
    }

    public String getChecksum() {
	return checksum;
    }

    public void setChecksum(String checksum) {
	this.checksum = checksum;
    }

    public Object getParams() {
	return params;
    }

    public void setParams(Object params) {
	this.params = params;
    }

    public String getAssignToQueueCode() {
	return assignToQueueCode;
    }

    public void setAssignToQueueCode(String assignToQueueCode) {
	this.assignToQueueCode = assignToQueueCode;
    }

}
