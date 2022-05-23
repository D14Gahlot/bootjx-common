
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "TeamRouter", code = { "bot_team_router" })
public class AgentTeamRouterController extends CommonBotController {

	public static Logger LOGGER = LoggerService.getLogger(AgentTeamRouterController.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Override
	public void onSessionRoute(InBoundEvent assignEvent) {
		LOGGER.debug("Loading chat :onSessionRoute");
		super.onSessionRoute(assignEvent);
		List<DepartmentDoc> teams = commonMongoTemplate.findAll(DepartmentDoc.class);
		askTeam(teams);
	}

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		LOGGER.debug("Loading chat :greet : isFM{}", inboxMessage.session().isFirstMessage());
		if (!inboxMessage.session().isFirstMessage()) {
			List<DepartmentDoc> teams = commonMongoTemplate.findAll(DepartmentDoc.class);
			askTeam(teams);
		}
	}

	@ChatMapping(key = "on_team_select")
	public void onTeamSelect(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		List<DepartmentDoc> teams = commonMongoTemplate.findAll(DepartmentDoc.class);
		for (DepartmentDoc team : teams) {
			if (ArgUtil.areEqual(StringUtils.toLowerCase(team.getDept_code()), text)
					|| ArgUtil.areEqual(StringUtils.toLowerCase(team.getDept_name()), text)
					|| ArgUtil.areEqual(StringUtils.toLowerCase(team.getDept_id()), text)) {
				assignToAgentDepartment(team.getDept_code());
				return;
			}
		}
		askTeam(teams);
	}

	private void askTeam(List<DepartmentDoc> teams) {
		LOGGER.debug("Loading chat :askTeam");
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) {
			reply(new OutboxMessage().template(template));
		} else {
			List<TmplElement> buttons = new ArrayList<TmplElement>();
			for (DepartmentDoc team : teams) {
				buttons.add(new TmplElement().name(team.getDept_code()).label(team.getDept_name()));
			}
			reply(new OutboxMessage().message("Select team").options("buttons", buttons));
		}
		next("on_team_select");
	}

}
