package com.boot.jx.account.doc;

import java.io.Serializable;

public class SocialDoc implements Serializable {

    private static final long serialVersionUID = -3354844112176554561L;

    private String logo;
    private String facebookBMId;
    private String facebookPageId;
    private String facebookPage;
    private String twitterHandler;
    private String instagramHandler;
    private String whatsApp;
    private String telegram;
    private String customerSupportEmail;
    private String customerSupportPhone;

    public String getFacebookBMId() {
	return facebookBMId;
    }

    public void setFacebookBMId(String facebookBMId) {
	this.facebookBMId = facebookBMId;
    }

    public String getFacebookPageId() {
	return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
	this.facebookPageId = facebookPageId;
    }

    public String getFacebookPage() {
	return facebookPage;
    }

    public void setFacebookPage(String facebookPage) {
	this.facebookPage = facebookPage;
    }

    public String getTwitterHandler() {
	return twitterHandler;
    }

    public void setTwitterHandler(String twitterHandler) {
	this.twitterHandler = twitterHandler;
    }

    public String getInstagramHandler() {
	return instagramHandler;
    }

    public void setInstagramHandler(String instagramHandler) {
	this.instagramHandler = instagramHandler;
    }

    public String getWhatsApp() {
	return whatsApp;
    }

    public void setWhatsApp(String whatsApp) {
	this.whatsApp = whatsApp;
    }

    public String getTelegram() {
	return telegram;
    }

    public void setTelegram(String telegram) {
	this.telegram = telegram;
    }

    public String getCustomerSupportEmail() {
	return customerSupportEmail;
    }

    public void setCustomerSupportEmail(String customerSupportEmail) {
	this.customerSupportEmail = customerSupportEmail;
    }

    public String getCustomerSupportPhone() {
	return customerSupportPhone;
    }

    public void setCustomerSupportPhone(String customerSupportPhone) {
	this.customerSupportPhone = customerSupportPhone;
    }

    public String getLogo() {
	return logo;
    }

    public void setLogo(String logo) {
	this.logo = logo;
    }
}
