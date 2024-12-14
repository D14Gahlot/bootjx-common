package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.model.MapModel.EntryMeta;
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
	public static final String CHANNELS = "CHANNELS";
	public static final String BOT = "BOT";
	public static final String CAMPAIGN = "CAMPAIGN";
	public static final String APP_MODULES = "APP_MODULES";

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

	static {
		CONFIG_SETUP_KEY.values();
		CONFIG_FEATURES_KEY.values();
	}

}
