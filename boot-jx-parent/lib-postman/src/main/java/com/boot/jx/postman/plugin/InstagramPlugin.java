package com.boot.jx.postman.plugin;

import java.util.List;
import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.ig.InstagramConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.model.MapModel;

public class InstagramPlugin implements ChannelPlugin<InstagramConfig> {

    @Override
    public InstagramConfig newChannelDetails() {
	return new InstagramConfig();
    }

    @Override
    public Map<String, InstagramConfig> getDetails(PMConfiguration config) {
	return config.getInstagram();
    }

    @Override
    public void setDetails(ChannelConfig config, InstagramConfig details) {
	config.setInstagram(details);
    }

    @Override
    public InstagramConfig getDetails(ChannelConfig config) {
	return config.getInstagram();
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.instagram(config.getInstagram(), config.isDisabled());
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> list) {
	list.add(new ConfigMeta().path("instagram.pageId").title("Page Id").nonEditable());
	list.add(new ConfigMeta().path("instagram.type").title("Type").optionValues("page").hidden());
	list.add(new ConfigMeta().path("instagram.handler").title("Handler"));
	list.add(new ConfigMeta().path("instagram.verifyToken").title("Verify Token"));
	list.add(new ConfigMeta().path("instagram.accessToken").title("Access Token"));
	list.add(new ConfigMeta().path("instagram.appSecret").title("App Secret"));
    }

    @Override
    public void importChannelDetailsFromMap(InstagramConfig channelDetails, MapModel map) {
	channelDetails.setPageId(map.pathEntry("instagram.pageId").asString(channelDetails.getPageId()));
	channelDetails.setHandler(map.pathEntry("instagram.handler").asString(channelDetails.getHandler()));
	channelDetails.setType(map.pathEntry("instagram.type").asString(channelDetails.getType()));
	channelDetails.setVerifyToken(map.pathEntry("instagram.verifyToken").asString(channelDetails.getVerifyToken()));
	channelDetails.setAccessToken(map.pathEntry("instagram.accessToken").asString(channelDetails.getAccessToken()));
	channelDetails.setAppSecret(map.pathEntry("instagram.appSecret").asString(channelDetails.getAppSecret()));
    }

}
