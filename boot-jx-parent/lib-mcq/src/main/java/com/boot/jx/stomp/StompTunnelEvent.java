package com.boot.jx.stomp;

import com.boot.jx.AppParam;
import com.boot.jx.tunnel.TunnelEvent;

public class StompTunnelEvent extends TunnelEvent {
	private static final long serialVersionUID = 1426912782817649062L;

	private String topic;
	private String xsessionId;
	private String jsessionId;
	private String tagId;
	private String tenantToken;
	private Object data;
	private String appType;
	private String originator;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getXsessionId() {
		return xsessionId;
	}

	public void setXsessionId(String xsessionId) {
		this.xsessionId = xsessionId;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getTenantToken() {
		return tenantToken;
	}

	public void setTenantToken(String tenantToken) {
		this.tenantToken = tenantToken;
	}

	public String getJsessionId() {
		return jsessionId;
	}

	public void setJsessionId(String jsessionId) {
		this.jsessionId = jsessionId;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}

	public static StompTunnelEvent createInstance() {
		StompTunnelEvent instance = new StompTunnelEvent();
		instance.setOriginator(AppParam.APP_INSTANCE_UID.getValue());
		return instance;
	}
}
