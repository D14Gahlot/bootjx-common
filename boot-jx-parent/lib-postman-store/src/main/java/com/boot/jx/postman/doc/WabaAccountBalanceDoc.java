package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.SimpleDocument;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;

@Document(collection ="WABA_ACCOUNT_BALANCE")
@TypeAlias("WabaAccountBalanceDoc")
public class WabaAccountBalanceDoc extends TimeStampDoc
implements Serializable, SimpleDocument, JsonIgnoreUnknown, JsonIgnoreNull {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2604878253251388988L;
	@Id
	String id;
	String wabaId;
	String currencyCode;
	long timeStamp;
	double depositAmt=0.0;
	double balanceAmt=0.0;
	long totalMsgCost;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getWabaId() {
		return wabaId;
	}
	public void setWabaId(String wabaId) {
		this.wabaId = wabaId;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public double getDepositAmt() {
		return depositAmt;
	}
	public void setDepositAmt(double depositAmt) {
		this.depositAmt = depositAmt;
	}
	public double getBalanceAmt() {
		return balanceAmt;
	}
	public void setBalanceAmt(double balanceAmt) {
		this.balanceAmt = balanceAmt;
	}
	public long getTotalMsgCost() {
		return totalMsgCost;
	}
	public void setTotalMsgCost(long totalMsgCost) {
		this.totalMsgCost = totalMsgCost;
	}
	
	

}
