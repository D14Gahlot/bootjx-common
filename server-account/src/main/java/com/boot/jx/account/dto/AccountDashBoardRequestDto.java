package com.boot.jx.account.dto;

public class AccountDashBoardRequestDto {
	Object contactType;
	long dateRange1;
	long dateRange2;
	
	
	
	public long getDateRange1() {
		return dateRange1;
	}
	public void setDateRange1(long dateRange1) {
		this.dateRange1 = dateRange1;
	}
	
	public Object getContactType() {
		return contactType;
	}
	public void setContactType(Object contactType) {
		this.contactType = contactType;
	}
	public long getDateRange2() {
		return dateRange2;
	}
	public void setDateRange2(long dateRange2) {
		this.dateRange2 = dateRange2;
	}
	
}
