package com.boot.jx.admin.dto;

import java.util.Map;

public class WabaSummaryDocDto {
	String id;
	String domain;
	String date;
	String channel;
	String type;
	String country;
	String lane;
	private Map<String, Object> pricing;
    Map<String, Object> meta;
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
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLane() {
		return lane;
	}
	public void setLane(String lane) {
		this.lane = lane;
	}
	public Map<String, Object> getPricing() {
		return pricing;
	}
	public void setPricing(Map<String, Object> pricing) {
		this.pricing = pricing;
	}
	

}
