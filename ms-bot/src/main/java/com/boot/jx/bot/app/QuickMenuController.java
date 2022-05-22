
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.QuickStore;
import com.boot.jx.postman.store.QuickStore.QuickGalleryItem;
import com.boot.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "bot_quick_gallery" })
public class QuickMenuController extends CommonBotController {

	@Autowired
	private QuickStore commonMongoTemplate;

	@SuppressWarnings("unchecked")
	private <T extends QuickGalleryItem> Class<T> getItemClass() {
		ClientApp app = context().clientApp();
		String itemType = ArgUtil.parseAsString(app.props().get("gallery_item_type"));
		if ("QUICK_MEDIA".equalsIgnoreCase(itemType)) {
			return (Class<T>) QuickMedia.class;
		} else if ("QUICK_ACTION".equalsIgnoreCase(itemType)) {
			return (Class<T>) QuickAction.class;
		} else if ("QUICK_REPLY".equalsIgnoreCase(itemType)) {
			return (Class<T>) QuickReply.class;
		}
		return (Class<T>) QuickReply.class;
	}

	private <T extends QuickGalleryItem> List<T> getGalleryCategories() {
		return commonMongoTemplate.groupByCategory(getItemClass());
	}

	private <T extends QuickGalleryItem> void showGalleryMenu(List<T> items) {
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		buttons.add(new TmplElement().name("*").label("^Exit"));
		buttons.add(new TmplElement().name("#").label("#TalkToAgent"));
		for (T item : items) {
			buttons.add(new TmplElement().name(item.getCategory()).label(item.getCategory()));
		}
		reply(new OutboxMessage().message("Select Category").options("buttons", buttons));
		next("on_category_select");
	}

	private void showGalleryMenu() {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("gallery_menu_template"));
		if (ArgUtil.is(template)) {
			reply(new OutboxMessage().template(template));
		} else {
			showGalleryMenu(getGalleryCategories());
		}
	}

	private void showItems(String category) {
		context().session().put("bot_quick_gallery_last_category", category);
		List<QuickGalleryItem> items = commonMongoTemplate.findByCategory(category, getItemClass());
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		buttons.add(new TmplElement().name("*").label("^MainMenu"));
		buttons.add(new TmplElement().name("#").label("#TalkToAgent"));
		for (QuickGalleryItem item : items) {
			buttons.add(new TmplElement().name(ArgUtil.nonEmpty(item.getCode(), item.getId())).label(item.getTitle()));
		}
		reply(new OutboxMessage().message("Please Select").options("buttons", buttons));
		next("on_item_select");
	}

	private void showItems() {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("item_menu_template"));
		if (ArgUtil.is(template)) { // item_menu_template
			reply(new OutboxMessage().template(template));
			next("on_item_select");
		} else {
			String category = new MapEntry(context().session().get("bot_quick_gallery_last_category")).asString();
			if (!ArgUtil.is(category)) {
				showGalleryMenu();
			} else {
				showItems(category);
			}
		}
	}

	private void showDefaultMenu() {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("item_menu_template"));
		if (ArgUtil.is(template)) { // item_menu_template
			showItems();
			return;
		}

		String category = ArgUtil.parseAsString(app.props().get("item_menu_category"));
		if (ArgUtil.is(category)) {
			showItems(category);
			return;
		}

		showGalleryMenu();
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		showDefaultMenu();
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		// System.out.println("Nothing....");
		if (!inboxMessage.session().isFirstMessage()) {
			showDefaultMenu();
		}
	}

	@ChatMapping(key = "on_category_select")
	public void onCategorySelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);

		if (ArgUtil.is(text)) {
			switch (text) {
			case "^Exit":
			case "^":
			case "*":
				routeSession(text);
				return;
			case "#TalkToAgent":
			case "#":
				assignToDefaultAgent();
				return;
			default:
				break;
			}
		}

		List<QuickGalleryItem> categories = getGalleryCategories();
		if (ArgUtil.is(text)) {
			for (QuickGalleryItem team : categories) {
				if (ArgUtil.areEqual(StringUtils.toLowerCase(team.getCategory()), text)) {
					showItems(text);
					return;
				}
			}
		}
		showGalleryMenu();
	}

	@ChatMapping(key = "on_item_select")
	public void onItemSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		switch (text) {
		case "^MainMenu":
		case "^":
		case "*":
			onCategorySelect(null, null);
			return;
		case "#TalkToAgent":
		case "#":
			assignToDefaultAgent();
			return;
		default:
			break;
		}
		if (ArgUtil.is(text)) {
			List<QuickGalleryItem> items = commonMongoTemplate.findGalleryItems(text, getItemClass());
			for (QuickGalleryItem item : items) {
				if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), text)
						|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), text)
						|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), text)) {

					if (item instanceof QuickMedia) {
						sendQuickMedia((QuickMedia) item);
					} else if (item instanceof QuickAction) {
						sendQuickAction((QuickAction) item);
					} else if (item instanceof QuickReply) {
						sendQuickReply((QuickReply) item);
					}
					next("on_item_select");
					return;
				}
			}
		}
		// No infromation Found
		showItems();
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

}
