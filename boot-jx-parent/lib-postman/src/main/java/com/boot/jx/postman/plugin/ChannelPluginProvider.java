package com.boot.jx.postman.plugin;

import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;

public class ChannelPluginProvider {

    public static interface ChannelPlugin<C extends AChannelDetails> {
	public String getChannelType();

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

    }

    public static final Map<String, ChannelPlugin<?>> MAP = new HashMap<String, ChannelPlugin<?>>();

    public static <C extends AChannelDetails> void register(ChannelPlugin<C> channelPlugin) {
	MAP.put(channelPlugin.getChannelType(), channelPlugin);
    }

    public static final FacebookPlugin FACEBOOK = new FacebookPlugin();
    public static final TwitterPlugin TWITTER = new TwitterPlugin();
    public static final TelegramPlugin TELEGRAM = new TelegramPlugin();
    public static final WAGupShupPlugin WA_GUPSHUP = new WAGupShupPlugin();
    public static final WA360Plugin WA_360D = new WA360Plugin();

    static {
	register(FACEBOOK);
	register(TWITTER);
	register(TELEGRAM);
	register(WA_GUPSHUP);
	register(WA_360D);
    }

}
