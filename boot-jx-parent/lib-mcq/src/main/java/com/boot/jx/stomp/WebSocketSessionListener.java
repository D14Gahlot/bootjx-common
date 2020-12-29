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
	public static JsonPath ID_PATH = new JsonPath("/simpSessionAttributes/x-session-id");

	@Autowired
	StompTunnelSessionManager stompTunnelSessionManager;

	@EventListener
	public void connectionEstablished(SessionConnectedEvent sce) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(sce.getMessage());

		GenericMessage<?> simpConnectMessage = (GenericMessage<?>) sha.getHeader("simpConnectMessage");
		if (!ArgUtil.isEmpty(simpConnectMessage)) {
			String token = TOKEN_PATH.load(simpConnectMessage.getHeaders(), Constants.BLANK);
			String sessionId = ID_PATH.load(simpConnectMessage.getHeaders(), Constants.BLANK);
			logger.info("WS_CREATED http:{}, ws:{}, token:{}", sessionId, sha.getSessionId(), token);
		}
	}

	@EventListener
	public void webSockectDisconnect(SessionDisconnectEvent sde) {
		StompHeaderAccessor sha = StompHeaderAccessor.wrap(sde.getMessage());
		if (!ArgUtil.isEmpty(sha.getSessionAttributes())) {
			String httpSessionId = ArgUtil.parseAsString(sha.getSessionAttributes().get(AppConstants.SESSION_ID_XKEY));
			logger.info("WS_DESTROYED http:{}, ws:{}", sha.getSessionId(), httpSessionId);
			stompTunnelSessionManager.delinkWs2Http(httpSessionId, sha.getSessionId());
		}
	}

}