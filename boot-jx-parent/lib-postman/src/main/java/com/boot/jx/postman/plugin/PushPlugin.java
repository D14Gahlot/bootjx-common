package com.boot.jx.postman.plugin;

import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.DefaultChannelPlugin;
import com.boot.jx.postman.plugin.PushPlugin.PushConfigDetails;
import com.fasterxml.jackson.annotation.JsonView;

public class PushPlugin implements DefaultChannelPlugin<PushConfigDetails> {

	@Override
	public ContactType getContactType() {
		return ContactType.PUSH;
	}

	@Override
	public String getChannelType() {
		return CHANNEL_TYPE.FIREBASE;
	}

	public static final class PushConfigDetails extends AChannelDetails {

		private static final long serialVersionUID = -1204213453486344023L;

		@ConfigMetaProperty(path = "push.title", title = "Title", createonly = true,
				desc = "Eneter Push Notification Title")
		private String from;

		@ConfigMetaProperty(path = "push.serverKey", title = "API Key", writeonly = true,
				desc = "Enter Server WABA Key")
		@JsonView(PMEnvironment.ProtectedProperty.class)
		private String serverKey;

		@Override
		public String getLane() {
			return this.from;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getServerKey() {
			return serverKey;
		}

		public void setServerKey(String serverKey) {
			this.serverKey = serverKey;
		}

	}

	@Override
	public void setDetails(ChannelConfig config, PushConfigDetails details) {
		config.setPush(details);
	}

	@Override
	public PushConfigDetails getDetails(ChannelConfig config) {
		return config.getPush();
	}

	@Override
	public PushConfigDetails newChannelDetails() {
		return new PushConfigDetails();
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
