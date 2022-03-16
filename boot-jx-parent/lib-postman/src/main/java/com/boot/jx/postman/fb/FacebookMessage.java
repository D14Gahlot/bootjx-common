package com.boot.jx.postman.fb;

import java.io.Serializable;

import com.boot.jx.postman.ig.InstagramAttachment;
import com.boot.model.MapModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookMessage implements Serializable {
    private static final long serialVersionUID = 260395496229953084L;
    private String mid;
    private Long seq;
    private String text;

    @JsonProperty("quick_reply")
    private MapModel quickReply;

    @JsonProperty("reply_to")
    private MapModel replyTo;

    private InstagramAttachment[] attachments;

    public String getMid() {
	return mid;
    }

    public void setMid(String mid) {
	this.mid = mid;
    }

    public Long getSeq() {
	return seq;
    }

    public void setSeq(Long seq) {
	this.seq = seq;
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    public InstagramAttachment[] getAttachments() {
	return attachments;
    }

    public void setAttachments(InstagramAttachment[] attachments) {
	this.attachments = attachments;
    }

    public MapModel getQuickReply() {
	return quickReply;
    }

    public void setQuickReply(MapModel quickReply) {
	this.quickReply = quickReply;
    }

    public MapModel getReplyTo() {
	return replyTo;
    }

    public void setReplyTo(MapModel replyTo) {
	this.replyTo = replyTo;
    }
}
