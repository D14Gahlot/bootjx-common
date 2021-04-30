package com.boot.jx.postman.tg;

import java.io.Serializable;

public class TelegramConfig implements Serializable {

	private static final long serialVersionUID = -2397678752642150000L;
	private String handler;
	private String type;

	private String accessToken;
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

}
