package com.boot.jx.agent.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.ProxyService;
import com.boot.jx.http.RequestType;
import com.boot.model.MapModel;
import com.boot.utils.CryptoUtil;

import io.swagger.annotations.ApiOperation;

@Controller
public class AgentProxyController {

	// private final RestTemplate restTemplate;
	@Autowired
	private ProxyService service;

	@Value("${mry.nexus.url}")
	private String nexusUrl;

	@CrossOrigin(origins = "*")
	@ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Try API's", hidden = true)

	@RequestMapping(value = { "/nexus/{pathHash}" })
	public MapModel proxch(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response, @PathVariable String domainHash, @PathVariable String pathHash)
			throws URISyntaxException, MalformedURLException {
		// String domain =
		// CryptoUtil.getEncoder().message(domainHash).decodeBase64Hack().toString();
		// URL url = new URL(domain);
		String path = CryptoUtil.getEncoder().message(pathHash).decodeBase64Hack().toString();

		return MapModel
				.fromSafe(service.processProxyRequest(nexusUrl, path, body, method, request, response).getBody());
	}

}
