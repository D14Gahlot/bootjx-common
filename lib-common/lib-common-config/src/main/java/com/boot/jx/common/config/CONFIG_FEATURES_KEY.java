package com.boot.jx.common.config;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.model.MapModel.EntryMeta;

public enum CONFIG_FEATURES_KEY implements EntryMeta {

	BUILD_VERSION(
			new ConfigMeta("BUILD_VERSION", "feature.build.version").inputType(INPUT_TYPE.NUMBER).defaultValue(3)),

	PLAN(new ConfigMeta("Customer Profile", "feature.plan").options(//
			new ConfigOption(PLANS.BLOCKED), //
			new ConfigOption(PLANS.FREEMIUM), //
			new ConfigOption(PLANS.CUSTOM) //
	)),

	MSG_MEDIA_TEMPLATE(new ConfigMeta("Message Media Template", "feature.message.media.tmpl").optionsOnOff()
			.group(ConfigConstants.APP_MODULES)),
	MSG_OUTBOUND(new ConfigMeta("Outbound Message", "feature.message.outbound").optionsOnOff().defaultTrue()),

	AUTH_2FA(new ConfigMeta("2FA Auth for login", "feature.auth.2fa").optionsOnOff()),

	CONTACT_CENTER(new ConfigMeta("Customer Profile", "feature.contact.center").optionsOnOff()),

	CHANNEL_AUTOCONFIGURE_FACEBOOK(new ConfigMeta("AutoConfigure Facebook", "feature.channel.autoconfigure.facebook")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),
	CHANNEL_AUTOCONFIGURE_WHATSAPP(new ConfigMeta("AutoConfigure WhatsApp", "feature.channel.autoconfigure.whatsapp")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),
	CHANNEL_AUTOCONFIGURE_INSTAGRAM(new ConfigMeta("AutoConfigure Instagram", "feature.channel.autoconfigure.instagram")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),
	CHANNEL_AUTOCONFIGURE_TELEGRAM(new ConfigMeta("AutoConfigure Telegram", "feature.channel.autoconfigure.telegram")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),
	CHANNEL_AUTOCONFIGURE_TWITTER(new ConfigMeta("AutoConfigure Twitter", "feature.channel.autoconfigure.twitter")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),
	CHANNEL_AUTOCONFIGURE_OUTLOOK(new ConfigMeta("AutoConfigure Outlook", "feature.channel.autoconfigure.outlook")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),

	CHANNEL_AUTOCONFIGURE_GMAIL(new ConfigMeta("AutoConfigure Gmail", "feature.channel.autoconfigure.gmail")
			.optionsOnOff().group(ConfigConstants.CHANNELS)),

	CHANNEL_BROADCAST_TELEGRAM(new ConfigMeta("AutoConfigure Telegram", "feature.channel.broadcast.telegram")
			.optionsOnOff().group(ConfigConstants.CHANNELS).defaultFalse()),
	CHANNEL_BROADCAST_WHATSAPP(new ConfigMeta("Broadcast WhatsApp", "feature.channel.broadcast.whatsapp").optionsOnOff()
			.group(ConfigConstants.CHANNELS).defaultTrue()),

	BOT_FLOW_BUILDER(
			new ConfigMeta("Bot Flow Builder", "feature.bot.flow.builder").optionsOnOff().group(ConfigConstants.BOT)),

	CAMPAIGN_SCHEDULER(new ConfigMeta("Campaign Scheduler", "feature.campaign.scheduler").optionsOnOff()
			.group(ConfigConstants.CAMPAIGN)),
	CAMPAIGN_RESEND_FAILED(new ConfigMeta("Campaign Resend Failed Marketing Messages", "feature.campaign.resend.failed")
			.optionsOnOff().group(ConfigConstants.CAMPAIGN)),

	APP_MODULE_CALENDAR(new ConfigMeta("Calendar Module", "feature.app.module.calandar").optionsOnOff()
			.group(ConfigConstants.APP_MODULES)),
	APP_MODULE_AGENT(new ConfigMeta("Agent Module", "feature.app.module.agent").optionsOnOff()
			.group(ConfigConstants.APP_MODULES)),
	APP_MODULE_ADMIN(new ConfigMeta("Admin Module", "feature.app.module.admin").optionsOnOff()
			.group(ConfigConstants.APP_MODULES)),
	APP_MODULE_SOCIAL(new ConfigMeta("Social Module", "feature.app.module.social").optionsOnOff()
			.group(ConfigConstants.APP_MODULES)),

	AGENT_RM_USER(new ConfigMeta("Relationship Managment", "feature.agent.rm.user").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),

	EVENTS_TIMEOUT(new ConfigMeta("Events Timeout Managment", "feature.events.timeout").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),

	// Ends here
	;

	public static class PLANS {
		public static final String BLOCKED = "BLOCKED";
		public static final String FREEMIUM = "FREEMIUM";
		public static final String CUSTOM = "CUSTOM";
	}

	private String key;
	private Object defaultValue;
	private String ukey;

	CONFIG_FEATURES_KEY(ConfigMeta defaultFalse) {
		this.key = defaultFalse.getKey();
		this.ukey = defaultFalse.getUkey();
		ConfigConstants.PERMS_CONFIG_LIST.add(defaultFalse);
		defaultValue = defaultFalse.getDefaultValue();
	}

	public String getKey() {
		return key;
	}

	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue() {
		return (T) defaultValue;
	}

	public String getUkey() {
		return ukey;
	}
}