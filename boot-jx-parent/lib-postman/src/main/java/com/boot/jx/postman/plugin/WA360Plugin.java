package com.boot.jx.postman.plugin;

import java.util.List;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.plugin.WA360Plugin.WA360ConfigDetails;
import com.boot.model.MapModel;
import com.fasterxml.jackson.annotation.JsonView;

public class WA360Plugin implements ChannelPlugin<WA360ConfigDetails> {

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.WA_360D;
    }

    @Override
    public ContactType getContactType() {
	return ContactType.WHATSAPP;
    }

    public static class WA360ConfigDetails extends AChannelDetails {

	private static final long serialVersionUID = -2397678752642150000L;
	private String number;

	@JsonView(PMEnvironment.ProtectedProperty.class)
	private String apiKey;

	@Override
	public String getLane() {
	    return this.number;
	}

	public String getNumber() {
	    return number;
	}

	public void setNumber(String number) {
	    this.number = number;
	}

	public String getApiKey() {
	    return apiKey;
	}

	public void setApiKey(String apiKey) {
	    this.apiKey = apiKey;
	}
    }

    @Override
    public void setDetails(ChannelConfig config, WA360ConfigDetails details) {
	config.setWa360d(details);
    }

    @Override
    public WA360ConfigDetails getDetails(ChannelConfig config) {
	return config.getWa360d();
    }

    @Override
    public WA360ConfigDetails newChannelDetails() {
	return new WA360ConfigDetails();
    }

    @Override
    public void addConfigMeta(List<ConfigMeta> configMetaList) {
	configMetaList.add(new ConfigMeta().path("wa360d.number").title("Number").createonly());
	configMetaList.add(new ConfigMeta().path("wa360d.apiKey").title("API Key").writeonly());
    }

    @Override
    public void importChannelDetailsFromMap(WA360ConfigDetails channelDetails, MapModel map) {
	channelDetails.setNumber(map.pathEntry("wa360d.number").asString(channelDetails.getNumber()));
	channelDetails.setApiKey(map.pathEntry("wa360d.apiKey").asString(channelDetails.getApiKey()));
    }

    @Override
    public boolean isPushAllowed() {
	return true;
    }

    @Override
    public boolean isPushOnlyApproved() {
	return true;
    }

    @Override
    public boolean isPushFreeTextAllowed() {
	return false;
    }

    @Override
    public boolean isPushToNewContactAllowed() {
	return true;
    }

    @Override
    public boolean isWebhookManual() {
	return false;
    }

}
