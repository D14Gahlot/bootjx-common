package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.InputType;
import com.boot.jx.postman.PMConstants;

public class ConfigConstants {

    public static final String[] APP_CONFIG_PREFIX = new String[] {
	    // PRefixe
	    "mry.prop.logo.", "mry.prop.service.", "mry.prop.social.", };
    public static final Map<String, String> APP_CONFIG = new ConcurrentHashMap<String, String>();
    public static final List<ConfigMeta> SETUP_CONFIG_LIST = new ArrayList<ConfigMeta>();

    static {
	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Bot Name", "postman.bot.name"));
	ConfigConstants.SETUP_CONFIG_LIST
		.add(new ConfigMeta("Contact Details Provider Webhook", "postman.contact.details.url"));

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Chat Tag Enabled", "chat.tag.enabled").optionsOnOff());

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Chat Session Timeout", "postman.chat.session.timeout")
		.optionValues("8hr", "12hr", "16hr", "20hr", "24hr"));

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Chat Alert Timer", "postman.chat.idle.timeout")
		.optionValues("5min", "10min", "15min", "20min", "25min", "30min"));

	ConfigConstants.SETUP_CONFIG_LIST
		.add(new ConfigMeta("Agent can initiate new chat", "postman.agent.chat.init").optionsOnOff());

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Agent Assignment", "postman.agent.chat.assignment")
		.optionValues(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN, PMConstants.ASSIGNMENT_RULE.MANUAL,
			PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT)
		.defaultValue(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN));

	ConfigConstants.SETUP_CONFIG_LIST
		.add(new ConfigMeta("Sticky Session", "postman.agent.chat.stickysession")
			.optionValues(PMConstants.CHAT_SESSION_STICKY.NONE, PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE,
				PMConstants.CHAT_SESSION_STICKY.STRICT)
			.defaultValue(PMConstants.CHAT_SESSION_STICKY.NONE));

	ConfigConstants.SETUP_CONFIG_LIST
		.add(new ConfigMeta("Enable Beta UI", "postman.ui.beta").optionsOnOff().defaultValue(ConfigOption.OFF));

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Agent Panel Color Scheme", "postman.agent.scheme.color")
		.inputType(InputType.COLOR).defaultValue("#4267b2"));

	ConfigConstants.SETUP_CONFIG_LIST.add(new ConfigMeta("Agent Color Scheme 2", "postman.agent.scheme2.color")
		.inputType(InputType.COLOR_PALLETE).defaultValue(new ConfigMeta.ColorPalette()));

	ConfigConstants.SETUP_CONFIG_LIST
		.add(new ConfigMeta("Show History Tab", "postman.agent.tab.history").optionsOnOff().defaultFalse());
    }

}
