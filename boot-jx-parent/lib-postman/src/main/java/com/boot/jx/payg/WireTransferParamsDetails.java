package com.boot.jx.payg;

import java.math.BigDecimal;

public class WireTransferParamsDetails {
	private BigDecimal activeMinutes;
	private String bankName;
	public BigDecimal getActiveMinutes() {
		return activeMinutes;
	}
	public void setActiveMinutes(BigDecimal activeMinutes) {
		this.activeMinutes = activeMinutes;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
}
