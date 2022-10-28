package com.boot.jx.admin.dto;

public class DateWiseHourCountDto {
	String date;
	String hour;
	String channel;
	String msgType;
	long hourStamp;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public long getHourStamp() {
		return hourStamp;
	}
	public void setHourStamp(long hourStamp) {
		this.hourStamp = hourStamp;
	}

}
