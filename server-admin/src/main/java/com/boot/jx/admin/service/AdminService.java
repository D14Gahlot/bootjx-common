package com.boot.jx.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.dto.AgentRequestDto;
import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.admin.dto.DepartmentRequestDto;
import com.boot.jx.admin.dto.DepartmentResponseDto;
import com.boot.jx.admin.manager.AdminManager;
import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.model.Department;

@Service
public class AdminService {
	
	@Autowired
	AdminManager adminManager;
	
	
	
	public List<Agent> saveAgent(Agent reqDto) {
		List<Agent> lstOfAgent =adminManager.saveAgent(reqDto);
		return lstOfAgent;
	}
	
	public List<Agent> fetchAgent(Integer agentId) {
		List<Agent> lstOfAgent =adminManager.fetchAgentList(agentId);
		return lstOfAgent;
	}
	public List<Agent> updateAgentStatus(Integer agentId,String status) {
		List<Agent> lstOfAgent =adminManager.updateAgentStatus(agentId,status);
		return lstOfAgent;
	}
	
	public List<Department> createAndUpdateDepartment(Department deptReqDto){
		List<Department> lstDept = adminManager.createAndUpdateDepartment(deptReqDto);
		return lstDept;
	}
	
	public List<Department> fetchDepartment(Integer deptId){
		List<Department> lstDept = adminManager.fetchDept(deptId);
		return lstDept;
	}
	
	public List<Department> updateDepartment(Integer deptId, String status){
		List<Department> lstDept = adminManager.updateDeptStatus(deptId,status);
		return lstDept;
	}
	

	

	/*
	public List<AgentResponseDto> saveAgent(AgentRequestDto reqDto) {
		List<AgentResponseDto> lstOfAgent =adminManager.saveAgent(reqDto);
		return lstOfAgent;
	}*/
	
	

	
	/*public List<AgentResponseDto> fetchAgent(Integer agentId) {
		List<AgentResponseDto> lstOfAgent =adminManager.fetchAgentList(agentId);
		return lstOfAgent;
	}
	
	
	public List<AgentResponseDto> updateAgentStatus(Integer agentId,String status) {
		List<AgentResponseDto> lstOfAgent =adminManager.updateAgentStatus(agentId,status);
		return lstOfAgent;
	}*/
	
	
	/*public List<DepartmentResponseDto> createAndUpdateDepartment(DepartmentRequestDto deptReqDto){
		List<DepartmentResponseDto> lstDept = adminManager.createAndUpdateDepartment(deptReqDto);
		return lstDept;
	}
	
	public List<DepartmentResponseDto> fetchDepartment(Integer deptId){
		List<DepartmentResponseDto> lstDept = adminManager.fetchDept(deptId);
		return lstDept;
	}
	
	public List<DepartmentResponseDto> updateDepartment(Integer deptId, String status){
		List<DepartmentResponseDto> lstDept = adminManager.updateDeptStatus(deptId,status);
		return lstDept;
	}
	*/

}
