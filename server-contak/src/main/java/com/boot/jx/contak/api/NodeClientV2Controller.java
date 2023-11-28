package com.boot.jx.contak.api;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.cache.OtpAlertEvent;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.manager.ContakApiContext;
import com.boot.jx.contak.manager.ContakApiContext.AUTH_RULES;
import com.boot.jx.contak.manager.ContakInboundRouter;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.filter.AppRequestUtil;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;
import com.boot.utils.ArgUtil;

@RestController
@RequestMapping("/client/api/v2")
public class NodeClientV2Controller {

	@Autowired
	private ContakApiContext apiContext;

	@Autowired
	private ContakInboundRouter otpAlertEventManager;

	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER) })
	@ApiRequest(authenticateTenant = true, rules = { AUTH_RULES.VALID_SESSION })
	@RequestMapping(value = "/inbound/fetch", method = { RequestMethod.POST })
	public ApiResponse<OtpAlertEvent, Object> messageInboundFetch(@RequestBody HashMap<String, String> msg) {
		AppRequestUtil.log("MESSAGE APIKEY", msg);
		String name = msg.get("name");
		if (!ArgUtil.is(name)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("name"));
		}
		CompanyDoc compoc = apiContext.getCompany();
		if (!ArgUtil.is(compoc)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("apiKey"));
		}

		List<OtpAlertEvent> inbounds = otpAlertEventManager.pollOtpAlertEvents(compoc.companyId);

		return ApiResponse.buildResults(inbounds);
	}

}