package com.boot.jx.contak.dto;


import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "COMPANY_NOTP")
public class CompanyDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String companyId;

	@Indexed
	public String legalBusinessName;


	public String displayName;
	
	
	public String countryOfOperation;
	
	
	
	public String address;
	
	
	public String websiteUrl;
	
	
	
	public String coiFileUrl;
	
	
	public String gstFileUrl;
	
	
	public String panFileUrl;
	
	
	public String contactPersonName;
	
	
	public String contactPhoneNumber;
	
	
	public String contactPersonEmailId;
	
	
	public String password;
	
	public String companyTimeZone;

	public TimeStampIndex createdAt;

	public boolean isActive;

	@Indexed
	public String number;
	
	@Indexed
	public String apiKey;
	
	public String logoUrl;

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getLegalBusinessName() {
		return legalBusinessName;
	}

	public void setLegalBusinessName(String legalBusinessName) {
		this.legalBusinessName = legalBusinessName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCountryOfOperation() {
		return countryOfOperation;
	}

	public void setCountryOfOperation(String countryOfOperation) {
		this.countryOfOperation = countryOfOperation;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getCoiFileUrl() {
		return coiFileUrl;
	}

	public void setCoiFileUrl(String coiFileUrl) {
		this.coiFileUrl = coiFileUrl;
	}

	public String getGstFileUrl() {
		return gstFileUrl;
	}

	public void setGstFileUrl(String gstFileUrl) {
		this.gstFileUrl = gstFileUrl;
	}

	public String getPanFileUrl() {
		return panFileUrl;
	}

	public void setPanFileUrl(String panFileUrl) {
		this.panFileUrl = panFileUrl;
	}

	public String getContactPersonName() {
		return contactPersonName;
	}

	public void setContactPersonName(String contactPersonName) {
		this.contactPersonName = contactPersonName;
	}

	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public String getContactPersonEmailId() {
		return contactPersonEmailId;
	}

	public void setContactPersonEmailId(String contactPersonEmailId) {
		this.contactPersonEmailId = contactPersonEmailId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCompanyTimeZone() {
		return companyTimeZone;
	}

	public void setCompanyTimeZone(String companyTimeZone) {
		this.companyTimeZone = companyTimeZone;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}


	
	

}