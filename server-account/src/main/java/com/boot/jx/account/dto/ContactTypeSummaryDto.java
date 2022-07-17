package com.boot.jx.account.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactTypeSummaryDto {
	String tenant;
	String month;
	long  monthMinTimeStamp;
	long  monthMaxTimeStamp;
	Map<Object,Long> summaryCount;
	Map<Object,List<ContactTypeCountDto>> map;
	Map<String, Map<String, Long>> dateWiseSummaryCount;
	
	Map<Object,Map<Object,Object>> dateWiseCountMap = new HashMap<>();
	
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
	public Map<Object, Long> getSummaryCount() {
		return summaryCount;
	}
	public void setSummaryCount(Map<Object, Long> summaryCount) {
		this.summaryCount = summaryCount;
	}
	public Map<String, Map<String, Long>> getDateWiseSummaryCount() {
		return dateWiseSummaryCount;
	}
	public void setDateWiseSummaryCount(Map<String, Map<String, Long>> dateWiseSummaryCount) {
		this.dateWiseSummaryCount = dateWiseSummaryCount;
	}
	public Map<Object, Map<Object, Object>> getDateWiseCountMap() {
		return dateWiseCountMap;
	}
	public void setDateWiseCountMap(Map<Object, Map<Object, Object>> dateWiseCountMap) {
		this.dateWiseCountMap = dateWiseCountMap;
	}
	
}
