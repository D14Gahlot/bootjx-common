package com.boot.jx.postman.plugin;

import java.util.List;
import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.model.MapModel;

public class WAGupShupPlugin implements ChannelPlugin<GupShupConfigDetails> {

    @Override
    public Map<String, GupShupConfigDetails> getDetails(PMConfiguration configuration) {
	return configuration.getGupshup();
    }

    @Override
    public void setDetails(ChannelConfig config, GupShupConfigDetails details) {
	config.setGupshup(details);
    }

    @Override
    public GupShupConfigDetails getDetails(ChannelConfig config) {
	return config.getGupshup();
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.gupshup(config.getGupshup(), config.isDisabled());
    }

    @Override
    public GupShupConfigDetails getChannelDetails() {
	return new GupShupConfigDetails();
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> list) {
	list.add(new ConfigMeta().path("gupshup.number").title("Number"));
	list.add(new ConfigMeta().path("gupshup.chatId").title("Chat Id"));
	list.add(new ConfigMeta().path("gupshup.chatPass").title("Chat Password"));
	list.add(new ConfigMeta().path("gupshup.notifyId").title("Notification Id"));
	list.add(new ConfigMeta().path("gupshup.notifyPass").title("Notification Password"));
    }

    @Override
    public void extractChannelDetailsFromMap(GupShupConfigDetails channelDetails, MapModel map) {
	channelDetails.setNumber(map.path("gupshup.number").asString(channelDetails.getNumber()));
	channelDetails.setChatId(map.path("gupshup.chatId").asString(channelDetails.getChatId()));
	channelDetails.setChatPass(map.path("gupshup.chatPass").asString(channelDetails.getChatPass()));
	channelDetails.setNotifyId(map.path("gupshup.notifyId").asString(channelDetails.getNotifyId()));
	channelDetails.setNotifyPass(map.path("gupshup.notifyPass").asString(channelDetails.getNotifyPass()));
    }

}
