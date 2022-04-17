
package com.boot.jx.bot.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.bot.alex.AlexBotConstants;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.StringUtils.StringMatcher;

@BotController(name = "AgentTeam", code = { "bot_agent_team" })
public class AgentTeamController extends CommonBotController {

	public static final String REPLY_ID = "reply_id";

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@ChatMapping(key = AlexBotConstants.KEY.INITIATE + "*", pattern = "^*$")
	public void greet(InboxMessage inboxMessage, StringMatcher matcher) {
		String text = toReplyEnum(inboxMessage);
		List<DepartmentDoc> teams = commonMongoTemplate.find(CommonMongoQueryBuilder.collection(DepartmentDoc.class));
		for (DepartmentDoc team : teams) {
			if (ArgUtil.areEqual(StringUtils.toLowerCase(team.getDept_code()), text)) {
				assignToAgentDepartment(team.getDept_code());
				return;
			}
		}
		ClientApp app = context().clientApp();
		String template = ArgUtil.parseAsString(app.props().get("template"));
		if (ArgUtil.is(template)) {
			reply(new OutboxMessage().template(template));
		} else {
			List<TmplElement> buttons = new ArrayList<TmplElement>();
			for (DepartmentDoc team : teams) {
				buttons.add(new TmplElement().name(team.getDept_code()).label(team.getDept_name()));
			}
			reply(new OutboxMessage().options("buttons", buttons));
		}
	}

	public String toReplyEnum(InboxMessage inboxMessage) {
		String codeValue = inboxMessage.form().get(REPLY_ID) == null ? inboxMessage.getMessage()
				: inboxMessage.form().get(REPLY_ID).toString();
		if (ArgUtil.is(codeValue)) {
			codeValue = codeValue.toLowerCase().trim();
		}
		return codeValue;
	}
}
