package com.boot.jx.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.manager.AdminManager;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.jx.tunnel.sys.SharedConfigManager;
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
	SharedConfigManager sharedConfigManager;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	public List<AgentResponseAdminDto> fetchAgents(String agentId) {
		List<AgentDoc> lstOfAgent = adminManager.fetchAgentList(agentId);
		return buildAgentDto(lstOfAgent);
	}

	public List<AgentResponseAdminDto> createOrUpdateAgent(AgentResponseAdminDto reqDto) {
		AgentDoc reqEntity = EntityDtoUtil.dtoToEntity(reqDto, new AgentDoc());
		List<AgentDoc> lstOfAgent = adminManager.createOrUpdateAgent(reqEntity);
		return buildAgentDto(lstOfAgent);
	}

	public List<DepartmentResponseAdminDto> createOrUpdateDept(DepartmentResponseAdminDto dto) {
		DepartmentDoc reqEntity = EntityDtoUtil.dtoToEntity(dto, new DepartmentDoc());
		return new DepartmentResponseAdminDto().importFrom(adminManager.createOrUpdateDepartment(reqEntity));
	}

	public List<DepartmentResponseAdminDto> fetchDepts(String deptId) {
		List<DepartmentDoc> lstDept = adminManager.fetchDept(deptId);
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
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);
		if (agent.isDefaultValue()) {
			doc.agent().defaultAgents().put(dept.getDept_code(), agent.getAgent_code());
		} else {
			doc.agent().defaultAgents().remove(dept.getDept_code());
		}
		mongoTemplate.save(doc);
		sharedConfigManager.clear();

		return buildAgentDto(agentStore.findAll());
	}

	public List<DepartmentResponseAdminDto> updateDepartmentDefault(String deptId) {
		agentStore.updateDepartmentDefault(deptId);
		DepartmentDoc dept = agentStore.findDepartmentById(deptId);
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);
		if (dept.isDefaultValue()) {
			doc.agent().setDefaultTeamCode(dept.getDept_code());
		} else {
			doc.agent().setDefaultTeamCode(null);
		}
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return new DepartmentResponseAdminDto().importFrom(agentStore.findDepartmentAll());
	}

	private List<AgentResponseAdminDto> buildAgentDto(List<AgentDoc> lstOfAgent) {
		List<AgentResponseAdminDto> agentList = new AgentResponseAdminDto().importFrom(lstOfAgent);
		for (AgentResponseAdminDto agentResponseDto : agentList) {
			agentResponseDto.setAgent_password(null);
			if (ArgUtil.is(agentResponseDto.getAgent_id())) {
				agentResponseDto.setDept(new DepartmentResponseAdminDto().importFrom(CollectionUtil
						.getOne(adminManager.fetchDept(ArgUtil.parseAsString(agentResponseDto.getDept_id())))));
			}
		}
		return agentList;
	}

}
