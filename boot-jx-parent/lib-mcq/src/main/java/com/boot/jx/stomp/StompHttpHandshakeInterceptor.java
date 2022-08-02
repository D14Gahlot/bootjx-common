package com.boot.jx.stomp;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.boot.jx.AppConstants;
import com.boot.jx.AppContextUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

public class StompHttpHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			HttpSession session = servletRequest.getServletRequest().getSession();
			if (!ArgUtil.isEmpty(session)) {
				String jSessionId = session.getId();
				String xSessionId = ArgUtil.parseAsString(session.getAttribute(AppConstants.SESSION_ID_XKEY),
						AppContextUtil.getSessionId(false));
				String uSessionId = ArgUtil.parseAsString(session.getAttribute(AppConstants.SESSION_UID_XKEY),
						Constants.BLANK);
				attributes.put(AppConstants.SESSION_ID_XKEY, xSessionId);
				attributes.put(AppConstants.SESSION_JID_XKEY, jSessionId);
				attributes.put(AppConstants.SESSION_UID_XKEY, uSessionId);
			}
		}
		return true;
	}

	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception ex) {
	}

}
