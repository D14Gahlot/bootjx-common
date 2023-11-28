package com.boot.jx.contak.nodedocs;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@Api(tags = "BCS Messages", description = "API's to send OutBound Messages")
@RequestMapping("/entoc")
public class ClientDocController {

	@ApiRequest(authenticateTenant = true)
	@ApiOperation(value = "Send Message", notes = "${swagger.ClientDocController.sendCustomerNote.description}",
			authorizations = @Authorization("X_API_KEY"))
	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/send", method = { RequestMethod.POST })
	public ApiResponse<CustomerNote, Object> sendCustomerNote(@RequestBody CustomerNote msg) {
		return ApiResponse.buildResult(msg);
	}

}