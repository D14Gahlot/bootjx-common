package com.boot.jx.postman.plugin;

import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.EmailPlugin.EmailConfigDetails;
import com.boot.jx.postman.plugin.FacebookPlugin.FacebookConfigDetails;
import com.boot.jx.postman.plugin.FirebasePlugin.FirebaseConfigDetails;
import com.boot.jx.postman.plugin.InstagramPlugin.InstagramConfig;
import com.boot.jx.postman.plugin.OAPlugin.OAConfigDetails;
import com.boot.jx.postman.plugin.OutlookPlugin.OutlookConfigDetails;
import com.boot.jx.postman.plugin.SMSPlugin.SMSConfigDetails;
import com.boot.jx.postman.plugin.TelegramPlugin.TelegramConfigDetails;
import com.boot.jx.postman.plugin.TwilioSMSPlugin.TwilioConfigDetails;
import com.boot.jx.postman.plugin.TwitterPlugin.TwitterConfigDetails;
import com.boot.jx.postman.plugin.WA360CloudPlugin.WA360CloudConfigDetails;
import com.boot.jx.postman.plugin.WA360Plugin.WA360ConfigDetails;
import com.boot.jx.postman.plugin.WAGupShupPlugin.GupShupConfigDetails;
import com.boot.jx.postman.plugin.WacfbPlugin.WACFBConfigDetails;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;
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
	private FirebaseConfigDetails firebase;
	private EmailConfigDetails email;
	private TwilioConfigDetails twilio;
	private SMSConfigDetails sms;
	private WA360CloudConfigDetails wa360dc;
	private WACFBConfigDetails wacfb;
	private OAConfigDetails oa;
	private OutlookConfigDetails outlook;

	private boolean isAutoCreated;
	private boolean isMaster;
	private boolean isPushAllowed;
	private boolean isPushOnlyApproved;
	private boolean isPushFreeTextAllowed;
	private boolean isPushToNewContactAllowed;
	private boolean isWebhookManual;

	private Map<String, Object> meta;

	private String masterChannelId;
	private String channelConfigTempId;

	private Object error;

	private String callbackPath;
	private String unhandledInboundForward;
	protected String domain;
	private String domainProxy;

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

	public EmailConfigDetails getEmail() {
		return email;
	}

	public void setEmail(EmailConfigDetails email) {
		this.email = email;
	}

	public TwilioConfigDetails getTwilio() {
		return twilio;
	}

	public void setTwilio(TwilioConfigDetails twilio) {
		this.twilio = twilio;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	public SMSConfigDetails getSms() {
		return sms;
	}

	public void setSms(SMSConfigDetails sms) {
		this.sms = sms;
	}

	public WA360CloudConfigDetails getWa360dc() {
		return wa360dc;
	}

	public void setWa360dc(WA360CloudConfigDetails wa360dc) {
		this.wa360dc = wa360dc;
	}

	public OAConfigDetails getOa() {
		return oa;
	}

	public void setOa(OAConfigDetails oa) {
		this.oa = oa;
	}

	public String getUnhandledInboundForward() {
		return unhandledInboundForward;
	}

	public void setUnhandledInboundForward(String unhandledInboundForward) {
		this.unhandledInboundForward = unhandledInboundForward;
	}

	public FirebaseConfigDetails getFirebase() {
		return firebase;
	}

	public void setFirebase(FirebaseConfigDetails firebase) {
		this.firebase = firebase;
	}

	public WACFBConfigDetails getWacfb() {
		return wacfb;
	}

	public void setWacfb(WACFBConfigDetails wacfb) {
		this.wacfb = wacfb;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public boolean isAutoCreated() {
		return isAutoCreated;
	}

	public void setAutoCreated(boolean isAutoCreated) {
		this.isAutoCreated = isAutoCreated;
	}

	public AChannelDetails details() {
		switch (getChannelType()) {
		case CHANNEL_TYPE.WACFB:
			return this.getWacfb();
		case CHANNEL_TYPE.WA_360DC:
			return this.getWa360dc();
		case CHANNEL_TYPE.WA_360D:
			return this.getWa360d();
		case CHANNEL_TYPE.WEB:
			return this.getWeb();
		case CHANNEL_TYPE.FACEBOOK:
			return this.getFacebook();
		case CHANNEL_TYPE.TWITTER:
			return this.getTwitter();
		case CHANNEL_TYPE.TELEGRAM:
			return this.getTelegram();
		default:
			return null;
		}
	}

	public OutlookConfigDetails getOutlook() {
		return outlook;
	}

	public void setOutlook(OutlookConfigDetails outlook) {
		this.outlook = outlook;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}
	public Map<String, Object> meta() {
		  if (!ArgUtil.is(this.meta)) { 
		        meta = new HashMap<String, Object>();
		    }
	    return meta;
	}
   
	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public String getChannelConfigTempId() {
		return channelConfigTempId;
	}

	public void setChannelConfigTempId(String channelConfigTempId) {
		this.channelConfigTempId = channelConfigTempId;
	}

	public String getMasterChannelId() {
		return masterChannelId;
	}

	public void setMasterChannelId(String masterChannelId) {
		this.masterChannelId = masterChannelId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDomainProxy() {
		return domainProxy;
	}

	public void setDomainProxy(String domainProxy) {
		this.domainProxy = domainProxy;
	}

}
