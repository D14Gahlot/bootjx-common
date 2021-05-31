package com.boot.jx.agent.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.agent.dto.AgentResponseAgentDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.utils.ArgUtil;

@Controller
public class AgentMetaController {

	@Value("${mry.admin.url}")
	private String adminUrl;

	@Autowired
	private AgentStore agentStore;

	@Autowired
	private MongoTemplate mongoTemplate;

	@ResponseBody
	@RequestMapping(value = { "/api/options/agents" }, method = { RequestMethod.GET })
	public ApiResponse<AgentResponseAgentDto, Object> listAgents() {
		return ApiResponse.buildResults(new AgentResponseAgentDto().importFrom(agentStore.findAllActive()));
	}

	@ResponseBody
	@RequestMapping(value = { "/api/options/agents/status" }, method = { RequestMethod.GET })
	public ApiResponse<AgentResponseAgentDto, Object> listAgentsOnline() {
		return ApiResponse.buildResults(new AgentResponseAgentDto().importFrom(agentStore.findAll()));
	}

	@ResponseBody
	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<QuickReply> listSmartReply(@RequestParam(value = "value", required = false) List<String> categories) {
		if (ArgUtil.is(categories)) {
			Query query2 = new Query();
			query2.addCriteria(Criteria.where("category").in(categories.stream().toArray(String[]::new)));
			return mongoTemplate.find(query2, QuickReply.class);
		}
		return mongoTemplate.findAll(QuickReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.GET })
	public List<TemplateReply> listMediaReply() {
		return mongoTemplate.findAll(TemplateReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/gallery/map/quick_actions", method = { RequestMethod.GET })
	public List<QuickAction> listQuickActions() {
		return mongoTemplate.findAll(QuickAction.class);
	}

	@ResponseBody
	@RequestMapping(value = { "/gallery/map/quick_labels" }, method = { RequestMethod.GET })
	public List<QuickLabel> listQuickTags() {
		return mongoTemplate.findAll(QuickLabel.class);
	}
}
