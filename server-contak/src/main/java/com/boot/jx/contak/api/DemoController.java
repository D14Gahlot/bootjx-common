package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.CrypToken;
import com.boot.utils.CryptoUtil.HashBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "XMS ViewConroller", description = "API's for Messaging", hidden = true)
@Controller
public class DemoController {
	@Autowired
	AppConfig appConfig;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Value("${swagger.auth.password}")
	String swaggerAuthPassword;

	@Autowired
	private ConfigManager configManager;

	@Autowired
	private CDNBuilder cdnBuilder;

	private boolean isLoggedIn() {
		String apiId = commonHttpRequest.get("swagger.auth.apiId");
		String hash = commonHttpRequest.get("swagger.auth.hash");
		String token = commonHttpRequest.get("swagger.auth.token");
		String apiKey = commonHttpRequest.get("swagger.auth.apiKey");
		HashBuilder builder = new HashBuilder().interval(300).secret(swaggerAuthPassword).message(apiId);
		if (ArgUtil.is(hash) && builder.validate(hash)) {
			return true;
		}

		if (!ArgUtil.is(apiKey)) {
			if (ArgUtil.is(token)) {
				CrypToken xToken = CryptoUtil.getEncoder().message(token).decrypt().toToken();
				if (ArgUtil.is(xToken) && !xToken.isExpired()) {
					apiKey = xToken.message;
				}
			}
			if (!ArgUtil.is(apiKey)) {
				return false;
			}
		}

		ClientApp apiKeyConfig = pmEnvironment.local().clientApiKey(apiKey);
		if (ArgUtil.is(apiKeyConfig) && ArgUtil.areEqual(apiKey, apiKeyConfig.getKey())) {
			hash = builder.toHmac().output();
			token = CryptoUtil.getEncoder().message(apiKey).tokenize(300).encrypt().toString();
			commonHttpRequest.setCookie("swagger.auth.apiId", apiId, 300);
			commonHttpRequest.setCookie("swagger.auth.hash", hash, 300);
			commonHttpRequest.setCookie("swagger.auth.token", token, 300);
			return true;
		}
		return false;
	}

	@ApiOperation(value = "Try API's", hidden = true)
	@RequestMapping(value = { "/" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String swagger(Model model) {

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}

		if (!isLoggedIn()) {
			return "app-contak-login";
		}
		return "swagger-uix";
	}

	@ApiOperation(value = "Page", hidden = true)
	@RequestMapping(value = { "/demo" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String notp(Model model) {

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}

		return "app-contak";
	}

	@RequestMapping(value = "/pub/config/cdn", method = { RequestMethod.POST })
	public ApiResponse<PMConfigurationObject, Object> updateCDN(@RequestParam(required = false) String url,
			@RequestParam(required = false) String version,
			@RequestParam(required = false, defaultValue = "false") boolean beta) {

		String domainServer = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString();
		String key = beta ? "mry.cdn.url.beta" : "mry.cdn.url";

		PMConfigurationObject config = pmEnvironment.keyEntry(key);
		config.setKey(key);

		String oldUrl = config.asString();
		config.setServer(domainServer);

		if (ArgUtil.is(version) && ArgUtil.is(oldUrl)) {
			url = cdnBuilder.updateVersion(oldUrl, version);
		}

		if (ArgUtil.is(url)) {
			config.setValue(url);
			configManager.save(config);
		}

		return ApiResponse.buildResults(config);
	}
}
