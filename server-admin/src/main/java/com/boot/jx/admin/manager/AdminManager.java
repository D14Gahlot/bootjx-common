package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.PostManUrls;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class AdminManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminManager.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private AgentStore agentStore;

    @Autowired
    AuditDetailProvider auditDetailProvider;

    @Autowired
    CommonMongoTemplate commonMongoTemplate;

    public List<AgentDoc> createOrUpdateAgent(AgentDoc agent) {

	if (ArgUtil.isEmpty(agent)) {
	    ApiResponseUtil.throwException("Input Required");
	}

	if ((ArgUtil.isEmpty(agent.getAgent_id()) || agent.getAgent_id().equals("0"))) {
	    agent.setIsactive("Y");
	    agent.setModified_date(null);
	    agent.setAgent_id(null);
	    agent.setCreate_by(auditDetailProvider.getAuditUser());
	    agent.setCreatedStamp(System.currentTimeMillis());
	} else {
	    agent.setModifiedStamp(System.currentTimeMillis());
	    agent.setModified_by(auditDetailProvider.getAuditUser());
	}
	if (ArgUtil.isEmpty(agent.getAgent_code()) || ArgUtil.isEmpty(agent.getDept_id())
		|| ArgUtil.isEmpty(agent.getAgent_name())) {
	    ApiResponseUtil.throwException("All Inputs Required");
	}

	AgentDoc oldAgent = commonMongoTemplate.findByIdString(agent.getAgent_id(), AgentDoc.class);
	if (ArgUtil.is(oldAgent)) {
	    if (!oldAgent.getAgent_code().equals(agent.getAgent_code()))
		ApiResponseUtil.throwException("Agent Code cannot be Modified");

	    if (!ArgUtil.is(agent.getAgent_password())) {
		agent.setAgent_password(oldAgent.getAgent_password());
	    }

	    agent.oldVersion(oldAgent);
	}
	agentStore.save(agent);
	return fetchAgentList(null);
    }

    public List<AgentDoc> fetchAgentList(String agentId) {
	List<AgentDoc> agentList = new ArrayList<AgentDoc>();
	if (ArgUtil.is(agentId)) {
	    AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
	    agentList.add(agent);
	} else {
	    agentList = commonMongoTemplate.findAll(AgentDoc.class);
	}
	return agentList;
    }

    public List<AgentDoc> updateAgentActive(String agentId, String status) {
	List<AgentDoc> agentList = new ArrayList<AgentDoc>();
	if (ArgUtil.is(agentId) && ArgUtil.is(status)) {
	    AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
	    if (ArgUtil.is(agent)) {
		agent.setIsactive(status);
		mongoTemplate.save(agent);
	    }
	}
	agentList = fetchAgentList(null);
	return agentList;
    }

    public List<AgentDoc> updateAgentAdmin(String agentId) {
	List<AgentDoc> agentList = new ArrayList<AgentDoc>();
	if (ArgUtil.is(agentId)) {
	    AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
	    if (ArgUtil.is(agent)) {
		agent.setAdmin(!agent.isAdmin());
		mongoTemplate.save(agent);
	    }
	}
	agentList = fetchAgentList(null);
	return agentList;
    }

    public List<DepartmentDoc> createOrUpdateDepartment(DepartmentDoc dept) {
	if (ArgUtil.isEmpty(dept)) {
	    ApiResponseUtil.throwException("Input Required");
	}

	if (dept != null && (ArgUtil.isEmpty(dept.getDept_id()) || dept.getDept_id().equals("0"))) {
	    dept.setIsactive("Y");
	    dept.setModified_date(null);
	    dept.setDept_id(null);
	    dept.setCreatedStamp(System.currentTimeMillis());
	    dept.setCreate_by(auditDetailProvider.getAuditUser());

	} else {
	    dept.setDept_id(dept.getDept_id());
	    dept.setModified_date(new Date());
	    dept.setModifiedStamp(System.currentTimeMillis());
	    dept.setModified_by(auditDetailProvider.getAuditUser());
	}

	if (!ArgUtil.is(dept.getDept_email())) {
	    dept.setDept_email(dept.getDept_name() + "@" + AppContextUtil.getTenant());
	}

	if (ArgUtil.isEmpty(dept.getDept_name()) || ArgUtil.isEmpty(dept.getDept_code())) {
	    ApiResponseUtil.throwException("All Inputs Required");
	}

	DepartmentDoc oldDept = commonMongoTemplate.findByIdString(dept.getDept_id(), DepartmentDoc.class);
	if (ArgUtil.is(oldDept)) {

	    if (!oldDept.getDept_code().equals(dept.getDept_code()))
		ApiResponseUtil.throwException("Team Code cannot be Modified");

	    dept.oldVersion(oldDept);
	}
	mongoTemplate.save(dept);
	return fetchDept(null);
    }

    public List<DepartmentDoc> fetchDept(String deptId) {
	List<DepartmentDoc> lstDept = new ArrayList<DepartmentDoc>();
	if (ArgUtil.is(deptId)) {
	    DepartmentDoc dept = mongoTemplate.findOne(new Query(Criteria.where("_id").is(deptId)),
		    DepartmentDoc.class);
	    lstDept.add(dept);
	} else {
	    lstDept = mongoTemplate.findAll(DepartmentDoc.class);
	}
	return lstDept;
    }

    public List<DepartmentDoc> updateDeptStatus(Integer deptId, String status) {
	List<DepartmentDoc> lstDept = new ArrayList<DepartmentDoc>();
	if (ArgUtil.is(deptId) && ArgUtil.is(status)) {
	    DepartmentDoc dept = mongoTemplate.findOne(new Query(Criteria.where("_id").is(deptId)),
		    DepartmentDoc.class);
	    if (ArgUtil.is(dept)) {
		dept.setIsactive(status);
		mongoTemplate.save(dept);
	    }
	}
	lstDept = fetchDept(null);
	return lstDept;
    }

    public List<AgentResponseAdminDto> fetchAgent() {
	List<AgentDoc> agent = mongoTemplate.findAll(AgentDoc.class);
	List<AgentResponseAdminDto> agentList = getAgents(agent);
	return agentList;
    }

    public List<AgentResponseAdminDto> getAgents(List<AgentDoc> agentList) {
	List<AgentResponseAdminDto> agentLst = new ArrayList<AgentResponseAdminDto>();
	try {
	    for (AgentDoc agent : agentList) {
		AgentResponseAdminDto resdto = copyAgent(agent);
		agentLst.add(resdto);
	    }
	} catch (Exception e) {
	    LOGGER.debug("bene list display", e);
	}
	return agentLst;
    }

    public List<DepartmentResponseAdminDto> getDepartmets(List<DepartmentDoc> lstDept) {
	List<DepartmentResponseAdminDto> deptLst = new ArrayList<DepartmentResponseAdminDto>();
	try {
	    for (DepartmentDoc dept : lstDept) {
		DepartmentResponseAdminDto dto = copyDept(dept);
		deptLst.add(dto);
	    }
	} catch (Exception e) {
	    LOGGER.debug("dept list display", e);
	}
	return deptLst;
    }

    public AgentResponseAdminDto copyAgent(AgentDoc agent) {
	AgentResponseAdminDto dto = new AgentResponseAdminDto();
	try {
	    BeanUtils.copyProperties(dto, agent);
	} catch (Exception e) {
	    LOGGER.debug("agent list display", e);
	}

	return dto;
    }

    public DepartmentResponseAdminDto copyDept(DepartmentDoc dept) {
	DepartmentResponseAdminDto dto = new DepartmentResponseAdminDto();
	try {
	    BeanUtils.copyProperties(dto, dept);
	} catch (Exception e) {
	    LOGGER.debug("dept list display", e);

	}
	return dto;
    }

}
