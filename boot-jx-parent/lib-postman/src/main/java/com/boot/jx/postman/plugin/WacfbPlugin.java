package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.CONVERT_TYPE;
import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.common.impl.ConfigMeta.DATA_TYPE;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.fasterxml.jackson.annotation.JsonView;

public class WacfbPlugin implements DefaultChannelPlugin<com.boot.jx.postman.plugin.WacfbPlugin.WACFBConfigDetails> {
	@Override
	public String getChannelType() {
		return CHANNEL_TYPE.WACFB;
	}

	@Override
	public ContactType getContactType() {
		return ContactType.WHATSAPP;
	}

	public static class WACFBConfigDetails extends AChannelDetails {

		private static final long serialVersionUID = 5956062194604118502L;

		private String masterAppTitle;
		private String masterAppId;
		private String masterAppConfigId;
		private String masterAppSecret;
		private String masterAppVerifyToken;

		@ConfigMetaProperty(path = "wacfb.number", title = "Number", createonly = true,
				desc = "Enter WABA number with country code")
		private String number;

		@ConfigMetaProperty(path = "wa3cfb.apiKey", title = "API Key", writeonly = true, desc = "Enter Your WABA Key")
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String apiKey;

		@ConfigMetaProperty(path = "wacfb.promptEmail", title = "Prompt Email", inputType = INPUT_TYPE.OPTIONS,
				dataType = DATA_TYPE.SWITCH, converterType = CONVERT_TYPE.BOOLEAN, defaultValue = "true")
		private boolean promptEmail;

		@ConfigMetaProperty(path = "wacfb.promptPhone", title = "Prompt Phone", inputType = INPUT_TYPE.OPTIONS,
				dataType = DATA_TYPE.SWITCH, converterType = CONVERT_TYPE.BOOLEAN, defaultValue = "true")
		private boolean promptPhone;

		@ConfigMetaProperty(path = "wacfb.promptName", title = "Prompt Name", inputType = INPUT_TYPE.OPTIONS,
				dataType = DATA_TYPE.SWITCH, converterType = CONVERT_TYPE.BOOLEAN, defaultValue = "true")
		private boolean promptName;

		@Override
		public String getLane() {
			return number;
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

		public boolean isPromptEmail() {
			return promptEmail;
		}

		public void setPromptEmail(boolean promptEmail) {
			this.promptEmail = promptEmail;
		}

		public boolean isPromptPhone() {
			return promptPhone;
		}

		public void setPromptPhone(boolean promptPhone) {
			this.promptPhone = promptPhone;
		}

		public boolean isPromptName() {
			return promptName;
		}

		public void setPromptName(boolean promptName) {
			this.promptName = promptName;
		}

		public String getMasterAppTitle() {
			return masterAppTitle;
		}

		public void setMasterAppTitle(String masterAppTitle) {
			this.masterAppTitle = masterAppTitle;
		}

		public String getMasterAppId() {
			return masterAppId;
		}

		public void setMasterAppId(String masterAppId) {
			this.masterAppId = masterAppId;
		}

		public String getMasterAppConfigId() {
			return masterAppConfigId;
		}

		public void setMasterAppConfigId(String masterAppConfigId) {
			this.masterAppConfigId = masterAppConfigId;
		}

		public String getMasterAppSecret() {
			return masterAppSecret;
		}

		public void setMasterAppSecret(String masterAppSecret) {
			this.masterAppSecret = masterAppSecret;
		}

		public String getMasterAppVerifyToken() {
			return masterAppVerifyToken;
		}

		public void setMasterAppVerifyToken(String masterAppVerifyToken) {
			this.masterAppVerifyToken = masterAppVerifyToken;
		}

	}

	@Override
	public WACFBConfigDetails newChannelDetails() {
		return new WACFBConfigDetails();
	}

	@Override
	public void setDetails(ChannelConfig config, WACFBConfigDetails details) {
		config.setWacfb(details);
	}

	@Override
	public WACFBConfigDetails getDetails(ChannelConfig config) {
		return config.getWacfb();
	}

	@Override
	public boolean isPushAllowed() {
		return false;
	}

	@Override
	public boolean isPushOnlyApproved() {
		return false;
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
		return false;
	}
}
