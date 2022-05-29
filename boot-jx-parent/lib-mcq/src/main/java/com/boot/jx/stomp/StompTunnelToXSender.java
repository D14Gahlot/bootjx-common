package com.boot.jx.stomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.core.MessageSendingOperations;

import com.boot.jx.AppParam;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.utils.ArgUtil;

@TunnelEventMapping(scheme = TunnelEventXchange.SHOUT_LISTNER, integrity = false)
@ConditionalOnProperty("app.stomp")
public class StompTunnelToXSender implements ITunnelSubscriber<StompTunnelEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static String getSendTopic(String prefix) {
		return prefix + "_STOMP_TO";
	}

	@Autowired(required = false)
	private StompTunnelSessionManager stompTunnelSessionManager;

	@Autowired(required = false)
	private MessageSendingOperations<String> messagingTemplate;

	@Override
	public String getTopic() {
		return getSendTopic(StompTunnelSessionManager.getMSInstanceId());
	}

	@Override
	public void onMessage(String channel, StompTunnelEvent msg) {

		if (ArgUtil.is(msg.getAppType()) && !ArgUtil.is(msg.getAppType(), AppParam.APP_TYPE.getValue())) {
			// Not for me
			return;
		}

		if (!ArgUtil.isEmpty(msg.getXsessionId())) {
			String sessionUId = stompTunnelSessionManager.getSessionUId(msg.getXsessionId(), msg.getJsessionId());
			if (!ArgUtil.isEmpty(sessionUId)) {
				messagingTemplate.convertAndSend("/queue/" + sessionUId + msg.getTopic(), msg.getData());
			} else {
				LOGGER.error("SessionUId is Missing for HttpSessionId:{}, Topic:{}", msg.getTopic());
			}
		} else {
			LOGGER.error("HttpSessionId is Missing for Topic:{}", msg.getTopic());
		}
	}

}
