package com.boot.jx.postman.plugin;

import java.util.List;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.plugin.WebPlugin.WebConfigDetails;
import com.boot.model.MapModel;

public class WebPlugin implements ChannelPlugin<WebConfigDetails> {

    public static final class WebConfigDetails extends AChannelDetails {

	private static final long serialVersionUID = 8692015716138195462L;

	public WebConfigDetails() {
	    super(CHANNEL_TYPE.WEB);
	}

	@Override
	public String getLane() {
	    return null;
	}

	@Override
	public boolean isPushAllowed() {
	    return false;
	}

	@Override
	public boolean isPushOnlyApproved() {
	    return false;
	}

	@Override
	public boolean isPushFreeTextAllowed() {
	    return false;
	}

	@Override
	public boolean isPushToNewContactAllowed() {
	    return false;
	}

    }

    @Override
    public void setDetails(ChannelConfig config, WebConfigDetails details) {
	config.setWeb(details);
    }

    @Override
    public WebConfigDetails getDetails(ChannelConfig config) {
	return config.getWeb();
    }

    @Override
    public WebConfigDetails getChannelDetails() {
	return new WebConfigDetails();
    }

    @Override
    public void extractChannelDetailsFromMap(WebConfigDetails channelDetails, MapModel map) {
	// TODO Auto-generated method stub
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> configMetaList) {
	// TODO Auto-generated method stub
    }

}
