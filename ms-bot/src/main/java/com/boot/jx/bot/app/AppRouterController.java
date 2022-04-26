
package com.boot.jx.bot.app;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AppRouter", code = { "bot_app_router" })
public class AppRouterController extends CommonBotController {

	@Autowired
	private SessionStore sessionStore;

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		super.onSessionRoute(assignEvent);
		ClientApp app = context().clientApp();

		ChatSessionDoc session = sessionStore.getPreviousSession(assignEvent.contact());

		if (!ArgUtil.is(session)) {
			Object connect_first = app.props().get("connect_first");
			if (ArgUtil.is(connect_first)) {
				routeSession(ArgUtil.parseAsString(connect_first));
			}
		} else if (session.isResolved()) {
			Object connect_next = app.props().get("connect_next");
			if (ArgUtil.is(connect_next)) {
				routeSession(ArgUtil.parseAsString(connect_next));
			}
		} else
		// if (session.isExpired())
		{
			Object connect_contiue = app.props().get("connect_contiue");
			if (ArgUtil.is(connect_contiue)) {
				routeSession(ArgUtil.parseAsString(connect_contiue));
			}
		}
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {

	}

}
