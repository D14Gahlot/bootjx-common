package com.boot.jx.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.postman.PMConstants.APP_TYPE;

public class ClientAppConfigConstants {

	public static final String[] APP_CONFIG_PREFIX = new String[] {
			// PRefixe
			"mry.prop.logo.", "mry.prop.service.", "mry.prop.social.", };
	public static final Map<APP_TYPE, ConfigMeta[]> APP_CONFIGS = new ConcurrentHashMap<APP_TYPE, ConfigMeta[]>();

	static {

		APP_CONFIGS.put(APP_TYPE.WEBHOOK, new ConfigMeta[] { new ConfigMeta().path("webhook").title("Webhook Url") });

		APP_CONFIGS.put(APP_TYPE.AGENT, new ConfigMeta[] {
				new ConfigMeta().path("prop.agent_connect_first").title("Greeting to first-time customer")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("TEMPLATES"),
				new ConfigMeta().path("prop.agent_connect_next")
						.title("Greeting to returning customer to start new conversation")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("TEMPLATES"),
				new ConfigMeta().path("prop.agent_connect_contiue")
						.title("Greetings to returning customer to conitune last conversation")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc")
						.group("TEMPLATES") });

		APP_CONFIGS.put(APP_TYPE.MITEL,
				new ConfigMeta[] { 
						new ConfigMeta().path("prop.end_point").title("Mitel End Point"),
						new ConfigMeta().path("prop.grant_type").title("Grant Type").options(
								new ConfigOption("client_credentials").label("Client Credentials"),
								new ConfigOption("password").label("Password")),
						new ConfigMeta().path("props.client_id").title("Client Id"),
						new ConfigMeta().path("secret.client_secret").title("Client Secret"),
						new ConfigMeta().path("props.queue").title("Mitel Queue"),
						new ConfigMeta().path("props.to").title("To").optional(),
						new ConfigMeta().path("props.from").title("Default From").optional() });

		APP_CONFIGS.put(APP_TYPE.BOT, new ConfigMeta[] { new ConfigMeta().path("prop.botCode").title("Bot Code") });

		APP_CONFIGS.put(APP_TYPE.AGENT_ROUTER,
				new ConfigMeta[] { new ConfigMeta().path("prop.template").title("Team Options").group("TEMPLATES")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc") });

		APP_CONFIGS.put(APP_TYPE.APP_ROUTER, new ConfigMeta[] { new ConfigMeta().path("prop.connect_first")
				.title("First-time customer").desc("First customers, with not Last Conversation")
				.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code").group("Apps"),
				new ConfigMeta().path("prop.connect_next").title("Returning customer to start new conversation")
						.desc("Returning customer, if last session was RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps"),
				new ConfigMeta().path("prop.connect_contiue").title("Returning customer to conitune last conversation")
						.desc("Returning customer, if last session was NOT RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps") });

	}

}
