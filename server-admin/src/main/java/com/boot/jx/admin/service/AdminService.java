package com.boot.jx.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.admin.dto.DepartmentResponseDto;
import com.boot.jx.admin.manager.AdminManager;
import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.model.Department;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.EntityDtoUtil;

@Service
public class AdminService {

	@Autowired
	AdminManager adminManager;

	public List<Agent> saveAgent(Agent reqDto) {
		List<Agent> lstOfAgent = adminManager.saveAgent(reqDto);
		return lstOfAgent;
	}

	public List<Agent> fetchAgent(Integer agentId) {
		List<Agent> lstOfAgent = adminManager.fetchAgentList(agentId);
		return lstOfAgent;
	}

	public List<AgentResponseDto> saveAgent(AgentResponseDto reqDto) {
		Agent reqEntity = EntityDtoUtil.dtoToEntity(reqDto, new Agent());
		List<Agent> lstOfAgent = adminManager.saveAgent(reqEntity);
		return fetchAgents(lstOfAgent);
	}

	public List<AgentResponseDto> fetchAgents(Integer agentId) {
		List<Agent> lstOfAgent = adminManager.fetchAgentList(agentId);
		return fetchAgents(lstOfAgent);
	}

	private List<AgentResponseDto> fetchAgents(List<Agent> lstOfAgent) {
		List<AgentResponseDto> agentList = new AgentResponseDto().importFrom(lstOfAgent);
		for (AgentResponseDto agentResponseDto : agentList) {
			if (ArgUtil.is(agentResponseDto.getAgent_id())) {
				agentResponseDto.setDept(new DepartmentResponseDto()
						.importFrom(CollectionUtil.getOne(adminManager.fetchDept(agentResponseDto.getAgent_id()))));
			}
		}
		return agentList;
	}

	public List<DepartmentResponseDto> saveDept(DepartmentResponseDto dto) {
		Department reqEntity = EntityDtoUtil.dtoToEntity(dto, new Department());
		return new DepartmentResponseDto().importFrom(adminManager.createAndUpdateDepartment(reqEntity));
	}

	public List<DepartmentResponseDto> fetchDepartments(Integer deptId) {
		List<Department> lstDept = adminManager.fetchDept(deptId);
		return new DepartmentResponseDto().importFrom(lstDept);
	}

	public List<Agent> updateAgentStatus(Integer agentId, String status) {
		List<Agent> lstOfAgent = adminManager.updateAgentStatus(agentId, status);
		return lstOfAgent;
	}

	public List<Department> createAndUpdateDepartment(Department deptReqDto) {
		List<Department> lstDept = adminManager.createAndUpdateDepartment(deptReqDto);
		return lstDept;
	}

	public List<Department> fetchDepartment(Integer deptId) {
		List<Department> lstDept = adminManager.fetchDept(deptId);
		return lstDept;
	}

	public List<Department> updateDepartment(Integer deptId, String status) {
		List<Department> lstDept = adminManager.updateDeptStatus(deptId, status);
		return lstDept;
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
