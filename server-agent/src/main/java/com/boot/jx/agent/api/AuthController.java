package com.boot.jx.agent.api;

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
import com.boot.jx.agent.AgentAuthProvider;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.api.ApiResponse;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Controller
public class AuthController {

	@Autowired
	private AppConfig appConfig;

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

	@RequestMapping(value = "/app/home", method = { RequestMethod.POST, RequestMethod.GET })
	public String home() {
		return "whatsweb";
	}

	@RequestMapping(value = "/auth/login", method = { RequestMethod.POST, RequestMethod.GET })
	public String login(Model model) {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		return "login";
	}

	@Autowired
	private AgentAuthProvider agentAuthProvider;

	@Autowired
	private AgentSessionBean agentSession;

	@ResponseBody
	@RequestMapping(value = "/auth/login/submit", method = { RequestMethod.POST })
	public ApiResponse<String, String> login(@RequestParam String username, @RequestParam String password,
			HttpServletRequest request) {
		ApiResponse<String, String> x = ApiResponse.buildData("success", "success");

		if (ArgUtil.isEqual(username, password) && username.startsWith("agent")) {
			x.redirectUrl(appConfig.getAppPrefix() + "/app/home");
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
			token.setDetails(new WebAuthenticationDetails(request));
			Authentication authentication = agentAuthProvider.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			agentSession.setLoggedIn(true);
			agentSession.setOnline(true);
			agentSession.setAgentCode(username);
			agentSession.setLastOnlineStamp(System.currentTimeMillis());
			agentSession.update();
		} else {
			x.setData("error");
			x.setMeta("error");
			x.redirectUrl(appConfig.getAppPrefix() + "/auth/login?error");
		}

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
