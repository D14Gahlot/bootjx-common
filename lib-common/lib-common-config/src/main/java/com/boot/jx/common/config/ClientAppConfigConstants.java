package com.boot.jx.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.ConfigOption;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.common.impl.ConfigMeta.MESSAGE_TYPE;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;

public class ClientAppConfigConstants {

	public static final String[] APP_CONFIG_PREFIX = new String[] {
			// PRefixe
			"mry.props.logo.", "mry.props.service.", "mry.props.social.", };
	public static final Map<APP_TYPE, ConfigMeta[]> APP_CONFIGS = new ConcurrentHashMap<APP_TYPE, ConfigMeta[]>();

	static {

		APP_CONFIGS.put(APP_TYPE.DEFAULT, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Basic App")
						.desc("Use this app to Send Messages using API End Point").group("About App") });

		APP_CONFIGS.put(APP_TYPE.WEBHOOK, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("External Webhook")
						.desc("Use this app to Receive Inbound Messages on Webhook URL and Reply using API End Point")
						.group("About App"),

				new ConfigMeta().path("webhook").title("Webhook Url") });

		APP_CONFIGS.put(APP_TYPE.AGENT, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Agent Desk")
						.desc("Use this app to route session to AgentDesk.").group("About App"),

				new ConfigMeta().title("Default Agent Team").path("props.deptCode")
						.optionsSource("getx:/api/admins/dept").optionsKey("code").optionsLabel("Name").group("Team"),
				new ConfigMeta().title("Default Agent").path("props.agentCode").optionsSource("getx:/api/admins/agent")
						.optionsKey("code").optionsLabel("Name").group("Team"),
				new ConfigMeta().title("When Agent is connected").path("props.agent_connected")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("Templates"),
				new ConfigMeta().title("When no agent is found").path("props.agent_notfound")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc").group("Templates"),
				new ConfigMeta().title("When chat is transferred").path("props.agent_transfer")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc")
						.group("Templates") });

		APP_CONFIGS.put(APP_TYPE.MITEL, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Mitel")
						.desc("Use this app to route session to Mitel Instance").group("About App"),

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

		APP_CONFIGS.put(APP_TYPE.BOT,
				new ConfigMeta[] {
						new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Bot")
								.desc("Use this app to route session to default BOT flow").group("About App"),

						new ConfigMeta().title("Bot Code").path("props.botCode").example("complaint_flow") });

		APP_CONFIGS.put(APP_TYPE.TEAM_ROUTER, new ConfigMeta[] { new ConfigMeta()
				.inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Team Router")
				.desc("Use this app to route session to Team based on customer's input. Selected template should have team code in button code")
				.group("About App"),

				new ConfigMeta().title("Team Options Template").path("props.template").group("TEMPLATES")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("desc") });

		APP_CONFIGS.put(APP_TYPE.APP_SWITCH, new ConfigMeta[] { new ConfigMeta()
				.inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("App Switch Menu")
				.desc("Use this app to giver user an option menu to switch between app. Selected template should have queue code in button code")
				.group("About App"),
				new ConfigMeta().title("App Options Template").path("props.template").group("TEMPLATES")
						.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("code") });

		APP_CONFIGS.put(APP_TYPE.APP_ROUTER, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Session based App Router")
						.desc("Use this app to route session based on customer's session status").group("About App"),

				new ConfigMeta().title("First-time customer").path("props.connect_first")
						.desc("First time customers, with no Last Conversation hitsory")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps"),
				new ConfigMeta().title("Returning customer to start new conversation").path("props.connect_next")
						.desc("Returning customer, if last session was RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps"),
				new ConfigMeta().title("Returning customer to conitune last conversation").path("props.connect_contiue")
						.desc("Returning customer, if last session was NOT RESOLVED")
						.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").optionsLabel("code")
						.group("Apps")

		});

		APP_CONFIGS.put(APP_TYPE.QUICK_GALLERY, new ConfigMeta[] {

				new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Quick Gallery")
						.desc("Use this app to give user an option menu to Explore Quick Gallery."
								+ "\n Selected template should have Item code in button code")
						.group("About App"),

				new ConfigMeta().title("Item Type").path("props.gallery_item_type").options(
						new ConfigOption("QUICK_MEDIA").label("QUICK_MEDIA"),
						new ConfigOption("QUICK_ACTION").label("QUICK_ACTION"),
						new ConfigOption("QUICK_REPLY").label("QUICK_REPLY")),

				new ConfigMeta().title("Category Menu Template")
						.desc("If Set, will show this template instead of all categories, "
								+ "Selected template should use category name as button code")
						.path("props.gallery_menu_template").group("TEMPLATES").optionsSource("getx:/api/tmpl/hsm")
						.optionsKey("code").optionsLabel("code").optional(),

				new ConfigMeta().title("Item Menu Template")
						.desc("If Set, will show this template instead of all categories, "
								+ "Selected template should use Item Code name as button code")
						.path("props.item_menu_template").group("TEMPLATES").optionsSource("getx:/api/tmpl/hsm")
						.optionsKey("code").optionsLabel("code").optional(),

				new ConfigMeta().title("Item Menu Category")
						.desc("if set, it will skip the category menu and display items in this category")
						.path("props.item_menu_category").optional()

		});

		APP_CONFIGS.put(APP_TYPE.QUICK_MENU, new ConfigMeta[] { new ConfigMeta()
				.inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Quick Menu")
				.desc("Use this app to create Quick Menu. Selected template should have codes in button code prefixed with type of trigger."
						+ "\n[ ! HSMTemplate ] " //
						+ "\n[ # Team] " //
						+ "\n[ @ App] " //
						+ "\n[ / QuickAction] "//
						+ "\n[ & QuickMedia] " //
						+ "\n[ % QuickReply] "//
				).group("About App"),

				new ConfigMeta().title("First Options Template")
						.desc("Use coding convetions in template for this to work").path("props.template")
						.group("TEMPLATES").optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("code"),

				new ConfigMeta().title("No/Wrong Options Template")
						.desc("Use coding convetions in template for this to work").path("props.noption_template")
						.group("TEMPLATES").optionsSource("getx:/api/tmpl/hsm").optionsKey("code")
						.optionsLabel("code") });

		APP_CONFIGS.put(APP_TYPE.FEEDBACK,
				new ConfigMeta[] { new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Feedback")
						.desc("Use this app to collect feedback from customers when ticket is closed"//
						).group("About App"),
						new ConfigMeta().title("Feedback Message Template").path("props.template").group("TEMPLATES")
								.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("code"),
						new ConfigMeta().title("Session Close Template").path("props.template_close").group("TEMPLATES")
								.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("code")

				});

		APP_CONFIGS.put(APP_TYPE.APP_SCRIPT,
				new ConfigMeta[] { new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("App Script")
						.desc("Use this app to write custom handling of inbound/outboud messages using script editor "//
						).group("About App"),
						new ConfigMeta().title("Sender Type").path("props.sender_type").options(
								new ConfigOption(MESSAGE_SENDER_TYPE.BOT).label("Bot"),
								new ConfigOption(MESSAGE_SENDER_TYPE.AGENT).label("Agent"),
								new ConfigOption(MESSAGE_SENDER_TYPE.ADMIN).label("Admin"),
								new ConfigOption(MESSAGE_SENDER_TYPE.SYSTEM).label("System"))

				});

		APP_CONFIGS.put(APP_TYPE.FAQ,
				new ConfigMeta[] { new ConfigMeta().inputType(INPUT_TYPE.MESSAGE, MESSAGE_TYPE.INFO).title("Faq")
						.desc("Use this app  to tranfer from FAQ bot to Bot Builder Menu"//
						).group("About App"),
						new ConfigMeta().title("Bot Builder Template").path("props.template").group("TEMPLATES")
								.optionsSource("getx:/api/tmpl/hsm").optionsKey("code").optionsLabel("code"),

				});
	}

}
