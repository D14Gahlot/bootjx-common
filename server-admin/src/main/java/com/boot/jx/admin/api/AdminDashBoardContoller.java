package com.boot.jx.admin.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.ContactTypeSummaryDto;
import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.TagDocumentDto;
import com.boot.jx.admin.dto.TagDocumentLst;
import com.boot.jx.admin.manager.AdminDashBoardManager;
import com.boot.jx.admin.manager.AgentAnalyticsManager;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.ChatSessionDoc;

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
	public ApiResponse<TagDocumentLst, Object> getTagAnalytics(@RequestBody DashBoardRequestDto req) {
		TagDocumentDto lst = adminDbMgr.getTagDocumentDetails(req);
		return ApiResponse.buildResults(lst.getLstTagDocument());
	}
	
	
	@RequestMapping(value = "/admin/fetch-month", method = { RequestMethod.GET })
	public ApiResponse<Map<Object, Object>, Object> getMonthLst() {
		Map<Object, Object> set =adminDbMgr.fetchUniqueMonth(); 
		return  ApiResponse.buildResult(set);
	}
	@RequestMapping(value = "/admin/monthwise-summary-count", method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getMonthLst(long timestamp) {
		ContactTypeSummaryDto summary =adminDbMgr.getMonthWiseCount(timestamp); 
		return  ApiResponse.buildResult(summary);
	}
	
	@RequestMapping(value = "/admin/monthwise-summary-save", method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getMonthWiseSaving(long timestamp) {
		ContactTypeSummaryDto summary =adminDbMgr.summaryV1(timestamp); 
		return  ApiResponse.buildResult(summary);
	}

}