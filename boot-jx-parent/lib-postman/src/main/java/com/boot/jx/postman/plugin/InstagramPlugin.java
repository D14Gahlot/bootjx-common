package com.boot.jx.postman.plugin;

import java.util.List;

import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.plugin.InstagramPlugin.InstagramConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

public class InstagramPlugin implements ChannelPlugin<InstagramConfig> {

    @Override
    public ContactType getContactType() {
	return ContactType.INSTAGRAM;
    }

    @Override
    public String getChannelType() {
	return CHANNEL_TYPE.INSTAGRAM;
    }

    public static class InstagramConfig extends AChannelDetails {

	private static final long serialVersionUID = -2397678752642150000L;
	private String pageId;
	private String handler;
	private String type;

	@JsonView(PMEnvironment.ProtectedProperty.class)
	private String accessToken;
	@JsonView(PMEnvironment.ProtectedProperty.class)
	private String verifyToken;
	@JsonView(PMEnvironment.ProtectedProperty.class)
	private String appSecret;

	public String getPageId() {
	    return pageId;
	}

	public void setPageId(String pageId) {
	    this.pageId = pageId;
	}

	public String getType() {
	    return type;
	}

	public void setType(String type) {
	    this.type = type;
	}

	public String getAccessToken() {
	    return accessToken;
	}

	public void setAccessToken(String accessToken) {
	    this.accessToken = accessToken;
	}

	public String getVerifyToken() {
	    return verifyToken;
	}

	public void setVerifyToken(String verifyToken) {
	    this.verifyToken = verifyToken;
	}

	public String getAppSecret() {
	    return appSecret;
	}

	public void setAppSecret(String appSecret) {
	    this.appSecret = appSecret;
	}

	@Override
	public String getLane() {
	    return this.pageId;
	}

	public String getHandler() {
	    return handler;
	}

	public void setHandler(String handler) {
	    this.handler = handler;
	}

    }

    @Override
    public String getDefaultName(ChannelConfig config) {
	if (!ArgUtil.is(config.getName())) {
	    if (ArgUtil.is(config.getInstagram().getHandler())) {
		return config.getInstagram().getHandler();
	    }
	    return String.format("IG %s", config.getLane());
	}
	return config.getName();
    }

    @Override
    public InstagramConfig newChannelDetails() {
	return new InstagramConfig();
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
    public void addConfigMeta(List<ConfigMeta> list) {
	list.add(new ConfigMeta().path("instagram.pageId").title("Page Id").createonly());
	list.add(new ConfigMeta().path("instagram.type").title("Type").optionValues("page").hidden());
	list.add(new ConfigMeta().path("instagram.handler").title("Handler"));
	list.add(new ConfigMeta().path("instagram.verifyToken").title("Verify Token").writeonly());
	list.add(new ConfigMeta().path("instagram.accessToken").title("Access Token").writeonly());
	list.add(new ConfigMeta().path("instagram.appSecret").title("App Secret").writeonly());
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

    @Override
    public boolean isPushAllowed() {
	return false;
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
	return false;
    }
}
