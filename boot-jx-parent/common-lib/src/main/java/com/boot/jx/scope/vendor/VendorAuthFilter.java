package com.boot.jx.scope.vendor;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;

public interface VendorAuthFilter {
	public boolean isAuthorizedVendorRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId,
			String authToken);

}