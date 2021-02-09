package com.boot.jx.admin.dto;

public class DashBoardResponseDto {
	
	
	Object contactType;
	String filter;
	long totalInMsgExchanged;
	long totalOutMsgExchanged;
	long totalMsgExchanged;
	long uniqueConversation;
	long todayMsgExchanged;
	PeakLoadDto peakLoad;
	
	
	

	public Object getContactType() {
		return contactType;
	}

	public void setContactType(Object contactType) {
		this.contactType = contactType;
	}

	public long getTotalInMsgExchanged() {
		return totalInMsgExchanged;
	}

	public void setTotalInMsgExchanged(long totalInMsgExchanged) {
		this.totalInMsgExchanged = totalInMsgExchanged;
	}

	public long getTotalOutMsgExchanged() {
		return totalOutMsgExchanged;
	}

	public void setTotalOutMsgExchanged(long totalOutMsgExchanged) {
		this.totalOutMsgExchanged = totalOutMsgExchanged;
	}

	public long getTotalMsgExchanged() {
		return totalMsgExchanged;
	}

	public void setTotalMsgExchanged(long totalMsgExchanged) {
		this.totalMsgExchanged = totalMsgExchanged;
	}

	public long getUniqueConversation() {
		return uniqueConversation;
	}

	public void setUniqueConversation(long uniqueConversation) {
		this.uniqueConversation = uniqueConversation;
	}

	public long getTodayMsgExchanged() {
		return todayMsgExchanged;
	}

	public void setTodayMsgExchanged(long todayMsgExchanged) {
		this.todayMsgExchanged = todayMsgExchanged;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public PeakLoadDto getPeakLoad() {
		return peakLoad;
	}

	public void setPeakLoad(PeakLoadDto peakLoad) {
		this.peakLoad = peakLoad;
	}

	

}
