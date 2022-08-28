
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
		} else {
			reply(new OutboxMessage().message("Thank you for interacting with us via WhatsApp Support.\n"
					+ "Kindly share your feedback on a scale of 1-5."
					+ "1 being Not Satisfied and 5 being Very Satisfied"));
		}
		next("feedback-onselect");
	}

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		showFeedbackMenu();
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		showFeedbackMenu();
	}

	@ChatMapping(key = "feedback-onselect")
	public void feedback(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		if (StringUtils.isNumeric(text)) {
			context().session().set("feedback.score", ArgUtil.parseAsDouble(text));
		} else {
			context().session().set("feedback.tag", StringUtils.trim(text));
		}
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template_close"));
		if (ArgUtil.is(template)) { // item_menu_template
			reply(new OutboxMessage().template(template));
		}
		logManager.trace(inboxMessage, "FeedbackController.feedback", "closeSession");
		closeSession();
	}

}
