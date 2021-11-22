package com.boot.jx.postman.ig;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

public class InstagramConfig extends AChannelDetails {

    public InstagramConfig() {
	super(CHANNEL_TYPE.INSTAGRAM);
    }

    private static final long serialVersionUID = -2397678752642150000L;
    private String pageId;
    private String handler;
    private String type;

    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String accessToken;
    @JsonView(PMEnvironment.ProtectedProperty.class)
    private String verifyToken;
    @JsonView(PMEnvironment.ProtectedProperty.class)
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
	return ContactType.INSTAGRAM;
    }

    @Override
    public String getChannel() {
	return null;
    }

    public String getHandler() {
	return handler;
    }

    public void setHandler(String handler) {
	this.handler = handler;
    }

    @Override
    public String getName() {
	if (!ArgUtil.is(this.name)) {
	    if (ArgUtil.is(this.handler)) {
		return this.handler;
	    }
	    return String.format("FB %s", this.getLane());
	}
	return name;
    }

}
