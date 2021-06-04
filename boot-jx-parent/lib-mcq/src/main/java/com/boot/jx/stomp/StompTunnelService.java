package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
public class StompTunnelService {

	public static Logger LOGGER = LoggerService.getLogger(StompTunnelService.class);

	@Autowired
	TunnelService tunnelService;

	@Autowired
	StompTunnelSessionManager stompTunnelSessionManager;

	@Async
	public void sendToAll(String topic, Object message) {
		try {
			StompTunnelEvent event = new StompTunnelEvent();
			event.setTopic(topic);
			event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));
			
			Map<String, Object> messageData = new HashMap<String, Object>();
			messageData.put("data", message);
			event.setData(JsonUtil.toJsonMap(messageData));
			tunnelService.shout(StompTunnelToAllSender.STOMP_TO_ALL, event);
		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage", e);
		}
	}

	@Async
	public void sendToTag(String topic, Object message, String tag) {
		try {
			StompTunnelEvent event = new StompTunnelEvent();
			event.setTopic(topic);
			event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));
			event.setTagId(stompTunnelSessionManager.createTagId(tag));
			
			Map<String, Object> messageData = new HashMap<String, Object>();
			messageData.put("data", message);
			event.setData(JsonUtil.toJsonMap(messageData));
			tunnelService.shout(StompTunnelToAllSender.STOMP_TO_ALL, event);
		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage", e);
		}
	}

	/**
	 * This method will work only if
	 * {@link StompTunnelSessionManager#mapHTTPSession(stompUID, String)} has been
	 * called already for the session
	 * 
	 * @param stompUID - UNIQUE ID to Identify End User
	 * @param topic
	 * @param message
	 */
	@Async
	public void sendTo(String stompUID, String topic, Object message) {
		try {
			if (!ArgUtil.is(stompUID)) {
				LOGGER.error("stompSession for stompUID {} cannot be empty for {}", stompUID, topic);
				return;
			}
			StompTunnelEvent event = new StompTunnelEvent();
			event.setTopic(topic);
			event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));
			
			StompSession stompSession = stompTunnelSessionManager.getStompSession(stompUID);
			if (!ArgUtil.isEmpty(stompSession)) {
				event.setHttpSessionId(stompSession.getHttpSessionId());
				Map<String, Object> messageData = new HashMap<String, Object>();
				messageData.put("data", message);
				event.setData(JsonUtil.toJsonMap(messageData));
				tunnelService.shout(StompTunnelToXSender.getSendTopic(stompSession.getPrefix()), event);
			} else {
				LOGGER.error("stompSession for stompUID {}  not found to send on topic {}", stompUID, topic);
			}
		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage to stompUID " + stompUID, e);
		}
	}

}
