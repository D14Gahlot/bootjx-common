package com.boot.jx.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.manager.AdminManager;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.EntityDtoUtil;

@Service
public class AdminService {

	@Autowired
	AdminManager adminManager;

	@Autowired
	AgentStore agentStore;

	public List<AgentDoc> saveAgent(AgentDoc reqDto) {
		List<AgentDoc> lstOfAgent = adminManager.saveAgent(reqDto);
		return lstOfAgent;
	}

	public List<AgentDoc> fetchAgent(String agentId) {
		List<AgentDoc> lstOfAgent = adminManager.fetchAgentList(agentId);
		return lstOfAgent;
	}

	public List<AgentResponseAdminDto> saveAgent(AgentResponseAdminDto reqDto) {
		AgentDoc reqEntity = EntityDtoUtil.dtoToEntity(reqDto, new AgentDoc());
		List<AgentDoc> lstOfAgent = adminManager.saveAgent(reqEntity);
		return fetchAgents(lstOfAgent);
	}

	public List<AgentResponseAdminDto> fetchAgents(String agentId) {
		List<AgentDoc> lstOfAgent = adminManager.fetchAgentList(agentId);
		return fetchAgents(lstOfAgent);
	}

	private List<AgentResponseAdminDto> fetchAgents(List<AgentDoc> lstOfAgent) {
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

	public List<DepartmentResponseAdminDto> saveDept(DepartmentResponseAdminDto dto) {
		DepartmentDoc reqEntity = EntityDtoUtil.dtoToEntity(dto, new DepartmentDoc());
		return new DepartmentResponseAdminDto().importFrom(adminManager.createAndUpdateDepartment(reqEntity));
	}

	public List<DepartmentResponseAdminDto> fetchDepartments(String deptId) {
		List<DepartmentDoc> lstDept = adminManager.fetchDept(deptId);
		return new DepartmentResponseAdminDto().importFrom(lstDept);
	}

	public List<AgentResponseAdminDto> updateAgentStatus(String agentId, String status) {
		return fetchAgents(adminManager.updateAgentStatus(agentId, status));
	}

	public List<AgentResponseAdminDto> updateAgentAdmin(String agentId) {
		return fetchAgents(adminManager.updateAgentAdmin(agentId));
	}

	public List<DepartmentDoc> createAndUpdateDepartment(DepartmentDoc deptReqDto) {
		List<DepartmentDoc> lstDept = adminManager.createAndUpdateDepartment(deptReqDto);
		return lstDept;
	}

	public List<DepartmentDoc> fetchDepartment(String deptId) {
		List<DepartmentDoc> lstDept = adminManager.fetchDept(deptId);
		return lstDept;
	}

	public List<DepartmentDoc> updateDepartment(Integer deptId, String status) {
		List<DepartmentDoc> lstDept = adminManager.updateDeptStatus(deptId, status);
		return lstDept;
	}

	public List<AgentResponseAdminDto> updateAgentDefault(String agentId) {
		agentStore.updateAgentDefault(agentId);
		return fetchAgents(agentStore.findAll());
	}

	public List<DepartmentResponseAdminDto> updateDepartmentDefault(String deptId) {
		agentStore.updateDepartmentDefault(deptId);
		return new DepartmentResponseAdminDto().importFrom(agentStore.findDepartmentAll());
	}

	/*
	 * public List<AgentResponseDto> saveAgent(AgentRequestDto reqDto) {
	 * List<AgentResponseDto> lstOfAgent =adminManager.saveAgent(reqDto); return
	 * lstOfAgent; }
	 */

	/*
	 * public List<AgentResponseDto> fetchAgent(Integer agentId) {
	 * List<AgentResponseDto> lstOfAgent =adminManager.fetchAgentList(agentId);
	 * return lstOfAgent; }
	 * 
	 * 
	 * public List<AgentResponseDto> updateAgentStatus(Integer agentId,String
	 * status) { List<AgentResponseDto> lstOfAgent
	 * =adminManager.updateAgentStatus(agentId,status); return lstOfAgent; }
	 */

	/*
	 * public List<DepartmentResponseDto>
	 * createAndUpdateDepartment(DepartmentRequestDto deptReqDto){
	 * List<DepartmentResponseDto> lstDept =
	 * adminManager.createAndUpdateDepartment(deptReqDto); return lstDept; }
	 * 
	 * public List<DepartmentResponseDto> fetchDepartment(Integer deptId){
	 * List<DepartmentResponseDto> lstDept = adminManager.fetchDept(deptId); return
	 * lstDept; }
	 * 
	 * public List<DepartmentResponseDto> updateDepartment(Integer deptId, String
	 * status){ List<DepartmentResponseDto> lstDept =
	 * adminManager.updateDeptStatus(deptId,status); return lstDept; }
	 */

}
