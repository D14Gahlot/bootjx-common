package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.ClientApiKey;
import com.boot.jx.postman.PMEnvironment;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil.HashBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "XMS ViewConroller", description = "API's for Messaging", hidden = true)
@Controller
public class XmsController {
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

    private boolean isLoggedIn() {
	String apiId = commonHttpRequest.get("swagger.auth.apiId");
	String token = commonHttpRequest.get("swagger.auth.token");
	String apiKey = commonHttpRequest.get("swagger.auth.apiKey");
	HashBuilder builder = new HashBuilder().interval(300).secret(swaggerAuthPassword).message(apiId);
	if (ArgUtil.is(token) && builder.validate(token)) {
	    return true;
	}

	if (!ArgUtil.is(apiKey)) {
	    return false;
	}

	ClientApiKey apiKeyConfig = pmEnvironment.local().clientApiKey(apiKey);
	if (ArgUtil.is(apiKeyConfig) && ArgUtil.areEqual(apiKey, apiKeyConfig.getKey())) {
	    token = builder.toHmac().output();
	    commonHttpRequest.setCookie("swagger.auth.apiId", apiId);
	    commonHttpRequest.setCookie("swagger.auth.token", token);
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
	    return "app-xms";
	}
	return "swagger-uix";
    }

    @ApiOperation(value = "Try docs", hidden = true)
    @RequestMapping(value = { "/docs" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String docs(Model model, @RequestParam(required = false) String path) {
	return "redirect:" + pmEnvironment.keyEntry("mry.prop.service.docs.link").asString()
		+ ArgUtil.nonEmpty(path, Constants.BLANK);
    }
}
