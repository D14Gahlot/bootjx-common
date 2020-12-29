package com.boot.jx.payg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class TransactionHisDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BigDecimal idno;

	private BigDecimal customerReference;

	private BigDecimal documentNumber;

	private String beneficaryAccountNumber;

	private String beneficaryBankName;

	private String beneficaryBranchName;

	private String beneficaryName;

	private String beneficaryCorespondingBankName;

	private Date documentDate;

	private BigDecimal documentFinanceYear;
	
	private BigDecimal documentCode;

	private String foreignCurrencyCode;

	private BigDecimal foreignTransactionAmount;

	private String currencyQuoteName;

	private String serviceDescription;

	private String transactionStatusDesc;

	private String transactionTypeDesc;

	private BigDecimal collectionDocumentNo;

	private BigDecimal collectionDocumentCode;

	private BigDecimal collectionDocumentFinYear;

	private String branchDesc;
	
	private String trnxIdNumber;
	
	private BigDecimal customerId;
	
	private BigDecimal applicationCountryId;
	private BigDecimal companyId;
	private BigDecimal currencyId;
	private BigDecimal languageId;
	

	private BigDecimal beneficiaryRelationSeqId;
	
	private BigDecimal localTrnxAmount;
	
	
	private BigDecimal sourceOfIncomeId;
	
	private String purposeOfTrnx;
	
	private List<BeneficiaryErrStatusDto> beneficiaryErrorStatus;
	
	private String transactionReference;

    private BigDecimal bankRuleFieldId;
    
    private BigDecimal srlId;

	private Boolean beneIsActive;
	
	private BigDecimal exRateApplied;
	
	private BigDecimal exRateReversed;
	
	private String receiptNumber;
	
	private String receiptType;
	
	
	public String getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}

	public BigDecimal getIdno() {
		return idno;
	}

	public void setIdno(BigDecimal idno) {
		this.idno = idno;
	}

	public BigDecimal getCustomerReference() {
		return customerReference;
	}

	public void setCustomerReference(BigDecimal customerReference) {
		this.customerReference = customerReference;
	}

	public BigDecimal getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(BigDecimal documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getBeneficaryAccountNumber() {
		return beneficaryAccountNumber;
	}

	public void setBeneficaryAccountNumber(String beneficaryAccountNumber) {
		this.beneficaryAccountNumber = beneficaryAccountNumber;
	}

	public String getBeneficaryBankName() {
		return beneficaryBankName;
	}

	public void setBeneficaryBankName(String beneficaryBankName) {
		this.beneficaryBankName = beneficaryBankName;
	}

	public String getBeneficaryBranchName() {
		return beneficaryBranchName;
	}

	public void setBeneficaryBranchName(String beneficaryBranchName) {
		this.beneficaryBranchName = beneficaryBranchName;
	}

	public String getBeneficaryName() {
		return beneficaryName;
	}

	public void setBeneficaryName(String beneficaryName) {
		this.beneficaryName = beneficaryName;
	}

	public String getBeneficaryCorespondingBankName() {
		return beneficaryCorespondingBankName;
	}

	public void setBeneficaryCorespondingBankName(String beneficaryCorespondingBankName) {
		this.beneficaryCorespondingBankName = beneficaryCorespondingBankName;
	}

	public Date getDocumentDate() {
		return documentDate;
	}

	public void setDocumentDate(Date documentDate) {
		this.documentDate = documentDate;
	}

	public BigDecimal getDocumentFinanceYear() {
		return documentFinanceYear;
	}

	public void setDocumentFinanceYear(BigDecimal documentFinanceYear) {
		this.documentFinanceYear = documentFinanceYear;
	}

	public String getForeignCurrencyCode() {
		return foreignCurrencyCode;
	}

	public void setForeignCurrencyCode(String foreignCurrencyCode) {
		this.foreignCurrencyCode = foreignCurrencyCode;
	}

	public BigDecimal getForeignTransactionAmount() {
		return foreignTransactionAmount;
	}

	public void setForeignTransactionAmount(BigDecimal foreignTransactionAmount) {
		this.foreignTransactionAmount = foreignTransactionAmount;
	}

	public String getCurrencyQuoteName() {
		return currencyQuoteName;
	}

	public void setCurrencyQuoteName(String currencyQuoteName) {
		this.currencyQuoteName = currencyQuoteName;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public String getTransactionStatusDesc() {
		return transactionStatusDesc;
	}

	public void setTransactionStatusDesc(String transactionStatusDesc) {
		this.transactionStatusDesc = transactionStatusDesc;
	}

	public String getTransactionTypeDesc() {
		return transactionTypeDesc;
	}

	public void setTransactionTypeDesc(String transactionTypeDesc) {
		this.transactionTypeDesc = transactionTypeDesc;
	}

	public BigDecimal getCollectionDocumentNo() {
		return collectionDocumentNo;
	}

	public void setCollectionDocumentNo(BigDecimal collectionDocumentNo) {
		this.collectionDocumentNo = collectionDocumentNo;
	}

	public BigDecimal getCollectionDocumentCode() {
		return collectionDocumentCode;
	}

	public void setCollectionDocumentCode(BigDecimal collectionDocumentCode) {
		this.collectionDocumentCode = collectionDocumentCode;
	}

	public BigDecimal getCollectionDocumentFinYear() {
		return collectionDocumentFinYear;
	}

	public void setCollectionDocumentFinYear(BigDecimal collectionDocumentFinYear) {
		this.collectionDocumentFinYear = collectionDocumentFinYear;
	}

	public String getBranchDesc() {
		return branchDesc;
	}

	public void setBranchDesc(String branchDesc) {
		this.branchDesc = branchDesc;
	}

	public String getTrnxIdNumber() {
		return trnxIdNumber;
	}

	public void setTrnxIdNumber(String trnxIdNumber) {
		this.trnxIdNumber = trnxIdNumber;
	}

	public BigDecimal getCustomerId() {
		return customerId;
	}

	public void setCustomerId(BigDecimal customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getApplicationCountryId() {
		return applicationCountryId;
	}

	public void setApplicationCountryId(BigDecimal applicationCountryId) {
		this.applicationCountryId = applicationCountryId;
	}

	public BigDecimal getCompanyId() {
		return companyId;
	}

	public void setCompanyId(BigDecimal companyId) {
		this.companyId = companyId;
	}

	public BigDecimal getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(BigDecimal currencyId) {
		this.currencyId = currencyId;
	}

	public BigDecimal getLanguageId() {
		return languageId;
	}

	public void setLanguageId(BigDecimal languageId) {
		this.languageId = languageId;
	}

	public BigDecimal getBeneficiaryRelationSeqId() {
		return beneficiaryRelationSeqId;
	}

	public void setBeneficiaryRelationSeqId(BigDecimal beneficiaryRelationSeqId) {
		this.beneficiaryRelationSeqId = beneficiaryRelationSeqId;
	}

	public BigDecimal getLocalTrnxAmount() {
		return localTrnxAmount;
	}

	public void setLocalTrnxAmount(BigDecimal localTrnxAmount) {
		this.localTrnxAmount = localTrnxAmount;
	}

	public BigDecimal getSourceOfIncomeId() {
		return sourceOfIncomeId;
	}

	public void setSourceOfIncomeId(BigDecimal sourceOfIncomeId) {
		this.sourceOfIncomeId = sourceOfIncomeId;
	}

	public String getPurposeOfTrnx() {
		return purposeOfTrnx;
	}

	public void setPurposeOfTrnx(String purposeOfTrnx) {
		this.purposeOfTrnx = purposeOfTrnx;
	}

	public List<BeneficiaryErrStatusDto> getBeneficiaryErrorStatus() {
		return beneficiaryErrorStatus;
	}

	public void setBeneficiaryErrorStatus(
			List<BeneficiaryErrStatusDto> beneficiaryErrorStatus) {
		this.beneficiaryErrorStatus = beneficiaryErrorStatus;
	}

    /**
     * @return the bankRuleFieldId
     */
    public BigDecimal getBankRuleFieldId() {
        return bankRuleFieldId;
    }

    /**
     * @param bankRuleFieldId the bankRuleFieldId to set
     */
    public void setBankRuleFieldId(BigDecimal bankRuleFieldId) {
        this.bankRuleFieldId = bankRuleFieldId;
    }

    /**
     * @return the srlId
     */
    public BigDecimal getSrlId() {
        return srlId;
    }

    /**
     * @param srlId the srlId to set
     */
    public void setSrlId(BigDecimal srlId) {
        this.srlId = srlId;
    }


    /**
     * @return the beneIsActive
     */
    public Boolean getBeneIsActive() {
        return beneIsActive;
    }

    /**
     * @param beneIsActive the beneIsActive to set
     */
    public void setBeneIsActive(Boolean beneIsActive) {
        this.beneIsActive = beneIsActive;
    }

	public BigDecimal getExRateApplied() {
		return exRateApplied;
	}

	public void setExRateApplied(BigDecimal exRateApplied) {
		this.exRateApplied = exRateApplied;
	}

	public BigDecimal getExRateReversed() {
		return exRateReversed;
	}

	public void setExRateReversed(BigDecimal exRateReversed) {
		this.exRateReversed = exRateReversed;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public BigDecimal getDocumentCode() {
		return documentCode;
	}

	public void setDocumentCode(BigDecimal documentCode) {
		this.documentCode = documentCode;
	}

	public String getReceiptType() {
		return receiptType;
	}

	public void setReceiptType(String receiptType) {
		this.receiptType = receiptType;
	}
	
	
	
}
