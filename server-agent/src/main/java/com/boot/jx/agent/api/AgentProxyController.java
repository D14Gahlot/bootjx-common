package com.boot.jx.agent.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppContextUtil;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.ProxyService;
import com.boot.jx.http.RequestType;
import com.boot.model.MapModel;

import io.swagger.annotations.ApiOperation;

@Controller
public class AgentProxyController {

	// private final RestTemplate restTemplate;
	@Autowired
	private ProxyService service;

	@Value("${mry.nexus.url}")
	private String nexusUrl;

	@Autowired
	private AgentSessionBean agentSession;

	@CrossOrigin(origins = "*")
	@ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "ProxyAPI")
	@RequestMapping(value = { "/nexus/**" })
	@ResponseBody
	public MapModel proxch(@RequestBody(required = false) String body, HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		// String domain =
		// CryptoUtil.getEncoder().message(domainHash).decodeBase64Hack().toString();
		// URL url = new URL(domain);

		Map<String, String> addHeaders = new HashMap<String, String>();
		addHeaders.put("x-agent-code", agentSession.getAgentCode());
		addHeaders.put("x-agent-user", agentSession.getAuthUser());
		addHeaders.put("tnt", AppContextUtil.getTenant());

		return MapModel
				.fromSafe(service.forwardRequest("/nexus/", nexusUrl, body, addHeaders, request, response).getBody());
	}

	@CrossOrigin(origins = "*")
	@ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Only for test")
	@RequestMapping(value = { "/pub/nexus/**" })
	@ResponseBody
	public MapModel proxch2(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		// String domain =
		// CryptoUtil.getEncoder().message(domainHash).decodeBase64Hack().toString();
		// URL url = new URL(domain);

		Map<String, String> addHeaders = new HashMap<String, String>();
		addHeaders.put("x-agent-code", agentSession.getAgentCode());
		addHeaders.put("x-agent-user", agentSession.getAuthUser());
		addHeaders.put("tnt", AppContextUtil.getTenant());

		return MapModel.fromSafe(
				service.forwardRequest("/pub/nexus/", nexusUrl, body, addHeaders, request, response).getBody());
	}

}
