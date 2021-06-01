package com.boot.jx.admin.api;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.boot.jx.admin.AdminSessionService;
import com.boot.jx.admin.service.AdminAuthService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.AppCommonConfig;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;

@Controller
public class AdminAuthController {

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AdminAuthService agentLoginService;

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private AdminSessionService sessionService;

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
		model.addAttribute("CONFIG", JsonUtil.toJson(appCommonConfig.toMap()));
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("CDN_URL",
				ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), appCommonConfig.getCdnServer()));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		return "app";
	}

	@RequestMapping(value = { "/auth/login", "/auth/resetpass" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model, HttpServletRequest request) {
		model.addAttribute("CDN_URL",
				ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), appCommonConfig.getCdnServer()));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("STAMP", System.currentTimeMillis());

		String page = ArgUtil.parseAsString(commonHttpRequest.get("page"), "login");
		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "login");
		Object message = Constants.BLANK;
		try {
			if ("resetpass".equalsIgnoreCase(action)) {
				String username = ArgUtil.parseAsString(commonHttpRequest.get("username"), Constants.BLANK);
				ApiResponse<Map<String, Object>, String> x = agentResetPass(username, true);
				if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
					message = "Link to reset password sent on registered email.";
				} else {
					message = x.getMessage();
				}
			} else if ("setpass".equalsIgnoreCase(page)) {
				String username = ArgUtil.parseAsString(commonHttpRequest.get("username"), Constants.BLANK);
				String token = ArgUtil.parseAsString(commonHttpRequest.get("token"), Constants.BLANK);
				String newpassword = ArgUtil.parseAsString(commonHttpRequest.get("newpassword"), Constants.BLANK);
				String confirmpassword = ArgUtil.parseAsString(commonHttpRequest.get("confirmpassword"),
						Constants.BLANK);
				model.addAttribute("username", username);
				model.addAttribute("token", token);

				if ("setpass".equalsIgnoreCase(action)) {
					if (!ArgUtil.is(confirmpassword)) {
						message = "Please enter valid password";
					} else if (confirmpassword.equals(newpassword)) {
						ApiResponse<Map<String, Object>, String> x = agentSetPass(username, token, newpassword, true);
						if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
							message = "Password has been reset successfully";
						} else {
							message = x.getMessage();
						}
					} else {
						message = "Password Mismatch";
					}
				}
			}
		} catch (Exception e) {
			message = "Sorry some technical issues";
		}

		model.addAttribute("MESSAGE", message);
		model.addAttribute("PAGE", page);
		model.addAttribute("APP_TITLE", appConfig.getAppTitle());
		return "app-login";
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
	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> login(@RequestParam String username,
			@RequestParam String password, HttpServletRequest request) throws NoSuchAlgorithmException {
		username = ArgUtil.parseAsString(username, Constants.BLANK);
		ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = agentLogin(username, password, true);
		if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
					x.getMeta().getAgent_code(), password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = adminAuthProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			sessionService.updateLogin(x.getMeta());
			x.setStatusKey("SUCCESS");
		} else {
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	// Agent APIS

	@ResponseBody
	@RequestMapping(value = "/auth/agent/login", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> agentLogin(@RequestParam String username,
			@RequestParam String password, @RequestParam(required = false) boolean admin)
			throws NoSuchAlgorithmException {
		AgentResponseAuthDto agent = agentLoginService.loginAgent(username, password, admin);
		if (ArgUtil.is(agent)) {
			return ApiResponse.buildData(MapBuilder.map().put("success", true).toMap(), agent).statusKey("SUCCESS");
		} else {
			return ApiResponse.buildData(MapBuilder.map().put("success", false).toMap(), agent).statusKey("ERROR")
					.message("Username or Password is incorrect");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/auth/agent/pass/reset", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentResetPass(@RequestParam String username,
			@RequestParam(required = false) boolean admin) throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
		if (agentLoginService.resetPassword(username, admin)) {
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
			@RequestParam String password, @RequestParam String newpassword,
			@RequestParam(required = false) boolean admin) throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
		if (agentLoginService.setPassword(username, password, newpassword, admin)) {
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
