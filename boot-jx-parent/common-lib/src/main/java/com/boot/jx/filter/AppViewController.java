package com.boot.jx.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil.HashBuilder;

@Controller
public class AppViewController {

    @Autowired
    AppConfig appConfig;

    @Autowired
    CommonHttpRequest commonHttpRequest;

    @Value("${swagger.auth.username}")
    String swaggerAuthUsername;

    @Value("${swagger.auth.password}")
    String swaggerAuthPassword;

    @Autowired(required = false)
    private AppCommonConfig appCommonConfig;

    private boolean isLoggedIn() {
	if (!ArgUtil.is(swaggerAuthPassword)) {
	    return true;
	}

	String token = commonHttpRequest.get("swagger.auth.token");

	HashBuilder builder = new HashBuilder().interval(300).secret(swaggerAuthPassword).message(swaggerAuthUsername);
	if (ArgUtil.is(token) && builder.validate(token)) {
	    return true;
	}
	String username = commonHttpRequest.get("swagger.auth.username");
	String password = commonHttpRequest.get("swagger.auth.password");

	if (ArgUtil.areEqual(username, swaggerAuthUsername) && ArgUtil.areEqual(password, swaggerAuthPassword)) {
	    token = builder.toHmac().output();
	    commonHttpRequest.setCookie("swagger.auth.token", token);
	    return true;
	}

	return false;
    }

    @RequestMapping(value = { "/swagger-ui.html" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String swagger(Model model) {

	model.addAttribute("CDN_URL", appConfig.getAppPrefix());
	if (ArgUtil.is(appCommonConfig)) {
	    model.addAllAttributes(appCommonConfig.appAttributes());
	}

	if (!isLoggedIn()) {
	    return "swagger-login";
	}
	return "swagger-ui";
    }

    @RequestMapping(value = { "/swagger-uix.html" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String swagger2(Model model) {

	model.addAttribute("CDN_URL", appConfig.getAppPrefix());
	if (ArgUtil.is(appCommonConfig)) {
	    model.addAllAttributes(appCommonConfig.appAttributes());
	}

	if (!isLoggedIn()) {
	    return "swagger-login";
	}
	model.addAttribute("APP_NAME", appConfig.getAppName());
	model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
	return "swagger-uix";
    }

    @GetMapping({ "favicon.ico", "/favicon.ico", "/favicon.icon", "/favicon.**" })
    @ResponseBody
    public  ResponseEntity<byte[]> returnNoFavicon() {
	byte[] image = new byte[0];
	return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

}
