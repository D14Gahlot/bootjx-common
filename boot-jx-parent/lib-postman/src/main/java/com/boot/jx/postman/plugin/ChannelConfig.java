package com.boot.jx.postman.plugin;

import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.plugin.FacebookPlugin.FacebookConfigDetails;
import com.boot.jx.postman.plugin.InstagramPlugin.InstagramConfig;
import com.boot.jx.postman.plugin.TelegramPlugin.TelegramConfigDetails;
import com.boot.jx.postman.plugin.TwitterPlugin.TwitterConfigDetails;
import com.boot.jx.postman.plugin.WA360Plugin.WA360ConfigDetails;
import com.boot.jx.postman.plugin.WAGupShupPlugin.GupShupConfigDetails;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;

public class ChannelConfig extends AChannelConfig {

    private static final long serialVersionUID = -254797155595466825L;
    private String lane;

    private FacebookConfigDetails facebook;
    private TwitterConfigDetails twitter;
    private TelegramConfigDetails telegram;
    private GupShupConfigDetails gupshup;
    private InstagramConfig instagram;
    private WA360ConfigDetails wa360d;
    private WebConfigDetails web;

    private boolean isPushAllowed;
    private boolean isPushOnlyApproved;
    private boolean isPushFreeTextAllowed;
    private boolean isPushToNewContactAllowed;
    private boolean isWebhookManual;

    private String callbackPath;

    public String getLane() {
	return lane;
    }

    @Override
    public boolean isPushAllowed() {
	return this.isPushAllowed;
    }

    @Override
    public boolean isPushOnlyApproved() {
	return this.isPushOnlyApproved;
    }

    @Override
    public boolean isPushFreeTextAllowed() {
	return this.isPushFreeTextAllowed;
    }

    @Override
    public boolean isPushToNewContactAllowed() {
	return this.isPushToNewContactAllowed;
    }

    public void setLane(String lane) {
	this.lane = lane;
    }

    public FacebookConfigDetails getFacebook() {
	return facebook;
    }

    public void setFacebook(FacebookConfigDetails facebook) {
	this.facebook = facebook;
    }

    public InstagramConfig getInstagram() {
	return instagram;
    }

    public void setInstagram(InstagramConfig instagram) {
	this.instagram = instagram;
    }

    public TwitterConfigDetails getTwitter() {
	return twitter;
    }

    public void setTwitter(TwitterConfigDetails twitter) {
	this.twitter = twitter;
    }

    public TelegramConfigDetails getTelegram() {
	return telegram;
    }

    public void setTelegram(TelegramConfigDetails telegram) {
	this.telegram = telegram;
    }

    public GupShupConfigDetails getGupshup() {
	return gupshup;
    }

    public void setGupshup(GupShupConfigDetails gupshup) {
	this.gupshup = gupshup;
    }

    public ChannelConfig disabled(boolean isDisabled) {
	this.setDisabled(isDisabled);
	return this;
    }

    public WA360ConfigDetails getWa360d() {
	return wa360d;
    }

    public void setWa360d(WA360ConfigDetails wa360d) {
	this.wa360d = wa360d;
    }

    public WebConfigDetails getWeb() {
	return web;
    }

    public void setWeb(WebConfigDetails web) {
	this.web = web;
    }

    public String getCallbackPath() {
	return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
	this.callbackPath = callbackPath;
    }

    public void setPushAllowed(boolean isPushAllowed) {
	this.isPushAllowed = isPushAllowed;
    }

    public void setPushOnlyApproved(boolean isPushOnlyApproved) {
	this.isPushOnlyApproved = isPushOnlyApproved;
    }

    public void setPushFreeTextAllowed(boolean isPushFreeTextAllowed) {
	this.isPushFreeTextAllowed = isPushFreeTextAllowed;
    }

    public void setPushToNewContactAllowed(boolean isPushToNewContactAllowed) {
	this.isPushToNewContactAllowed = isPushToNewContactAllowed;
    }

    @Override
    public boolean isWebhookManual() {
	return this.isWebhookManual;
    }

    public void setWebhookManual(boolean isWebhookManual) {
	this.isWebhookManual = isWebhookManual;
    }

}
