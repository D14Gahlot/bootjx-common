package com.boot.jx.account.doc;

import java.io.Serializable;

public class CompanyDoc implements Serializable {

    private static final long serialVersionUID = -3354844112176554561L;

    private String businessName;
    private String businessType;
    private String websiteUrl;
    private String conactEmail;

    private String conactAddress;
    private String conactCity;
    private String conactPhone;
    private String conactCountry;
    private String conactPostalCode;

    private String businessAbout;

    public String getBusinessName() {
	return businessName;
    }

    public void setBusinessName(String businessName) {
	this.businessName = businessName;
    }

    public String getBusinessType() {
	return businessType;
    }

    public void setBusinessType(String businessType) {
	this.businessType = businessType;
    }

    public String getWebsiteUrl() {
	return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
	this.websiteUrl = websiteUrl;
    }

    public String getConactEmail() {
	return conactEmail;
    }

    public void setConactEmail(String conactEmail) {
	this.conactEmail = conactEmail;
    }

    public String getConactAddress() {
	return conactAddress;
    }

    public void setConactAddress(String conactAddress) {
	this.conactAddress = conactAddress;
    }

    public String getConactCity() {
	return conactCity;
    }

    public void setConactCity(String conactCity) {
	this.conactCity = conactCity;
    }

    public String getConactCountry() {
	return conactCountry;
    }

    public void setConactCountry(String conactCountry) {
	this.conactCountry = conactCountry;
    }

    public String getConactPostalCode() {
	return conactPostalCode;
    }

    public void setConactPostalCode(String conactPostalCode) {
	this.conactPostalCode = conactPostalCode;
    }

    public String getBusinessAbout() {
	return businessAbout;
    }

    public void setBusinessAbout(String businessAbout) {
	this.businessAbout = businessAbout;
    }

    public String getConactPhone() {
        return conactPhone;
    }

    public void setConactPhone(String conactPhone) {
        this.conactPhone = conactPhone;
    }

}
