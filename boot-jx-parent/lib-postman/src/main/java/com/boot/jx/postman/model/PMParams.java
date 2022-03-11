package com.boot.jx.postman.model;

import java.io.Serializable;

import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.SessionId;
import com.boot.model.MapModel;

public class PMParams implements SessionId, Serializable {
    private static final long serialVersionUID = -1002441635191227105L;

    private String checksum;
    private String sessionId;
    private String assignToDeptCode;
    private String assignToAgentCode;
    private Contactable contact;
    private MapModel data;

    public MapModel data() {
	if (this.data == null) {
	    this.data = MapModel.createInstance();
	}
	return this.data;
    }

    public PMParams sessionId(String sessionId) {
	this.sessionId = sessionId;
	return this;
    }

    public PMParams assignToDeptCode(String deptCode) {
	this.assignToDeptCode = deptCode;
	return this;
    }

    public PMParams assignToAgentCode(String agentCode) {
	this.assignToAgentCode = agentCode;
	return this;
    }

    public PMParams contact(Contactable contact) {
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

}
