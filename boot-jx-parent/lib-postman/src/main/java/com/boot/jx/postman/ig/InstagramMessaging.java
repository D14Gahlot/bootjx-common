package com.boot.jx.postman.ig;

import java.io.Serializable;
import java.util.Map;

import com.boot.utils.ArgUtil;

public class InstagramMessaging implements Serializable {
    private static final long serialVersionUID = 4410460271776560521L;
    private Map<String, String> sender;
    private Map<String, String> recipient;
    private Long timestamp;
    private InstagramMessage message;
    private InstagramPostback postBack;
    private Map<String, Object> read;
    

    public Map<String, String> getSender() {
	return sender;
    }

    public void setSender(Map<String, String> sender) {
	this.sender = sender;
    }

    public Map<String, String> getRecipient() {
	return recipient;
    }

    public void setRecipient(Map<String, String> recipient) {
	this.recipient = recipient;
    }

    public Long getTimestamp() {
	return timestamp;
    }

    public void setTimestamp(Long timestamp) {
	this.timestamp = timestamp;
    }

    public InstagramMessage getMessage() {
    	return message;
    }

    public void setMessage(InstagramMessage message) {
	this.message = message;
    }
    
    public InstagramPostback getPostBack() {
    	return postBack;
    }

    public void setPostback(InstagramPostback postBack) {
    	this.postBack = postBack;
    }

    public Map<String, Object> getRead() {
	return read;
    }

    public void setRead(Map<String, Object> read) {
	this.read = read;
    }

    public long getReadWatermark() {
	if (this.read == null) {
	    return 0L;
	}
	if (ArgUtil.is(read.get("watermark"))) {
		return ArgUtil.parseAsLong(this.read.get("watermark"));
	}
	return 0L;
    }
    
    
    
    public boolean isValidCustomerMessage() {
    	return (ArgUtil.is(this.message) && !this.message.isIs_echo());
    }
}
