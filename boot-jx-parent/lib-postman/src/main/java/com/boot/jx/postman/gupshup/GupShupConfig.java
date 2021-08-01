package com.boot.jx.postman.gupshup;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GupShupConfig extends AChannelDetails {

    public GupShupConfig() {
	super(CHANNEL_TYPE.GUPSHUP);
    }

    private static final long serialVersionUID = -2397678752642150000L;
    private String number;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String notifyId;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String notifyPass;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String chatId;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String chatPass;

    public String getNumber() {
	return number;
    }

    public void setNumber(String number) {
	this.number = number;
    }

    public String getNotifyId() {
	return notifyId;
    }

    public void setNotifyId(String notifyId) {
	this.notifyId = notifyId;
    }

    public String getNotifyPass() {
	return notifyPass;
    }

    public void setNotifyPass(String notifyPass) {
	this.notifyPass = notifyPass;
    }

    public String getChatId() {
	return chatId;
    }

    public void setChatId(String chatId) {
	this.chatId = chatId;
    }

    public String getChatPass() {
	return chatPass;
    }

    public void setChatPass(String chatPass) {
	this.chatPass = chatPass;
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
	return true;
    }

    @Override
    public boolean isPushFreeTextAllowed() {
	return false;
    }

    @Override
    public boolean isPushToNewContactAllowed() {
	return true;
    }

    @Override
    public ContactType getContactType() {
	return ContactType.WHATSAPP;
    }

    @Override
    public String getChannel() {
	return "GUPSHUPW";
    }
}
