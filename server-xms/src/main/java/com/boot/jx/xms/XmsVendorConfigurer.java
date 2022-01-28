package com.boot.jx.xms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.scope.tnt.TenantAuthContext.TenantAuthFilter;
import com.boot.jx.scope.tnt.TenantSpecific;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
@TenantSpecific("*")
public class XmsVendorConfigurer implements TenantAuthFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmsVendorConfigurer.class);

    private static long CONFIG_REFRESH_TIME = TimeUtils.toMillis("5min");

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private AppConfigPackage appConfigPackage;

    public static ClientApp getClientApp() {
	return AppContextUtil.get("XmsVendorConfigurer:ClientApp");
    }

    @Override
    public boolean filterTenantRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {
	String apiKey = req.get(XmsConstants.X_API_KEY);
	if (!ArgUtil.is(apiKey)) {
	    String message = "Missing " + XmsConstants.X_API_KEY;
	    ApiResponseUtil.addError(message);
	    return false;
	}
	PMConfigurationModel config = pmEnvironment.local();
	ClientApp apiKeyConfig = config.clientApiKey(apiKey);
	if (!ArgUtil.is(apiKeyConfig)) {
	    if (TimeUtils.isExpired(config.getUpdateStamp(), CONFIG_REFRESH_TIME)) {
		appConfigPackage.clear(null);
		config = pmEnvironment.local();
	    }
	    if (!ArgUtil.is(apiKeyConfig)) {
		String message = "Invalid " + XmsConstants.X_API_KEY;
		ApiResponseUtil.addError(message);
		return false;
	    }
	}
	if (ArgUtil.areEqual(apiKey, apiKeyConfig.getKey())) {
	    AppContextUtil.set("XmsVendorConfigurer:ClientApp", apiKeyConfig);
	    return true;
	}
	return false;
    }

}