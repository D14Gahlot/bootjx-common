package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
public class StompTunnelService {

	public static Logger LOGGER = LoggerService.getLogger(StompTunnelService.class);

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@Async
	public void sendToAll(String topic, Object message) {
		try {
			StompTunnelEvent event = StompTunnelEvent.createInstance();
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
	public void sendToTag(String tag, String topic, Object message) {
		try {
			if (!ArgUtil.is(tag)) {
				return;
			}
			StompTunnelEvent event = StompTunnelEvent.createInstance();
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
			StompTunnelEvent event = StompTunnelEvent.createInstance();
			event.setTopic(topic);
			event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));

			StompSession stompSession = stompTunnelSessionManager.getStompSession(stompUID);
			if (!ArgUtil.isEmpty(stompSession)) {
				event.setXsessionId(stompSession.getXsessionId());
				event.setJsessionId(stompSession.getJsessionId());
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

	@Async
	public void sendTo(StompQuery stompQuery, Object message) {
		try {

			// To One Users
			if (ArgUtil.is(stompQuery.getStompUID())) {
				this.sendTo(stompQuery.getStompUID(), stompQuery.getTopic(), message);
			}

			// To Multiple Tags
			if (ArgUtil.is(stompQuery.getTags())) {

				StompTunnelEvent event = StompTunnelEvent.createInstance();
				event.setTopic(stompQuery.getTopic());
				event.setAppType(stompQuery.getAppType());
				event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));

				// Tags
				StringJoiner sb = new StringJoiner(",");
				for (String tag : stompQuery.getTags()) {
					sb.add(stompTunnelSessionManager.createTagId(tag));
				}
				event.setTagId(sb.toString());
				//// Tags

				Map<String, Object> messageData = new HashMap<String, Object>();
				messageData.put("data", message);
				event.setData(JsonUtil.toJsonMap(messageData));
				tunnelService.shout(StompTunnelToAllSender.STOMP_TO_ALL, event);
			}

			if (stompQuery.isShout()) {
				StompTunnelEvent event = StompTunnelEvent.createInstance();
				event.setOriginator(AppParam.APP_INSTANCE_UID.getValue());
				event.setTopic(stompQuery.getTopic());
				event.setAppType(stompQuery.getAppType());
				event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));

				Map<String, Object> messageData = new HashMap<String, Object>();
				messageData.put("data", message);
				event.setData(JsonUtil.toJsonMap(messageData));
				tunnelService.shout(StompTunnelToAllSender.STOMP_TO_ALL, event);
			}

		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage", e);
		}
	}

}
