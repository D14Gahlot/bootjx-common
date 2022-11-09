package com.boot.jx.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableEncryptableProperties
@PropertySource("classpath:application.app.properties")
public class PMDomainConfigImpl implements PMDomainConfig {

	@Autowired
	private PMEnvironment environment;

	@Override
	public String getDefaultInboundQueue() {
		return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_QUEUE).asString();
	}

	@Override
	public String getDefaultInboundQueue(String channelId, CHAT_MODE mode) {
		if (CHAT_MODE.AGENT.equals(mode)) {
			return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_AGENT_QUEUE).asString();
		} else {
			ChannelConfig channel = environment.config().channel(channelId);
			if (ArgUtil.is(channel) && ArgUtil.is(channel.getInboundQueue())) {
				return channel.getInboundQueue();
			} else {
				return getDefaultInboundQueue();
			}
		}
	}

	@Override
	public String getDefaultInboundQueue(Contactable contact) {
		return getDefaultInboundQueue(PostManUtil.CHANNEL_ID(contact), null);
	}

	@Override
	public String getDefaultInboundQueue(Contactable contact, CHAT_MODE mode) {
		return getDefaultInboundQueue(PostManUtil.CHANNEL_ID(contact), mode);
	}

	@Override
	public String getDomainUrl() {
		return String.format("https://%s.%s", AppContextUtil.getTenant(),
				environment.keyEntry("mry.prop.service.server").asString());
	}

	@Override
	public PMConfigurationObject getAgentHistoryPeriod() {
		return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_TAB_HISTORY_PERIOD);
	}

	@Override
	public PMConfigurationObject getAgentHistoryCount() {
		return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_TAB_HISTORY_LIMIT);
	}

	@Override
	public PMConfigurationObject getChatIdleTimeout() {
		return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_IDLE_TIMEOUT);
	}
}
