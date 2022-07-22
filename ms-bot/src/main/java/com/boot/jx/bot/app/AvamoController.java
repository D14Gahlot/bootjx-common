
package com.boot.jx.bot.app;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.JsonPath;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AvamoBot", code = { "bot_avamo" })
public class AvamoController extends CommonBotController {

	@Autowired
	public RestService restService;

	@Override
	public void externalOutboundMessage(MapModel mapModel) {
		OutboxMessage outbox = new OutboxMessage().message(mapModel.pathEntry("message.text").asString());
		outbox.setContact(mapModel.pathEntry("user.custom_properties").as(ContactMeta.class));
		outbox.route().setSenderCode(mapModel.keyEntry("sender").asString());
		send(outbox);
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		ClientApp app = context().clientApp();
		MapModel prop = MapModel.from(app.props());

		MapPathEntry channel_uuid = prop.keyEntry("channel_uuid");
		MapPathEntry end_point = prop.keyEntry("end_point");

		if (end_point.exists() && channel_uuid.exists()) {
			restService.ajax(end_point.asString()).post(//
					MapModel.createInstance()//
							.put("channel_uuid", channel_uuid.asString())//
							.put("user", MapModel.createInstance()//
									.put("first_name", inboxMessage.contact().getName())//
									.put("uuid", inboxMessage.getSessionId())//
									.put("custom_properties", inboxMessage.contact())//
									.toMap())
							.put("message", MapModel.createInstance()//
									.put("text", inboxMessage.getMessage())//
									.toMap())//
							.toMap());
		}

	}

}
