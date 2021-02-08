package com.boot.jx.admin.dto;

public class DashBoardRequestDto {
	Object contactType;
	long dateRange1;
	long dateReange2;
	
	public long getDateRange1() {
		return dateRange1;
	}
	public void setDateRange1(long dateRange1) {
		this.dateRange1 = dateRange1;
	}
	public long getDateReange2() {
		return dateReange2;
	}
	public void setDateReange2(long dateReange2) {
		this.dateReange2 = dateReange2;
	}
	public Object getContactType() {
		return contactType;
	}
	public void setContactType(Object contactType) {
		this.contactType = contactType;
	}
	
}
