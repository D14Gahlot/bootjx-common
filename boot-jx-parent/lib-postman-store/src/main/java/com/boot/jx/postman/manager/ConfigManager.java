package com.boot.jx.postman.manager;

import java.util.Map;

import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;

public interface ConfigManager {
	ClientAppConfigDoc save(ClientAppConfigDoc xo);

	void refresh();

	void save(ChannelConfig config);

	ChannelConfig saveChannelConfig(String channelType, Map<String, Object> data);
}
