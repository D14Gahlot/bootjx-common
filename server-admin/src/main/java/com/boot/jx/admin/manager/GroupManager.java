package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.GroupDoc;
import com.boot.jx.common.dto.GroupReqDto;
import com.boot.jx.common.dto.GroupSessionDto;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

@Component
public class GroupManager {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;
	@Autowired
	AuditDetailProvider auditDetailProvider;

	public List<GroupReqDto> createAndUpdateGroup(GroupReqDto reqDto){
		
		GroupDoc grpDoc = new GroupDoc();
			if(ArgUtil.is(reqDto.getGroupId())) {
				 grpDoc = commonMongoTemplate.findByIdString(reqDto.getGroupId(), GroupDoc.class);
				 if(ArgUtil.is(grpDoc)) {
					grpDoc.setGroupId(grpDoc.getGroupId());
					grpDoc.setGroupName(reqDto.getGroupName()==null?grpDoc.getGroupName():reqDto.getGroupName());
					if(ArgUtil.is(reqDto.getSessions())) {
						grpDoc.setSessions(getUniqueList(reqDto.getSessions()));
					}else {
						grpDoc.setSessions(grpDoc.getSessions());
					}
					grpDoc.setActive(reqDto.isActive());
					grpDoc.setModified_by(auditDetailProvider.getAuditUser());
					grpDoc.setModifiedStamp(System.currentTimeMillis());
					mongoTemplate.save(grpDoc);
				 }
			}else {
				grpDoc.setGroupName(reqDto.getGroupName());
				grpDoc.setSessions(getUniqueList(reqDto.getSessions()));
				grpDoc.setActive(reqDto.isActive());
				grpDoc.setCreatedBy(auditDetailProvider.getAuditUser());
				grpDoc.setCreatedStamp(System.currentTimeMillis());
				mongoTemplate.save(grpDoc);
			}
		
		return fetchGroups(grpDoc.getGroupId());
	}

	public List<GroupReqDto> fetchGroups(String groupId) {
		List<GroupReqDto> dtoLst = new ArrayList<>();
		GroupDoc grpDoc = null;
		if (ArgUtil.is(groupId)) {
			grpDoc = commonMongoTemplate.findByIdString(groupId, GroupDoc.class);
			if (ArgUtil.is(grpDoc)) {
				GroupReqDto dto = EntityDtoUtil.entityToDto(grpDoc, new GroupReqDto());
				dtoLst.add(dto);
			}
		} else {
			List<GroupDoc> lstGropDocs = mongoTemplate.findAll(GroupDoc.class);
			for (GroupDoc doc : lstGropDocs) {
				GroupReqDto dto = EntityDtoUtil.entityToDto(doc, new GroupReqDto());
				dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	public GroupDoc findGroupByName(String groupName) {
	GroupDoc groupDoc = mongoTemplate.findOne(new Query(Criteria.where("groupName").is(groupName)),GroupDoc.class);
	return groupDoc;
	}
	
	private List<GroupSessionDto> getUniqueList(List<GroupSessionDto> lstDtos){
		 	Set<String> uniquePhones = new HashSet<>();
	        List<GroupSessionDto> uniqueList = new ArrayList<>();

	        for (GroupSessionDto dto : lstDtos) {
	            if (uniquePhones.add(dto.getPhone())) {
	                uniqueList.add(dto);
	            }
	        }
		 return uniqueList;
	}
	
		public List<GroupReqDto> deleteGroups(GroupReqDto req) {
		GroupDoc grpDoc = new GroupDoc();
		if(ArgUtil.is(req.getGroupId()) && ArgUtil.is(req.getSessions())) {
			 grpDoc = commonMongoTemplate.findByIdString(req.getGroupId(), GroupDoc.class);
			 List<GroupSessionDto> uniLstFromDb=grpDoc.getSessions();
			 List<GroupSessionDto> uniLstReqDtos= getUniqueList(req.getSessions());
			 if(ArgUtil.is(uniLstReqDtos) && ArgUtil.is(uniLstFromDb)) {
				 uniLstFromDb.removeIf(myObject ->
				 uniLstReqDtos.stream().anyMatch(reqObject ->
	                        myObject.getContactType().equals(reqObject.getContactType()) &&
	                        myObject.getPhone().equals(reqObject.getPhone())
	                        // Add other conditions as needed
	                )
				   );
		
			 }
			 
			 CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
				builder.whereIdSafe(req.getGroupId());
				builder.set("sessions", uniLstFromDb);
				builder.set("modifiedStamp", System.currentTimeMillis());
				builder.set("modified_by", auditDetailProvider.getAuditUser());
				mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,"GROUPS");
			 
		}
		return fetchGroups(req.getGroupId());
	}

}
