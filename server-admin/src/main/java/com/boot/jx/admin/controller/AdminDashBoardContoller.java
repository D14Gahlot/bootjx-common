package com.boot.jx.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
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
	
	@RequestMapping(value = "/admin/contactType-dashboard-analytics", method = { RequestMethod.POST})
	public  ApiResponse<List<DashBoardResponseDto>,Object>  dashBoardAnalytics(@RequestBody DashBoardRequestDto req){//@RequestParam (value="contactType" ,required = true) Object contactType) {
		List<DashBoardResponseDto> lst= adminDbMgr.getContactWiseDashBoardAnalytics(req);
		ApiResponse<List<DashBoardResponseDto>,Object> resp=new ApiResponse<List<DashBoardResponseDto>,Object>();
		resp.setData(lst);
		return resp;
	}
	
	@RequestMapping(value = "/admin/fetch-contact-type", method = { RequestMethod.GET})
	public  List<String> fetchContactType() {
		return adminDbMgr.getListOfContactType();
	}
	
	@RequestMapping(value = "/admin/fetch-agent-chat-session-list", method = { RequestMethod.GET})
	public  List<ChatSessionDoc> getAgentList() {
		return agentAnaMgr.getAgentList();
	}
	
	
	@RequestMapping(value = "/admin/agent-dashboard-analytics", method = { RequestMethod.POST})
	public  ApiResponse<List<DashBoardResponseDto>,Object> getAgentWiseAnalytics(@RequestBody  DashBoardRequestDto req) {
		List<DashBoardResponseDto> lst=agentAnaMgr.getAgentWiseAnalytics(req);
		ApiResponse<List<DashBoardResponseDto>,Object> resp=new ApiResponse<List<DashBoardResponseDto>,Object>();
		resp.setData(lst);
		return resp;
	}
	
	
}