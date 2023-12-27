package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.boot.jx.postman.plugin.FacebookPlugin.FacebookConfigDetails;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

public class FacebookPlugin implements DefaultChannelPlugin<FacebookConfigDetails> {

	@Override
	public String getChannelType() {
		return CHANNEL_TYPE.FACEBOOK;
	}

	@Override
	public ContactType getContactType() {
		return ContactType.FACEBOOK;
	}

	public static class FacebookConfigDetails extends AChannelDetails {

		private static final long serialVersionUID = -2397678752642150000L;
		@ConfigMetaProperty(path = "facebook.pageId", title = "Page Id", createonly = true)
		private String pageId;
		@ConfigMetaProperty(path = "facebook.type", title = "Type", hidden = true)
		private String type;
		@ConfigMetaProperty(path = "facebook.handler", title = "Handler")
		private String handler;

		@ConfigMetaProperty(path = "facebook.accessToken", title = "Access Token", writeonly = true)
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String accessToken;
		@ConfigMetaProperty(path = "facebook.verifyToken", title = "Verify Token", writeonly = true)
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String verifyToken;
		@ConfigMetaProperty(path = "facebook.appSecret", title = "App Secret", writeonly = true)
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

		public String getHandler() {
			return handler;
		}

		public void setHandler(String handler) {
			this.handler = handler;
		}

	}

	@Override
	public String getDefaultName(ChannelConfig config) {
		if (!ArgUtil.is(config.getName())) {
			if (ArgUtil.is(config.getFacebook().getHandler())) {
				return config.getFacebook().getHandler();
			}
			return String.format("FB %s", config.getLane());
		}
		return config.getName();
	}

	@Override
	public FacebookConfigDetails newChannelDetails() {
		return new FacebookConfigDetails();
	}

	@Override
	public void setDetails(ChannelConfig config, FacebookConfigDetails details) {
		config.setFacebook(details);
	}

	@Override
	public FacebookConfigDetails getDetails(ChannelConfig config) {
		return config.getFacebook();
	}

/*	@Override
	public void addConfigMeta(List<ConfigMeta> list) {
		list.add(new ConfigMeta().path("facebook.pageId").title("Page Id").createonly());
		list.add(new ConfigMeta().path("facebook.type").title("Type").optionValues("page").hidden());
		list.add(new ConfigMeta().path("facebook.handler").title("Handler"));
		list.add(new ConfigMeta().path("facebook.verifyToken").title("Verify Token").writeonly());
		list.add(new ConfigMeta().path("facebook.accessToken").title("Access Token").writeonly());
		list.add(new ConfigMeta().path("facebook.appSecret").title("App Secret").writeonly());
	}

	@Override
	public void importChannelDetailsFromMap(FacebookConfigDetails channelDetails, MapModel map) {
		channelDetails.setPageId(map.pathEntry("facebook.pageId").asString(channelDetails.getPageId()));
		channelDetails.setHandler(map.pathEntry("facebook.handler").asString(channelDetails.getHandler()));
		channelDetails.setType(map.pathEntry("facebook.type").asString(channelDetails.getType()));
		channelDetails.setVerifyToken(map.pathEntry("facebook.verifyToken").asString(channelDetails.getVerifyToken()));
		channelDetails.setAccessToken(map.pathEntry("facebook.accessToken").asString(channelDetails.getAccessToken()));
		channelDetails.setAppSecret(map.pathEntry("facebook.appSecret").asString(channelDetails.getAppSecret()));
	}*/

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
	public boolean isWebhookManual() {
		return true;
	}

}
