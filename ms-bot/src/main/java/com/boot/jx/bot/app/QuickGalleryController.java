
package com.boot.jx.bot.app;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.QuickStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "bot_quick_gallery" })
public class QuickGalleryController extends CommonBotController {

	@Autowired
	private QuickStore commonMongoTemplate;

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		// TODO Auto-generated method stub
		super.onSessionRoute(assignEvent);
		List<QuickMedia> teams = commonMongoTemplate.findAll(QuickMedia.class);
		gallerySelect(teams);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {

	}

	@ChatMapping(key = "on_category_select")
	public void onTeamSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		List<QuickMedia> teams = commonMongoTemplate.groupByCategory(QuickMedia.class);
		for (QuickMedia team : teams) {
			if (ArgUtil.areEqual(StringUtils.toLowerCase(team.getCode()), text)
					|| ArgUtil.areEqual(StringUtils.toLowerCase(team.getTitle()), text)) {
				// assignToAgentDepartment(team.getDept_code());
				return;
			}
		}
		gallerySelect(teams);
	}

	private void gallerySelect(List<QuickMedia> teams) {
//		ClientApp app = context().clientApp();
//		String template = ArgUtil.parseAsString(app.props().get("template"));
//		if (ArgUtil.is(template)) {
//			reply(new OutboxMessage().template(template));
//		} else {
//			List<TmplElement> buttons = new ArrayList<TmplElement>();
//			for (DepartmentDoc team : teams) {
//				buttons.add(new TmplElement().name(team.getDept_code()).label(team.getDept_name()));
//			}
//			reply(new OutboxMessage().message("Select team").options("buttons", buttons));
//		}
//		next("on_category_select");
	}

	//
	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void start(InboxMessage inboxMessage, StringMatcher matcher) {
		reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
		next("menu-on-select");
	}

	@ChatMapping(key = "menu-on-select")
	public void panOnSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		switch (toReplyEnum(inboxMessage)) {
		case "scoreboard":
			reply(new OutboxMessage().attachment(new Attachment()
					.mediaURL("https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/zpl/scoreboard.jpg")
					.mediaType(FileType.IMAGE.toString())));
			next("menu-on-select");
			break;
		case "points_table":
			reply(new OutboxMessage().attachment(new Attachment()
					.mediaURL("https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-content@main/zpl/points_table.jpg")
					.mediaType(FileType.IMAGE.toString())));
			next("menu-on-select");
			break;
		default:
			reply(new OutboxMessage().template("zpl_updates").put("name", context().contact().getName()));
			return;
		}
	}

}
