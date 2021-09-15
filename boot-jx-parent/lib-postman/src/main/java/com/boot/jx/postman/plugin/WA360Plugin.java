package com.boot.jx.postman.plugin;

import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.wa360.WA360ConfigDetails;

public class WA360Plugin implements ChannelPlugin<WA360ConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.WA_360D;
    }

    @Override
    public void setDetails(ChannelConfig config, WA360ConfigDetails details) {
	config.setWa360d(details);
    }

    @Override
    public WA360ConfigDetails getDetails(ChannelConfig config) {
	return config.getWa360d();
    }

}
