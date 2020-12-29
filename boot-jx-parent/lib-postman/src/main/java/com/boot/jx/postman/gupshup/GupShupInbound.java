package com.boot.jx.postman.gupshup;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GupShupInbound implements Serializable {

	private static final long serialVersionUID = -5985194546657716271L;

	@ApiMockModelProperty(example = "919560222091")
	String waNumber;

	@ApiMockModelProperty(example = "919004371797")
	String mobile;

	@ApiMockModelProperty(example = "3900363981641897487")
	String replyId;
	@ApiMockModelProperty(example = "custom Message ID")
	String messageId;

	@ApiMockModelProperty(example = "Hola Amigo")
	String text;

	@ApiMockModelProperty(example = "John Smith")
	String name;

	@ApiMockModelProperty(example = "text")
	String type;

	@ApiMockModelProperty(example = "1564471290000")
	String timestamp;

	public String getWaNumber() {
		return waNumber;
	}

	public void setWaNumber(String waNumber) {
		this.waNumber = waNumber;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getReplyId() {
		return replyId;
	}

	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
