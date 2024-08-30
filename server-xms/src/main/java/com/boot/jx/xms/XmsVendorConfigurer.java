package com.boot.jx.xms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.postman.PMContextUtil;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.scope.tnt.TenantAuthContext.TenantAuthFilter;
import com.boot.jx.scope.tnt.TenantSpecific;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.CrypToken;
import com.boot.utils.TimeUtils;

@Component
@TenantSpecific("*")
public class XmsVendorConfigurer implements TenantAuthFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmsVendorConfigurer.class);

	private static long CONFIG_REFRESH_TIME = TimeUtils.toMillis("5min");

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private AppConfigPackage appConfigPackage;

	@Autowired
	private TenantResolver tenantResolver;

	@Override
	public boolean filterTenantRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {

		if (!tenantResolver.isValid()) {
			ApiResponseUtil.addError("Invalid Domain");
			return false;
		}

		if (!pmEnvironment.config().keyEntry("mry.domain.active").asBoolean()) {
			ApiResponseUtil.addError("Domain not Active. Please contact support");
			return false;
		}

		if (!pmEnvironment.config().keyEntry("mry.domain.xms.active").asBoolean()) {
			ApiResponseUtil.addError("Messaging APIs not Active");
			return false;
		}

		String apiKey = req.get(ParamKeys.X_API_KEY);
		String apiId = req.get(ParamKeys.X_API_ID);
		String apiCode = req.get(ParamKeys.X_API_CODE);

		// For Swagger Handling
		if (!ArgUtil.is(apiKey)) {
			String token = req.get("swagger.auth.token");
			if (ArgUtil.is(token)) {
				CrypToken xToken = CryptoUtil.getEncoder().message(token).decrypt().toToken();
				if (ArgUtil.is(xToken) && !xToken.isExpired()) {
					apiKey = xToken.message;
				}
			}
		}

		if (!ArgUtil.is(apiKey)) {
			String message = "Missing " + ParamKeys.X_API_KEY;
			ApiResponseUtil.addError(message);
			return false;
		}

		PMConfigurationModel config = pmEnvironment.local();
		ClientApp apiKeyConfig = null;
		if (ArgUtil.is(apiId) && apiKey.equals(pmCommonConfig.getScriptusSecret())) {
			apiKeyConfig = config.clientApiKey(apiId);
			if (ArgUtil.is(apiKeyConfig))
				apiKey = apiKeyConfig.getKey();
		} else if (ArgUtil.is(apiCode) && apiKey.equals(pmCommonConfig.getScriptusSecret())) {
			apiKeyConfig = config.clientApiKey(apiCode);
			if (ArgUtil.is(apiKeyConfig))
				apiKey = apiKeyConfig.getKey();
		} else {
			apiKeyConfig = config.clientApiKey(apiKey);
		}

		if (!ArgUtil.is(apiKeyConfig) && apiKey.equals(pmCommonConfig.getScriptusSecret())) {
			config = pmEnvironment.shared();
			if (ArgUtil.is(apiId)) {
				apiKeyConfig = config.clientApiKey(apiId);
				if (ArgUtil.is(apiKeyConfig))
					apiKey = apiKeyConfig.getKey();
			} else if (ArgUtil.is(apiCode)) {
				apiKeyConfig = config.clientApiKey(apiCode);
				if (ArgUtil.is(apiKeyConfig))
					apiKey = apiKeyConfig.getKey();
			} else {
				apiKeyConfig = config.clientApiKey(apiKey);
			}
		}

		if (!ArgUtil.is(apiKeyConfig)) {
			if (TimeUtils.isExpired(config.getUpdateStamp(), CONFIG_REFRESH_TIME)) {
				appConfigPackage.clear();
				config = pmEnvironment.local();
			}
			if (!ArgUtil.is(apiKeyConfig)) {
				String message = "Invalid " + ParamKeys.X_API_KEY;
				ApiResponseUtil.addError(message);
				return false;
			}
		}
		if (ArgUtil.areEqual(apiKey, apiKeyConfig.getKey())) {
			PMContextUtil.clientApp(apiKeyConfig);
			return true;
		}
		return false;
	}

}