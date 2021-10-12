package com.boot.jx.postman.plugin;

import java.util.List;
import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.model.MapModel;

public class FacebookPlugin implements ChannelPlugin<FacebookConfigDetails> {

    @Override
    public FacebookConfigDetails getChannelDetails() {
	return new FacebookConfigDetails();
    }

    @Override
    public Map<String, FacebookConfigDetails> getDetails(PMConfiguration config) {
	return config.getFacebook();
    }

    @Override
    public void setDetails(ChannelConfig config, FacebookConfigDetails details) {
	config.setFacebook(details);
    }

    @Override
    public FacebookConfigDetails getDetails(ChannelConfig config) {
	return config.getFacebook();
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.facebook(config.getFacebook(), config.isDisabled());
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> list) {
	list.add(new ConfigMeta().path("facebook.pageId").title("Page Id"));
	list.add(new ConfigMeta().path("facebook.type").title("Type").optionValues("page").hidden());
	list.add(new ConfigMeta().path("facebook.handler").title("Handler"));
	list.add(new ConfigMeta().path("facebook.verifyToken").title("Verify Token"));
	list.add(new ConfigMeta().path("facebook.accessToken").title("Access Token"));
	list.add(new ConfigMeta().path("facebook.appSecret").title("App Secret"));
    }

    @Override
    public void extractChannelDetailsFromMap(FacebookConfigDetails channelDetails, MapModel map) {
	channelDetails.setPageId(map.path("facebook.pageId").asString(channelDetails.getPageId()));
	channelDetails.setHandler(map.path("facebook.handler").asString(channelDetails.getHandler()));
	channelDetails.setType(map.path("facebook.type").asString(channelDetails.getType()));
	channelDetails.setVerifyToken(map.path("facebook.verifyToken").asString(channelDetails.getVerifyToken()));
	channelDetails.setAccessToken(map.path("facebook.accessToken").asString(channelDetails.getAccessToken()));
	channelDetails.setAppSecret(map.path("facebook.appSecret").asString(channelDetails.getAppSecret()));
    }

}
