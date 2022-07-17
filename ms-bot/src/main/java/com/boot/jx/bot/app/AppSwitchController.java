
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AppSwitch", code = { "bot_app_switch" })
public class AppSwitchController extends CommonBotController {

	@Autowired
	PMEnvironment env;

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		// TODO Auto-generated method stub
		super.onSessionRoute(assignEvent);
		List<ClientApp> apps = env.config().listApps();;
		askApp(apps);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {

	}

	@ChatMapping(key = "on_app_select")
	public void onAppSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		List<ClientApp> apps = env.config().listApps();;
		for (ClientApp app : apps) {
			if (ArgUtil.areEqual(StringUtils.toLowerCase(app.getId()), text)
					|| ArgUtil.areEqual(StringUtils.toLowerCase(app.getQueue()), text)
					|| ArgUtil.areEqual(StringUtils.toLowerCase(app.getKeyName()), text)) {
				routeSession(app.getQueue());
				return;
			}
		}
		askApp(apps);
	}

	private void askApp(List<ClientApp> apps) {
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) {
			reply(new OutboxMessage().template(template));
		} else {
			List<TmplElement> buttons = new ArrayList<TmplElement>();
			for (ClientApp appTemp : apps) {
				buttons.add(new TmplElement().code(appTemp.getQueue()).label(appTemp.getKeyName()));
			}
			reply(new OutboxMessage().message("Select").options("buttons", buttons));
		}
		next("on_app_select");
	}

}
