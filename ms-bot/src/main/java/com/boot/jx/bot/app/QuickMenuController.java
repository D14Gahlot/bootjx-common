
package com.boot.jx.bot.app;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLocation;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.pbook.PBLocation;
import com.boot.jx.postman.pbook.PBVCard;
import com.boot.jx.postman.store.QuickStore;
import com.boot.jx.postman.store.QuickStore.QuickGalleryItem;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;
import com.boot.utils.UniqueID;

@BotController(name = "DemoBot", code = { "bot_quick_menu" })
public class QuickMenuController extends CommonBotController {

	@Autowired
	private QuickStore commonMongoTemplate;

	@SuppressWarnings("unchecked")
	private <T extends QuickGalleryItem> Class<T> getItemClass(String itemType) {
		switch (itemType) {
		case "QUICK_MEDIA":
		case "media":
			return (Class<T>) QuickMedia.class;
		case "QUICK_ACTION":
		case "action":
			return (Class<T>) QuickAction.class;
		case "QUICK_REPLY":
		case "reply":
			return (Class<T>) QuickReply.class;
		case "QUICK_LOCATION":
		case "location":
		case "loc":
			return (Class<T>) QuickLocation.class;
		default:
			break;
		}
		return (Class<T>) QuickReply.class;
	}

	private void showDefaultMenu(String log) {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) { // item_menu_template
			OutboxMessage msg = new OutboxMessage().template(template);
			msg.trace().add(log);
			reply(msg);
			next("on_item_select");
			return;
		}
	}

	private void showDefaultMenu() {
		showDefaultMenu(null);
	}

	private void showWrongOptionMenu() {
		ClientApp app = context().clientApp();

		String action = ArgUtil.parseAsString(app.props().get("noption_action"));
		if (ArgUtil.is(action)) {
			if (selectQuickOption(action)) {
				return;
			}
		}

		String template = ArgUtil.parseAsString(app.props().get("noption_template"));
		if (ArgUtil.is(template)) { // item_menu_template
			reply(new OutboxMessage().template(template));
			next("on_item_select");
			return;
		} else {
			showDefaultMenu("wrong");
		}
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		showDefaultMenu("onSessionRoute");
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		showDefaultMenu("mobile " + System.currentTimeMillis() + "  " + UniqueID.generateString62());
	}

	@ChatMapping(key = "on_item_select")
	public void onItemSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		if (!selectQuickOption(text)) {
			showWrongOptionMenu();
		}
	}

	private boolean selectQuickOption(String text) {
		String[] texts = StringUtils.split(text, " ");
		String commond = ArgUtil.parseAsString(StringUtils.trim(texts[0]), Constants.BLANK);
		String sign = commond.substring(0, 1);
		String code = (commond.length() > 0 ? commond.substring(1) : Constants.BLANK).toLowerCase();

		if (ArgUtil.is(sign)) {
			switch (sign) {
			case "!":
				reply(new OutboxMessage().template(code));
				next("on_item_select");
				return true;
			case "#":
				if (ArgUtil.is(code)) {
					assignToAgentDepartment(code);
				} else {
					assignToDefaultAgent();
				}
				return true;
			case "@":
				routeSession(code);
				return true;
			case "/":
				if (ArgUtil.is(code)) {
					List<QuickAction> items = commonMongoTemplate.findGalleryItems(code, QuickAction.class);
					for (QuickAction item : items) {
						if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), text)) {
							sendQuickAction(item);
							next("on_item_select");
							return true;
						}
					}
					if ("exit_chat".equals(code)) {
						this.closeSession();
						return true;
					}
					if ("resolve_chat".equals(code)) {
						this.resolveSession();
						return true;
					}
				}
			case "&":
				if (ArgUtil.is(code)) {
					List<QuickMedia> items = commonMongoTemplate.findGalleryItems(code, QuickMedia.class);
					for (QuickMedia item : items) {
						if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), text)) {
							sendQuickMedia(item);
							next("on_item_select");
							return true;
						}
					}
				}
			case "%":
				if (ArgUtil.is(code)) {
					List<QuickReply> items = commonMongoTemplate.findGalleryItems(code, QuickReply.class);
					for (QuickReply item : items) {
						if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), text)) {
							sendQuickReply(item);
							next("on_item_select");
							return true;
						}
					}
				}
			case "$":
				if (ArgUtil.is(code)) {
					String itemCode = (texts.length > 0
							? ArgUtil.parseAsString(StringUtils.trim(texts[1]), Constants.BLANK)
							: Constants.BLANK).toLowerCase();
					if (ArgUtil.is(itemCode)) {
						switch (code) {
						case "hsm":
							reply(new OutboxMessage().template(itemCode));
							next("on_item_select");
							return true;
						default: {
							List<QuickGalleryItem> items = commonMongoTemplate.findGalleryItems(itemCode,
									getItemClass(code));
							for (QuickGalleryItem item : items) {
								if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), itemCode)
										|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), itemCode)
										|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), itemCode)) {
									if (item instanceof QuickMedia) {
										sendQuickMedia((QuickMedia) item);
									} else if (item instanceof QuickAction) {
										sendQuickAction((QuickAction) item);
									} else if (item instanceof QuickReply) {
										sendQuickReply((QuickReply) item);
									} else if (item instanceof QuickLocation) {
										sendQuickLocation((QuickLocation) item);
									}
									next("on_item_select");
									return true;
								}
							}
						}
						}
					}
				}
			}
		}
		return false;
	}

	private void sendQuickMedia(QuickMedia media) {
		reply(new OutboxMessage().attachment(new Attachment().mediaURL(media.getUrl())
				.mediaType(FileType.IMAGE.toString()).mediaCaption(media.getTitle()).mediaName(media.getTitle())));
	}

	private void sendQuickReply(QuickReply item) {
		OutboxMessage msg = new OutboxMessage();
		msg.hsm().id("QR=" + item.getId());
		msg.hsm().code("QR=" + item.getCode());
		reply(msg);
	}

	private void sendQuickAction(QuickAction item) {
		OutboxMessage msg = new OutboxMessage();
		msg.setAction(item.getAction());
		reply(msg);
	}

	private void sendQuickLocation(QuickLocation item) {
		OutboxMessage msg = new OutboxMessage();
		PBLocation pbLocation = new PBLocation();
		pbLocation.setName(item.getTitle());
		pbLocation.setAddress(item.getAddress());
		pbLocation.setLatitude(item.getLatitude());
		pbLocation.setLongitude(item.getLongitude());
		pbLocation.setUrl(item.getUrl());
		msg.vccards().add(new PBVCard().locations(pbLocation));
		reply(msg);
	}
}
