package com.boot.jx.filter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.utils.TimeZoneUtil;
import com.boot.utils.TimeZoneUtil.TimeZoneDto;

@RestController
public class AppOptionsController {

	@RequestMapping(value = "/pub/meta/options/timezone", method = { RequestMethod.GET })
	public ApiResponse<TimeZoneDto, Object> getTimeZones() {
		return ApiResponse.buildResults(TimeZoneUtil.getTimeZoneDtoLst());
	}

	@RequestMapping(value = "/pub/meta/options/timezone/key", method = { RequestMethod.GET })
	public ApiResponse<String, Object> getTimeZoneKeys() {
		return ApiResponse.buildResults(TimeZoneUtil.getTimeZoneLst());
	}

}
