package com.boot.jx.postman.plugin;

import java.util.Map;

import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.tg.TelegramConfigDetails;

public class TelegramPlugin implements ChannelPlugin<TelegramConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.TELEGRAM;
    }

    @Override
    public Map<String, TelegramConfigDetails> getDetails(PMConfiguration config) {
	return config.getTelegram();
    }

    @Override
    public void setDetails(ChannelConfig config, TelegramConfigDetails details) {
	config.setTelegram(details);
    }

    @Override
    public TelegramConfigDetails getDetails(ChannelConfig config) {
	return config.getTelegram();
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.telegram(config.getTelegram(), config.isDisabled());
    }

}
