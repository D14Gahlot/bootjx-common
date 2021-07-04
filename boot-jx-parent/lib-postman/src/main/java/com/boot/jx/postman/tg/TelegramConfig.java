package com.boot.jx.postman.tg;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMEnvironment.PMConnectorConfig;
import com.fasterxml.jackson.annotation.JsonView;

public class TelegramConfig implements PMConnectorConfig {

	private static final long serialVersionUID = -2397678752642150000L;
	private String handler;
	private String type;

	@JsonView(PMConnectorConfig.Protected.class)
	private String accessToken;
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

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
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
		return ContactType.TELEGRAM;
	}
}
