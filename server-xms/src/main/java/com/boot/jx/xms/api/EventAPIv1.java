package com.boot.jx.xms.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.DigitalObjectWrapper;

@Controller
public class EventAPIv1 {

	
	@ApiClientParams
	@ResponseBody
	@RequestMapping(value = "/api/v1/data/push", method = { RequestMethod.POST })
	public <T> ApiResponse<DigitalObjectWrapper, Object> dataPush(@RequestBody DigitalObjectWrapper<T> digitalWrapper) {
		return ApiResponse.buildResult(digitalWrapper);
	}

	
}
