package com.boot.jx.agent.dto;

import java.io.Serializable;

import com.boot.jx.postman.model.TagDocument;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDto implements Serializable {
	private static final long serialVersionUID = 7766790295486098869L;
	private String text;
	private String name;
	private String sessionId;
	private long timestamp;
	private boolean type;
	private TagDocument tags;

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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isType() {
		return type;
	}

	public void setType(boolean type) {
		this.type = type;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public TagDocument getTags() {
		return tags;
	}

	public void setTags(TagDocument tags) {
		this.tags = tags;
	}
}
