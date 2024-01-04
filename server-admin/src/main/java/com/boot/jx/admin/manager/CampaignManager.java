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
import com.boot.jx.admin.dto.GroupReqDto;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.doc.GroupContactDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.utils.ArgUtil;

@Component
public class CampaignManager {
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	AuditDetailProvider auditDetailProvider;
   

	
	
	public List<GroupContactDoc> fetchGroupLists(String campaignId, boolean IsActive) {
		List<GroupContactDoc> campaignList = new ArrayList<GroupContactDoc>();
		if (ArgUtil.is(campaignId)) {
			GroupContactDoc campaign = mongoTemplate.findOne(new Query(Criteria.where("_id").is(campaignId)), GroupContactDoc.class);
			campaignList.add(campaign);
		} else {
			campaignList = mongoTemplate.findAll(GroupContactDoc.class);
		}
		return campaignList;
	}
	
	public GroupContactDoc createorUpdateGroup(GroupReqDto group) {
		if (ArgUtil.isEmpty(group)) {
			ApiResponseUtil.throwException("Input Required");
		}
			GroupContactDoc groupDoc=new GroupContactDoc();
			
			//String  contactType =camp.getChatsessionDto().contact().getContactType();
			
			List<Object> lists=new ArrayList<Object>();
			lists.addAll(group.getSession());
			List<String>camplist=new ArrayList<String>();
			
			for (Object it:lists)
			{ 
			;
			     camplist.add(phone);
				}
		if ((ArgUtil.isEmpty(group.getGroupId())))
				{
			groupDoc.setIsActive(true);
			groupDoc.setCreatedTimestamp(System.currentTimeMillis());
			groupDoc.setGroupName(group.getGroupName());
			groupDoc.setGroupId(null);
			groupDoc.setCreatedBy(auditDetailProvider.getAuditUser());
		
             

		} else {
			campDoc.setCampaign_id(group.getCampaignId());
			campDoc.setCampaign_name(group.getCampaignName());
			campDoc.setCreatedBy(auditDetailProvider.getAuditUser());
			campDoc.setIsActive(true);
			//campDoc.setPhone(phone1);
			campDoc.setTimetsamp(System.currentTimeMillis());
			//campDoc.setCreatedBy(camp.getChatsessionDto().getName());;
		}

		if (ArgUtil.isEmpty(group.getCampaignName())) {
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

	

