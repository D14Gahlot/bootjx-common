package com.boot.jx.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.postman.PMConstants.APP_TYPE;

public class ClientAppConfigConstants {

	public static final String[] APP_CONFIG_PREFIX = new String[] {
			// PRefixe
			"mry.props.logo.", "mry.props.service.", "mry.props.social.", };
	public static final Map<APP_TYPE, ConfigMeta[]> APP_CONFIGS = new ConcurrentHashMap<APP_TYPE, ConfigMeta[]>();

	static {

		APP_CONFIGS.put(APP_TYPE.WEBHOOK, new ConfigMeta[] { new ConfigMeta().path("webhook").title("Webhook Url") });

		APP_CONFIGS.put(APP_TYPE.AGENT, new ConfigMeta[] {

				new ConfigMeta().title("Default Agent Team").path("props.agentCode")
						.optionsSource("getx:/api/admins/dept").group("Team"),
				new ConfigMeta().title("When Agent is connected").path("props.agent_connected")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("Templates"),
				new ConfigMeta().title("When no agent is found").path("props.agent_notfound")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("Templates"),
				new ConfigMeta().title("When chat is transferred").path("props.agent_transfer")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc")
						.group("Templates") });

		APP_CONFIGS.put(APP_TYPE.MITEL,
				new ConfigMeta[] {
						new ConfigMeta().title("Mitel End Point").path("props.end_point")
								.example("http://yourerver.com/callback_path"),
						new ConfigMeta().title("Grant Type").path("props.grant_type").options(
								new ConfigOption("client_credentials").label("Client Credentials"),
								new ConfigOption("password").label("Password")),
						new ConfigMeta().title("Client Id").path("props.client_id").example("ProfessionalServices"),
						new ConfigMeta().title("Client Secret").path("secret.client_secret"),
						new ConfigMeta().title("Mitel Queue").path("props.queue")
								.example("6106ee72-81a1-49a7-9e10-df591d5194f3"),
						new ConfigMeta().title("To").path("props.to").optional(),
						new ConfigMeta().title("Default From").path("props.from").optional() });

		APP_CONFIGS.put(APP_TYPE.BOT, new ConfigMeta[] {
				new ConfigMeta().title("Bot Code").path("props.botCode").example("complaint_flow") });

		APP_CONFIGS
				.put(APP_TYPE.TEAM_ROUTER,new ConfigMeta[] { 
								
				new ConfigMeta().title("Team Options Template")
						.path("props.template").group("TEMPLATES").
						optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc")
						
				});

		APP_CONFIGS.put(APP_TYPE.APP_ROUTER, new ConfigMeta[] {

				new ConfigMeta().title("First-time customer")
						.path("props.connect_first")
						.desc("First customers, with not Last Conversation")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps"),
				new ConfigMeta().title("Returning customer to start new conversation")
						.path("props.connect_next")
						.desc("Returning customer, if last session was RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps"),
				new ConfigMeta().title("Returning customer to conitune last conversation")
						.path("props.connect_contiue")
						.desc("Returning customer, if last session was NOT RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps") });

	}

}
