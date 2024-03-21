package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.CONVERT_TYPE;
import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.common.impl.ConfigMeta.DATA_TYPE;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.boot.jx.postman.plugin.FacebookPlugin.MetaMasterConfigDetails;
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

	public static class WACFBConfigDetails extends MetaMasterConfigDetails {

		private static final long serialVersionUID = 5956062194604118502L;

		@ConfigMetaProperty(path = "wacfb.phoneNumber", title = "Phone Number", createonly = true,
				desc = "Enter WABA number with country code")
		private String phoneNumber;

		@ConfigMetaProperty(path = "wa3cfb.acessToken", title = "Acess Token", writeonly = true,
				desc = "Enter Your Acess Token")
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String acessToken;
		
		@ConfigMetaProperty(path = "wa3cfb.phoneNumberId", title = "Phone Number Id", writeonly = true,
				desc = "Enter Your Phone Number Id")
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String phoneNumberId;
		
		
		public String getPhoneNumberId() {
			return phoneNumberId;
		}

		public void setPhoneNumberId(String phoneNumberId) {
			this.phoneNumberId = phoneNumberId;
		}

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
			return phoneNumber;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getAcessToken() {
			return acessToken;
		}

		public void setAcessToken(String acessToken) {
			this.acessToken = acessToken;
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
