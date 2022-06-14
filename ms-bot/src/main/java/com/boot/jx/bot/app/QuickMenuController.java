
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
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.QuickStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "bot_quick_menu" })
public class QuickMenuController extends CommonBotController {

	@Autowired
	private QuickStore commonMongoTemplate;

	private void showDefaultMenu() {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) { // item_menu_template
			reply(new OutboxMessage().template(template));
			next("on_item_select");
			return;
		}
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		showDefaultMenu();
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		if (!inboxMessage.session().isFirstMessage()) {
			showDefaultMenu();
		}
	}

	@ChatMapping(key = "on_item_select")
	public void onItemSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		String[] texts = StringUtils.split(text, " ");
		String commond = ArgUtil.parseAsString(StringUtils.trim(texts[0]), Constants.BLANK);
		String sign = commond.substring(0, 1);
		String code = commond.length() > 0 ? commond.substring(1) : null;

		if (ArgUtil.is(sign)) {
			switch (sign) {
			case "!":
				reply(new OutboxMessage().template(code));
				next("on_item_select");
				return;
			case "#":
				if (ArgUtil.is(code)) {
					assignToAgentDepartment(code);
				} else {
					assignToDefaultAgent();
				}
				return;
			case "@":
				routeSession(code);
				return;
			case "/":
				if (ArgUtil.is(code)) {
					List<QuickAction> items = commonMongoTemplate.findGalleryItems(code, QuickAction.class);
					for (QuickAction item : items) {
						if (ArgUtil.areEqual(StringUtils.toLowerCase(item.getCode()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getId()), text)
								|| ArgUtil.areEqual(StringUtils.toLowerCase(item.getTitle()), text)) {
							sendQuickAction(item);
						}
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
						}
					}
				}
			}
		}

		next("on_item_select");

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
