package com.boot.jx.xms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.ClientApiKey;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.scope.tnt.TenantAuthContext.TenantAuthFilter;
import com.boot.jx.scope.tnt.TenantSpecific;
import com.boot.utils.ArgUtil;

@Component
@TenantSpecific("*")
public class XmsVendorConfigurer implements TenantAuthFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmsVendorConfigurer.class);

    @Autowired
    private PMEnvironment pmEnvironment;

    @Override
    public boolean filterTenantRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {
	String apiKey = req.get(XmsConstants.X_API_KEY);
	if (!ArgUtil.is(apiKey)) {
	    String message = "Missing " + XmsConstants.X_API_KEY;
	    ApiResponseUtil.addError(message);
	    return false;
	}
	ClientApiKey apiKeyConfig = pmEnvironment.config().clientApiKey(apiKey);
	if (!ArgUtil.is(apiKeyConfig)) {
	    String message = "Invalid " + XmsConstants.X_API_KEY;
	    ApiResponseUtil.addError(message);
	    return false;
	}
	return ArgUtil.areEqual(apiKey, apiKeyConfig.getKey());
    }

}