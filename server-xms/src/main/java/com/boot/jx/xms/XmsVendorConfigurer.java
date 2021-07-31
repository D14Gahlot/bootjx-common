package com.boot.jx.xms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.scope.tnt.TenantAuthContext.TenantAuthFilter;
import com.boot.jx.scope.tnt.TenantSpecific;

@Component
@TenantSpecific("*")
public class XmsVendorConfigurer implements TenantAuthFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmsVendorConfigurer.class);

    @Override
    public boolean isAuthorizedTenantRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {
	
	
	return true;
    }

}