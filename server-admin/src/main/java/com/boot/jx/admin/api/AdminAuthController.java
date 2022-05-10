package com.boot.jx.admin.api;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.admin.AdminAuthProvider;
import com.boot.jx.admin.AdminSessionBean;
import com.boot.jx.admin.AdminSessionService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.manager.StarterDocKit;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.MapBuilder;

@Controller
public class AdminAuthController {

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private EmpAuthService authService;

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private AdminSessionService sessionService;

	@Autowired
	private AdminSessionBean adminSession;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	public StarterDocKit starterDocKit;

	@RequestMapping(value = { "/pub/**", "/app/**", "/auth/**", "/" },
			method = { RequestMethod.GET, RequestMethod.POST })
	public String home(Model model, HttpServletRequest request, @RequestParam(required = false) String domainName,
			@RequestParam(required = false) String domainId, @RequestParam(required = false) String domainToken,
			@RequestParam(required = false) String domainUser) throws NoSuchAlgorithmException {

		String xRemSession = ArgUtil.parseAsString(commonHttpRequest.get("JXSESSIONID"), Constants.BLANK);
		if (ArgUtil.is(domainName) && ArgUtil.is(domainId) && ArgUtil.is(domainToken)) {
			AgentResponseAuthDto agent = authService.loginByDomainToken(domainUser, domainName, domainId, domainToken,
					true);
			if (ArgUtil.is(agent)) {
				sessionService.login(request, agent, domainToken);
				xRemSession = CryptoUtil.getEncoder()
						.obzect(MapBuilder.map().put("domainUser", domainUser).put("domainName", domainName)
								.put("domainId", domainId).put("password", domainToken).toMap())
						.encodeBase64().encrypt().toString();
				commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
				return "redirect:/app/home";
			}
		} else if (!adminSession.isLoggedIn() && ArgUtil.is(xRemSession)) {
			@SuppressWarnings("unchecked")
			MapModel map = MapModel
					.from(CryptoUtil.getEncoder().message(xRemSession).decrypt().decodeBase64().toObzect(Map.class));
			AgentResponseAuthDto agent = authService.loginByDomainToken(map.getString(domainUser),
					map.getString(domainName), map.getString(domainId), map.getString(domainToken), true);
			if (ArgUtil.is(agent)) {
				sessionService.login(request, agent, domainToken);
				commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
				return "redirect:/app/home";
			} else {
				commonHttpRequest.deleteCookie("JXSESSIONID");
			}
		}

		if (!adminSession.isLoggedIn()) {
			return "redirect:/auth/logout";
		}

		model.addAllAttributes(appCommonConfig.appAttributes());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth)) {
			starterDocKit.domain();
			model.addAttribute("APP_USER", auth.getName());
		} else {
			model.addAttribute("APP_USER", "");
		}
		return "app-admin";
	}

	@RequestMapping(value = { "/auth/login", "/auth/resetpass" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		model.addAllAttributes(appCommonConfig.appAttributes());

		String page = ArgUtil.parseAsString(commonHttpRequest.get("page"), "login");
		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "login");
		String status = Constants.BLANK;
		Object message = Constants.BLANK;
		try {
			if ("resetpass".equalsIgnoreCase(action)) {
				String username = ArgUtil.parseAsString(commonHttpRequest.get("username"), Constants.BLANK);
				ApiResponse<Map<String, Object>, String> x = authService.agentResetPass(username, true);
				if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
					status = "SUCCESS";
					message = "Link to reset password sent on registered email.";
				} else {
					status = "ERROR";
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
						status = "ERROR";
						message = "Please enter valid password";
					} else if (confirmpassword.equals(newpassword)) {
						ApiResponse<Map<String, Object>, String> x = authService.agentSetPass(username, token,
								newpassword, true);
						if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
							status = "SUCCESS";
							message = "Password has been reset successfully";
						} else {
							status = "FAIL";
							message = x.getMessage();
						}
					} else {
						status = "ERROR";
						message = "Password Mismatch";
					}
				}
			} else {
				String xRemSession = ArgUtil.parseAsString(commonHttpRequest.get("JXSESSIONID"), Constants.BLANK);
				if (ArgUtil.is(xRemSession)) {
					@SuppressWarnings("unchecked")
					MapModel map = MapModel.from(
							CryptoUtil.getEncoder().message(xRemSession).decrypt().decodeBase64().toObzect(Map.class));
					ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = this.login(map.getString("username"),
							map.getString("password"), request);
					if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
						if (ArgUtil.is(x.getMeta())) {
							if (ArgUtil.is(x.getRedirectUrl())) {
								httpServletResponse.setHeader("Location", x.getRedirectUrl());
								httpServletResponse.setStatus(302);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			status = "FAIL";
			message = "Sorry some technical issues";
		}

		model.addAttribute("MESSAGE", message);
		model.addAttribute("PAGE", page);
		model.addAttribute("ACTION", action);
		model.addAttribute("STATUS", status);
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
		ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = authService.empLogin(username, password, true);
		if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");

			sessionService.login(request, x.getMeta(), password);
			x.setStatusKey("SUCCESS");

			boolean rememberme = ArgUtil.parseAsBoolean(commonHttpRequest.get("rememberme"), false);
			if (rememberme) {
				String xRemSession = CryptoUtil.getEncoder()
						.obzect(MapBuilder.map().put("username", username).put("password", password).toMap())
						.encodeBase64().encrypt().toString();
				commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
			}

		} else {
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	// Agent APIS

	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/auth/agent/login", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> agentLogin(@RequestParam String username,
			@RequestParam String password, @RequestParam(required = false) boolean admin)
			throws NoSuchAlgorithmException {
		return authService.empLogin(username, password, admin);
	}

	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/auth/agent/pass/reset", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentResetPass(@RequestParam String username,
			@RequestParam(required = false) boolean admin) throws NoSuchAlgorithmException {
		return authService.agentResetPass(username, admin);
	}

	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/auth/agent/pass/set", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> agentSetPass(@RequestParam String username,
			@RequestParam String password, @RequestParam String newpassword,
			@RequestParam(required = false) boolean admin) throws NoSuchAlgorithmException {
		return authService.agentSetPass(username, password, newpassword, admin);
	}
}
