package com.boot.jx.rest;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;

public final class AppRequestInterfaces {
    public interface ClientAuthFilter {
	public boolean isAuthorizedClientRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId,
		String authToken);
    }
}
