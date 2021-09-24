package com.boot.jx.postman.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.AppContextUtil;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;

public class ChannelPluginProvider {

    public static interface ChannelPlugin<C extends AChannelDetails> {
	/**
	 * usually return new AChannelDetails();
	 * 
	 * @return new instance of {@link AChannelDetails}
	 */
	public C getChannelDetails();

	@SuppressWarnings("unchecked")
	default public C getChannelDetails(AChannelDetails channelDetails) {
	    return (C) channelDetails;
	}

	public default String getChannelType() {
	    return this.getChannelDetails().getChannelType();
	}

	@Deprecated
	public default Map<String, C> getDetails(PMConfiguration configuration) {
	    return new HashMap<String, C>();
	}

	/**
	 * For new Configs
	 * 
	 * @param config
	 * @param details
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default ChannelConfig fromDetails(ChannelConfig config, AChannelDetails details) {
	    config.copy(details);
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

	@Deprecated
	public default void setConfig(PMConfiguration configuration, ChannelConfig config) {
	    configuration.channels(config);
	}

	public void addConfigMeta(List<ConfigMeta> configMetaList);

	default public List<ConfigMeta> listConfigMeta(PMEnvironment pmEnvironment) {
	    List<ConfigMeta> list = ConfigMeta.createList();
	    list.add(new ConfigMeta().key("name").title("Desc"));
	    list.add(new ConfigMeta().key("channelKey").title("Channel Key").readonly().hidden()
		    .defaultValue(PostManUtil.UNIQUE_API_KEY()));
	    String serviceDomain = pmEnvironment.get("mry.prop.service.domain").asString();
	    String clientDomain = AppContextUtil.getTenant();
	    list.add(new ConfigMeta().key("webhookUrl").title("Webhook URL").hidden()
		    .defaultValue(String.format("https://%s.%s/postman", clientDomain, serviceDomain)));
	    this.addConfigMeta(list);
	    return list;
	}

	void extractChannelDetailsFromMap(C channelDetails, MapModel map);

	@SuppressWarnings("unchecked")
	default void extractChannelDetailsFromMap(AChannelDetails channelDetails, MapModel map, String channelType) {
	    extractChannelDetailsFromMap((C) channelDetails, map);
	}

	public default C getChannelDetailsFromMap(MapModel map) {
	    C channelDetails = getChannelDetails();
	    extractChannelDetailsFromMap(channelDetails, map);
	    return channelDetails;
	}

    }

    public static final Map<String, ChannelPlugin<? extends AChannelDetails>> MAP = new HashMap<String, ChannelPlugin<? extends AChannelDetails>>();
    public static final Map<String, AChannelDetails> DETAILS = new HashMap<String, AChannelDetails>();

    public static final WebPlugin WEB = new WebPlugin();

    public static <C extends AChannelDetails> void register(ChannelPlugin<C> channelPlugin) {
	DETAILS.put(channelPlugin.getChannelType(), channelPlugin.getChannelDetails());
	MAP.put(channelPlugin.getChannelType(), channelPlugin);
    }

    public ChannelPlugin<? extends AChannelDetails> get(String channelType) {
	return MAP.getOrDefault(channelType, WEB);
    }

    public static final FacebookPlugin FACEBOOK = new FacebookPlugin();
    public static final TwitterPlugin TWITTER = new TwitterPlugin();
    public static final TelegramPlugin TELEGRAM = new TelegramPlugin();
    public static final WAGupShupPlugin WA_GUPSHUP = new WAGupShupPlugin();
    public static final WA360Plugin WA_360D = new WA360Plugin();

    static {
	register(WEB);
	register(FACEBOOK);
	register(TWITTER);
	register(TELEGRAM);
	register(WA_GUPSHUP);
	register(WA_360D);
    }

}
