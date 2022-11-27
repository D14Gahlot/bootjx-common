package com.boot.jx.contak.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.contak.ContakAuthService;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Contak Panel", description = "API's for Panel", hidden = true)
@Controller
@RequestMapping("/panel")
public class PanelController {
	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private ContakAuthService authService;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@ApiOperation(value = "Page", hidden = true)
	@RequestMapping(path = { "/", "/**" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String defaultPage(Model model) {

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}

		return "app-contak";
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/login" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> login(Model model, @RequestParam String username, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) {
		ContakUserDoc user = authService.authenticate(username, password, request);
		return ApiResponse.build().meta(MapModel.createInstance().put("username", user.getName()).toMap());
	}
}
