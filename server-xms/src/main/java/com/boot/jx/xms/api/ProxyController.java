package com.boot.jx.xms.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.xms.service.ProxyService;
import com.boot.model.MapModel;
import com.boot.utils.CryptoUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Proxy ViewConroller", description = "API's for CORS", hidden = true)
@RestController
public class ProxyController {

	@Autowired
	ProxyService service;

	@ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Try API's", hidden = true)
	@RequestMapping(value = { "/proxy/{domainHash}", "/proxy/{domainHash}/*", "/proxy/{domainHash}/**" },
			method = { RequestMethod.GET, RequestMethod.POST })
	public MapModel proxy(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response, @PathVariable String domainHash)
			throws URISyntaxException, MalformedURLException {
		String domain = CryptoUtil.getEncoder().message(domainHash).decodeBase64().toString();
		URL url = new URL(domain);
		String requestUrl = request.getRequestURI();
		String path = requestUrl.replaceFirst("/xms/proxy/" + domainHash + "/", "/");

		return MapModel
				.from(service.processProxyRequest(url.getHost(), path, body, method, request, response).getBody());
	}

}
