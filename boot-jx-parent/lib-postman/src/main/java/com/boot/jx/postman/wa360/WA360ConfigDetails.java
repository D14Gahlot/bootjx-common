package com.boot.jx.postman.wa360;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.fasterxml.jackson.annotation.JsonView;

public class WA360ConfigDetails extends AChannelDetails {

    public WA360ConfigDetails() {
	super(CHANNEL_TYPE.WA_360D);
    }

    private static final long serialVersionUID = -2397678752642150000L;
    private String number;

    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String apiKey;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String webhookUrl;

    public String getWebhookUrl() {
	return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
	this.webhookUrl = webhookUrl;
    }

    @Override
    public String getLane() {
	return this.number;
    }

    @Override
    public boolean isPushAllowed() {
	return true;
    }

    @Override
    public boolean isPushOnlyApproved() {
	return false;
    }

    @Override
    public boolean isPushFreeTextAllowed() {
	return true;
    }

    @Override
    public boolean isPushToNewContactAllowed() {
	return false;
    }

    @Override
    public ContactType getContactType() {
	return ContactType.WHATSAPP;
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
}
