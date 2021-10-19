package com.boot.jx.admin.api;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.json.NamedEntityDeserializer.NamedMapModel;
import com.boot.jx.admin.dto.AgentResponseAdminDto;
import com.boot.jx.admin.dto.DepartmentResponseAdminDto;
import com.boot.jx.admin.service.AdminService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.ContactStore;
import com.boot.utils.ArgUtil;

@RestController
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ContactStore contactStore;

    // Agent
    @RequestMapping(value = "/api/admins/agent", method = { RequestMethod.GET })
    public ApiResponse<AgentResponseAdminDto, Object> fetchAgents(
	    @RequestParam(value = "agent_id", required = false) String agent_id) {
	return ApiResponse.buildResults(adminService.fetchAgents(agent_id));
    }

    @RequestMapping(value = "/api/admins/agent", method = { RequestMethod.POST })
    public ApiResponse<AgentResponseAdminDto, Object> createOrUpdateAgent(@RequestBody AgentResponseAdminDto dto) {
	return ApiResponse.buildResults(adminService.createOrUpdateAgent(dto))
		.message(ArgUtil.is(dto.getAgent_id()) ? "Agent Updated" : "Agent Created");
    }

    @RequestMapping(value = "/api/admins/agent", method = { RequestMethod.DELETE })
    public List<AgentResponseAdminDto> updateAgentActive(
	    @RequestParam(value = "agent_id", required = true) String agent_id,
	    @RequestParam(value = "status", required = true) String status) {
	return adminService.updateAgentActive(agent_id, status);
    }

    @RequestMapping(value = "/api/admins/agent/admin", method = { RequestMethod.POST })
    public List<AgentResponseAdminDto> updateAgentAdmin(
	    @RequestParam(value = "agent_id", required = true) String agent_id) {
	return adminService.updateAgentAdmin(agent_id);
    }

    @RequestMapping(value = "/api/admins/agent/default", method = { RequestMethod.POST })
    public ApiResponse<AgentResponseAdminDto, Object> updateAgentDefault(
	    @RequestParam(value = "agent_id", required = true) String agent_id) {
	return ApiResponse.buildResults(adminService.updateAgentDefault(agent_id));
    }

    @RequestMapping(value = "/api/admins/agent/reset", method = { RequestMethod.POST })
    public ApiResponse<AgentResponseAdminDto, Object> sendResetPassEmail(
	    @RequestParam(value = "agent_id", required = true) String agent_id) throws NoSuchAlgorithmException {
	return ApiResponse.buildResults(adminService.agentResetPass(agent_id));
    }

    // DepartMent
    @RequestMapping(value = { "/api/admins/dept" }, method = { RequestMethod.GET })
    public ApiResponse<DepartmentResponseAdminDto, Object> fetchDepts(
	    @RequestParam(value = "dept_id", required = false) String deptId) {
	return ApiResponse.buildResults(adminService.fetchDepts(deptId));
    }

    @RequestMapping(value = { "/api/admins/dept" }, method = { RequestMethod.POST })
    public ApiResponse<DepartmentResponseAdminDto, Object> createOrUpdateDept(
	    @RequestBody DepartmentResponseAdminDto dto) {
	return ApiResponse.buildResults(adminService.createOrUpdateDept(dto))
		.message(ArgUtil.is(dto.getDept_id()) ? "Team Updated" : "Team Created");
    }

    @RequestMapping(value = { "/api/admins/dept/default" }, method = { RequestMethod.POST })
    public ApiResponse<DepartmentResponseAdminDto, Object> updateDeptDefault(
	    @RequestParam(value = "dept_id", required = true) String deptId) {
	return ApiResponse.buildResults(adminService.updateDepartmentDefault(deptId));
    }

    // OTHER APIS?
    @RequestMapping(value = "/admin/create-update-dept", method = { RequestMethod.POST })
    public List<DepartmentDoc> createDepartment(@RequestBody DepartmentDoc requestModel) {
	return adminService.createAndUpdateDepartment(requestModel);
    }

    @RequestMapping(value = "/admin/delete-dept", method = { RequestMethod.POST })
    public List<DepartmentDoc> updateDepartment(@RequestParam(value = "dept_id", required = true) Integer dept_id,
	    @RequestParam(value = "status", required = true) String status) {
	return adminService.updateDepartment(dept_id, status);
    }

    @RequestMapping(value = "/api/admins/contacts", method = { RequestMethod.GET })
    public ApiResponse<ContactDTO, Object> allContacts(@RequestParam(required = false) ContactType contactType,
	    @RequestParam(required = false) NamedMapModel lane, @RequestParam(required = false) String search) {
	String laneValue = ArgUtil.is(lane) ? lane.name("lane") : null;
	if (ArgUtil.is(laneValue)) {
	    return ApiResponse.buildResults( // Wrap with ApiResponse
		    ChatDTOUtil.getContactDTO( // Convert to DTO
			    contactStore.searchContacts(search, laneValue) // Search Docs
		    ));
	} else {
	    return new ApiResponse<ContactDTO, Object>();
	}

    }
}
