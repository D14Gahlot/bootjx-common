package com.boot.jx.stomp;

import java.util.ArrayList;
import java.util.List;

public class StompQuery {
    public static final String PING_TAG = "ping_tag";

    private String topic;
    private String stompUID;
    private List<String> tags;

    public StompQuery(String topic) {
	this.topic = topic;
    }

    public String getTopic() {
	return topic;
    }

    public void setTopic(String topic) {
	this.topic = topic;
    }

    public List<String> getTags() {
	return tags;
    }

    public void setTags(List<String> tags) {
	this.tags = tags;
    }

    public String getStompUID() {
	return stompUID;
    }

    public void setStompUID(String stompUID) {
	this.stompUID = stompUID;
    }

    public StompQuery stompUID(String stompUID) {
	this.stompUID = stompUID;
	return this;
    }

    public StompQuery tags(String... tags) {
	if (this.tags == null) {
	    this.tags = new ArrayList<String>();
	}
	for (String tag : tags) {
	    this.tags.add(tag);
	}
	return this;
    }
}
