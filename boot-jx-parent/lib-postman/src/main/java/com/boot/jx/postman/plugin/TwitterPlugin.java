package com.boot.jx.postman.plugin;

import java.util.List;
import java.util.Map;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.tw.TwitterConfigDetails;
import com.boot.model.MapModel;

public class TwitterPlugin implements ChannelPlugin<TwitterConfigDetails> {

    @Override
    public Map<String, TwitterConfigDetails> getDetails(PMConfiguration config) {
	return config.getTwitter();
    }

    @Override
    public void setDetails(ChannelConfig config, TwitterConfigDetails details) {
	config.setTwitter(details);
    }

    @Override
    public TwitterConfigDetails getDetails(ChannelConfig config) {
	return config.getTwitter();
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.twitter(config.getTwitter(), config.isDisabled());
    }

    @Override
    public TwitterConfigDetails getChannelDetails() {
	return new TwitterConfigDetails();
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> configMetaList) {
	configMetaList.add(new ConfigMeta().path("twitter.handler").title("Handler"));
	configMetaList.add(new ConfigMeta().path("twitter.type").title("Type").hidden());
	configMetaList.add(new ConfigMeta().path("twitter.envName").title("Env").optional());
	configMetaList.add(new ConfigMeta().path("twitter.accessToken").title("Access Token"));
	configMetaList.add(new ConfigMeta().path("twitter.accessTokenSecret").title("Access Token Secret"));
	configMetaList.add(new ConfigMeta().path("twitter.consumerKey").title("Consumer Key"));
	configMetaList.add(new ConfigMeta().path("twitter.consumerSecret").title("Consumer Secret"));
    }

    @Override
    public void extractChannelDetailsFromMap(TwitterConfigDetails channelDetails, MapModel map) {
	channelDetails.setHandler(map.path("twitter.handler").asString(channelDetails.getHandler()));
	channelDetails.setType(map.path("twitter.type").asString(channelDetails.getType()));
	channelDetails.setEnvName(map.path("twitter.envName").asString(channelDetails.getEnvName()));
	channelDetails.setAccessToken(map.path("twitter.accessToken").asString(channelDetails.getAccessToken()));
	channelDetails.setAccessTokenSecret(
		map.path("twitter.accessTokenSecret").asString(channelDetails.getAccessTokenSecret()));
	channelDetails.setConsumerKey(map.path("twitter.consumerKey").asString(channelDetails.getConsumerKey()));
	channelDetails
		.setConsumerSecret(map.path("twitter.consumerSecret").asString(channelDetails.getConsumerSecret()));
    }

}
