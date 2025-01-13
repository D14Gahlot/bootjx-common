package com.boot.jx.xms.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppContextUtil;
import com.boot.jx.http.ProxyService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Native API's Proxy", description = "Use Native Structure for Channel API's")
@Controller
public class NativeProxyController {

	private static final Logger LOGGER = LoggerService.getLogger(NativeProxyController.class);

	// private final RestTemplate restTemplate;
	@Autowired
	private ProxyService service;

	@Autowired
	private PMEnvironment pmEnvironment;

	private Map<String, String> addHeaders(Map<String, String> headers, ChannelConfig channel) {
		return headers;
	}

	@CrossOrigin(origins = "*")
	// @ApiRequest(type = RequestType.NO_TRACK_PING)
	@ApiOperation(value = "Native proxy API", notes = "${swagger.OutboundApiV1.sendMessage.description}",
			authorizations = @Authorization("X_API_KEY"))
	@RequestMapping(value = { "/native/{channelid}/**" },
			method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
	@XMSClientAuth
	@ResponseBody
	public MapModel proxch(@RequestBody(required = false) String body, HttpServletRequest request,
			HttpServletResponse response, @PathVariable(required = false) String channelId)
			throws URISyntaxException, MalformedURLException {

		String destUrl = "https://google.com";
		ChannelConfig channel = pmEnvironment.config().channel(channelId);
		Map<String, String> additioalHeaders = addHeaders(new HashMap<String, String>(), channel);
		String channelType = channel.getChannelType();

		switch (channelType) {
		case CHANNEL_TYPE.WACFB:
			if (ArgUtil.is(channel.getWacfb()))
				destUrl = WA360Constants.META_WA_CLOUD_URL;
			additioalHeaders.put("Authorization", "Bearer " + channel.getWacfb().getAccessToken());
			break;
		default:
			additioalHeaders.put("tnt", AppContextUtil.getTenant());
			break;
		}

		return MapModel.fromSafe(service
				.forwardRequestNoRetry("/native/" + channelId, destUrl, body, additioalHeaders, request, response)
				.getBody());
	}

}
