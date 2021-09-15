package com.boot.jx.postman.plugin;

import java.util.Map;

import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;

public class WAGupShupPlugin implements ChannelPlugin<GupShupConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.WA_GUPSHUP;
    }

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
	return null;
    }

    @Override
    public void setConfig(PMConfiguration configuration, ChannelConfig config) {
	configuration.gupshup(config.getGupshup(), config.isDisabled());
    }

}
