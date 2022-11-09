package com.boot.jx.postman;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMConfiguration.PMConfigurationWrappper;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.model.MapModel.EntryMeta;
import com.boot.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils.TimePeriod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Component
public class PMEnvironment {

	public static interface PublicProperty {
	}

	public static interface ProtectedProperty extends PublicProperty {
	}

	public static interface OneTimeVisibleProperty extends ProtectedProperty {
	}

	public static interface PMEnvironmentProvider {

		public PMConfigurationModel local();

		public PMConfigurationModel shared();

		public ChannelConfig addChannel(ChannelConfig config);

		public void updateChannel(ChannelConfig config, String action);

		public void initConfig();
	}

	public static interface ChannelTypeSpecificProps {
		@JsonView(PublicProperty.class)
		public boolean isPushAllowed();

		@JsonView(PublicProperty.class)
		public boolean isPushOnlyApproved();

		@JsonView(PublicProperty.class)
		public boolean isPushFreeTextAllowed();

		@JsonView(PublicProperty.class)
		public boolean isPushToNewContactAllowed();

		@JsonView(PublicProperty.class)
		public boolean isWebhookManual();

		@Deprecated
		@JsonView(PublicProperty.class)
		public default String getChannel() {
			return null;
		}

		@JsonView(PublicProperty.class)
		public ContactType getContactType();

		@JsonView(PublicProperty.class)
		public String getChannelType();
	}

	public static interface ChannelDetails extends Serializable {

		@JsonView(PublicProperty.class)
		public String getLane();

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static abstract class AChannelDetails implements ChannelDetails {
		private static final long serialVersionUID = -5531902306230415784L;
	}

	public static abstract class AChannelConfig extends AChannelDetails implements ChannelTypeSpecificProps {

		private static final long serialVersionUID = 1950315645271368433L;

		protected ContactType contactType;
		protected String channelType;

		@JsonView(PMEnvironment.ProtectedProperty.class)
		protected String channelKey;
		protected String channelCode;

		protected String name;
		protected String inboundQueue;

		private boolean isProxyEnabled;
		private boolean isSandbox;
		private boolean isShared;
		private boolean isDisabled;

		@JsonView(PMEnvironment.PublicProperty.class)
		private String server;

		@JsonView(PMEnvironment.PublicProperty.class)
		protected String webhookUrl;

		public AChannelConfig() {
			this.channelType = "WEBSITE";
		}

		public ContactType getContactType() {
			return contactType;
		}

		public void setContactType(ContactType contactType) {
			this.contactType = contactType;
		}

		@Override
		public String getChannelType() {
			return channelType;
		}

		public void setChannelType(String channelType) {
			this.channelType = channelType;
		}

		public String getChannelId() {
			return String.format("%s:%s", this.getChannelType(), this.getLane()).toLowerCase();
		}

		public String getChannelKey() {
			return channelKey;
		}

		public void setChannelKey(String channelKey) {
			this.channelKey = channelKey;
		}

