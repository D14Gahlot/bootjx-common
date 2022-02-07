package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.jx.xms.dto.DigitalEventDto;
import com.boot.jx.xms.dto.DigitalObjectDto;
import com.boot.jx.xms.service.ApiService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Digital Analytics", description = "Analytics APIS")
@RestController
public class DigitalApiV1 {

    @Autowired
    ApiService apiService;

    @ApiOperation(value = "Push Customer Data", notes = "This API can be used to upload customer details",
	    authorizations = @Authorization("X_API_KEY"))
    @XMSClientAuth
    @RequestMapping(value = "/api/v1/data/push", method = { RequestMethod.POST })
    public ApiResponse<DigitalObjectDto, Object> dataPush(@RequestBody DigitalObjectDto digitalObjectDto) {
	apiService.saveDigitalInfo(digitalObjectDto);
	return ApiResponse.buildResult(digitalObjectDto);
    }

    @ApiOperation(value = "Push Customer Event", notes = "This API can be used to upload customer event",
	    authorizations = @Authorization("X_API_KEY"))
    @XMSClientAuth
    @RequestMapping(value = "/api/v1/event/push", method = { RequestMethod.POST })
    public ApiResponse<DigitalEventDto, Object> dataEvent(@RequestBody DigitalEventDto digitalEventDto) {
	apiService.saveDigitalEvent(digitalEventDto);
	return ApiResponse.buildResult(digitalEventDto);
    }

}
