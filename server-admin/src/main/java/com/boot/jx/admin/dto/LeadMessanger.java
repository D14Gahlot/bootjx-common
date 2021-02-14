package com.boot.jx.admin.dto;

public class LeadMessanger {
	Object contactType;
	long noOfMessage;
	long totalContactMessage;
	double percentage;
	public Object getContactType() {
		return contactType;
	}
	public void setContactType(Object contactType) {
		this.contactType = contactType;
	}
	public long getNoOfMessage() {
		return noOfMessage;
	}
	public void setNoOfMessage(long noOfMessage) {
		this.noOfMessage = noOfMessage;
	}
	public long getTotalContactMessage() {
		return totalContactMessage;
	}
	public void setTotalContactMessage(long totalContactMessage) {
		this.totalContactMessage = totalContactMessage;
	}
	public double getPercentage() {
		return percentage;
	}
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
	
}
