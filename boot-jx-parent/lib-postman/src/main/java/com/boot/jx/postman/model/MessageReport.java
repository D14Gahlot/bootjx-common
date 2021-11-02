package com.boot.jx.postman.model;

import java.io.Serializable;

import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageReport implements Serializable {

    private static final long serialVersionUID = -9039777977577457215L;

    private String messageId;
    private String messageIdExt;
    private String messageIdRef;
    private Contactable contact;

    protected long changeStamp;
    protected long watermarkStamp;
    private Status status = null;
    private String reason = null;

    public String getMessageId() {
	return messageId;
    }

    public void setMessageId(String messageId) {
	this.messageId = messageId;
    }

    public String getMessageIdExt() {
	return messageIdExt;
    }

    public void setMessageIdExt(String messageIdExt) {
	this.messageIdExt = messageIdExt;
    }

    public String getMessageIdRef() {
	return messageIdRef;
    }

    public void setMessageIdRef(String messageIdRef) {
	this.messageIdRef = messageIdRef;
    }

    public Status getStatus() {
	return status;
    }

    public void setStatus(Status status) {
	this.status = status;
    }

    public long getChangeStamp() {
	return changeStamp;
    }

    public void setChangeStamp(long changeStamp) {
	this.changeStamp = changeStamp;
    }

    public String getReason() {
	return reason;
    }

    public void setReason(String reason) {
	this.reason = reason;
    }

    public Contactable getContact() {
	return contact;
    }

    public void setContact(Contactable contact) {
	this.contact = contact;
    }

    public Contactable contact() {
	if (this.contact == null) {
	    this.contact = new ContactMeta();
	}
	return this.contact;
    }

    public long getWatermarkStamp() {
	return watermarkStamp;
    }

    public void setWatermarkStamp(long watermarkStamp) {
	this.watermarkStamp = watermarkStamp;
    }

}
