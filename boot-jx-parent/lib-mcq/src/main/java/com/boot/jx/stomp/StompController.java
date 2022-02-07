package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConstants;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Controller
@ConditionalOnProperty("app.stomp")
public class StompController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StompTunnelSessionManager.class);

    @Autowired
    StompTunnelSessionManager stompTunnelSessionManager;

    @Autowired
    StompTunnelService stompTunnelService;

    @SubscribeMapping("/stomp/tunnel/meta")
    public Map<String, Object> meta(SimpMessageHeaderAccessor headerAccessor) {
	Map<String, Object> map = new HashMap<String, Object>();

	String httpsSessionId = ArgUtil
		.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_ID_XKEY));

	if (!ArgUtil.is(httpsSessionId)) {
	    LOGGER.warn("httpsSessionId is Empty");
	    return map;
	}

	StompSession stompSession = stompTunnelSessionManager.getStompSessionByHttpSessionId(httpsSessionId);

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
		headerAccessor.getSessionId(), httpsSessionId,
		ArgUtil.parseAsString(headerAccessor.getSessionAttributes().get(AppConstants.SESSION_UID_XKEY))));

	return map;
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
	map.put("message", "Hey baby ping pong!");
	stompTunnelService.sendToAll("/stomp/tunnel/pong",
		MapModel.createInstance().put("message", "sendToAll/ping_pong").toMap());
	stompTunnelService.sendToTag(StompQuery.PING_TAG, "/stomp/tunnel/pong",
		MapModel.createInstance().put("message", "sendToTag:ping_tag/ping_pong").toMap());
	return map;
    }
}
