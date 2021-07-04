package com.boot.jx.postman.fb;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMEnvironment.PMConnectorConfig;
import com.fasterxml.jackson.annotation.JsonView;

public class FacebookConfig implements PMConnectorConfig {

	private static final long serialVersionUID = -2397678752642150000L;
	private String pageId;
	private String type;

	@JsonView(PMConnectorConfig.Protected.class)
	private String accessToken;
	@JsonView(PMConnectorConfig.Protected.class)
	private String verifyToken;
	@JsonView(PMConnectorConfig.Protected.class)
	private String appSecret;

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
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

	public String getVerifyToken() {
		return verifyToken;
	}

	public void setVerifyToken(String verifyToken) {
		this.verifyToken = verifyToken;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	@Override
	public String getLane() {
		return this.pageId;
	}

	@Override
	public boolean isPushAllowed() {
		return false;
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
		return false;
	}

	@Override
	public ContactType getContactType() {
		return ContactType.FACEBOOK;
	}

	@Override
	public String getChannel() {
		return null;
	}

}
