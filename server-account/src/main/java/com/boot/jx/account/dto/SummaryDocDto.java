package com.boot.jx.account.dto;

import java.util.Map;

public class SummaryDocDto {
	String id;
	String domain;
	String date;
	String channel;
	String type;
    Map<String, Object> meta;
    Map<Object,Map<String,Long>> hourWiseCount;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<String, Object> getMeta() {
		return meta;
	}
	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}
	public Map<Object, Map<String, Long>> getHourWiseCount() {
		return hourWiseCount;
	}
	public void setHourWiseCount(Map<Object, Map<String, Long>> hourWiseCount) {
		this.hourWiseCount = hourWiseCount;
	}
	
	

}
