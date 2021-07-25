package com.boot.jx.agent.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import com.boot.jx.agent.AgentAuthProvider;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.store.PMStoreConstants;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelSessionManager;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;

@Controller
public class AgentAuthController {

	@Value("${mry.admin.url}")
	private String adminUrl;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private RestService restService;

	@Autowired
	private AppCommonConfig appCommonConfig;
	
	@Autowired
	private PMEnvironment pmEnvironment;

	private long getVersion() {
		return System.currentTimeMillis() / 300000;
	}

	@RequestMapping(value = { "/app/home", "/", "", "/app/**" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {

		if (!ArgUtil.is(agentSession.getAgentCode())) {
			return "redirect:/auth/logout";
		}

		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("CDN_URL",
				ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), appCommonConfig.getCdnServer()));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		model.addAttribute("CONFIG", JsonUtil.toJson(appCommonConfig.toMap()));
		String cdnnew = ArgUtil.parseAsString(commonHttpRequest.get("CDN_NEW"), "true");
		
		model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.get("postman.agent.scheme.color").asString());

		if ("true".equalsIgnoreCase(cdnnew)) {
			return "app";
		} else {
			String appUrl = ArgUtil.parseAsString(commonHttpRequest.get("APP_URL"), Constants.BLANK);
			model.addAttribute("APP_URL", appUrl);
			theme = ArgUtil.nonEmpty(commonHttpRequest.get("theme"), "dashboard.agent.bubble");
			model.addAttribute("APP_THEME", theme);
			return "dashboard.agent";
		}
	}

	@RequestMapping(value = "/app/home1", method = { RequestMethod.POST, RequestMethod.GET })
	public String home2(Model model) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.get("postman.agent.scheme.color").asString());
		return "whatsweb";
	}

	@RequestMapping(value = "/pub/customer/{page}", method = { RequestMethod.POST, RequestMethod.GET })
	public String customertest(Model model, @RequestParam String page) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("POSTMAN_CONTEXT", "/postman");
		model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.get("postman.agent.scheme.color").asString());
		return "customer." + page;
	}

	@RequestMapping(value = { "/auth/login", "/auth/resetpass" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		model.addAttribute("CDN_URL",
				ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), appCommonConfig.getCdnServer()));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("CDN_VERSION", getVersion());
		model.addAttribute("STAMP", System.currentTimeMillis());
		model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.get("postman.agent.scheme.color").asString());

		String page = ArgUtil.parseAsString(commonHttpRequest.get("page"), "login");
		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "login");
		String status = Constants.BLANK;
		Object message = Constants.BLANK;
		try {
			if ("resetpass".equalsIgnoreCase(action)) {
				String username = ArgUtil.parseAsString(commonHttpRequest.get("username"), Constants.BLANK);
				ApiResponse<Map<String, Object>, String> x = restService.ajax(adminUrl).path("/auth/agent/pass/reset")
						.field("username", username).postForm()
						.as(new ParameterizedTypeReference<ApiResponse<Map<String, Object>, String>>() {
						});
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
						ApiResponse<Map<String, Object>, String> x = restService.ajax(adminUrl)
								.path("/auth/agent/pass/set").field("username", username).field("password", token)
								.field("newpassword", newpassword).postForm()
								.as(new ParameterizedTypeReference<ApiResponse<Map<String, Object>, String>>() {
								});
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
		model.addAttribute("APP_TITLE", appConfig.getAppTitle());

		return "app-login";
	}

	@Autowired
	private AgentAuthProvider agentAuthProvider;

	@Autowired
	private AgentSessionService agentSessionService;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@ResponseBody
	@RequestMapping(value = "/auth/login/submit", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> login(@RequestParam String username,
			@RequestParam String password, HttpServletRequest request) {
		username = ArgUtil.parseAsString(username, Constants.BLANK);
		ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = restService.ajax(adminUrl).path("/auth/agent/login")
				.field("username", username).field("password", password).postForm()
				.as(new ParameterizedTypeReference<ApiResponse<Map<String, Object>, AgentResponseAuthDto>>() {
				});
		if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			AgentResponseAuthDto agent = x.getMeta();
			if (ArgUtil.is(agent)) {
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
						agent.getAgent_code(), password);
				token.setDetails(new WebAuthenticationDetails(request));
				Authentication authentication = agentAuthProvider.authenticate(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				agentSessionService.updateLogin(agent);
				stompTunnelSessionManager.registerUser(agent.getAgent_code(), agent.getDept().getDept_code(),
						PMStoreConstants.NO_DEPT);

				boolean rememberme = ArgUtil.parseAsBoolean(commonHttpRequest.get("rememberme"), false);
				if (rememberme) {
					String xRemSession = CryptoUtil.getEncoder()
							.obzect(MapBuilder.map().put("username", username).put("password", password).toMap())
							.encodeBase64().encrypt().toString();
					commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
				}
			}
		} else {
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	@ResponseBody
	@RequestMapping(value = "/auth/online/status", method = { RequestMethod.POST })
	public ApiResponse<AgentSessionDoc, Map<String, Object>> onlineStatus(
			@RequestParam(required = false) Boolean status) {
		BuilderMap meta = MapBuilder.map();
		if (ArgUtil.is(status)) {
			agentSessionService.setOnline(status.booleanValue());
			meta.put("isOnline", status);
		}
		return ApiResponse.buildResults(agentSessionService.getAgentSessions(), meta.toMap());
	}

}
