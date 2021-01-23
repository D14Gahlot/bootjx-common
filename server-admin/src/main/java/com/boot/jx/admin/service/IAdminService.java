package com.boot.jx.admin.service;

import java.util.List;

import com.boot.jx.admin.dto.AgentRequestDto;
import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.api.ApiResponse;

public interface IAdminService {
	public static class Path {
		public static final String PREFIX = "/admin/";
		public static final String SAVE_AGENT = PREFIX + "/save-agent/";
		public static final String FETCH_AGENT = PREFIX + "/fetch-agent/";
	}
	
	
	ApiResponse<List<AgentResponseDto>,Object> saveAgent(AgentRequestDto requestModel);
	ApiResponse<List<AgentResponseDto>,Object> fetchAgentList();
	
}
