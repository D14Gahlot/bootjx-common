package com.boot.jx.admin.service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;

import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.manager.AdminManager;
import com.boot.jx.admin.manager.GroupManager;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.ConfigManagerImpl;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.doc.GroupDoc;
import com.boot.jx.common.dto.GroupReqDto;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.EntityDtoUtil;

@Service
public class AdminService {

	@Autowired
	AdminManager adminManager;

	@Autowired
	AgentStore agentStore;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	ConfigManagerImpl configManager;

	@Autowired
	private EmpAuthService empAuthService;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;
	
	@Autowired
	GroupManager groupMgr;

	public List<AgentResponseAdminDto> fetchAgents(String agentId, boolean includeInActive) {
		List<AgentDoc> lstOfAgent = adminManager.fetchAgentList(agentId, includeInActive);
		return buildAgentDto(lstOfAgent);
	}

	public List<AgentResponseAdminDto> createOrUpdateAgent(AgentResponseAdminDto reqDto) {
		AgentDoc reqEntity = EntityDtoUtil.dtoToEntity(reqDto, new AgentDoc());
		List<AgentDoc> lstOfAgent = adminManager.createOrUpdateAgent(reqEntity);
		try {
			if (!ArgUtil.is(reqDto.getId()) && ArgUtil.is(reqEntity.getId())) {
				empAuthService.resetPassword(reqEntity.getAgent_code(), false);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return buildAgentDto(lstOfAgent);
	}

	public List<DepartmentResponseAdminDto> createOrUpdateDept(DepartmentResponseAdminDto dto) {
		DepartmentDoc reqEntity = EntityDtoUtil.dtoToEntity(dto, new DepartmentDoc());
		return new DepartmentResponseAdminDto().importFrom(adminManager.createOrUpdateDepartment(reqEntity));
	}

	public List<DepartmentResponseAdminDto> fetchDepts(String deptId, boolean includeInActive) {
		List<DepartmentDoc> lstDept = adminManager.fetchDept(deptId, includeInActive);
		return new DepartmentResponseAdminDto().importFrom(lstDept);
	}

	public List<AgentResponseAdminDto> updateAgentActive(String agentId, String status) {
		agentStore.updateAgentActive(agentId, status);
		documentUpdateListner.onAgentUpdate(agentId);
		return buildAgentDto(agentStore.findAll());
	}

	public List<AgentResponseAdminDto> updateAgentAdmin(String agentId) {
		return buildAgentDto(adminManager.updateAgentAdmin(agentId));
	}

	public List<DepartmentDoc> createAndUpdateDepartment(DepartmentDoc deptReqDto) {
		List<DepartmentDoc> lstDept = adminManager.createOrUpdateDepartment(deptReqDto);
		return lstDept;
	}

	public List<DepartmentDoc> updateDepartment(Integer deptId, String status) {
		List<DepartmentDoc> lstDept = adminManager.updateDeptStatus(deptId, status);
		return lstDept;
	}

	public List<AgentResponseAdminDto> updateAgentDefault(String agentId) {
		agentStore.updateAgentDefault(agentId);

		AgentDoc agent = agentStore.findById(agentId);
		DepartmentDoc dept = agentStore.findDepartmentById(agent.getDept_id());
		PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
		if (agent.isDefaultValue()) {
			doc.agent().defaultAgents().put(dept.getDept_code(), agent.getAgent_code());
		} else {
			doc.agent().defaultAgents().remove(dept.getDept_code());
		}
		mongoTemplate.save(doc);
		configManager.refresh();

		return buildAgentDto(agentStore.findAll());
	}

	public List<DepartmentResponseAdminDto> updateDepartmentDefault(String deptId) {
		agentStore.updateDepartmentDefault(deptId);
		DepartmentDoc dept = agentStore.findDepartmentById(deptId);
		PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
		if (dept.isDefaultValue()) {
			doc.agent().setDefaultTeamCode(dept.getDept_code());
		} else {
			doc.agent().setDefaultTeamCode(null);
		}
		mongoTemplate.save(doc);
		configManager.refresh();
		return new DepartmentResponseAdminDto().importFrom(agentStore.findDepartmentAll());
	}

	private List<AgentResponseAdminDto> buildAgentDto(List<AgentDoc> lstOfAgent) {
		List<AgentResponseAdminDto> agentList = new AgentResponseAdminDto().importFrom(lstOfAgent);
		List<DepartmentDoc> depts = adminManager.fetchDept(null, true);
		Map<String, DepartmentDoc> deptMap = new HashMap<String, DepartmentDoc>();
		for (DepartmentDoc departmentDoc : depts) {
			deptMap.put(departmentDoc.getDept_id(), departmentDoc);
		}
		for (AgentResponseAdminDto agentResponseDto : agentList) {
			agentResponseDto.setAgent_password(null);
			if (ArgUtil.is(agentResponseDto.getId())) {
				agentResponseDto.setDept(
						new DepartmentResponseAdminDto().importFrom(deptMap.get(agentResponseDto.getDept_id())));
			}
		}
		return agentList;
	}

	public List<AgentResponseAdminDto> resetPassByAgentId(String agentId) throws NoSuchAlgorithmException {
		AgentDoc agent = agentStore.findById(agentId);
		empAuthService.resetPassword(agent.getAgent_code(), agent.isAdmin());
		return buildAgentDto(CollectionUtil.asList(agent));
	}
	
	public List<GroupReqDto>  createAndUpdateGroup(GroupReqDto req) {
		List<GroupReqDto> reqDto = groupMgr.createAndUpdateGroup(req);
		return reqDto;
	}
	
	public List<GroupReqDto>  fetchGroups(String groupId) {
		List<GroupReqDto> reqDto = groupMgr.fetchGroups(groupId);
		return reqDto;
	}
	
	public void checkDupGroupName(GroupReqDto req) {
		GroupDoc groupDoc = groupMgr.findGroupByName(req.getGroupName());
		if(ArgUtil.is(groupDoc)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidNameDuplicate")
					.description("Group name already exists"));
			
		}
	}

}
