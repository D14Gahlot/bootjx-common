package com.boot.jx.admin.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.admin.AdminAuthProvider;
import com.boot.jx.admin.service.AgentLoginService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;
import com.boot.utils.MapBuilder;

@Controller
public class AdminMainController {

	@Value("${mry.cdn.url}")
	private String cdnServer;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@RequestMapping(value = { "/pub/**", "/app/**", "/auth/**", "/" }, method = { RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), cdnServer));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		return "app";
	}

	@Autowired
	private AdminAuthProvider adminAuthProvider;

	@ResponseBody
	@RequestMapping(value = "/auth/meta", method = { RequestMethod.GET })
	public ApiResponse<String, String> meta(@RequestParam String username, @RequestParam String password,
			HttpServletRequest request) {
		ApiResponse<String, String> x = ApiResponse.buildData("success", "success");

		if (username.startsWith("agent") && password.equals("mehery@1234")) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = adminAuthProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} else {
			x.setData("error");
			x.setMeta("error");
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	@ResponseBody
	@RequestMapping(value = "/auth/login/submit", method = { RequestMethod.POST })
	public ApiResponse<String, String> login(@RequestParam String username, @RequestParam String password,
			HttpServletRequest request) {
		ApiResponse<String, String> x = ApiResponse.buildData("success", "success");

		if (username.startsWith("admin") && password.equals("mehery@1234")) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = adminAuthProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			x.setStatusKey("SUCCESS");
		} else {
			x.setData("error");
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username or Password is incorrect");
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	@Autowired
	private AgentLoginService agentLoginService;

	@ResponseBody
	@RequestMapping(value = "/auth/agent/login", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentLogin(@RequestParam String username,
			@RequestParam String password, HttpServletRequest request) throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", "success").toMap(), "success");
		if (agentLoginService.loginAgent(username, password)) {
			x.setStatusKey("SUCCESS");
		} else {
			x.data().put("success", false);
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username or Password is incorrect");
		}
		return x;
	}
}
