package com.boot.jx.admin.dto;

public class MonthDto {
	String id;
	long timeStamp;
	long timeStampWithoutTime;
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public long getTimeStampWithoutTime() {
		return timeStampWithoutTime;
	}
	public void setTimeStampWithoutTime(long timeStampWithoutTime) {
		this.timeStampWithoutTime = timeStampWithoutTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
