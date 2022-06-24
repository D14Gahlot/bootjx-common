package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.CONVERT_TYPE;
import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.common.impl.ConfigMeta.DATA_TYPE;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;
import com.boot.model.UtilityModels.JsonIgnoreNull;

public class WebPlugin implements DefaultChannelPlugin<WebConfigDetails> {

	@Override
	public ContactType getContactType() {
		return ContactType.WEBSITE;
	}

	@Override
	public String getChannelType() {
		return CHANNEL_TYPE.WEB;
	}

	public static final class WebConfigDetails extends AChannelDetails {

		private static final long serialVersionUID = 8692015716138195462L;

		@ConfigMetaProperty(path = "web.site", title = "Site", createonly = true)
		private String site;

		@ConfigMetaProperty(path = "web.promptEmail", title = "Prompt Email", inputType = INPUT_TYPE.OPTIONS,
				dataType = DATA_TYPE.SWITCH, converterType = CONVERT_TYPE.BOOLEAN, defaultValue = "true")
		private boolean promptEmail;

		@ConfigMetaProperty(path = "web.promptPhone", title = "Prompt Phone", inputType = INPUT_TYPE.OPTIONS,
				dataType = DATA_TYPE.SWITCH, converterType = CONVERT_TYPE.BOOLEAN, defaultValue = "false")
		private boolean promptPhone;

		@ConfigMetaProperty(path = "web.iceBreaker", title = "Ice Breaker Message", inputType = INPUT_TYPE.OPTIONS,
				optionsSource = "getx:/api/tmpl/hsm", optionsKey = "code", optionsLabel = "desc")
		private String iceBreaker;

		@Override
		public String getLane() {
			return this.site;
		}

		public String getSite() {
			return site;
		}

		public void setSite(String site) {
			this.site = site;
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

	}

	@Override
	public void setDetails(ChannelConfig config, WebConfigDetails details) {
		config.setWeb(details);
	}

	@Override
	public WebConfigDetails getDetails(ChannelConfig config) {
		return config.getWeb();
	}

	@Override
	public WebConfigDetails newChannelDetails() {
		return new WebConfigDetails();
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
		return true;
	}

}