		public String getName() {
			if (!ArgUtil.is(this.name)) {
				return String.format("%s %s", this.getContactType(), this.getLane());
			}
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getWebhookUrl() {
			return webhookUrl;
		}

		public void setWebhookUrl(String webhookUrl) {
			this.webhookUrl = webhookUrl;
		}

		public String toString() {
			return this.getChannelId();
		}

		public boolean isSandbox() {
			return isSandbox;
		}

		public void setSandbox(boolean isSandbox) {
			this.isSandbox = isSandbox;
		}

		public boolean isDisabled() {
			return isDisabled;
		}

		public void setDisabled(boolean isDisabled) {
			this.isDisabled = isDisabled;
		}

		public boolean isReadOnly() {
			return false;
		}

		public boolean isShared() {
			return isShared;
		}

		public void setShared(boolean isShared) {
			this.isShared = isShared;
		}

		public String getInboundQueue() {
			return inboundQueue;
		}

		public void setInboundQueue(String inboundQueue) {
			this.inboundQueue = inboundQueue;
		}

		public String getChannelCode() {
			return channelCode;
		}

		public void setChannelCode(String channelCode) {
			this.channelCode = channelCode;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public boolean isProxyEnabled() {
			return isProxyEnabled;
		}

		public void setProxyEnabled(boolean isProxyEnabled) {
			this.isProxyEnabled = isProxyEnabled;
		}

		public boolean equals(ContactType type) {
			return this.contactType == type;
		}

	}

	public static class PMConfigurationObject extends MapEntry implements Serializable {

		private static final long serialVersionUID = 2678154770516185408L;
		String key;
		String description;
		String domain;
		String server;
		boolean shared;
		boolean disabled;

		public PMConfigurationObject(String key, Object value) {
			super(value);
			this.key = key;
		}

		public PMConfigurationObject() {
			super(null);
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isShared() {
			return shared;
		}

		public void setShared(boolean shared) {
			this.shared = shared;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

	}

	@Autowired(required = false)
	private PMEnvironmentProvider provider;

	public PMConfigurationModel local() {
		PMConfigurationModel config = null;
		if (ArgUtil.is(provider)) {
			config = provider.local();
		}
		if (config == null) {
			config = PMConfiguration.instance();
		}
		return config;
	}

	public PMConfigurationModel shared() {
		PMConfigurationModel config = null;
		if (ArgUtil.is(provider)) {
			config = provider.shared();
		}
		if (config == null) {
			config = PMConfiguration.instance();
		}
		return config;
	}

	public PMConfigurationWrappper config() {
		PMConfigurationWrappper config = new PMConfigurationWrappper().appConfig(appConfig);
		if (ArgUtil.is(provider)) {
			return config.local(provider.local()).shared(provider.shared());
		}
		return config;
	}

	public void initConfig() {
		if (ArgUtil.is(provider)) {
			provider.initConfig();
		}
	}

	@Autowired
	private AppConfig appConfig;

	public PMConfigurationObject keyEntry(String key) {
		PMConfigurationObject configObject = this.local().prefs().get(key);

		String tnt = AppContextUtil.getTenant();
		if (ArgUtil.isEmpty(configObject) && !Tenants.isDefault(tnt)) {
			PMConfigurationObject sharedConfigObject = this.shared().prefs().get(key);
			if (ArgUtil.is(sharedConfigObject)) {
				return sharedConfigObject;
			}
		}

		if (ArgUtil.isEmpty(configObject)) {
			String value = appConfig.prop(key);
			configObject = new PMConfigurationObject(key, value);
			// this.config().map().put(key, configObject);
		}

		return configObject;
	}

	public PMConfigurationObject keyEntry(EntryMeta entryMeta) {
		return keyEntry(entryMeta.getKey());
	}

	public PMConfigurationObject permEntry(String key) {
		PMConfigurationObject configObject = this.local().perms().get(key);
		String tnt = AppContextUtil.getTenant();
		if (ArgUtil.isEmpty(configObject) && !Tenants.isDefault(tnt)) {
			PMConfigurationObject sharedConfigObject = this.shared().perms().get(key);
			if (ArgUtil.is(sharedConfigObject)) {
				return sharedConfigObject;
			}
		}
		if (ArgUtil.isEmpty(configObject)) {
			String value = appConfig.prop(key);
			configObject = new PMConfigurationObject(key, value);
			// this.config().map().put(key, configObject);
		}
		return configObject;
	}

	public PMConfigurationObject permEntry(EntryMeta entryMeta) {
		return permEntry(entryMeta.getKey());
	}

	public void addChannel(ChannelConfig config) {
		if (ArgUtil.is(provider)) {
			provider.addChannel(config);
		}
	}

	public void updateChannel(ChannelConfig config, String action) {
		if (ArgUtil.is(provider)) {
			provider.updateChannel(config, action);
		}
	}

	public interface PMCommonConfig extends AppCommonConfig {
		public String getCdnServer();

		public String getBotUrl();

		public String getAgentUrl();

		public String getServiceServer();

		public String getScriptusUrl();

		public String getScriptusSecret();

		public boolean isValidDomain();

		public boolean isDefaultDomain();

		public String mainDomainRedirect();

		public String mainDomainRedirect(String path);

	}

	public interface PMDomainConfig {
		public String getDefaultInboundQueue();

		public String getDefaultInboundQueue(String channelId, CHAT_MODE mode);

		public String getDefaultInboundQueue(Contactable contact);

		String getDomainUrl();

		PMConfigurationObject getAgentHistoryPeriod();

		PMConfigurationObject getChatIdleTimeout();

		PMConfigurationObject getAgentHistoryCount();

		String getDefaultInboundQueue(Contactable contact, CHAT_MODE mode);

	}

	public interface PMClientConfig {

		String getWebhookBase(ChannelConfig channelConfig);

		String getChatSessionTimeout();

		TimePeriod getAgentSessionTimeout();

		String getWebhookUrl(ChannelConfig channelConfig);

		boolean isLocalDummyBotEnabled();

		String getDefaultSender();

		String getContactDetailsUrl();

	}
}
