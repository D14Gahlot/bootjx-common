package com.boot.jx.account.dto;

public class AccountDashBoardResponseDto {
	
	String domain;
	long totalInMsgExchanged;
	long totalOutMsgExchanged;
	long totalMsgExchanged;
	long totalTemplateMsgSent;
	long totalTemplateMsgDelivered;
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
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

}
