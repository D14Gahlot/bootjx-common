
package com.boot.jx.bot.app;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "DemoBot", code = { "bot_feedback" })
public class FeedbackController extends CommonBotController {

	private void showFeedbackMenu() {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) { // item_menu_template
			reply(new OutboxMessage().template(template));
			next("feedback-onselect");
			return;
		}
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		showFeedbackMenu();
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		if (!inboxMessage.session().isFirstMessage()) {
			showFeedbackMenu();
		}
	}

	@ChatMapping(key = "feedback-onselect")
	public void feedback(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		if (StringUtils.isNumeric(text)) {
			context().session().set("feedback.score", ArgUtil.parseAsDouble(text));
		} else {
			context().session().set("feedback.tag", StringUtils.trim(text));
		}
		closeSession();
	}

}
