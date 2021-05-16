package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.service.AdminService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.utils.ArgUtil;

@RestController
public class AdminController {

	@Autowired
	AdminService adminService;

	// Agent
	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.GET })
	public ApiResponse<AgentResponseAdminDto, Object> fetchAgents(
			@RequestParam(value = "agent_id", required = false) String agent_id) {
		return ApiResponse.buildResults(adminService.fetchAgents(agent_id));
	}

	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.POST })
	public ApiResponse<AgentResponseAdminDto, Object> createAgent(@RequestBody AgentResponseAdminDto dto) {
		return ApiResponse.buildResults(adminService.saveAgent(dto))
				.message(ArgUtil.is(dto.getAgent_id()) ? "Agent Updated" : "Agent Created");
	}

	@RequestMapping(value = "/api/admins/agent", method = { RequestMethod.DELETE })
	public List<AgentResponseAdminDto> updateAgentStatus(@RequestParam(value = "agent_id", required = true) String agent_id,
			@RequestParam(value = "status", required = true) String status) {
		return adminService.updateAgentStatus(agent_id, status);
	}

	@RequestMapping(value = "/api/admins/agent/admin", method = { RequestMethod.POST })
	public List<AgentResponseAdminDto> updateAgentAdmin(@RequestParam(value = "agent_id", required = true) String agent_id) {
		return adminService.updateAgentAdmin(agent_id);
	}

	@RequestMapping(value = "/api/admins/agent/default", method = { RequestMethod.POST })
	public ApiResponse<AgentResponseAdminDto, Object> updateAgentDefault(
			@RequestParam(value = "agent_id", required = true) String agent_id) {
		return ApiResponse.buildResults(adminService.updateAgentDefault(agent_id));
	}

	// DepartMent
	@RequestMapping(value = "/api/admins/dept", method = { RequestMethod.GET })
	public ApiResponse<DepartmentResponseAdminDto, Object> fetchDepts(
			@RequestParam(value = "dept_id", required = false) String deptId) {
		return ApiResponse.buildResults(adminService.fetchDepartments(deptId));
	}

	@RequestMapping(value = "/api/admins/dept", method = { RequestMethod.POST })
	public ApiResponse<DepartmentResponseAdminDto, Object> fetchDepts(@RequestBody DepartmentResponseAdminDto dto) {
		return ApiResponse.buildResults(adminService.saveDept(dto))
				.message(ArgUtil.is(dto.getDept_id()) ? "Team Updated" : "Team Created");
	}

	@RequestMapping(value = "/api/admins/dept/default", method = { RequestMethod.POST })
	public ApiResponse<DepartmentResponseAdminDto, Object> updateDepartmentDefault(
			@RequestParam(value = "dept_id", required = true) String deptId) {
		return ApiResponse.buildResults(adminService.updateDepartmentDefault(deptId));
	}

	@RequestMapping(value = "/admin/create-update-agent", method = { RequestMethod.POST })
	public List<AgentDoc> createAgent(@RequestBody AgentDoc requestModel) {
		return adminService.saveAgent(requestModel);
	}

	@RequestMapping(value = "/admin/fetch-agent", method = { RequestMethod.GET })
	public List<AgentDoc> fetchAgentList(@RequestParam(value = "agent_id", required = false) String agent_id) {
		return adminService.fetchAgent(agent_id);
	}

	@RequestMapping(value = "/admin/create-update-dept", method = { RequestMethod.POST })
	public List<DepartmentDoc> createDepartment(@RequestBody DepartmentDoc requestModel) {
		return adminService.createAndUpdateDepartment(requestModel);
	}

	@RequestMapping(value = "/admin/fetch-dept", method = { RequestMethod.GET })
	public List<DepartmentDoc> fetchDepartment(@RequestParam(value = "dept_id", required = false) String deptId) {
		return adminService.fetchDepartment(deptId);
	}

	@RequestMapping(value = "/admin/delete-dept", method = { RequestMethod.POST })
	public List<DepartmentDoc> updateDepartment(@RequestParam(value = "dept_id", required = true) Integer dept_id,
			@RequestParam(value = "status", required = true) String status) {
		return adminService.updateDepartment(dept_id, status);
	}

}
