package com.boot.jx.postman.plugin;

import java.util.List;
import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.tg.TelegramConfigDetails;
import com.boot.model.MapModel;

public class TelegramPlugin implements ChannelPlugin<TelegramConfigDetails> {

    @Override
    public TelegramConfigDetails getChannelDetails() {
	return new TelegramConfigDetails();
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

    @Override
    public void addConfigMeta(List<ConfigMeta> configMetaList) {
	configMetaList.add(new ConfigMeta().path("telegram.handler").title("BotName"));
	configMetaList.add(new ConfigMeta().path("telegram.type").optionValues("bot").hidden());
	configMetaList.add(new ConfigMeta().path("telegram.accessToken").title("Access Token"));
    }

    @Override
    public void extractChannelDetailsFromMap(TelegramConfigDetails channelDetails, MapModel map) {
	channelDetails.setHandler(map.pathEntry("telegram.handler").asString(channelDetails.getHandler()));
	channelDetails.setType(map.pathEntry("telegram.type").asString(channelDetails.getType()));
	channelDetails.setAccessToken(map.pathEntry("telegram.accessToken").asString(channelDetails.getAccessToken()));
    }

}
