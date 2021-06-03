package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatClient;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.TimeUtils;

@Component
public class AppCommonConfig {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private ChatClient chatClient;

	@Value("${mry.cdn.url}")
	private String cdnServer;

	public String getCdnServer() {
		return pmEnvironment.config().get("mry.cdn.url").asString(cdnServer);
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("idleChatTimeout", 1000 * 60 * 5);
		map.put("agentSessionTimeout", TimeUtils.toMillis(chatClient.getChatOnlholdTimeout()));
		map.put("chatSessionTimeout", TimeUtils.toMillis(sessionStore.getChatSessionTimeout()));
		map.put("timestamp", System.currentTimeMillis());

		return map;
	}

}
