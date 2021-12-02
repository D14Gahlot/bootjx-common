package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.DigitalEventDto;
import com.boot.jx.xms.dto.DigitalObjectDto;
import com.boot.jx.xms.service.ApiService;

import io.swagger.annotations.Api;

@Api(tags = "Digital Analytics", description = "Analytics APIS")
@Controller
public class DigitalApiV1 {

    @Autowired
    ApiService apiService;

    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/data/push", method = { RequestMethod.POST })
    public ApiResponse<DigitalObjectDto, Object> dataPush(@RequestBody DigitalObjectDto digitalObjectDto) {
	apiService.saveDigitalInfo(digitalObjectDto);
	return ApiResponse.buildResult(digitalObjectDto);
    }

    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/event/push", method = { RequestMethod.POST })
    public ApiResponse<DigitalEventDto, Object> dataEvent(@RequestBody DigitalEventDto digitalEventDto) {

	apiService.saveDigitalEvent(digitalEventDto);
	return ApiResponse.buildResult(digitalEventDto);
    }

}
