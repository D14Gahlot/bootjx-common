package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.CampaignDTO;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.manager.CampaignManager;
//import com.boot.jx.admin.service.CampaignService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.CampaignContactDoc;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.utils.ArgUtil;

@RestController
public class AdminCampaignController {
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private CampaignManager campaignManager;
	

	@RequestMapping(value = "/pub/campaign/create", method = { RequestMethod.POST })
	public CampaignContactDoc createorUpdateCampaign(@RequestBody  CampaignDTO campaignDto )
	{
		//List<CampaignContactDoc>list1=campaignManager.createorUpdateCampaigns(campaignDto);
		return (campaignManager.createorUpdateCampaigns(campaignDto));
	}
	
	@RequestMapping(value = "/pub/campaign/view", method = { RequestMethod.GET })
	public List<CampaignContactDoc> fetchCampaign(
			@RequestParam(value = "campaign_id", required = false) String campaignId,
			@RequestParam(required = false, defaultValue = "false") boolean isActive) {
		return (campaignManager.fetchCampaignList(campaignId, isActive));
	
	}
}
