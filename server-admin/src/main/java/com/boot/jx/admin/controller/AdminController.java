package com.boot.jx.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.admin.dto.DepartmentResponseDto;
import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.model.Department;
import com.boot.jx.admin.service.AdminService;
import com.boot.jx.api.ApiResponse;

@RestController
public class AdminController  {
	
	@Autowired
	AdminService adminService;
	
	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.GET })
	public ApiResponse<AgentResponseDto, Object> fetchAgents(
			@RequestParam(value = "agent_id", required = false) Integer agent_id) {
		return ApiResponse.buildResults(adminService.fetchAgents(agent_id));
	}

	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.POST })
	public ApiResponse<AgentResponseDto, Object> createAgent(@RequestBody AgentResponseDto dto) {
		return ApiResponse.buildResults(adminService.saveAgent(dto));
	}

	@RequestMapping(value = "/api/admins/dept", method = { RequestMethod.GET })
	public ApiResponse<DepartmentResponseDto, Object> fetchDepts(
			@RequestParam(value = "dept_id", required = false) Integer deptId) {
		return ApiResponse.buildResults(adminService.fetchDepartments(deptId));
	}

	@RequestMapping(value = "/api/admins/dept", method = { RequestMethod.POST })
	public ApiResponse<DepartmentResponseDto, Object> fetchDepts(@RequestBody DepartmentResponseDto dto) {
		return ApiResponse.buildResults(adminService.saveDept(dto));
	}
	
	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.DELETE })
	public List<AgentResponseDto> updateAgentStatus(@RequestParam(value = "agent_id", required = true) Integer agent_id,
			@RequestParam(value = "status", required = true) String status) {
		return adminService.updateAgentStatus(agent_id, status);
	}
	
	
	@RequestMapping(value = "/admin/create-update-agent", method = { RequestMethod.POST})
	public List<Agent> createAgent(@RequestBody  Agent requestModel) {
		return adminService.saveAgent(requestModel);
	}
	
	@RequestMapping(value = "/admin/fetch-agent", method = { RequestMethod.GET})
	public  List<Agent> fetchAgentList(@RequestParam(value = "agent_id", required = false) Integer agent_id) {
		return adminService.fetchAgent(agent_id);
	}
	
	@RequestMapping(value = "/admin/create-update-dept", method = { RequestMethod.POST})
	public List<Department> createDepartment(@RequestBody  Department requestModel) {
		return adminService.createAndUpdateDepartment(requestModel);
	}
	
	@RequestMapping(value = "/admin/fetch-dept", method = { RequestMethod.GET})
	public  List<Department> fetchDepartment(@RequestParam(value = "dept_id", required = false) Integer deptId) {
		return adminService.fetchDepartment(deptId);
	}
	
	@RequestMapping(value = "/admin/delete-dept", method = { RequestMethod.POST})
	public  List<Department> updateDepartment(@RequestParam(value="dept_id" ,required = true) Integer dept_id,@RequestParam(value="status" ,required = true) String status) {
		return adminService.updateDepartment(dept_id,status);
	}
	
	
	
/*	@RequestMapping(value = "/admin/create-agent", method = { RequestMethod.POST})
	public List<AgentResponseDto> createAgent(@RequestBody  AgentRequestDto requestModel) {
		return adminService.saveAgent(requestModel);
	}
	
	@RequestMapping(value = "/admin/fetch-agent", method = { RequestMethod.GET})
	public  List<AgentResponseDto> fetchAgentList(@RequestParam(value = "agent_id", required = false) Integer agent_id) {
		return adminService.fetchAgent(agent_id);
	}
	
	@RequestMapping(value = "/admin/delete-agent", method = { RequestMethod.POST})
	public  List<AgentResponseDto> updateAgentStatus(@RequestParam(value="agent_id" ,required = true) Integer agent_id,@RequestParam(value="status" ,required = true) String status) {
		return adminService.updateAgentStatus(agent_id,status);
	}
	
	@RequestMapping(value = "/admin/create-dept", method = { RequestMethod.POST})
	public List<DepartmentResponseDto> createDepartment(@RequestBody  DepartmentRequestDto requestModel) {
		return adminService.createAndUpdateDepartment(requestModel);
	}
	
	@RequestMapping(value = "/admin/fetch-dept", method = { RequestMethod.GET})
	public  List<DepartmentResponseDto> fetchDepartment(@RequestParam(value = "dept_id", required = false) Integer deptId) {
		return adminService.fetchDepartment(deptId);
	}
	
	@RequestMapping(value = "/admin/delete-dept", method = { RequestMethod.POST})
	public  List<DepartmentResponseDto> updateDepartment(@RequestParam(value="dept_id" ,required = true) Integer dept_id,@RequestParam(value="status" ,required = true) String status) {
		return adminService.updateDepartment(dept_id,status);
	}
	
*/
}
