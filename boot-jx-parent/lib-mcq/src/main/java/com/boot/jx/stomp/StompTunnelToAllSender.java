package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.StringUtils;

@TunnelEventMapping(topic = StompTunnelToAllSender.STOMP_TO_ALL, scheme = TunnelEventXchange.SHOUT_LISTNER,
		integrity = false)
@ConditionalOnProperty("app.stomp")
public class StompTunnelToAllSender implements ITunnelSubscriber<StompTunnelEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StompTunnelToAllSender.class);

	public static final String STOMP_TO_ALL = "STOMP_TO_ALL";

	@Autowired(required = false)
	private MessageSendingOperations<String> messagingTemplate;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@Override
	public void onMessage(String channel, StompTunnelEvent msg) {

		if (ArgUtil.is(msg.getAppType()) && !ArgUtil.is(msg.getAppType(), AppParam.APP_TYPE.getValue())) {
			// Not for me
			return;
		}

		if (!ArgUtil.isEmpty(messagingTemplate)) {

			if (ArgUtil.is(msg.getTagId())) {
				String[] tagIds = StringUtils.split(msg.getTagId(), ",");
				for (String tagId : tagIds) {
					messagingTemplate.convertAndSend("/tag/" + (msg.getTenantToken() + "/" + tagId) + msg.getTopic(),
							msg.getData(), StompTunnelToXSender.getHeaders(msg).toMap());
				}
			} else {
				messagingTemplate.convertAndSend("/topic/" + msg.getTenantToken() + msg.getTopic(), msg.getData(),
						StompTunnelToXSender.getHeaders(msg).toMap());
			}

		}
	}

	public void sendToAll(String topic, Object message) {
		try {
			StompTunnelEvent event = StompTunnelEvent.createInstance();
			event.setTopic(topic);
			event.setTenantToken(stompTunnelSessionManager.createTagId(AppContextUtil.getTenant()));

			Map<String, Object> messageData = new HashMap<String, Object>();
			messageData.put("data", message);
			event.setData(JsonUtil.toJsonMap(messageData));
			onMessage(StompTunnelToAllSender.STOMP_TO_ALL, event);
		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage", e);
		}
	}

	@Scheduled(fixedDelay = 30000)
	public void healthMessage() {
		sendToAll("/stomp/tunnel/health", MapModel.createInstance().put("message", "Happy Ping-Pong").toMap());
	}

}
