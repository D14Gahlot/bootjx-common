package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.OPTIONS_TYPE;
import com.boot.jx.postman.PMConstants;
import com.boot.model.MapModel.EntryMeta;
import com.boot.utils.TimeUtils;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class ConfigConstants {

    public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

    public static final String[] APP_CONFIG_PREFIX = new String[] {
	    // PRefixe
	    "mry.prop.logo.", "mry.prop.service.", "mry.prop.social.", };
    public static final Map<String, String> APP_CONFIG = new ConcurrentHashMap<String, String>();
    public static final List<ConfigMeta> SETUP_CONFIG_LIST = new ArrayList<ConfigMeta>();

    public static enum KEY implements EntryMeta {
	POSTMAN_BOT_NAME(new ConfigMeta("Bot Name", "postman.bot.name")),
	POSTMAN_CONTACT_DETAILS_URL(new ConfigMeta("Contact Details Provider Webhook", "postman.contact.details.url")),

	POSTMAN_CHAT_INBOUND_WEBHOOK(new ConfigMeta("Message Inbound Webhook", "postman.chat.inbound.webhook")),

	CHAT_TAG_ENABLED(new ConfigMeta("Chat Tag Enabled", "chat.tag.enabled").optionsOnOff()),

	POSTMAN_CHAT_SESSION_TIMEOUT(new ConfigMeta("Chat Session Timeout", "postman.chat.session.timeout")
		.optionValues("8hr", "12hr", "16hr", "20hr", "24hr")),

	POSTMAN_CHAT_IDLE_TIMEOUT(new ConfigMeta("Chat Alert Timer", "postman.chat.idle.timeout").optionValues("5min",
		"10min", "15min", "20min", "25min", "30min")),

	POSTMAN_AGENT_CHAT_INIT(
		new ConfigMeta("Agent can initiate new Chat", "postman.agent.chat.init").optionsOnOff().deprecated()),

	POSTMAN_AGENT_CHAT_INIT_SESSION(
		new ConfigMeta("Agent can initiate chat with existing contact", "postman.agent.chat.init.session")
			.optionsOnOff()),

	POSTMAN_AGENT_CHAT_INIT_CONTACT(
		new ConfigMeta("Agent can initiate chat with new contact", "postman.agent.chat.init.contact")
			.optionsOnOff()),

	POSTMAN_AGENT_CHAT_ASSIGNMENT(new ConfigMeta("Agent Assignment", "postman.agent.chat.assignment")
		.optionValues(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN, PMConstants.ASSIGNMENT_RULE.MANUAL,
			PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT)
		.defaultValue(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN)),

	POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT(new ConfigMeta("Message to customer while chat is transferred to agent",
		"postman.agent.chat.autoreply.talk2agent").options("getx:/api/tmpl/pushtemplate")),

	POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED(new ConfigMeta("Message to customer when chat is resolevd by agent",
		"postman.agent.chat.autoreply.resolved").options("getx:/api/tmpl/pushtemplate")),

	POSTMAN_AGENT_CHAT_STICKYSESSION(
		new ConfigMeta("Sticky Session", "postman.agent.chat.stickysession")
			.optionValues(PMConstants.CHAT_SESSION_STICKY.NONE, PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE,
				PMConstants.CHAT_SESSION_STICKY.STRICT)
			.defaultValue(PMConstants.CHAT_SESSION_STICKY.NONE)),
	POSTMAN_UI_BETA(
		new ConfigMeta("Enable Beta UI", "postman.ui.beta").optionsOnOff().defaultValue(ConfigOption.OFF)),

	POSTMAN_AGENT_SCHEME_COLOR(new ConfigMeta("Agent Panel Color Scheme", "postman.agent.scheme.color")
		.inputType(OPTIONS_TYPE.COLOR).defaultValue("#4267b2")),

	POSTMAN_AGENT_SCHEME2_COLOR(new ConfigMeta("Agent Color Scheme 2", "postman.agent.scheme2.color")
		.inputType(OPTIONS_TYPE.COLOR_PALLETE).defaultValue(new ConfigMeta.ColorPalette())),

	POSTMAN_AGENT_TAB_HISTORY_PERIOD(new ConfigMeta("Show History Period", "postman.agent.tab.history.period")
		.options(new ConfigOption(0).label("OFF"), new ConfigOption(TimeUtils.toMillis("1d")).label("+1Days"),
			new ConfigOption(TimeUtils.toMillis("3d")).label("+3Days"),
			new ConfigOption(TimeUtils.toMillis("5d")).label("+5Days"),
			new ConfigOption(TimeUtils.toMillis("7d")).label("+7Days"))
		.defaultValue(0)),

	POSTMAN_PHONEBOOK_REGION(new ConfigMeta("Default ISD Country", "postman.phonebook.region")
		.optionValues(PHONE_NUMBER_UTIL.getSupportedRegions().toArray()).defaultValue("IN")),

	// Ends here
	;

	private String key;

	KEY(ConfigMeta defaultFalse) {
	    this.key = defaultFalse.getKey();
	    ConfigConstants.SETUP_CONFIG_LIST.add(defaultFalse);
	}

	public String getKey() {
	    return key;
	}
    }

    static {
	ConfigConstants.KEY.values();
    }

}
