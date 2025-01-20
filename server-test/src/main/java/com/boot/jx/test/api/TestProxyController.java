package com.boot.jx.test.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppContextUtil;
import com.boot.jx.http.ProxyService;
import com.boot.jx.logger.LoggerService;
import com.boot.model.MapModel;

import io.swagger.annotations.ApiOperation;

@Controller
public class TestProxyController {

	private static final Logger LOGGER = LoggerService.getLogger(TestProxyController.class);

	// private final RestTemplate restTemplate;
	@Autowired
	private ProxyService service;

	@Value("${mry.nexus.url}")
	private String nexusUrl;

	@Value("${mry.scriptus.url}")
	private String scriptusUrl;

	private Map<String, String> addHeaders(Map<String, String> headers) {
		headers.put("tnt", AppContextUtil.getTenant());
		return headers;
	}

	@CrossOrigin(origins = "*")
	// @ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "ProxyAPI")
	@RequestMapping(value = { "/nexus/**" })
	@ResponseBody
	public MapModel proxch(@RequestBody(required = false) String body, HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		// String domain =
		// CryptoUtil.getEncoder().message(domainHash).decodeBase64Hack().toString();
		// URL url = new URL(domain);

		Map<String, String> additioalHeaders = addHeaders(new HashMap<String, String>());

		return MapModel.fromSafe(service
				.forwardRequestNoRetry("/nexus/", nexusUrl, body, additioalHeaders, request, response).getBody());
	}

	@CrossOrigin(origins = "*")
	// @ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Only for test")
	@RequestMapping(value = { "/pub/nexus/**" })
	@ResponseBody
	public MapModel proxch2(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		// String domain =
		// CryptoUtil.getEncoder().message(domainHash).decodeBase64Hack().toString();
		// URL url = new URL(domain);

		Map<String, String> additioalHeaders = addHeaders(new HashMap<String, String>());

		return MapModel.fromSafe(service
				.forwardRequestNoRetry("/pub/nexus/", nexusUrl, body, additioalHeaders, request, response).getBody());
	}

	@CrossOrigin(origins = "*")
	// @ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Only for test")
	@RequestMapping(value = { "/pub/bot/**" })
	@ResponseBody
	public MapModel proxch2ForBot(@RequestBody(required = false) String body, HttpMethod method,
			HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		Map<String, String> additioalHeaders = addHeaders(new HashMap<String, String>());
		return MapModel.fromSafe(service
				.forwardRequestNoRetry("/pub/bot/", scriptusUrl, body, additioalHeaders, request, response).getBody());
	}

	@CrossOrigin(origins = "*")
	// @ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Only for test")
	@RequestMapping(value = { "/pub/scriptus/pub/**" })
	@ResponseBody
	public MapModel proxch2ForScriptus(@RequestBody(required = false) String body, HttpMethod method,
			HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, MalformedURLException {
		Map<String, String> additioalHeaders = addHeaders(new HashMap<String, String>());
		return MapModel.fromSafe(service
				.forwardRequestNoRetry("/pub/", scriptusUrl, body, additioalHeaders, request, response).getBody());
	}

}
