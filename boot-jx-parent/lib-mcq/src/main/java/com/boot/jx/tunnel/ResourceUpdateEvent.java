package com.boot.jx.tunnel;

import java.math.BigDecimal;
import java.util.Map;

public class ResourceUpdateEvent extends TunnelEvent {

	private static final long serialVersionUID = 1426912782817649062L;

	public static enum ResourceUpdateEventType {
		CREATED, DELETED, ACTIVATED, DEACTIVATED, MODIFIED, ADDED
	}

	public ResourceUpdateEvent(BigDecimal resourceId, ResourceUpdateEventType eventType) {
		super();
		this.resourceId = resourceId;
		this.eventType = eventType;
	}

	public ResourceUpdateEvent() {
	}

	protected BigDecimal resourceId;
	protected String resourceCode;
	protected String resourceStatus;

	protected ResourceUpdateEventType eventType;

	protected Map<String, String> data;

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public BigDecimal getResourceId() {
		return resourceId;
	}

	public void setResourceId(BigDecimal resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceCode() {
		return resourceCode;
	}

	public void setResourceCode(String resourceCode) {
		this.resourceCode = resourceCode;
	}

	public String getResourceStatus() {
		return resourceStatus;
	}

	public void setResourceStatus(String resourceStatus) {
		this.resourceStatus = resourceStatus;
	}

	public ResourceUpdateEventType getEventType() {
		return eventType;
	}

	public void setEventType(ResourceUpdateEventType eventType) {
		this.eventType = eventType;
	}

}
