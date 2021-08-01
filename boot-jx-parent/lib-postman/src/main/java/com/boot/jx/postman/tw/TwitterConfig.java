package com.boot.jx.postman.tw;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.fasterxml.jackson.annotation.JsonView;

public class TwitterConfig extends AChannelDetails {

    public TwitterConfig() {
	super(CHANNEL_TYPE.TWITTER);
    }

    private static final long serialVersionUID = -2397678752642150000L;
    private String handler;
    private String type;
    private String envName;

    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String consumerKey;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String consumerSecret;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String accessToken;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String accessTokenSecret;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String webhookUrl;

    public String getHandler() {
	return handler;
    }

    public void setHandler(String handler) {
	this.handler = handler;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getEnvName() {
	return envName;
    }

    public void setEnvName(String envName) {
	this.envName = envName;
    }

    public String getConsumerKey() {
	return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
	this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
	return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
	this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
	return accessToken;
    }

    public void setAccessToken(String accessToken) {
	this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
	return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
	this.accessTokenSecret = accessTokenSecret;
    }

    public String getWebhookUrl() {
	return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
	this.webhookUrl = webhookUrl;
    }

    @Override
    public String getLane() {
	return this.handler;
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
	return ContactType.TWITTER;
    }
}
