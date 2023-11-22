package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.PROPERTIES;
import com.boot.model.MapModel.EntryMeta;
import com.boot.utils.TimeUtils;
import com.boot.utils.TimeZoneUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class ConfigConstants {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	public static final String[] APP_CONFIG_PREFIX = new String[] {
			// PRefixe
			"mry.prop.static.", "mry.prop.logo.", "mry.prop.service.", "mry.prop.social.", "mry.prop.theme.",
			// Domain Specific
			"mry.domain." };
	public static final Map<String, String> APP_CONFIG = new ConcurrentHashMap<String, String>();
	public static final List<ConfigMeta> SETUP_CONFIG_LIST = new ArrayList<ConfigMeta>();
	public static final List<ConfigMeta> PERMS_CONFIG_LIST = new ArrayList<ConfigMeta>();

	public static final String GROUP_CUSTOMER_CHAT = "CUSTOMER CHAT";
	public static final String GROUP_AGENT = "AGENT";
	public static final String GROUP_NLP = "NLP";
	public static final String GROUP_DEV = "DEVELOPMENT";


	public static enum APP_KEY implements EntryMeta {

		PROP_SERVICE_DOMAIN(new ConfigMeta("server", "mry.prop.service.domain")),
		PROP_SERVICE_SERVER(new ConfigMeta("server", "mry.prop.service.server")),
		PROP_SCRIPTUS_URL(new ConfigMeta("server", "mry.scriptus.url")),
		PROP_AGENT_URL(new ConfigMeta("server", "mry.agent.url")),
		PROP_BOT_URL(new ConfigMeta("server", "mry.bot.url")),
		// Ends here

		MITEL_SYNC_TIMER(new ConfigMeta("server", "mry.domain.mitel.sync.timer")),
		// Actually Ends Here
		;

		private String key;
		private String ukey;

		APP_KEY(ConfigMeta defaultFalse) {
			this.key = defaultFalse.getKey();
			this.ukey = defaultFalse.getUkey();
		}

		public String getKey() {
			return key;
		}

		@Override
		public String getUkey() {
			return ukey;
		}
	}

	public static enum SETUP_KEY implements EntryMeta {
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

		POSTMAN_CHAT_AGENT_QUEUE(
				new ConfigMeta("Default Agent Queue", "postman.chat.agent.queue").desc("Default Agent App")
						.optionsSource("getx:/api/options/agent_queue").optionsKey("code").optionsLabel("code")),

		POSTMAN_CHAT_FEEDBACK_QUEUE(new ConfigMeta("Default Feedback Queue", PROPERTIES.POSTMAN_CHAT_FEEDBACK_QUEUE)
				.desc("Default Feedback App").optionsSource("getx:/api/options/inbound_queue").optionsKey("code")
				.optionsLabel("code").filter("type", APP_TYPE.FEEDBACK)),

		POSTMAN_CHAT_CHANNEL_SANDBOX(new ConfigMeta("Enable Sandbox Channels", "postman.chat.channel.sandbox")
				.desc("Sandbox channels are preconfigured communication channels").optionsOnOff()),

		POSTMAN_CHAT_WEB_CHANNEL(new ConfigMeta("Default Web Channel", PROPERTIES.POSTMAN_CHAT_WEB_CHANNEL)
				.desc("This channel will be connected your Page").optionsSource("getx:/api/options/channels")
				.optionsKey("id").optionsLabel("name").filter("contactType", ContactType.WEBSITE)),

		POSTMAN_AGENT_2FA_ENABLED(new ConfigMeta("Enable 2FA Login", PROPERTIES.POSTMAN_AGENT_2FA_ENABLED)
				.superKey(PROPERTIES.POSTMAN_AGENT_2FA_ENABLED).desc("You will need OA app").optionsOnOff()),

		POSTMAN_AGENT_2FA_CHANNEL(new ConfigMeta("OTP Channel", PROPERTIES.POSTMAN_AGENT_2FA_CHANNEL)
				.superKey(PROPERTIES.POSTMAN_AGENT_2FA_ENABLED)
				.desc("This channel will be used for 2FA for Agent/Admin Login using login_otp template")
				.optionsSource("getx:/api/options/channels").optionsKey("id").optionsLabel("name")
				.filter("contactType", ContactType.OA)),

		// Agent Properties
		CHAT_TAG_ENABLED(
				new ConfigMeta("Chat Session Tags Enabled", "chat.tag.enabled").optionsOnOff().group(GROUP_AGENT)),
		POSTMAN_CHAT_SESSION_TIMEOUT(
				new ConfigMeta("Default Chat Session Idle Duration", "postman.chat.session.timeout")
						.desc("Use 2hr,3hr,4hr etc for hours and 2d,3d,4d etc for days.")
						// .optionValues("1hr", "2hr", "4hr", "8hr", "12hr", "16hr", "20hr", "24hr",
						// "2d", "5d", "3d", "7d")
						.group(GROUP_CUSTOMER_CHAT)),
		
		
		postman_agent_customer_contact_info_mask(new ConfigMeta("Mask Number","postman.agent.customer.contact.info.mask").optionsOnOff().group(GROUP_AGENT)),
		POSTMAN_AGENT_CHAT_DISABLE(new ConfigMeta("Disable Voice Record, Emoji and Attachments","postman.agent.customer.chat.disable").optionsOnOff().group(GROUP_AGENT)),
		
		POSTMAN_CHAT_IDLE_TIMEOUT(new ConfigMeta("Chat Alert Timer", "postman.chat.idle.timeout")
				.optionValues("5min", "10min", "15min", "20min", "25min", "30min").group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_INIT(
				new ConfigMeta("Agent can initiate new Chat", "postman.agent.chat.init").optionsOnOff().deprecated()),

		POSTMAN_AGENT_CHAT_INIT_SESSION(
				new ConfigMeta("Agent can initiate chat with existing contact", "postman.agent.chat.init.session")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_INIT_CONTACT(
				new ConfigMeta("Agent can initiate chat with new contact", "postman.agent.chat.init.contact")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_PICK_BOT(
				new ConfigMeta("Agent can INTERRUPT existing chat with bot", "postman.agent.chat.pick.bot")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_PICK_ASSIGNED(
				new ConfigMeta("Agent can INTERRUPT existing chat with other agent", "postman.agent.chat.pick.assigned")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_CC_FREETEXT(
				new ConfigMeta("Agent can send FreeText out of Customer Care Window", "postman.agent.chat.freetext.cc")
						.desc("Message can fail if selected channel does not permit").optionsOnOff()
						.group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_ONSEND_ASSIGNED(new ConfigMeta("Auto Assign Session", "postman.agent.chat.reassignment.auto")
				.desc("Session gets auto-assigned to sender agent").optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_ASSIGNMENT(new ConfigMeta("Agent Assignment", "postman.agent.chat.assignment")
				.optionValues(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN, PMConstants.ASSIGNMENT_RULE.MANUAL,
						PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT)
				.defaultValue(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN).group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_STICKY_RMAGENT(new ConfigMeta("Agent Assignment RM", "postman.agent.chat.sticky.rmagent")
				.optionValues(PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE, PMConstants.CHAT_SESSION_STICKY.STRICT)
				.defaultValue(PMConstants.CHAT_SESSION_STICKY.STRICT).group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_STICKYSESSION(new ConfigMeta("Sticky Session", "postman.agent.chat.stickysession")
				.optionValues(PMConstants.CHAT_SESSION_STICKY.NONE, PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE,
						PMConstants.CHAT_SESSION_STICKY.STRICT)
				.defaultValue(PMConstants.CHAT_SESSION_STICKY.NONE).group(GROUP_AGENT)),

		POSTMAN_AGENT_HEADER(new ConfigMeta("Header of Message Sent by agent", "postman.agent.chat.header")
				.desc("Use {{agent}} for agent name").group(GROUP_AGENT)),

		POSTMAN_AGENT_SCHEME_COLOR(new ConfigMeta("Agent Panel Color Scheme", "postman.agent.scheme.color")
				.inputType(INPUT_TYPE.COLOR).defaultValue("#4267b2").group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT(
				new ConfigMeta("Agent-Chat Outbound Idle Timeout Config", "postman.agent.chat.out.idle.timeout")
						.superKey("postman.agent.chat.out.idle.timeout")
						.desc("Chat gets timed-out if agent does not respond for this interval in Minutes")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL(new ConfigMeta("Agent-Chat Outbound Idle Timeout Interval",
				"postman.agent.chat.out.idle.timeout.interval").superKey("postman.agent.chat.out.idle.timeout")
						.desc("Chat gets timed-out if agent does not respond for this interval in Minutes")
						.inputType(INPUT_TYPE.NUMBER).min(5).group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE(
				new ConfigMeta("Agent-Chat Outbound Idle Timeout Queue", "postman.agent.chat.out.idle.timeout.queue")
						.superKey("postman.agent.chat.out.idle.timeout")
						.desc("Timed-out chat gets re-assigned to this queue")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT(
				new ConfigMeta("Inbound Idle Timeout Config", "postman.agent.chat.in.idle.timeout")
						.superKey("postman.agent.chat.in.idle.timeout")
						.desc("Chat gets timed-out if customer does not respond for this interval in Minutes")
						.optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL(
				new ConfigMeta("Inbound Idle Timeout Interval", "postman.agent.chat.in.idle.timeout.interval")
						.superKey("postman.agent.chat.in.idle.timeout")
						.desc("Chat gets timed-out if customer does not respond for this interval in Minutes")
						.inputType(INPUT_TYPE.NUMBER).min(5).group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE(
				new ConfigMeta("In-bound Idle Timeout Queue", "postman.agent.chat.in.idle.timeout.queue")
						.superKey("postman.agent.chat.in.idle.timeout")
						.desc("Timed-out chat gets re-assigned to this queue")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group(GROUP_AGENT)),

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
				.defaultValue(0).group(GROUP_AGENT)),

		POSTMAN_AGENT_TAB_ORG(new ConfigMeta("Agent can see Other Teams Chats", "postman.agent.tab.org")
				.desc("Enables Org tab in Agent Panel").optionsOnOff().group(GROUP_AGENT)),

		POSTMAN_AGENT_TAB_NONAGENT(
				new ConfigMeta("Agent can see Non-Agent Chats", PROPERTIES.POSTMAN_AGENT_TAB_NONAGENT)
						.desc("Agents will see Bot and Webhook chats under Org tabs ").optionsOnOff()
						.group(GROUP_AGENT)),

		POSTMAN_AGENT_TAB_HISTORY_LIMIT(
				new ConfigMeta("Show Chat Count Limit", "postman.agent.tab.history.limit")
						.options(new ConfigOption(100).label("100 Chats"), new ConfigOption(150).label("150 Chats"),
								new ConfigOption(200).label("200 Chats"))
						.defaultValue(100).group(GROUP_AGENT).hidden()),

		POSTMAN_PHONEBOOK_REGION(new ConfigMeta("Default ISD Country", "postman.phonebook.region")
				.optionValues(PHONE_NUMBER_UTIL.getSupportedRegions().toArray()).defaultValue("IN")),

		POSTMAN_TIMEZONE_OFFSET(new ConfigMeta("Time Zone", "postman.timezone.offset")
				.optionValues(TimeZoneUtil.getTimeZoneLst().toArray()).defaultValue("Asia/Kolkata::GMT+5:30")),

		POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT(new ConfigMeta("Message to customer while chat is transferred to agent",
				"postman.agent.chat.autoreply.talk2agent").optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
						.optionsLabel("desc").group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED(new ConfigMeta("Message to customer when chat is resolved by agent",
				PROPERTIES.POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED).optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
						.optionsLabel("desc").group(GROUP_AGENT)),

		POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT(
				new ConfigMeta("Message to customer when no agent available", "postman.agent.chat.autoreply.noagent")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc")
						.group(GROUP_AGENT)),

		// NLP
		POSTMAN_NLP_LANGUAGE(new ConfigMeta("Enable Detect Language", "postman.nlp.detect.lang").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_SENTIMENT(new ConfigMeta("Enable Detect Sentiment", "postman.nlp.detect.sentiment").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_CATEGORIES(new ConfigMeta("Enable Detect Categories", "postman.nlp.detect.categories")
				.optionsOnOff().defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_PERSONS(new ConfigMeta("Enable Detect Persons", "postman.nlp.detect.persons").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_ORGANIZATIONS(new ConfigMeta("Enable Detect Organizations", "postman.nlp.detect.organizations")
				.optionsOnOff().defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_COUNTRIES(new ConfigMeta("Enable Detect Countries", "postman.nlp.detect.countries").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_CITIES(new ConfigMeta("Enable Detect Cities", "postman.nlp.detect.cities").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),
		POSTMAN_NLP_LOCATIONS(new ConfigMeta("Enable Detect Locations", "postman.nlp.detect.locations").optionsOnOff()
				.defaultValue(ConfigOption.OFF).group(GROUP_NLP)),

		POSTMAN_DEBUG_CONTACT(
				new ConfigMeta("Debugging is enabled for Contact", "postman.debug.contact").group(GROUP_DEV).hidden()),

		// Ends here
		;

		private String key;
		private String ukey;
		private Object defaultValue;
		private ConfigMeta configMeta;

		SETUP_KEY(ConfigMeta defaultFalse) {
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

	public static enum PERMS_KEY implements EntryMeta {

		BUILD_VERSION(
				new ConfigMeta("BUILD_VERSION", "perms.build.version").inputType(INPUT_TYPE.NUMBER).defaultValue(3)),
		CONTACT_CENTER(new ConfigMeta("Contact Center", "perms.contact.center").optionsOnOff()),
		// Ends here
		;

		private String key;
		private Object defaultValue;
		private String ukey;

		PERMS_KEY(ConfigMeta defaultFalse) {
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

	static {
		ConfigConstants.SETUP_KEY.values();
		ConfigConstants.PERMS_KEY.values();
	}

}
