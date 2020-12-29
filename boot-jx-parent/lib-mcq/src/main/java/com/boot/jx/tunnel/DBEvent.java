package com.boot.jx.tunnel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DBEvent extends TunnelEvent {
	private static final long serialVersionUID = 1426912782817649062L;

	private String priority;
	private String description;
	private String text;

	private Map<String, String> data;
	private Map<String, String> details;

	public DBEvent() {
		super();
		this.details = new HashMap<String, String>();
		this.data = new HashMap<String, String>();
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Event [event_code=" + eventCode + ", priority=" + priority + ", description=" + description + ", data="
				+ data + "]";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Map<String, String> getDetails() {
		return details;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;
	}

}
