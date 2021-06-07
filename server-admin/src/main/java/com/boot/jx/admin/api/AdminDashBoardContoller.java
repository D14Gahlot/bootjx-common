package com.boot.jx.admin.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.TagDocumentDto;
import com.boot.jx.admin.manager.AdminDashBoardManager;
import com.boot.jx.admin.manager.AgentAnalyticsManager;
import com.boot.jx.admin.manager.ChatParserAndImportor;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;

@RestController
public class AdminDashBoardContoller {

	@Autowired
	AdminDashBoardManager adminDbMgr;

	@Autowired
	AgentAnalyticsManager agentAnaMgr;

	@RequestMapping(value = "/admin/contactType-dashboard-analytics", method = { RequestMethod.POST })
	public ApiResponse<List<DashBoardResponseDto>, Object> dashBoardAnalytics(@RequestBody DashBoardRequestDto req) {
		// @RequestParam (value="contactType" ,required = true) Object contactType) {
		List<DashBoardResponseDto> lst = adminDbMgr.getContactWiseDashBoardAnalytics(req);
		ApiResponse<List<DashBoardResponseDto>, Object> resp = new ApiResponse<List<DashBoardResponseDto>, Object>();
		resp.setData(lst);
		return resp;
	}

	@RequestMapping(value = "/admin/fetch-contact-type", method = { RequestMethod.GET })
	public List<String> fetchContactType() {
		return adminDbMgr.getListOfContactType();
	}

	@RequestMapping(value = "/admin/fetch-agent-chat-session-list", method = { RequestMethod.GET })
	public List<ChatSessionDoc> getAgentList() {
		return agentAnaMgr.getAgentList();
	}

	@RequestMapping(value = "/admin/agent-dashboard-analytics", method = { RequestMethod.POST })
	public ApiResponse<DashBoardResponseDto, Object> getAgentWiseAnalytics(@RequestBody DashBoardRequestDto req) {
		ApiResponse<DashBoardResponseDto, Object> resp = new ApiResponse<DashBoardResponseDto, Object>();
		List<DashBoardResponseDto> lst = agentAnaMgr.getAgentWiseAnalytics(req);
		resp.results(lst);
		resp.data(agentAnaMgr.getSummery(lst));
		return resp;
	}

	@RequestMapping(value = "/admin/tag-analytics", method = { RequestMethod.POST })
	public ApiResponse<TagDocumentDto, Object> getTagAnalytics(@RequestBody DashBoardRequestDto req) {
		TagDocumentDto lst = adminDbMgr.getTagDocumentDetails(req);
		ApiResponse<TagDocumentDto, Object> resp = new ApiResponse<TagDocumentDto, Object>();
		resp.setData(lst);
		return resp;
	}


}