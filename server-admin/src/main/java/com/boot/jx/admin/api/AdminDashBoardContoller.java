package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.ContactTypeSummaryDto;
import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.MonthDtlsDto;
import com.boot.jx.admin.dto.TagDocumentDto;
import com.boot.jx.admin.dto.TagDocumentLst;
import com.boot.jx.admin.manager.AdminDashBoardManager;
import com.boot.jx.admin.manager.AgentAnalyticsManager;
import com.boot.jx.api.ApiResponse;

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
	public List<String> getAgentList() {
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

	@ResponseBody
	@RequestMapping(value = { "/admin/hourwise-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getHourWiseSummary(@RequestParam(required = false) long timestamp,
			long hr) {
		ContactTypeSummaryDto summary = adminDbMgr.hourWisesummary(timestamp, hr);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/admin/daywise-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getDayWiseSummary(@RequestParam(required = false) long dateRange1,
			@RequestParam(required = false) long dateRange2, int days) {
		ContactTypeSummaryDto summary = adminDbMgr.dayChannelWiseWisesummary(dateRange1, dateRange2, days);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/admin/hourwise-msg-status-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getHourWiseMsgStatusSummary(
			@RequestParam(required = false) long timestamp, long hr) {
		ContactTypeSummaryDto summary = adminDbMgr.getHourWiseMsgStatusSummary(timestamp, hr);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/admin/datewise-msg-status-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getDayWiseMsgStatusSummary(long dateRange1, long dateRange2,
			int days) {
		ContactTypeSummaryDto summary = adminDbMgr.getDayWiseMsgStatusSummary(dateRange1, dateRange2, days);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/admin/fetch-month" }, method = { RequestMethod.GET })
	public ApiResponse<MonthDtlsDto, Object> getMonthLst() {
		List<MonthDtlsDto> listofMonth = adminDbMgr.fetchUniqueMonth();
		return ApiResponse.buildResults(listofMonth);
	}

}