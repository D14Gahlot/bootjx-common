package com.boot.jx.account.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WabaDateWiseBalanceDto {
	String tnt;
	String month;
	String currencyCode;
	long dateTimeStamp;
	double depostAmt=0;
	Integer totalCount=0;
	double totalCost=0;
	double balanceAmt=0;
	String wabaId;
	String number;
	String id;
	List<Map<String, Object>> countCostMap=new ArrayList<>();
	
	
	public long getDateTimeStamp() {
		return dateTimeStamp;
	}
	public void setDateTimeStamp(long dateTimeStamp) {
		this.dateTimeStamp = dateTimeStamp;
	}
	
	
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	public String getWabaId() {
		return wabaId;
	}
	public void setWabaId(String wabaId) {
		this.wabaId = wabaId;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public double getDepostAmt() {
		return depostAmt;
	}
	public void setDepostAmt(double depostAmt) {
		this.depostAmt = depostAmt;
	}
	public List<Map<String, Object>> getCountCostMap() {
		return countCostMap;
	}
	public void setCountCostMap(List<Map<String, Object>> countCostMap) {
		this.countCostMap = countCostMap;
	}
	public double getBalanceAmt() {
		return balanceAmt;
	}
	public void setBalanceAmt(double balanceAmt) {
		this.balanceAmt = balanceAmt;
	}
	public String getTnt() {
		return tnt;
	}
	public void setTnt(String tnt) {
		this.tnt = tnt;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
