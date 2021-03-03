package com.boot.jx.agent.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.boot.jx.agent.AgentAuthProvider;
import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.rest.RestService;
import com.boot.jx.stomp.StompTunnelSessionManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Controller
public class AuthController {

	@Value("${mry.cdn.url}")
	private String cdnServer;

	@Value("${mry.admin.url}")
	private String adminUrl;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private AgentChatHandlerImpl agentChatHandler;

	@Autowired
	private RestService restService;

	@ResponseBody
	@RequestMapping(value = "/pub/test", method = { RequestMethod.POST, RequestMethod.GET })
	public SampleSenderReply postVote(@RequestParam String xyz) {
		return new SampleSenderReply();
	}

	@ResponseBody
	@RequestMapping(value = "/api/test", method = { RequestMethod.POST, RequestMethod.GET })
	public SampleSenderReply apiVote(@RequestParam String xyz) {
		return new SampleSenderReply();
	}

	@RequestMapping(value = { "/app/home", "/", "", "/app/**" }, method = { RequestMethod.POST, RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), cdnServer));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));

		String cdnnew = ArgUtil.parseAsString(commonHttpRequest.get("CDN_NEW"), "true");

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
		return "whatsweb";
	}

	@RequestMapping(value = "/pub/customer/{page}", method = { RequestMethod.POST, RequestMethod.GET })
	public String customertest(Model model, @RequestParam String page) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		model.addAttribute("POSTMAN_CONTEXT", "/postman");
		return "customer." + page;
	}

	@RequestMapping(value = "/auth/login", method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model) {
		model.addAttribute("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), cdnServer));
		model.addAttribute("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("APP_USER", agentSession.getAgentCode());
		model.addAttribute("APP_DEPT", agentSession.getAgentDept());
		return "login";
	}

	@Autowired
	private AgentAuthProvider agentAuthProvider;

	@Autowired
	private AgentSessionService agentSessionService;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@ResponseBody
	@RequestMapping(value = "/auth/login/submit", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, String> login(@RequestParam String username, @RequestParam String password,
			HttpServletRequest request) {
		ApiResponse<Map<String, Object>, String> x = restService.ajax(adminUrl).path("/auth/agent/login")
				.field("username", username).field("password", password).postForm()
				.as(new ParameterizedTypeReference<ApiResponse<Map<String, Object>, String>>() {
				});
		if (ArgUtil.parseAsBoolean(x.getData().get("success"), false)) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = agentAuthProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			agentSessionService.updateLogin(username);
			stompTunnelSessionManager.registerUser(username);
		} else {
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}
		return x;
	}

	@ResponseBody
	@RequestMapping(value = "/auth/online/status", method = { RequestMethod.POST })
	public ApiResponse<String, Object> onlineStatus(@RequestParam boolean status) {
		ApiResponse<String, Object> x = ApiResponse.buildData("status", status);
		agentSessionService.setOnline(status);
		return x;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SampleSenderReply {
		Map<String, String> channel;
		public SampleSender sender;

		public Map<String, String> getChannel() {
			return channel;
		}

		public void setChannel(Map<String, String> channel) {
			this.channel = channel;
		}
	}

	public static class SampleSender {
		public SampleName name;
	}

	public static class SampleName {
		public String nameType;
		public String firstName;
		public String lastName;
	}

}
