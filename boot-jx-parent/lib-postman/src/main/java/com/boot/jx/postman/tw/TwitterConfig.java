package com.boot.jx.postman.tw;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMEnvironment.PMConnectorConfig;
import com.fasterxml.jackson.annotation.JsonView;

public class TwitterConfig implements PMConnectorConfig {

	private static final long serialVersionUID = -2397678752642150000L;
	private String handler;
	private String type;
	private String envName;

	@JsonView(PMConnectorConfig.Protected.class)
	private String consumerKey;
	@JsonView(PMConnectorConfig.Protected.class)
	private String consumerSecret;
	@JsonView(PMConnectorConfig.Protected.class)
	private String accessToken;
	@JsonView(PMConnectorConfig.Protected.class)
	private String accessTokenSecret;
	@JsonView(PMConnectorConfig.Protected.class)
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
