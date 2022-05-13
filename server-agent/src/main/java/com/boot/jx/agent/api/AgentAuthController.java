package com.boot.jx.agent.api;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelSessionManager;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
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

	@Autowired
	private EmpAuthService authService;

	private boolean isAgentPanelActive() {
		// return true;
		return pmEnvironment.keyEntry("mry.domain.active").asBoolean()
				&& pmEnvironment.keyEntry("mry.domain.agent.active").asBoolean();
	}

	@RequestMapping(value = { "/app/unauthorized", "/app/unauthorized/**" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public String unauthorized(Model model) {
		model.addAllAttributes(appCommonConfig.appAttributes());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		return "app-unauthorized";
	}

	public AgentResponseAuthDto loginFromXToken(HttpServletRequest request, HttpServletResponse response,
			String xRemSession) throws NoSuchAlgorithmException {
		@SuppressWarnings("unchecked")
		MapModel map = MapModel
				.from(CryptoUtil.getEncoder().message(xRemSession).decrypt().decodeBase64().toObzect(Map.class));

		String username = map.getString("username");
		String password = map.getString("password");

		if (ArgUtil.is(username) && ArgUtil.is(password)) {
			ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = this.login(map.getString("username"),
					map.getString("password"), request);
			if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
				if (ArgUtil.is(x.getMeta())) {
					if (ArgUtil.is(x.getRedirectUrl())) {
						response.setHeader("Location", appConfig.getAppPrefix() + "/app/home");
						response.setStatus(302);
					}
				}
				return x.getMeta();
			}
		}

		String domainUser = map.getString("domainUser");
		String domainName = map.getString("domainName");
		String domainId = map.getString("domainId");
		String domainToken = map.getString("domainToken");

		if (ArgUtil.is(domainToken)) {
			AgentResponseAuthDto agent = authService.loginByDomainToken(domainUser, domainName, domainId, domainToken,
					false);
			if (ArgUtil.is(agent)) {
				sessionService.login(request, agent, domainToken);
				commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
				response.setHeader("Location", appConfig.getAppPrefix() + "/app/home");
				response.setStatus(302);
			}
			return agent;
		}
		return null;
	}

	private String toHomePage() {
		return "redirect:" + appConfig.getAppPrefix() + "/app/home" + "?_=" + System.currentTimeMillis();
	}

	@RequestMapping(value = { "/app/home", "/", "", "/app/**", "/auth/**" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public String home(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam(required = false) String domainName, @RequestParam(required = false) String domainId,
			@RequestParam(required = false) String domainUser, @RequestParam(required = false) String domainToken,
			@RequestParam(required = false) String domainTokenValid) throws NoSuchAlgorithmException {

		if (!isAgentPanelActive()) {
			return unauthorized(model);
		}

		model.addAttribute("APP_PLUG", ArgUtil.nonEmpty(commonHttpRequest.getRequestParam("plug"), "none"));
		String xRemSession = ArgUtil.parseAsString(commonHttpRequest.get("JXSESSIONID"), Constants.BLANK);

		if (ArgUtil.is(domainName) && ArgUtil.is(domainId) && ArgUtil.is(domainToken)) {
			AgentResponseAuthDto agent = authService.loginByDomainToken(domainUser, domainName, domainId, domainToken,
					false);

			if (ArgUtil.is(agent)) {
				sessionService.login(request, agent, domainToken);
				xRemSession = CryptoUtil.getEncoder()
						.obzect(MapBuilder.map().put("domainUser", domainUser).put("domainName", domainName)
								.put("domainId", domainId).put("domainToken", domainToken).toMap())
						.encodeBase64().encrypt().toString();
				commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
				return toHomePage();
			}
			if (!ArgUtil.is(domainTokenValid)) {
				model.addAllAttributes(appCommonConfig.appAttributes());
				model.addAttribute("FORM_URL", "/agent/auth/login/direct?_=" + System.currentTimeMillis());
				model.addAttribute("DOMAIN_USER", domainUser);
				model.addAttribute("DOMAIN_NAME", domainName);
				model.addAttribute("DOMAIN_ID", domainId);
				model.addAttribute("DOMAIN_TOKEN", domainToken);
				model.addAttribute("DOMAIN_TOKEN_VALID", domainToken);
				return "app-goto";
			}
		} else if (!agentSession.isLoggedIn() && ArgUtil.is(xRemSession)) {
			AgentResponseAuthDto agent = loginFromXToken(request, response, xRemSession);
			if (ArgUtil.is(agent)) {
				return toHomePage();
			} else {
				commonHttpRequest.deleteCookie("JXSESSIONID");
			}
		}

		if (!ArgUtil.is(agentSession.getAgentCode())) {
			return "redirect:/auth/logout";
		}

		model.addAllAttributes(appCommonConfig.appAttributes());

		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());

		return "app-agent";
	}

	@RequestMapping(value = { "/plug/**", "/plug", "/plug_{plug}/**", "/plug_{plug}" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public String plugOlin(HttpServletRequest request, Model model, @PathVariable(required = false) String plug)
			throws NoSuchAlgorithmException {

		model.addAttribute("APP_PLUG", ArgUtil.nonEmpty(plug, commonHttpRequest.getRequestParam("plug"), "plug"));

		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "none");
		String username = commonHttpRequest.get("username");
		String password = commonHttpRequest.get("password");
		boolean rememberme = ArgUtil.parseAsBoolean(commonHttpRequest.get("rememberme"), false);
		String jxSessionId = ArgUtil.parseAsString(commonHttpRequest.get("JXSESSIONID"), Constants.BLANK);

		if ("login".equals(action)) {
			ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = authService.empLogin(username, password, false);
			if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
				AgentResponseAuthDto agent = x.getMeta();
				if (ArgUtil.is(agent)) {
					sessionService.login(request, agent, password);
					commonHttpRequest.setCookie("plug", plug);
					if (rememberme) {
						String xRemSession = CryptoUtil.getEncoder()
								.obzect(MapBuilder.map().put("username", username).put("password", password).toMap())
								.encodeBase64().encrypt().toString();
						commonHttpRequest.setCookie("JXSESSIONID", xRemSession);
					}
				}
			}
		}

		if (!agentSession.isLoggedIn() && ArgUtil.is(jxSessionId)) {
			@SuppressWarnings("unchecked")
			MapModel map = MapModel
					.from(CryptoUtil.getEncoder().message(jxSessionId).decrypt().decodeBase64().toObzect(Map.class));
			ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = authService.empLogin(map.getString("username"),
					map.getString("password"), false);
			if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
				AgentResponseAuthDto agent = x.getMeta();
				if (ArgUtil.is(agent)) {
					sessionService.login(request, agent, password);
				}
			}
		}

		model.addAllAttributes(appCommonConfig.appAttributes());
		if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
			model.addAttribute("APP_USER", agentSession.getAgentCode());
			model.addAttribute("APP_DEPT", agentSession.getAgentDept());
			return "app-agent";
		}
		return "app-agent-plugin";
	}

