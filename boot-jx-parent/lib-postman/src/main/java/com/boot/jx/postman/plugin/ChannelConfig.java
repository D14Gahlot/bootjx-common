package com.boot.jx.postman.plugin;

import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.ig.InstagramConfig;
import com.boot.jx.postman.plugin.TwitterPlugin.TwitterConfigDetails;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;
import com.boot.jx.postman.tg.TelegramConfigDetails;
import com.boot.jx.postman.wa360.WA360ConfigDetails;
import com.boot.utils.ArgUtil;

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

    private boolean isDisabled;
    private boolean isPushAllowed;
    private boolean isPushOnlyApproved;
    private boolean isPushFreeTextAllowed;
    private boolean isPushToNewContactAllowed;

    private String callbackPath;

    public void copy(AChannelDetails channelConfig) {
	this.name = channelConfig.getName();
	this.contactType = channelConfig.getContactType();
	this.channelType = channelConfig.getChannelType();
	this.channel = channelConfig.getChannel();
	this.channelKey = channelConfig.getChannelKey();
	this.lane = channelConfig.getLane();
	this.isPushAllowed = channelConfig.isPushAllowed();
	this.isPushOnlyApproved = channelConfig.isPushOnlyApproved();
	this.isPushFreeTextAllowed = channelConfig.isPushFreeTextAllowed();
	this.isPushToNewContactAllowed = channelConfig.isPushToNewContactAllowed();
	this.webhookUrl = ArgUtil.nonEmpty(channelConfig.getWebhookUrl(), this.webhookUrl);

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

    public ChannelConfig from(FacebookConfigDetails facebook) {
	this.copy(facebook);
	this.facebook = facebook;
	return this;
    }
    
    
    public ChannelConfig from(InstagramConfig instagram) {
    	this.copy(instagram);
    	this.instagram = instagram;
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

}
