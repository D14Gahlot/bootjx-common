package com.boot.jx.common.config;

import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.PROPERTIES;
import com.boot.model.MapModel.EntryMeta;
import com.boot.utils.TimeUtils;
import com.boot.utils.TimeZoneUtil;

public enum CONFIG_SETUP_KEY implements EntryMeta {
	POSTMAN_BOT_NAME(new ConfigMeta("Bot Name", "postman.bot.name")),
	POSTMAN_BOT_CODE(new ConfigMeta("Bot Code Prefix", "postman.bot.code").hidden()),
	POSTMAN_CONTACT_DETAILS_URL(
			new ConfigMeta("Contact Details ProviderWebhook", "postman.contact.details.url").hidden()),

	// POSTMAN_CHAT_INBOUND_WEBHOOK(new ConfigMeta("Fallback Webhook",
	// "postman.chat.inbound.webhook")
	// .desc("Inbound messages will be forwarded to this webhook, by default if
	// oth")),

	POSTMAN_CHAT_INBOUND_QUEUE(new ConfigMeta("Message Inbound Queue", "postman.chat.inbound.queue")
			.desc("Inbound messages will be forwarded to this Queue by default")
			.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")),

	POSTMAN_CHAT_AGENT_QUEUE(new ConfigMeta("Default Agent Queue", "postman.chat.agent.queue").desc("Default Agent App")
			.optionsSource("getx:/api/options/agent_queue").optionsKey("code").optionsLabel("code")),

	POSTMAN_CHAT_FEEDBACK_QUEUE(new ConfigMeta("Default Feedback Queue", PROPERTIES.POSTMAN_CHAT_FEEDBACK_QUEUE)
			.desc("Default Feedback App").optionsSource("getx:/api/options/inbound_queue").optionsKey("code")
			.optionsLabel("code").filter("feedbackApp", true)),

	POSTMAN_CHAT_CLOSE_WEBOOK(new ConfigMeta("Sesison Close Webhook", "postman.chat.close.webhook")
			.desc("Sesison Close Webhook").optionsSource("getx:/api/options/inbound_queue").optionsKey("code")
			.optionsLabel("code").filter("webhookApp", true)),

	POSTMAN_CHAT_CHANNEL_SANDBOX(new ConfigMeta("Enable Sandbox Channels", "postman.chat.channel.sandbox")
			.desc("Sandbox channels are preconfigured communication channels").optionsOnOff()),

	POSTMAN_CHAT_WEB_CHANNEL(new ConfigMeta("Default Web Channel", PROPERTIES.POSTMAN_CHAT_WEB_CHANNEL)
			.desc("This channel will be connected your Page").optionsSource("getx:/api/options/channels")
			.optionsKey("id").optionsLabel("name").filter("contactType", ContactType.WEBSITE)),

	POSTMAN_TRACK_MESSAGE(new ConfigMeta("Message Tracker", "postman.track.message").superKey("postman.track.message")
			.desc("Use Mehery's Tracker to track status for Channels (with no Status support)").optionsOnOff()),

	POSTMAN_TRACK_MESSAGE_URL(new ConfigMeta("Message Tracker", "postman.track.message.url").desc("Tracker URL")),

	POSTMAN_AGENT_2FA_ENABLED(new ConfigMeta("Enable 2FA Login", PROPERTIES.POSTMAN_AGENT_2FA_ENABLED)
			.superKey(PROPERTIES.POSTMAN_AGENT_2FA_ENABLED).desc("You will need OA app").optionsOnOff()),

	POSTMAN_AGENT_2FA_CHANNEL(new ConfigMeta("OTP Channel", PROPERTIES.POSTMAN_AGENT_2FA_CHANNEL)
			.superKey(PROPERTIES.POSTMAN_AGENT_2FA_ENABLED)
			.desc("This channel will be used for 2FA for Agent/Admin Login using login_otp template")
			.optionsSource("getx:/api/options/channels").optionsKey("id").optionsLabel("name")
			.filter("contactType", ContactType.OA)),

	// Agent Properties
	CHAT_TAG_ENABLED(new ConfigMeta("Chat Session Tags Enabled", "chat.tag.enabled").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),
	POSTMAN_CHAT_SESSION_TIMEOUT(new ConfigMeta("Default Chat Session Idle Duration", "postman.chat.session.timeout")
			.desc("Use 2hr,3hr,4hr etc for hours and 2d,3d,4d etc for days.")
			// .optionValues("1hr", "2hr", "4hr", "8hr", "12hr", "16hr", "20hr", "24hr",
			// "2d", "5d", "3d", "7d")
			.group(ConfigConstants.GROUP_CUSTOMER_CHAT)),

	POSTMAN_AGENT_CUSTOMER_CONTACT_INFO_MASK(new ConfigMeta("Mask Number", "postman.agent.customer.contact.info.mask")
			.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CUSTOMER_CHAT_DISABLE(
			new ConfigMeta("Disable Voice Record, Emoji and Attachments", "postman.agent.customer.chat.disable")
					.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_CHAT_IDLE_TIMEOUT(new ConfigMeta("Chat Alert Timer", "postman.chat.idle.timeout")
			.optionValues("5min", "10min", "15min", "20min", "25min", "30min").group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_INIT(
			new ConfigMeta("Agent can initiate new Chat", "postman.agent.chat.init").optionsOnOff().deprecated()),

	POSTMAN_AGENT_CHAT_INIT_SESSION(
			new ConfigMeta("Agent can initiate chat with existing contact", "postman.agent.chat.init.session")
					.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_INIT_CONTACT(
			new ConfigMeta("Agent can initiate chat with new contact", "postman.agent.chat.init.contact").optionsOnOff()
					.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_PICK_BOT(
			new ConfigMeta("Agent can INTERRUPT existing chat with bot", "postman.agent.chat.pick.bot").optionsOnOff()
					.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_PICK_ASSIGNED(
			new ConfigMeta("Agent can INTERRUPT existing chat with other agent", "postman.agent.chat.pick.assigned")
					.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_CC_FREETEXT(
			new ConfigMeta("Agent can send FreeText out of Customer Care Window", "postman.agent.chat.freetext.cc")
					.desc("Message can fail if selected channel does not permit").optionsOnOff()
					.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_ONSEND_ASSIGNED(new ConfigMeta("Auto Assign Session", "postman.agent.chat.reassignment.auto")
			.superKey("postman.agent.chat").desc("Session gets auto-assigned to sender agent").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_ASSIGNMENT(
			new ConfigMeta("Agent Assignment", "postman.agent.chat.assignment").superKey("postman.agent.chat")
					.optionValues(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN, PMConstants.ASSIGNMENT_RULE.MANUAL,
							PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT)
					.defaultValue(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_STICKY_RMAGENT(
			new ConfigMeta("Agent Assignment RM", "postman.agent.chat.sticky.rmagent").superKey("postman.agent.chat")
					.optionValues(PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE, PMConstants.CHAT_SESSION_STICKY.STRICT)
					.defaultValue(PMConstants.CHAT_SESSION_STICKY.STRICT).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_STICKYSESSION(
			new ConfigMeta("Sticky Session", "postman.agent.chat.stickysession").superKey("postman.agent.chat")
					.optionValues(PMConstants.CHAT_SESSION_STICKY.NONE, PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE,
							PMConstants.CHAT_SESSION_STICKY.STRICT)
					.defaultValue(PMConstants.CHAT_SESSION_STICKY.NONE).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_MESSAGE(new ConfigMeta("Customize Message Sent by agent", "postman.agent.chat.message")
			.superKey("postman.agent.chat.message").desc("Custom header and Signature").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_HEADER(new ConfigMeta("Header of Message Sent by agent", "postman.agent.chat.header")
			.superKey("postman.agent.chat.message").desc("Use {{agent}} for agent name")
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_SIGNATURE(new ConfigMeta("Signature of Message Sent by agent", "postman.agent.chat.message.signature")
			.superKey("postman.agent.chat.message").desc("Use {{agent}} for agent name").inputType(INPUT_TYPE.TEXTAREA)
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_SCHEME_COLOR(new ConfigMeta("Agent Panel Color Scheme", "postman.agent.scheme.color")
			.inputType(INPUT_TYPE.COLOR).defaultValue("#4267b2").group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT(
			new ConfigMeta("Agent-Chat Outbound Idle Timeout Config", "postman.agent.chat.out.idle.timeout")
					.superKey("postman.agent.chat.out.idle.timeout")
					.desc("Chat gets timed-out if agent does not respond for this interval in Minutes").optionsOnOff()
					.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL(
			new ConfigMeta("Agent-Chat Outbound Idle Timeout Interval", "postman.agent.chat.out.idle.timeout.interval")
					.superKey("postman.agent.chat.out.idle.timeout")
					.desc("Chat gets timed-out if agent does not respond for this interval in Minutes")
					.inputType(INPUT_TYPE.NUMBER).min(5).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE(
			new ConfigMeta("Agent-Chat Outbound Idle Timeout Queue", "postman.agent.chat.out.idle.timeout.queue")
					.superKey("postman.agent.chat.out.idle.timeout")
					.desc("Timed-out chat gets re-assigned to this queue")
					.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
					.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT(
			new ConfigMeta("Inbound Idle Timeout Config", "postman.agent.chat.in.idle.timeout")
					.superKey("postman.agent.chat.in.idle.timeout")
					.desc("Chat gets timed-out if customer does not respond for this interval in Minutes")
					.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL(
			new ConfigMeta("Inbound Idle Timeout Interval", "postman.agent.chat.in.idle.timeout.interval")
					.superKey("postman.agent.chat.in.idle.timeout")
					.desc("Chat gets timed-out if customer does not respond for this interval in Minutes")
					.inputType(INPUT_TYPE.NUMBER).min(5).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE(
			new ConfigMeta("In-bound Idle Timeout Queue", "postman.agent.chat.in.idle.timeout.queue")
					.superKey("postman.agent.chat.in.idle.timeout")
					.desc("Timed-out chat gets re-assigned to this queue")
					.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
					.group(ConfigConstants.GROUP_AGENT)),

//	POSTMAN_UI_BETA(new ConfigMeta("Enable Beta UI", "postman.ui.beta").optionsOnOff()
//		.defaultValue(ConfigOption.OFF).group(GROUP_AGENT)),
//	POSTMAN_AGENT_SCHEME2_COLOR(new ConfigMeta("Agent Color Scheme 2", "postman.agent.scheme2.color")
//		.inputType(OPTIONS_TYPE.COLOR_PALLETE).defaultValue(new ConfigMeta.ColorPalette()).group(GROUP_AGENT)),

	POSTMAN_AGENT_TAB_HISTORY_PERIOD(new ConfigMeta("Show History Period", "postman.agent.tab.history.period")
			.options(new ConfigOption(0).label("OFF"), new ConfigOption(TimeUtils.toMillis("1d")).label("+1Days"),
					new ConfigOption(TimeUtils.toMillis("3d")).label("+3Days"),
					new ConfigOption(TimeUtils.toMillis("5d")).label("+5Days"),
					new ConfigOption(TimeUtils.toMillis("7d")).label("+7Days"),
					new ConfigOption(TimeUtils.toMillis("2w")).label("+2Weeks"),
					new ConfigOption(TimeUtils.toMillis("3w")).label("+3Weeks"))
			.defaultValue(0).group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_TAB_ORG(new ConfigMeta("Agent can see Other Teams Chats", PROPERTIES.POSTMAN_AGENT_TAB_ORG).hidden()
			.deprecated().desc("Enables Org tab in Agent Panel").optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_TAB_LEVEL(new ConfigMeta("Agent can see Chats Assigned to", PROPERTIES.POSTMAN_AGENT_TAB_LEVEL)
			.desc("Enables Tabs in Agent Panel").optionValues("TEAM", "ORGANIZATION")
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_TAB_NONAGENT(new ConfigMeta("Agent can see Non-Agent Chats", PROPERTIES.POSTMAN_AGENT_TAB_NONAGENT)
			.desc("Agents will see Bot and Webhook chats under Org tabs ").optionsOnOff()
			.group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_TAB_HISTORY_LIMIT(new ConfigMeta("Show Chat Count Limit", "postman.agent.tab.history.limit")
			.options(new ConfigOption(100).label("100 Chats"), new ConfigOption(150).label("150 Chats"),
					new ConfigOption(200).label("200 Chats"))
			.defaultValue(100).group(ConfigConstants.GROUP_AGENT).hidden()),

	AGENT_MESSAGE_HELPER(new ConfigMeta("ChatGPT-based helper for outbound agents", "agent.message.helper").desc(
			"The agent will use ChatGPT to help draft a message before sending it . Please ensure that the API keys and ORG ID have already been set up in 'Token & Keys'")
			.optionsOnOff().group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_PHONEBOOK_REGION(new ConfigMeta("Default ISD Country", "postman.phonebook.region")
			.optionValues(ConfigConstants.PHONE_NUMBER_UTIL.getSupportedRegions().toArray()).defaultValue("IN")),

	POSTMAN_TIMEZONE_OFFSET(new ConfigMeta("Time Zone", "postman.timezone.offset")
			.optionValues(TimeZoneUtil.getTimeZoneLst().toArray()).defaultValue("Asia/Kolkata::GMT+5:30")),

	POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT(new ConfigMeta("Message to customer while chat is transferred to agent",
			"postman.agent.chat.autoreply.talk2agent").optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
					.optionsLabel("desc").group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED(new ConfigMeta("Message to customer when chat is resolved by agent",
			PROPERTIES.POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED).optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
					.optionsLabel("desc").group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT(new ConfigMeta("Message to customer when no agent available in working hours",
			"postman.agent.chat.autoreply.agent_notfound").optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
					.optionsLabel("desc").group(ConfigConstants.GROUP_AGENT)),

	POSTMAN_AGENT_CHAT_AUTOREPLY_ORGOFFLINE(

			new ConfigMeta("Message to customer when no agent available in non-working hours",
					"postman.agent.chat.autoreply.org_offline").optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
							.optionsLabel("desc").group(ConfigConstants.GROUP_AGENT)),
	POSTMAN_AGENT_CHAT_SCHEDULE(new ConfigMeta("List of schedules", "postman.agent.chat.schedule")
			.optionsSource("getx:/nexus/calendar/api/v1/orgSchedule/list").optionsKey("code").optionsLabel("desc")

			.group(ConfigConstants.GROUP_AGENT)),

	// NLP
	POSTMAN_NLP_LANGUAGE(new ConfigMeta("Enable Detect Language", "postman.nlp.detect.lang").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_SENTIMENT(new ConfigMeta("Enable Detect Sentiment", "postman.nlp.detect.sentiment").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_CATEGORIES(new ConfigMeta("Enable Detect Categories", "postman.nlp.detect.categories").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_PERSONS(new ConfigMeta("Enable Detect Persons", "postman.nlp.detect.persons").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_ORGANIZATIONS(new ConfigMeta("Enable Detect Organizations", "postman.nlp.detect.organizations")
			.optionsOnOff().defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_COUNTRIES(new ConfigMeta("Enable Detect Countries", "postman.nlp.detect.countries").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_CITIES(new ConfigMeta("Enable Detect Cities", "postman.nlp.detect.cities").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),
	POSTMAN_NLP_LOCATIONS(new ConfigMeta("Enable Detect Locations", "postman.nlp.detect.locations").optionsOnOff()
			.defaultValue(ConfigOption.OFF).group(ConfigConstants.GROUP_NLP)),

	POSTMAN_DEBUG_CONTACT(new ConfigMeta("Debugging is enabled for Contact", "postman.debug.contact")
			.group(ConfigConstants.GROUP_DEV).hidden()),

	// Ends here
	;

	private String key;
	private String ukey;
	private Object defaultValue;
	private ConfigMeta configMeta;

	CONFIG_SETUP_KEY(ConfigMeta defaultFalse) {
		this.configMeta = defaultFalse;
		this.key = defaultFalse.getKey();
		this.ukey = defaultFalse.getUkey();
		ConfigConstants.SETUP_CONFIG_LIST.add(defaultFalse);
		defaultValue = defaultFalse.getDefaultValue();
	}

	public String getKey() {
		return key;
	}

	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue() {
		return (T) defaultValue;
	}

	public ConfigMeta getConfigMeta() {
		return configMeta;
	}

	public String getUkey() {
		return ukey;
	}

}