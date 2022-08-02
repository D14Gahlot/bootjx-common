package com.boot.jx.account.dto;

public class ContactTypeCountDto {
	Object type;
	long totalCount;
	long timestamp;
	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
