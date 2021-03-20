package com.boot.jx.admin.api;

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
import com.boot.utils.Constants;
import com.boot.utils.MapBuilder;

@Controller
public class AdminMainController {

	@Value("${mry.cdn.url}")
	private String cdnServer;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AgentLoginService agentLoginService;
	
	private long getVersion() {
		return System.currentTimeMillis() / 300000;
	}

	@RequestMapping(value = { "/pub/**", "/app/**", "/auth/**", "/" }, method = { RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
		} else {
			model.addAttribute("APP_USER", "");
		}
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), cdnServer));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		return "app";
	}

	@RequestMapping(value = { "/auth/login" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model) {
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), cdnServer));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("STAMP", System.currentTimeMillis());
		String page = ArgUtil.parseAsString(commonHttpRequest.get("page"), "login");
		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "login");
		Object message = Constants.BLANK;
		model.addAttribute("MESSAGE", message);
		model.addAttribute("PAGE", page);
		return "admin-login";
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

	@ResponseBody
	@RequestMapping(value = "/auth/agent/login", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentLogin(@RequestParam String username,
			@RequestParam String password, HttpServletRequest request) throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
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

	@ResponseBody
	@RequestMapping(value = "/auth/agent/pass/reset", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentResetPass(@RequestParam String username,
			HttpServletRequest request) throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
		if (agentLoginService.resetPassword(username)) {
			x.setStatusKey("SUCCESS");
		} else {
			x.data().put("success", false);
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username is incorrect");
		}
		return x;
	}

	@ResponseBody
	@RequestMapping(value = "/auth/agent/pass/set", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentSetPass(@RequestParam String username,
			@RequestParam String password, @RequestParam String newpassword, HttpServletRequest request)
			throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
		if (agentLoginService.resetPassword(username)) {
			x.setStatusKey("SUCCESS");
		} else {
			x.data().put("success", false);
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username is incorrect");
		}
		return x;
	}
}
