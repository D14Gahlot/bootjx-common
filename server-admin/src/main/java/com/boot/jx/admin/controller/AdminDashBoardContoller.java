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

@RestController
public class AdminDashBoardContoller {

	@Autowired
	AdminDashBoardManager adminDbMgr;
	
	@RequestMapping(value = "/admin/dashboard-analytics", method = { RequestMethod.POST})
	public  List<DashBoardResponseDto> dashBoardAnalytics(@RequestBody DashBoardRequestDto req){
		List<DashBoardResponseDto> dto = adminDbMgr.getDashBoardAnalytics(req);
		return dto;
	}
	
	@RequestMapping(value = "/admin/fetch-contact-type", method = { RequestMethod.GET})
	public  List<String> fetchContactType() {
		return adminDbMgr.getListOfContactType();
	}
}