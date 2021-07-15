package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.TimeUtils;

@Component
public class AppCommonConfigImpl implements AppCommonConfig {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMClientConfig chatClientConfig;

	public String getCdnServer() {
		return pmEnvironment.get("mry.cdn.url").asString();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("AGENT_CHAT_INIT", pmEnvironment.get("postman.agent.chat.init").asBoolean());
		map.put("CHAT_TAG_ENABLED", pmEnvironment.config().get("chat.tag.enabled").asBoolean());
		map.put("chatIdleTimeout", TimeUtils.toMillis(chatClientConfig.getChatIdleTimeout()));
		map.put("agentSessionTimeout", TimeUtils.toMillis(chatClientConfig.getAgentSessionTimeout()));
		map.put("chatSessionTimeout", TimeUtils.toMillis(chatClientConfig.getChatSessionTimeout()));

		SafeKeyHashMap<Object> setup = new SafeKeyHashMap<Object>();
		for (ConfigBuilder config : ConfigBuilder.LIST) {
			setup.put(config.getKey().toUpperCase(), pmEnvironment.get(config.getKey()).getValue());
		}
		map.put("SETUP", setup);
		map.put("timestamp", System.currentTimeMillis());
		return map;
	}

}
