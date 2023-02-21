package com.boot.jx.contak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.manager.ContakApiContext;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.scope.tnt.TenantAuthContext.TenantAuthFilter;
import com.boot.jx.scope.tnt.TenantSpecific;
import com.boot.utils.ArgUtil;

@Component
@TenantSpecific("*")
public class ContakVendorConfigurer implements TenantAuthFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContakVendorConfigurer.class);

	@Autowired
	ContakApiContext apiContext;

	public static ClientApp getClientApp() {
		return AppContextUtil.get("XmsVendorConfigurer:ClientApp");
	}

	@Override
	public boolean filterTenantRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {

		String apiKey = req.get(ParamKeys.X_API_KEY);

		if (!ArgUtil.is(apiKey)) {
			String message = "Missing " + ParamKeys.X_API_KEY;
			ApiResponseUtil.addError(message);
			return false;
		}

		CompanyDoc company = apiContext.loadKey(apiKey, apiRequest.hasRule("VALID_SESSION"));

		if (ArgUtil.not(company)) {
			String message = "Invalid " + ParamKeys.X_API_KEY;
			ApiResponseUtil.addError(message);
			return false;
		}

		if (!isIPAddressAllowed(company, req)) {
			ApiResponseUtil.addError("IP Address not allowed");
			return false;
		} ;

		return true;
	}

	private boolean isIPAddressAllowed(CompanyDoc company, CommonHttpRequest req) {
		if (ArgUtil.is(company.getPrefs()) && ArgUtil.is(company.getPrefs().getAllowedIPAddresses())
				&& company.getPrefs().getAllowedIPAddresses().size() > 0) {
			for (String ipAddress : company.getPrefs().getAllowedIPAddresses()) {
				if (ArgUtil.is(req.getIPAddress(), ipAddress)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}