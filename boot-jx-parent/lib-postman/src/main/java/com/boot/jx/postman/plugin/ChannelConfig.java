package com.boot.jx.postman.plugin;

import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.tg.TelegramConfigDetails;
import com.boot.jx.postman.tw.TwitterConfigDetails;
import com.boot.jx.postman.wa360.WA360ConfigDetails;

public class ChannelConfig extends AChannelConfig {

    private static final long serialVersionUID = -254797155595466825L;
    private String lane;

    private FacebookConfigDetails facebook;
    private TwitterConfigDetails twitter;
    private TelegramConfigDetails telegram;
    private GupShupConfigDetails gupshup;
    private WA360ConfigDetails wa360d;

    private boolean isDisabled;
    private boolean isPushAllowed;
    private boolean isPushOnlyApproved;
    private boolean isPushFreeTextAllowed;
    private boolean isPushToNewContactAllowed;

    public void copy(AChannelDetails channelConfig) {
	this.contactType = channelConfig.getContactType();
	this.channelType = channelConfig.getChannelType();
	this.channel = channelConfig.getChannel();
	this.channelKey = channelConfig.getChannelKey();
	this.lane = channelConfig.getLane();
	this.isPushAllowed = channelConfig.isPushAllowed();
	this.isPushOnlyApproved = channelConfig.isPushOnlyApproved();
	this.isPushFreeTextAllowed = channelConfig.isPushFreeTextAllowed();
	this.isPushToNewContactAllowed = channelConfig.isPushToNewContactAllowed();

    }

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

    public ChannelConfig from(FacebookConfigDetails facebook) {
	this.copy(facebook);
	this.facebook = facebook;
	return this;
    }

    public ChannelConfig from(TwitterConfigDetails twitter) {
	this.copy(twitter);
	this.twitter = twitter;
	return this;
    }

    public ChannelConfig from(TelegramConfigDetails telegram) {
	this.copy(telegram);
	this.telegram = telegram;
	return this;
    }

    public ChannelConfig from(GupShupConfigDetails gupshup) {
	this.copy(gupshup);
	this.gupshup = gupshup;
	return this;
    }

    public boolean isDisabled() {
	return isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
	this.isDisabled = isDisabled;
    }

    public ChannelConfig disabled(boolean isDisabled) {
	this.isDisabled = isDisabled;
	return this;
    }

    public WA360ConfigDetails getWa360d() {
	return wa360d;
    }

    public void setWa360d(WA360ConfigDetails wa360d) {
	this.wa360d = wa360d;
    }

    public String getChannelKey() {
	return channelKey;
    }

    public void setChannelKey(String channelKey) {
	this.channelKey = channelKey;
    }

}
