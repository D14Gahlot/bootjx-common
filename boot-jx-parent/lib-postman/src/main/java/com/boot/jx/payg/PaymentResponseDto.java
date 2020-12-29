package com.boot.jx.payg;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.boot.jx.dict.payg.KioskReciept;

public class PaymentResponseDto extends PayGModel {

	private static final long serialVersionUID = -7991187321598015743L;

	public static final String PAYMENT_CAPTURE_URL = "/callback/payg/payment/capture";
	public static final String PAYMENT_IPOS_FAIL_URL = "/callback/payg/payment/failure";

	@NotNull
	BigDecimal customerId;
	@NotNull
	BigDecimal companyId;

	BigDecimal applicationId;

	@NotNull
	String resultCode;
	@NotNull
	String transactionId;
	String tranData;
	String auth_appNo;
	@NotNull
	String referenceId;
	String postDate;

	String userName;
	String product;
	String payId;

	String paymentMode;
	String amount;


//	CashTrnxModel cash;
	PayGParams params;
//	IPosReceipt iposReceipt;
	TransactionHisDTO transaction;
	KioskReciept kioskReciept;
	
	
	/**
	 * Local Payment Reference id, is used to track payment in local application
	 */
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getAuth_appNo() {
		return auth_appNo;
	}

	public void setAuth_appNo(String auth_appNo) {
		this.auth_appNo = auth_appNo;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getPostDate() {
		return postDate;
	}

	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}

	public String getTranData() {
		return tranData;
	}

	public void setTranData(String tranData) {
		this.tranData = tranData;
	}

	public BigDecimal getCustomerId() {
		return customerId;
	}

	public void setCustomerId(BigDecimal customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getCompanyId() {
		return companyId;
	}

	public void setCompanyId(BigDecimal companyId) {
		this.companyId = companyId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	@Override
	public String toString() {
		return "PaymentResponseDto [customerId=" + customerId + ", companyId=" + companyId + ", resultCode="
				+ resultCode + ", transactionId=" + transactionId + ", tranData=" + tranData + ", auth_appNo="
				+ auth_appNo + ", referenceId=" + referenceId + ", postDate=" + postDate + ", userName=" + userName
				+ ", product=" + product + "]";
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public BigDecimal getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(BigDecimal applicationId) {
		this.applicationId = applicationId;
	}

	public void importFrom(PayGParams payGParams) {
		this.setPayId(payGParams.getPayId());
		this.setAmount(payGParams.getAmount());
		this.params = payGParams;

	}

//	public CashTrnxModel getCash() {
//		return cash;
//	}
//
//	public void setCash(CashTrnxModel cash) {
//		this.cash = cash;
//	}

	public PayGParams getParams() {
		return params;
	}

	public void setParams(PayGParams params) {
		this.params = params;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

//	public IPosReceipt getIposReceipt() {
//		return iposReceipt;
//	}
//
//	public void setIposReceipt(IPosReceipt iposReceipt) {
//		this.iposReceipt = iposReceipt;
//	}

	public TransactionHisDTO getTransaction() {
		return transaction;
	}

	public void setTransaction(TransactionHisDTO transaction) {
		this.transaction = transaction;
	}

	public KioskReciept getKioskReciept() {
		return kioskReciept;
	}

	public void setKioskReciept(KioskReciept kioskReciept) {
		this.kioskReciept = kioskReciept;
	}
	
}
