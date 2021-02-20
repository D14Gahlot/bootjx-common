package com.boot.jx.admin.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DashBoardResponseDto {
	
	
	Object contactType;
	String filter;
	String agentName;
	long totalInMsgExchanged;
	long totalOutMsgExchanged;
	long totalMsgExchanged;
	long uniqueConversation;
	long openConversation;
	long converDuration;
	double startLag;
	PeakLoadDto peakLoad;
	Map<Object,Object> msgCountLst;
	LeadMessanger leadMessanger;
	
	
	
	

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

	public Map<Object, Object> getMsgCountLst() {
		return msgCountLst;
	}

	public void setMsgCountLst(Map<Object, Object> msgCountLst) {
		this.msgCountLst = msgCountLst;
	}

	public LeadMessanger getLeadMessanger() {
		return leadMessanger;
	}

	public void setLeadMessanger(LeadMessanger leadMessanger) {
		this.leadMessanger = leadMessanger;
	}

	public long getOpenConversation() {
		return openConversation;
	}

	public void setOpenConversation(long openConversation) {
		this.openConversation = openConversation;
	}

	public long getConverDuration() {
		return converDuration;
	}

	public void setConverDuration(long converDuration) {
		this.converDuration = converDuration;
	}

	public double getStartLag() {
		return startLag;
	}

	public void setStartLag(double startLag) {
		this.startLag = startLag;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	

}
