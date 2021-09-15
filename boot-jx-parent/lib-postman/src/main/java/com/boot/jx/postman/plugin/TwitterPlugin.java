package com.boot.jx.postman.plugin;

import java.util.Map;

import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.tw.TwitterConfigDetails;

public class TwitterPlugin implements ChannelPlugin<TwitterConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.TWITTER;
    }

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

}
