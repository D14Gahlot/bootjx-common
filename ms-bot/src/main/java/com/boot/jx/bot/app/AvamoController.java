
package com.boot.jx.bot.app;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
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
	public void onPostOutboundMessage(MapModel mapModel) {
		OutboxMessage outbox = new OutboxMessage();;
		outbox.setContact(mapModel.pathEntry("user.custom_properties").as(ContactMeta.class));
		outbox.route().setSenderCode(mapModel.keyEntry("sender").asString());
		context().setOutboxMessage(outbox);
		reply(mapModel.pathEntry("message").asMap(), outbox);
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
			MapModel x = restService.ajax(end_point.asString()).post(//
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
							.toMap())
					.asMapModel();
			context().session().set("meta.avamo.conversation", x.pathEntry("incoming_message.conversation").asMap());

			List<Map<String, Object>> botReplies = x.pathEntry("incoming_message.bot_replies").asListOfMap();
			if (botReplies.size() > 0) {
				for (Map<String, Object> map : botReplies) {
					reply(map, new OutboxMessage());
				}
			}
		}

	}

	private void reply(Map<String, Object> map, OutboxMessage outboxMessage) {
		MapModel botreply = MapModel.from(map);
		MapPathEntry text = botreply.keyEntry("text");
		MapPathEntry quickReplyEntries = botreply.keyEntry("quick_replies");
		MapPathEntry attachment = botreply.keyEntry("attachment");
		if (attachment.exists()) {
			if (attachment.keyEntry("type").is("template")) {
				MapPathEntry payload = attachment.keyEntry("payload");
				if (payload.keyEntry("template_type").is("button")) {
					text = payload.keyEntry("text");
					if (text.exists()) {
						outboxMessage.message(text.asString());
					}
					MapPathEntry buttonEntries = payload.keyEntry("buttons");
					if (buttonEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> button : buttonEntries.asListOfMap()) {
							MapModel buttonEntry = MapModel.from(button);
							buttons.add(new TmplElement() //
									.type(buttonEntry.keyEntry("type").asString())//
									.code(buttonEntry.keyEntry("payload").asString()) //
									.label(buttonEntry.keyEntry("title").asString()));
						}
						outboxMessage.buttons(buttons);
					}

				} else if (payload.keyEntry("template_type").is("list")) {
					MapPathEntry elementEntries = payload.keyEntry("elements");
					if (elementEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> element : elementEntries.asListOfMap()) {
							MapModel elementEntry = MapModel.from(element);

							outboxMessage.option("list_option_title", elementEntry.keyEntry("title").asString("Menu"));

							MapPathEntry subtitle = elementEntry.keyEntry("subtitle");
							if (subtitle.exists()) {
								outboxMessage.message(text.asString());
							}

							MapPathEntry buttonEntries = elementEntry.keyEntry("buttons");
							if (buttonEntries.exists()) {
								for (Map<String, Object> button : buttonEntries.asListOfMap()) {
									MapModel buttonEntry = MapModel.from(button);
									buttons.add(new TmplElement() //
											.type(buttonEntry.keyEntry("type").asString())//
											.code(buttonEntry.keyEntry("payload").asString()) //
											.label(buttonEntry.keyEntry("title").asString()));
								}
							}
						}
						outboxMessage.buttons(buttons);
					}
				} else if (payload.keyEntry("template_type").is("generic")) {
					MapPathEntry elementEntries = payload.keyEntry("elements");
					if (elementEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> element : elementEntries.asListOfMap()) {
							MapModel elementEntry = MapModel.from(element);

							outboxMessage.option("list_option_title", elementEntry.keyEntry("title").asString("Menu"));

							MapPathEntry subtitle = elementEntry.keyEntry("subtitle");
							MapPathEntry image = elementEntry.keyEntry("image_url");
							if (image.exists()) {
								Attachment attch = new Attachment().mediaURL(image.asString())
										.mediaType(FileType.IMAGE);
								if (subtitle.exists()) {
									attch.mediaCaption(subtitle.asString());
								}
								outboxMessage.attachment(attch);
							} else if (subtitle.exists()) {
								outboxMessage.message(text.asString());
							}

							MapPathEntry buttonEntries = elementEntry.keyEntry("buttons");
							if (buttonEntries.exists()) {
								for (Map<String, Object> button : buttonEntries.asListOfMap()) {
									MapModel buttonEntry = MapModel.from(button);
									buttons.add(new TmplElement() //
											.type(buttonEntry.keyEntry("type").asString())//
											.code(buttonEntry.keyEntry("payload").asString()) //
											.label(buttonEntry.keyEntry("title").asString()));
								}
							}

						}
						outboxMessage.buttons(buttons);
					}
				}
			}
			send(outboxMessage);
			return;
		} else if (quickReplyEntries.exists()) {
			List<TmplElement> buttons = TmplElement.list();
			for (Map<String, Object> quickReply : quickReplyEntries.asListOfMap()) {
				MapModel quickReplyEntry = MapModel.from(quickReply);
				buttons.add(new TmplElement() //
						.type(quickReplyEntry.keyEntry("content_type").asString())//
						.code(quickReplyEntry.keyEntry("payload").asString()) //
						.label(quickReplyEntry.keyEntry("title").asString()));
			}
			outboxMessage.buttons(buttons);
			if (text.exists()) {
				outboxMessage.message(text.asString());
			}
			send(outboxMessage);
			return;
		} else if (text.exists()) {
			send(outboxMessage.message(text.asString()));
			return;
		}
	}

}
