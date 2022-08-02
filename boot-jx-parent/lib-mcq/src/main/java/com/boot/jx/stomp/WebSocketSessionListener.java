package com.boot.jx.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.boot.jx.AppConstants;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;

@Component
@ConditionalOnProperty("app.stomp")
public class WebSocketSessionListener {
	private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionListener.class.getName());

	public static JsonPath TOKEN_PATH = new JsonPath("/nativeHeaders/token/[0]");
	public static JsonPath SESSION_ID_PATH = new JsonPath("/simpSessionAttributes/x-session-id");
	public static JsonPath JSESSION_ID_PATH = new JsonPath("/simpSessionAttributes/x-jsession-id");

	public static JsonPath SESSION_ID_PATH_FALLBACK = new JsonPath("/nativeHeaders/xSessionId/[0]");
	public static JsonPath JSESSION_ID_PATH_FALLBACK = new JsonPath("/nativeHeaders/jSessionId/[0]");

	@Autowired
	StompTunnelSessionManager stompTunnelSessionManager;

	@EventListener
	public void connectionEstablished(SessionConnectedEvent sce) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(sce.getMessage());

		GenericMessage<?> simpConnectMessage = (GenericMessage<?>) sha.getHeader("simpConnectMessage");
		if (!ArgUtil.isEmpty(simpConnectMessage)) {
			String token = TOKEN_PATH.load(simpConnectMessage.getHeaders(), Constants.BLANK);
			String xSessionId = ArgUtil.anyOf(SESSION_ID_PATH.load(simpConnectMessage.getHeaders(), Constants.BLANK),
					SESSION_ID_PATH_FALLBACK.load(simpConnectMessage.getHeaders(), Constants.BLANK));
			String jSessionId = ArgUtil.anyOf(JSESSION_ID_PATH.load(simpConnectMessage.getHeaders(), Constants.BLANK),
					JSESSION_ID_PATH_FALLBACK.load(simpConnectMessage.getHeaders(), Constants.BLANK));
			logger.info("WS_CREATED xS:{}, jS:{}, wS:{}, token:{}", xSessionId, jSessionId, sha.getSessionId(), token);
		}
	}

	@EventListener
	public void webSockectDisconnect(SessionDisconnectEvent sde) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(sde.getMessage());
		if (!ArgUtil.isEmpty(sha.getSessionAttributes())) {
			String xSessionId = ArgUtil.parseAsString(sha.getSessionAttributes().get(AppConstants.SESSION_ID_XKEY));
			String jSessionId = ArgUtil.parseAsString(sha.getSessionAttributes().get(AppConstants.SESSION_JID_XKEY));
			logger.info("WS_DESTROYED  xS:{}, jS:{}, wS:{}", xSessionId, jSessionId, sha.getSessionId());
			if (ArgUtil.is(xSessionId)) {
				try {
					stompTunnelSessionManager.delinkWs2Http(xSessionId, jSessionId, sha.getSessionId());
				} catch (Exception e) {
					logger.error("WS_DESTROY_EXCEPTION xS:{}, jS:{}, wS:{}", xSessionId, jSessionId,
							sha.getSessionId());
				}
			}
		}
	}

}