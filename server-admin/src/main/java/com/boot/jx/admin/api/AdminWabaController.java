package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.WabaBalanceDto;
import com.boot.jx.admin.service.WabaService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.WabaAccountBalanceDoc;

@RestController
public class AdminWabaController {

	@Autowired
	WabaService wabaService;
	
	@RequestMapping(value = "/api/add-edit-account", method = { RequestMethod.POST })
	public ApiResponse<WabaAccountBalanceDoc, Object> addEditWabaAccountBalance(@RequestBody WabaAccountBalanceDoc reqDto){
		WabaAccountBalanceDoc doc = wabaService.addEditAccount(reqDto);
		return ApiResponse.buildResult(doc);
	}
	
	@RequestMapping(value = "/api/fetch/balance/waba/summary", method = { RequestMethod.GET })
	public ApiResponse<WabaBalanceDto, Object> fetchWabaAccountBalance(@RequestParam long timestamp){
		WabaBalanceDto dto = wabaService.fetchWabaAccountBalance(timestamp);
		return ApiResponse.buildResult(dto);
	}

	
	
}
