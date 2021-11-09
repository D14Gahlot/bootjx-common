package com.boot.jx.admin.dto;

import java.util.Map;

public class DashBoardResponseDto {
	
	
	Object contactType;
	String filter;
	String agentName;
	long totalInMsgExchanged;
	long totalOutMsgExchanged;
	long totalMsgExchanged;
	long totalTemplateMsgSent;
	long totalTemplateMsgDelivered;
	long uniqueConversation;
	long openConversation;
	long resolvedConversation;
	long converDuration;
	double startLag;
	long botScore=0;
	double botClosure=0;
	PeakLoadDto peakLoad;
	Map<Object,Object> graphApiDetails;
	Map<Object,Object> graphApiDetailsV1;
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
	
	public long getTotalTemplateMsgSent() {
		return totalTemplateMsgSent;
	}

	public void setTotalTemplateMsgSent(long totalTemplateMsgSent) {
		this.totalTemplateMsgSent = totalTemplateMsgSent;
	}
	
	public long getTotalTemplateMsgDelivered() {
		return totalTemplateMsgDelivered;
	}

	public void setTotalTemplateMsgDelivered(long totalTemplateMsgDelivered) {
		this.totalTemplateMsgDelivered = totalTemplateMsgDelivered;
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

	public Map<Object, Object> getGraphApiDetails() {
		return graphApiDetails;
	}

	public void setGraphApiDetails(Map<Object, Object> graphApiDetails) {
		this.graphApiDetails = graphApiDetails;
	}

	public long getBotScore() {
		return botScore;
	}

	public void setBotScore(long botScore) {
		this.botScore = botScore;
	}

	public double getBotClosure() {
		return botClosure;
	}

	public void setBotClosure(double botClosure) {
		this.botClosure = botClosure;
	}

	public long getResolvedConversation() {
		return resolvedConversation;
	}

	public void setResolvedConversation(long resolvedConversation) {
		this.resolvedConversation = resolvedConversation;
	}

	public Map<Object, Object> getGraphApiDetailsV1() {
		return graphApiDetailsV1;
	}

	public void setGraphApiDetailsV1(Map<Object, Object> graphApiDetailsV1) {
		this.graphApiDetailsV1 = graphApiDetailsV1;
	}

	

}
