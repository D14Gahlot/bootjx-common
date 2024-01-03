package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.CampaignDTO;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.postman.doc.CampaignContactDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.utils.ArgUtil;

@Component
public class CampaignManager {
	@Autowired
	MongoTemplate mongoTemplate;
   

	
	
	public List<CampaignContactDoc> fetchCampaignList(String campaignId, boolean IsActive) {
		List<CampaignContactDoc> campaignList = new ArrayList<CampaignContactDoc>();
		if (ArgUtil.is(campaignId)) {
			CampaignContactDoc campaign = mongoTemplate.findOne(new Query(Criteria.where("_id").is(campaignId)), CampaignContactDoc.class);
			campaignList.add(campaign);
		} else {
			campaignList = mongoTemplate.findAll(CampaignContactDoc.class);
		}
		return campaignList;
	}
	
	public CampaignContactDoc createorUpdateCampaigns(CampaignDTO camp) {
		if (ArgUtil.isEmpty(camp)) {
			ApiResponseUtil.throwException("Input Required");
		}
			CampaignContactDoc campDoc=new CampaignContactDoc();
			String  contactType =camp.getChatsessionDto().contact().getContactType();
			
			List<Contactable> lists=new ArrayList<Contactable>();
			lists.add(camp.getChatsessionDto().contact());
			List<String>camplist=new ArrayList<String>();
			for (Contactable con:lists)
			{
				String phone=con.getPhone();
			     camplist.add(phone);
				}
		if ((ArgUtil.isEmpty(camp.getCampaignId())))
				{
			campDoc.setIsActive(true);
			campDoc.setTimetsamp(System.currentTimeMillis());
			//campDoc.getPhone().add(camp.getChatsessionDto().getPhone());
			campDoc.setCampaign_name(camp.getCampaignName());
			campDoc.setCampaign_id(null);
			campDoc.setCreatedBy(camp.getChatsessionDto().contact().getName());
			// campDoc.setPhone(phone1);
			// campDoc.setPhone(phone1);
             

		} else {
			campDoc.setCampaign_id(camp.getCampaignId());
			campDoc.setCampaign_name(camp.getCampaignName());
			campDoc.setCreatedBy(camp.getChatsessionDto().contact().getName());
			campDoc.setIsActive(true);
			//campDoc.setPhone(phone1);
			campDoc.setTimetsamp(System.currentTimeMillis());
			//campDoc.setCreatedBy(camp.getChatsessionDto().getName());;
		}

		if (ArgUtil.isEmpty(camp.getCampaignName())) {
			ApiResponseUtil.throwException("All Inputs Required");
		}

		List<String>phone1=camplist.stream().distinct().collect(Collectors.toList());
        campDoc.setPhone(phone1);
		mongoTemplate.save(campDoc);
		
	
		
			return campDoc;
			}
			
		
	}		
	//public List<CampaignContactDoc> fetchCampaignList(String campaignId) {
//	return fetchCampaignList(campaignId, true);

	

