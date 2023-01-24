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

import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.dto.AgentResponseAgentDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.dto.DepartmentResponseAuthDto;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.mongo.CommonMongoSource;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.ContactStore;
import com.boot.utils.ArgUtil;

@Controller
public class AgentMetaController {

	@Value("${mry.admin.url}")
	private String adminUrl;

	@Autowired
	private AgentStore agentStore;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AgentSessionService agentSessionService;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private ContactStore contactStore;

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/api/options/agents" }, method = { RequestMethod.GET })
	public ApiResponse<AgentResponseAgentDto, Object> listAgents() {
		return ApiResponse.buildResults(new AgentResponseAgentDto().importFrom(agentStore.findAllActive()));
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/api/options/agent_teams" }, method = { RequestMethod.GET })
	public ApiResponse<DepartmentResponseAuthDto, Object> listTeams() {
		return ApiResponse.buildResults(new DepartmentResponseAuthDto().importFrom(agentStore.findDepartmentAll()));
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/api/options/contacts" }, method = { RequestMethod.GET })
	public ApiResponse<ContactDTO, Object> searchContacts(@RequestParam String search, @RequestParam String lane) {
		return ApiResponse.buildResults( // Wrap with ApiResponse
				ChatDTOUtil.getContactDTO( // Convert to DTO
						contactStore.searchContacts(search, lane) // Search Docs
				));
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/api/options/agents/status" }, method = { RequestMethod.GET })
	public ApiResponse<AgentSessionDoc, Object> listAgentsOnline() {
		return ApiResponse.buildResults(agentSessionService.getAgentSessions());
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
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

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.GET })
	public List<QuickMedia> listMediaReply() {
		return mongoTemplate.findAll(QuickMedia.class);
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = "/gallery/map/quick_actions", method = { RequestMethod.GET })
	public List<QuickAction> listQuickActions() {
		return mongoTemplate.findAll(QuickAction.class);
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/gallery/map/quick_labels" }, method = { RequestMethod.GET })
	public List<QuickLabel> listQuickTags() {
		return mongoTemplate.findAll(QuickLabel.class);
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = { "/gallery/map/quick_tags" }, method = { RequestMethod.GET })
	public List<QuickTag> listQuickTagsCategory() {
		return mongoTemplate.findAll(QuickTag.class);
	}

	@ApiRequest(rules = CommonMongoSource.READ_ONLY_DB)
	@ResponseBody
	@RequestMapping(value = "/api/tmpl/pushtemplate", method = { RequestMethod.GET })
	public ApiResponse<HSMTemplateDoc, Object> listPushTemplates(@RequestParam String channelId) {
		ChannelConfig channelConfig = pmEnvironment.local().channel(channelId);

		return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplateDoc.class));
	}
}