//	@RequestMapping(value = { "/plug_mitel/**", "/plug_mitel" }, method = { RequestMethod.POST, RequestMethod.GET })
//	public String plugOlinMitle(HttpServletRequest request, Model model, @PathVariable(required = false) String plug)
//			throws NoSuchAlgorithmException {
//		return this.plugOlin(request, model, "mitle");
//	}

	@RequestMapping(value = "/pub/customer/{page}", method = { RequestMethod.POST, RequestMethod.GET })
	public String customertest(Model model, @RequestParam String page) {
		model.addAllAttributes(appCommonConfig.appAttributes());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("POSTMAN_CONTEXT", "/postman");
		return "customer." + page;
	}

	@RequestMapping(value = { "/auth/login", "/auth/resetpass" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model, HttpServletRequest request, HttpServletResponse httpServletResponse) {

		if (!isAgentPanelActive()) {
			return unauthorized(model);
		}

		model.addAttribute("APP_PLUG", ArgUtil.nonEmpty(commonHttpRequest.getRequestParam("plug"), "none"));
		model.addAllAttributes(appCommonConfig.appAttributes());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());

		String page = ArgUtil.parseAsString(commonHttpRequest.get("page"), "login");
		String action = ArgUtil.parseAsString(commonHttpRequest.get("action"), "login");
		String status = Constants.BLANK;
		Object message = Constants.BLANK;
		try {
			if ("resetpass".equalsIgnoreCase(action)) {
				String username = ArgUtil.parseAsString(commonHttpRequest.get("username"), Constants.BLANK);
				ApiResponse<Map<String, Object>, String> x = authService.agentResetPass(username, false);
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
								newpassword, false);
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
					AgentResponseAuthDto agent = loginFromXToken(request, httpServletResponse, xRemSession);
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
	private AgentAuthProvider agentAuthProvider;

	@Autowired
	private AgentSessionService sessionService;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@ResponseBody
	@RequestMapping(value = "/auth/login/submit", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> login(@RequestParam String username,
			@RequestParam String password, HttpServletRequest request) throws NoSuchAlgorithmException {
		username = ArgUtil.parseAsString(username, Constants.BLANK);
		ApiResponse<Map<String, Object>, AgentResponseAuthDto> x = authService.empLogin(username, password, false);
		if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			AgentResponseAuthDto agent = x.getMeta();
			if (ArgUtil.is(agent)) {
				sessionService.login(request, agent, password);
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
			sessionService.setOnline(status.booleanValue());
			meta.put("isOnline", status);
		}
		return ApiResponse.buildResults(sessionService.getAgentSessions(), meta.toMap());
	}

}
