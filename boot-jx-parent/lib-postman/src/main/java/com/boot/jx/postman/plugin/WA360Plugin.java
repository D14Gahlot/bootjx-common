package com.boot.jx.postman.plugin;

import java.util.List;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.wa360.WA360ConfigDetails;
import com.boot.model.MapModel;

public class WA360Plugin implements ChannelPlugin<WA360ConfigDetails> {

    @Override
    public void setDetails(ChannelConfig config, WA360ConfigDetails details) {
	config.setWa360d(details);
    }

    @Override
    public WA360ConfigDetails getDetails(ChannelConfig config) {
	return config.getWa360d();
    }

    @Override
    public WA360ConfigDetails getChannelDetails() {
	return new WA360ConfigDetails();
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> configMetaList) {
	configMetaList.add(new ConfigMeta().path("wa360d.number").title("Number"));
	configMetaList.add(new ConfigMeta().path("wa360d.apiKey").title("API Key"));
    }

    @Override
    public void extractChannelDetailsFromMap(WA360ConfigDetails channelDetails, MapModel map) {
	channelDetails.setNumber(map.path("wa360d.number").asString(channelDetails.getNumber()));
	channelDetails.setApiKey(map.path("wa360d.apiKey").asString(channelDetails.getApiKey()));
    }

}
