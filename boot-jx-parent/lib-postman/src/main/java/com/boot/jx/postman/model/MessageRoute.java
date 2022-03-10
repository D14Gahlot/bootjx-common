package com.boot.jx.postman.model;

import java.io.Serializable;

import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageRoute implements Serializable {
    private static final long serialVersionUID = 1875887497925865671L;
    String senderQueueCode;
    String senderAgentCode;
    String sendMode;

    public String getSenderQueueCode() {
	return senderQueueCode;
    }

    public void setSenderQueueCode(String senderQueueCode) {
	this.senderQueueCode = senderQueueCode;
    }

    public String getSenderAgentCode() {
	return senderAgentCode;
    }

    public void setSenderAgentCode(String senderAgentCode) {
	this.senderAgentCode = senderAgentCode;
    }

    public String getSendMode() {
	return sendMode;
    }

    public void setSendMode(String sendMode) {
	this.sendMode = sendMode;
    }
}
