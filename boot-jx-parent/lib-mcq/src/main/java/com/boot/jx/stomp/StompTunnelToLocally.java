package com.boot.jx.stomp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.AppParam;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.StringUtils;

@Component
@ConditionalOnProperty("app.stomp")
public class StompTunnelToLocally {

	private static final Logger LOGGER = LoggerFactory.getLogger(StompTunnelToLocally.class);

	@Autowired(required = false)
	private MessageSendingOperations<String> messagingTemplate;

	public void sendToAll(StompTunnelEvent msg) {

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
			event.setTenantToken("*");

			Map<String, Object> messageData = new HashMap<String, Object>();
			messageData.put("data", message);
			messageData.put("timetamp", System.currentTimeMillis());
			event.setData(JsonUtil.toJsonMap(messageData));
			sendToAll(event);
		} catch (Exception e) {
			LOGGER.error("Error While Sending StompMessage", e);
		}
	}

	@Scheduled(fixedDelay = 30000, initialDelay = 30000)
	public void healthMessage() {
		sendToAll("/stomp/tunnel/health", MapModel.createInstance().put("message", "Happy Ping-Pong")
				.put("timetamp", System.currentTimeMillis()).toMap());
	}
}
