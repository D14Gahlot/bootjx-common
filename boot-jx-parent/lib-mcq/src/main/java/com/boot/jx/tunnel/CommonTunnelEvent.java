package com.boot.jx.tunnel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonTunnelEvent extends TunnelEvent {
	private static final long serialVersionUID = 1426912782817649062L;
	private String priority;

	private Map<String, String> data;

	public CommonTunnelEvent() {
		super();
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

	@Override
	public String toString() {
		return "Event [priority=" + priority + ", data=" + data + "]";
	}

}
