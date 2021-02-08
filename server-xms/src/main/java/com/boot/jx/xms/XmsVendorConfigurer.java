package com.boot.jx.xms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.scope.vendor.VendorAuthFilter;
import com.boot.jx.scope.vendor.VendorAuthService;
import com.boot.jx.scope.vendor.VendorContext.VendorScoped;
import com.boot.jx.scope.vendor.VendorContext.VendorValue;

@Component
@VendorScoped("*")
public class XmsVendorConfigurer implements VendorAuthFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmsVendorConfigurer.class);

	@VendorValue("${vendor.auth.id}")
	String basicAuthUser;

	@Autowired
	VendorAuthService vendorAuthService;

	@Override
	public boolean isAuthorizedVendorRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId,
			String authToken) {
		LOGGER.debug("isAuthVendorRequest {} {}", authToken, basicAuthUser);
		return vendorAuthService.hasValidBasicAuth(traceId, authToken) && vendorAuthService.hasFeature(apiRequest)
				&& vendorAuthService.hasValidIp(req);
	}

}