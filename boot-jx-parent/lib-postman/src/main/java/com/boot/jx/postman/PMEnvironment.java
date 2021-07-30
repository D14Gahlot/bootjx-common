package com.boot.jx.postman;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.dict.ContactType;
import com.boot.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@Component
public class PMEnvironment {

    public static interface PMEnvironmentProvider {
	public PMConfiguration config();

	public void config(PMConfiguration configuration);

	public void config(ChannelConfig config);
    }

    public static interface ChannelDetails extends Serializable {

	public static interface Public {

	}

	public static interface Protected extends Public {

	}

	@JsonView(ChannelDetails.Public.class)
	public String getLane();

	@JsonView(ChannelDetails.Public.class)
	public default String getChannel() {
	    return null;
	}

	@JsonView(ChannelDetails.Public.class)
	public boolean isPushAllowed();

	@JsonView(ChannelDetails.Public.class)
	public boolean isPushOnlyApproved();

	@JsonView(ChannelDetails.Public.class)
	public boolean isPushFreeTextAllowed();

	@JsonView(ChannelDetails.Public.class)
	public boolean isPushToNewContactAllowed();

	@JsonView(ChannelDetails.Public.class)
	public ContactType getContactType();

    }

    public static abstract class AChannelDetails implements ChannelDetails {

	private static final long serialVersionUID = -5531902306230415784L;

	protected ContactType contactType;
	protected String channelType;

	protected String channel;

	public AChannelDetails(String channelType) {
	    this.channelType = channelType;
	}

	public ContactType getContactType() {
	    return contactType;
	}

	public void setContactType(ContactType contactType) {
	    this.contactType = contactType;
	}

	public String getChannel() {
	    return channel;
	}

	public void setChannel(String channel) {
	    this.channel = channel;
	}

	public String getChannelType() {
	    return channelType;
	}

	public void setChannelType(String channelType) {
	    this.channelType = channelType;
	}
    }

    public static abstract class AChannelConfig extends AChannelDetails {

	private static final long serialVersionUID = 1950315645271368433L;

	public AChannelConfig() {
	    super("WEBSITE");
	}

	public String getChannelId() {
	    return String.format("%s:%s", this.getChannelType(), this.getLane());
	}

    }

    public static class PMConfigurationObject extends MapEntry implements Serializable {

	private static final long serialVersionUID = 2678154770516185408L;
	String key;
	String description;

	public PMConfigurationObject(String key, Object value) {
	    super(value);
	    this.key = key;
	}

	public PMConfigurationObject() {
	    super(null);
	}

	public String getKey() {
	    return key;
	}

	public void setKey(String key) {
	    this.key = key;
	}

	public String getDescription() {
	    return description;
	}

	public void setDescription(String description) {
	    this.description = description;
	}

    }

    @Autowired(required = false)
    private PMEnvironmentProvider provider;

    public PMConfiguration config() {
	PMConfiguration config = null;
	if (ArgUtil.is(provider)) {
	    config = provider.config();
	}
	if (config == null) {
	    config = new PMConfiguration();
	}
	return config;
    }

    public void config(PMConfiguration config) {
	if (ArgUtil.is(provider)) {
	    provider.config(config);
	}
    }

    public void config(ChannelConfig config) {
	if (ArgUtil.is(provider)) {
	    provider.config(config);
	}
    }

    @Autowired
    AppConfig appConfig;

    public PMConfigurationObject get(String key) {
	PMConfigurationObject config = this.config().map().get(key);
	if (ArgUtil.isEmpty(config)) {
	    String value = appConfig.prop(key);
	    config = new PMConfigurationObject(key, value);
	    this.config().map().put(key, config);
	}
	return config;
    }

}
