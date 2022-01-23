package com.boot.jx.admin.dto;

import java.util.List;
import java.util.Map;

public class ContactTypeSummaryDto {
	String tenant;
	String month;
	long  monthMinTimeStamp;
	long  monthMaxTimeStamp;
	Map<Object,List<ContactTypeCountDto>> map;
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public long getMonthMinTimeStamp() {
		return monthMinTimeStamp;
	}
	public void setMonthMinTimeStamp(long monthMinTimeStamp) {
		this.monthMinTimeStamp = monthMinTimeStamp;
	}
	public long getMonthMaxTimeStamp() {
		return monthMaxTimeStamp;
	}
	public void setMonthMaxTimeStamp(long monthMaxTimeStamp) {
		this.monthMaxTimeStamp = monthMaxTimeStamp;
	}
	public Map<Object, List<ContactTypeCountDto>> getMap() {
		return map;
	}
	public void setMap(Map<Object, List<ContactTypeCountDto>> map) {
		this.map = map;
	}
	
}
