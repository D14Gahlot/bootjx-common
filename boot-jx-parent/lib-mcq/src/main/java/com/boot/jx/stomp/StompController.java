package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConstants;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Controller
@ConditionalOnProperty("app.stomp")
public class StompController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StompTunnelSessionManager.class);

	@Autowired
	StompTunnelSessionManager stompTunnelSessionManager;

	@Autowired
	StompTunnelService stompTunnelService;

	@ApiRequest(session = true)
	@SubscribeMapping("/stomp/tunnel/meta/{xSessionId}/{jSessionId}")
	public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable String xSessionId,
			@DestinationVariable String jSessionId) {
		Map<String, Object> map = new HashMap<String, Object>();

		if (!ArgUtil.is(xSessionId) && !ArgUtil.is(jSessionId)) {
			LOGGER.warn("xSessionId/jSessionId is Empty");
			return map;
		}

		if (ArgUtil.is(xSessionId)) {
			map.put(AppConstants.SESSION_ID_XKEY, xSessionId);
		}

		if (ArgUtil.is(jSessionId)) {
			map.put(AppConstants.SESSION_JID_XKEY, jSessionId);
		}

		StompSession stompSession = stompTunnelSessionManager.getStompSessionByHttpSessionId(xSessionId, jSessionId);

		if (ArgUtil.is(stompSession)) {
			if (ArgUtil.is(stompSession.getTags())) {
				map.put("tags", stompSession.getTags());
			}
			if (ArgUtil.is(stompSession.getTenantToken())) {
				map.put("x-tenant-token", stompSession.getTenantToken());
			}
		} else {
			LOGGER.warn("stompSession is Empty");
		}

		map.put(AppConstants.SESSION_UID_XKEY, stompTunnelSessionManager.createSessionMapping(
				headerAccessor.getSessionId(), xSessionId, jSessionId,
				ArgUtil.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_UID_XKEY))));

		return map;
	}

	@ApiRequest(session = true)
	@SubscribeMapping("/stomp/tunnel/meta")
	public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor) {
		String xSessionId = ArgUtil.parseAsString(
				headerAccessor.getSessionAttributes().get(AppConstants.SESSION_ID_XKEY), Constants.BLANK);

		String jSessionId = ArgUtil
				.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_JID_XKEY));

		return meta(headerAccessor, xSessionId, jSessionId);
	}

	@MessageMapping("/ping")
	public Map<String, String> ping(SimpMessageHeaderAccessor headerAccessor) throws InterruptedException {
		Thread.sleep(1000); // simulated delay
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", "Hey baby ping pong!");
		stompTunnelService.sendToAll("/pong", map);
		return map;
	}

	@ResponseBody
	@RequestMapping("/stomp/tunnel/ping")
	public Map<String, String> tunnelPing() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", "Hey baby ping pong!!");
		stompTunnelService.sendToAll("/stomp/tunnel/pong",
				MapModel.createInstance().put("message", "sendToAll/ping_pong").toMap());
		stompTunnelService.sendToTag(StompQuery.PING_TAG, "/stomp/tunnel/pong",
				MapModel.createInstance().put("message", "sendToTag:ping_tag/ping_pong").toMap());
		return map;
	}
}
