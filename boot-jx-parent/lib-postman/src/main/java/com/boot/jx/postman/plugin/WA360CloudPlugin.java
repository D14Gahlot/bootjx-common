package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.boot.jx.postman.plugin.WA360CloudPlugin.WA360CloudConfigDetails;

import com.fasterxml.jackson.annotation.JsonView;

public class WA360CloudPlugin implements DefaultChannelPlugin<WA360CloudConfigDetails> {

	@Override
	public String getChannelType() {
		return CHANNEL_TYPE.WA_360DC;
	}

	@Override
	public ContactType getContactType() {
		return ContactType.WHATSAPP;
	}

	public static class WA360CloudConfigDetails extends AChannelDetails {

		private static final long serialVersionUID = -2397678752642150000L;

		@ConfigMetaProperty(path = "wa360dc.number", title = "Number", createonly = true,
				desc = "Eneter WABA number with country code")
		private String number;

		@ConfigMetaProperty(path = "wa360dc.apiKey", title = "API Key", writeonly = true, desc = "Enter Your WABA cloud Key")
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String apiKey;

		@Override
		public String getLane() {
			return this.number;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}
	}

	@Override
	public void setDetails(ChannelConfig config, WA360CloudConfigDetails details) {
		config.setWa360dc(details);
	}

	@Override
	public WA360CloudConfigDetails getDetails(ChannelConfig config) {
		return config.getWa360dc();
	}

	@Override
	public WA360CloudConfigDetails newChannelDetails() {
		return new WA360CloudConfigDetails();
	}


	@Override
	public boolean isPushAllowed() {
		return true;
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
		return true;
	}

	@Override
	public boolean isWebhookManual() {
		return false;
	}

	
}
