package com.boot.jx.postman.plugin;

import java.util.Map;

import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;

public class FacebookPlugin implements ChannelPlugin<FacebookConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.FACEBOOK;
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

}
