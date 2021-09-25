package com.boot.jx.postman;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Component
public class PMEnvironment {

    public static interface PublicProperty {
    }

    public static interface ProtectedProperty extends PublicProperty {
    }

    public static interface OneTimeVisibleProperty extends ProtectedProperty {
    }

    public static interface PMEnvironmentProvider {

	@Deprecated
	public PMConfiguration config();

	public PMConfiguration shared();

	@Deprecated
	public void config(PMConfiguration configuration);

	public void config(ChannelConfig config);

	void remove(ChannelConfig config);
    }

    public static interface ChannelDetails extends Serializable {

	@JsonView(PublicProperty.class)
	public String getLane();

	@JsonView(PublicProperty.class)
	public default String getChannel() {
	    return null;
	}

	@JsonView(PublicProperty.class)
	public boolean isPushAllowed();

	@JsonView(PublicProperty.class)
	public boolean isPushOnlyApproved();

	@JsonView(PublicProperty.class)
	public boolean isPushFreeTextAllowed();

	@JsonView(PublicProperty.class)
	public boolean isPushToNewContactAllowed();

	@JsonView(PublicProperty.class)
	public ContactType getContactType();

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static abstract class AChannelDetails implements ChannelDetails {

	private static final long serialVersionUID = -5531902306230415784L;

	protected String name;
	protected ContactType contactType;
	protected String channelType;

	@Deprecated
	protected String channel;
	protected String channelKey;

	@JsonView(PMEnvironment.PublicProperty.class)
	protected String webhookUrl;

	public AChannelDetails(String channelType) {
	    this.channelType = channelType;
	}

	public ContactType getContactType() {
	    return contactType;
	}

	public void setContactType(ContactType contactType) {
	    this.contactType = contactType;
	}

	@Deprecated
	public String getChannel() {
	    return channel;
	}

	@Deprecated
	public void setChannel(String channel) {
	    this.channel = channel;
	}

	public String getChannelType() {
	    return channelType;
	}

	public void setChannelType(String channelType) {
	    this.channelType = channelType;
	}

	public String getChannelKey() {
	    if (!ArgUtil.is(this.channelKey)) {
		this.channelKey = PostManUtil.UNIQUE_API_KEY();
	    }
	    return channelKey;
	}

	public void setChannelKey(String channelKey) {
	    this.channelKey = channelKey;
	}

	public String getName() {
	    if (!ArgUtil.is(this.name)) {
		return String.format("%s %s", this.getContactType(), this.getLane());
	    }
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getWebhookUrl() {
	    return webhookUrl;
	}

	public void setWebhookUrl(String webhookUrl) {
	    this.webhookUrl = webhookUrl;
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
	boolean shared;

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

	public boolean isShared() {
	    return shared;
	}

	public void setShared(boolean shared) {
	    this.shared = shared;
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

    public PMConfiguration shared() {
	PMConfiguration config = null;
	if (ArgUtil.is(provider)) {
	    config = provider.shared();
	}
	if (config == null) {
	    config = new PMConfiguration();
	}
	return config;
    }

    @Deprecated
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
    private AppConfig appConfig;

    public PMConfigurationObject get(String key) {
	PMConfigurationObject configObject = this.config().map().get(key);

	String tnt = AppContextUtil.getTenant();
	if (ArgUtil.isEmpty(configObject) && !Tenants.isDefault(tnt)) {
	    PMConfigurationObject sharedConfigObject = this.shared().map().get(key);
	    if (ArgUtil.is(sharedConfigObject)) {
		return sharedConfigObject;
	    }
	}

	if (ArgUtil.isEmpty(configObject)) {
	    String value = appConfig.prop(key);
	    configObject = new PMConfigurationObject(key, value);
	    //this.config().map().put(key, configObject);
	}

	return configObject;
    }

}
