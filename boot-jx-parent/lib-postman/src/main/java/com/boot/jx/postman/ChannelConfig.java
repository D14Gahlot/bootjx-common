package com.boot.jx.postman;

import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;

public class ChannelConfig extends AChannelConfig {

    private static final long serialVersionUID = -254797155595466825L;
    private String lane;

    private FacebookConfig facebook;
    private TwitterConfig twitter;
    private TelegramConfig telegram;
    private GupShupConfig gupshup;

    private boolean isPushAllowed;
    private boolean isPushOnlyApproved;
    private boolean isPushFreeTextAllowed;
    private boolean isPushToNewContactAllowed;

    public void copy(AChannelDetails channelConfig) {
	this.contactType = channelConfig.getContactType();
	this.channelType = channelConfig.getChannelType();
	this.channel = channelConfig.getChannel();
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

    public FacebookConfig getFacebook() {
	return facebook;
    }

    public void setFacebook(FacebookConfig facebook) {
	this.facebook = facebook;
    }

    public TwitterConfig getTwitter() {
	return twitter;
    }

    public void setTwitter(TwitterConfig twitter) {
	this.twitter = twitter;
    }

    public TelegramConfig getTelegram() {
	return telegram;
    }

    public void setTelegram(TelegramConfig telegram) {
	this.telegram = telegram;
    }

    public GupShupConfig getGupshup() {
	return gupshup;
    }

    public void setGupshup(GupShupConfig gupshup) {
	this.gupshup = gupshup;
    }

    public ChannelConfig from(FacebookConfig facebook) {
	this.copy(facebook);
	this.facebook = facebook;
	return this;
    }

    public ChannelConfig from(TwitterConfig twitter) {
	this.copy(twitter);
	this.twitter = twitter;
	return this;
    }

    public ChannelConfig from(TelegramConfig telegram) {
	this.copy(telegram);
	this.telegram = telegram;
	return this;
    }

    public ChannelConfig from(GupShupConfig gupshup) {
	this.copy(gupshup);
	this.gupshup = gupshup;
	return this;
    }

}
