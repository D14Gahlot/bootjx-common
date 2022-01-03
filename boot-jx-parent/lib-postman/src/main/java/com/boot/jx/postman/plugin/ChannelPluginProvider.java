package com.boot.jx.postman.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.AppContextUtil;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.ChannelTypeSpecificProps;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

public class ChannelPluginProvider {

    public static interface ChannelPlugin<C extends AChannelDetails> extends ChannelTypeSpecificProps {
	/**
	 * usually return new AChannelDetails();
	 * 
	 * @return new instance of {@link AChannelDetails}
	 */
	public C newChannelDetails();

	@SuppressWarnings("unchecked")
	default public C getChannelDetails(AChannelDetails channelDetails) {
	    return (C) channelDetails;
	}

	/**
	 * For new Configs
	 * 
	 * @param config
	 * @param details
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default ChannelConfig updateChannelConfig(ChannelConfig config, AChannelDetails details) {
	    // Plugin Specific Properties
	    config.setContactType(this.getContactType());
	    config.setChannelType(this.getChannelType());

	    config.setPushAllowed(this.isPushAllowed());
	    config.setPushOnlyApproved(this.isPushOnlyApproved());
	    config.setPushFreeTextAllowed(this.isPushFreeTextAllowed());
	    config.setPushToNewContactAllowed(this.isPushToNewContactAllowed());

	    // Channel Specific Properties
	    config.setLane(details.getLane());

	    setDetails(config, (C) details);
	    return config;
	}

	/**
	 * For new Configs
	 * 
	 * @param config
	 * @param details
	 */
	public void setDetails(ChannelConfig config, C details);

	public C getDetails(ChannelConfig config);

	public void addConfigMeta(List<ConfigMeta> configMetaList);

	default public List<ConfigMeta> listConfigMeta(PMEnvironment pmEnvironment) {
	    List<ConfigMeta> list = ConfigMeta.createList();
	    list.add(new ConfigMeta().key("name").title("Desc"));
	    list.add(new ConfigMeta().key("channelKey").title("Channel Key").readonly().hidden()
		    .defaultValue(PostManUtil.UNIQUE_API_KEY()));
	    String serviceDomain = pmEnvironment.keyEntry("mry.prop.service.domain").asString();
	    String clientDomain = AppContextUtil.getTenant();
	    list.add(new ConfigMeta().key("webhookUrl").title("Webhook URL").hidden()
		    .defaultValue(String.format("https://%s.%s/postman", clientDomain, serviceDomain)));
	    this.addConfigMeta(list);
	    return list;
	}

	void importChannelDetailsFromMap(C channelDetails, MapModel map);

	@SuppressWarnings("unchecked")
	default void importChannelDetailsFromMap(AChannelDetails channelDetails, MapModel map, String channelType) {
	    importChannelDetailsFromMap((C) channelDetails, map);
	}

	default public void importChannelConfigFromMap(ChannelConfig config, MapModel map, String channelType) {
	    AChannelDetails channelDetails = getDetails(config);
	    if (channelDetails == null) {
		channelDetails = newChannelDetails();
	    }
	    importChannelDetailsFromMap(channelDetails, map, channelType);

	    config.setName(map.getString("name", ArgUtil.nonEmpty(config.getName(), getDefaultName(config))));
	    config.setChannelKey(map.getString("channelKey",
		    ArgUtil.nonEmpty(config.getChannelKey(), PostManUtil.UNIQUE_API_KEY())));

	    config.setWebhookUrl(map.getString("webhookUrl", config.getWebhookUrl()));
	    updateChannelConfig(config, channelDetails);
	}

	default public String getDefaultName(ChannelConfig config) {
	    if (!ArgUtil.is(config.getName())) {
		return String.format("%s %s", this.getContactType(), config.getLane());
	    }
	    return config.getName();
	}

    }

    public static final Map<String, ChannelPlugin<? extends AChannelDetails>> PLUGIN_MAPPING = new HashMap<String, ChannelPlugin<? extends AChannelDetails>>();
    public static final Map<String, AChannelDetails> DETAILS_MAPPING = new HashMap<String, AChannelDetails>();

    public static <C extends AChannelDetails> void register(ChannelPlugin<C> channelPlugin) {
	C details = channelPlugin.newChannelDetails();
	DETAILS_MAPPING.put(channelPlugin.getChannelType(), channelPlugin.newChannelDetails());
	PLUGIN_MAPPING.put(channelPlugin.getChannelType(), channelPlugin);
    }

    public ChannelPlugin<? extends AChannelDetails> get(String channelType) {
	return PLUGIN_MAPPING.getOrDefault(channelType, WEB);
    }

    public static final WebPlugin WEB = new WebPlugin();
    public static final FacebookPlugin FACEBOOK = new FacebookPlugin();
    public static final TwitterPlugin TWITTER = new TwitterPlugin();
    public static final TelegramPlugin TELEGRAM = new TelegramPlugin();
    public static final WAGupShupPlugin WA_GUPSHUP = new WAGupShupPlugin();
    public static final WA360Plugin WA_360D = new WA360Plugin();
    public static final InstagramPlugin INSTAGRAM = new InstagramPlugin();

    static {
	register(WEB);
	register(FACEBOOK);
	register(TWITTER);
	register(TELEGRAM);
	register(WA_GUPSHUP);
	register(WA_360D);
	register(INSTAGRAM);
    }

}
