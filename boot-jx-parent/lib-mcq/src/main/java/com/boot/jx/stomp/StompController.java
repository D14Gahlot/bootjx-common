package com.boot.jx.stomp;

import java.io.Serializable;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConstants;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.scope.tnt.TenantContextHolder;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.jx.stomp.StompConfig.StompSession;
import com.boot.model.MapModel;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils;

@Controller
@ConditionalOnProperty("app.stomp")
public class StompController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StompController.class);

	@Autowired
	StompTunnelSessionManager stompTunnelSessionManager;

	@Autowired
	StompTunnelService stompTunnelService;

	@Autowired(required = false)
	TenantResolver tenantResolver;

	@ApiRequest(session = true)
	@SubscribeMapping("/stomp/tunnel/meta/{tnt}/{xSessionId}/{jSessionId}")
	public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable String tnt,
			@DestinationVariable String xSessionId, @DestinationVariable String jSessionId) {

		if (ArgUtil.is(tenantResolver)) {
			tnt = tenantResolver.resolve(tnt);
		}

		if (!StringUtils.isEmpty(tnt)) {
			TenantContextHolder.setCurrent(tnt, null);
		}

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
	@SubscribeMapping("/stomp/tunnel/meta/{xSessionId}/{jSessionId}")
	public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor, @DestinationVariable String xSessionId,
			@DestinationVariable String jSessionId) {

		String tnt = ArgUtil.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_TNT_XKEY));

		return meta(headerAccessor, tnt, xSessionId, jSessionId);
	}

	@ApiRequest(session = true)
	@SubscribeMapping("/stomp/tunnel/meta")
	public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor) {

		String tnt = ArgUtil.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_TNT_XKEY));

		String xSessionId = ArgUtil.parseAsString(
				headerAccessor.getSessionAttributes().get(AppConstants.SESSION_ID_XKEY), Constants.BLANK);

		String jSessionId = ArgUtil
				.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_JID_XKEY));

		return meta(headerAccessor, tnt, xSessionId, jSessionId);
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

	public static class StompSockPacket implements Serializable, JsonIgnoreNull, JsonIgnoreUnknown {
		private static final long serialVersionUID = 1L;
		public StompQuery filter;
		public Object payload;
	}

	@ResponseBody
	@RequestMapping(value = "/stomp/tunnel/send", method = { RequestMethod.POST })
	public ApiResponse<Object, Object> tunnelSend(@RequestBody StompSockPacket req) {
		stompTunnelService.sendTo(req.filter, req.payload);
		return ApiResponse.build();
	}

}
