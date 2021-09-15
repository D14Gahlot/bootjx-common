package com.boot.jx.postman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.boot.jx.agent.AgentConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.tg.TelegramConfigDetails;
import com.boot.jx.postman.tw.TwitterConfigDetails;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.Random;

public class PMConfiguration implements Serializable {

    private static final long serialVersionUID = -5432956433673368768L;

    private Map<String, FacebookConfigDetails> facebook;
    private Map<String, TwitterConfigDetails> twitter;
    private Map<String, TelegramConfigDetails> telegram;
    private Map<String, GupShupConfigDetails> gupshup;

    private Map<String, ChannelConfig> channels;
    private Map<String, ClientApiKey> clientApiKeys;

    private Map<String, PMConfigurationObject> map;

    private AgentConfig agent;
    private String accountKey;

    // Facebook
    public SafeKeyHashMap<FacebookConfigDetails> facebook() {
	if (ArgUtil.isEmpty(facebook)) {
	    facebook = new HashMap<String, FacebookConfigDetails>();
	}
	return new SafeKeyHashMap<FacebookConfigDetails>(facebook);
    }

    public Map<String, FacebookConfigDetails> getFacebook() {
	return facebook;
    }

    public void setFacebook(Map<String, FacebookConfigDetails> facebook) {
	this.facebook = facebook;
    }

    public FacebookConfigDetails facebook(String pageId) {
	return facebook().get(pageId);
    }

    public PMConfiguration facebook(FacebookConfigDetails config, boolean disbaled) {
	if (disbaled) {
	    this.facebook().remove(config.getPageId());
	} else
	    this.facebook().put(config.getPageId(), config);
	return this;
    }

    // TWITTER
    public SafeKeyHashMap<TwitterConfigDetails> twitter() {
	if (ArgUtil.isEmpty(twitter)) {
	    twitter = new HashMap<String, TwitterConfigDetails>();
	}
	return new SafeKeyHashMap<TwitterConfigDetails>(twitter);
    }

    public TwitterConfigDetails twitter(String handler) {
	return twitter().get(handler);
    }

    public PMConfiguration twitter(TwitterConfigDetails config, boolean disbaled) {
	if (disbaled) {
	    this.twitter().remove(config.getHandler());
	} else
	    this.twitter().put(config.getHandler(), config);
	return this;
    }

    public Map<String, TwitterConfigDetails> getTwitter() {
	return twitter;
    }

    public void setTwitter(Map<String, TwitterConfigDetails> twitter) {
	this.twitter = twitter;
    }

    // Telegram
    public SafeKeyHashMap<TelegramConfigDetails> telegram() {
	if (ArgUtil.isEmpty(telegram)) {
	    telegram = new HashMap<String, TelegramConfigDetails>();
	}
	return new SafeKeyHashMap<TelegramConfigDetails>(telegram);
    }

    public Map<String, TelegramConfigDetails> getTelegram() {
	return telegram;
    }

    public void setTelegram(Map<String, TelegramConfigDetails> telegram) {
	this.telegram = telegram;
    }

    public PMConfiguration telegram(TelegramConfigDetails config, boolean disbaled) {
	if (disbaled) {
	    this.telegram().remove(config.getHandler());
	} else
	    this.telegram().put(config.getHandler(), config);
	return this;
    }

    public TelegramConfigDetails telegram(String handler) {
	return telegram().get(handler);
    }

    // GupShup
    public SafeKeyHashMap<GupShupConfigDetails> gupshup() {
	if (ArgUtil.isEmpty(gupshup)) {
	    gupshup = new HashMap<String, GupShupConfigDetails>();
	}
	return new SafeKeyHashMap<GupShupConfigDetails>(gupshup);
    }

    public Map<String, GupShupConfigDetails> getGupshup() {
	return gupshup;
    }

    public void setGupshup(Map<String, GupShupConfigDetails> gupshup) {
	this.gupshup = gupshup;
    }

    public GupShupConfigDetails gupshup(String handler) {
	return gupshup().get(handler);
    }

    public PMConfiguration gupshup(GupShupConfigDetails config, boolean disbaled) {
	if (disbaled) {
	    this.gupshup().remove(config.getNumber());
	} else
	    this.gupshup().put(config.getNumber(), config);
	return this;
    }

    // All Channels
    public SafeKeyHashMap<ChannelConfig> channels() {
	if (ArgUtil.isEmpty(channels)) {
	    channels = new HashMap<String, ChannelConfig>();
	}
	return new SafeKeyHashMap<ChannelConfig>(channels);
    }

    public Map<String, ChannelConfig> getChannels() {
	return channels;
    }

    public void setChannels(Map<String, ChannelConfig> channels) {
	this.channels = channels;
    }

    public ChannelConfig channels(String channelId) {
	return channels().get(channelId);
    }

    public PMConfiguration channels(ChannelConfig channel) {
	this.channels().put(channel.getChannelId(), channel);
	return this;
    }

    // All A:PI Ckeys
    public SafeKeyHashMap<ClientApiKey> clientApiKeys() {
	if (ArgUtil.isEmpty(clientApiKeys)) {
	    clientApiKeys = new HashMap<String, ClientApiKey>();
	}
	return new SafeKeyHashMap<ClientApiKey>(clientApiKeys);
    }

    public ClientApiKey clientApiKey(String apiKey) {
	return clientApiKeys().get(apiKey);
    }

    public PMConfiguration clientApiKey(ClientApiKey clientApiKey) {
	this.clientApiKeys().put(clientApiKey.getKey(), clientApiKey);
	return this;
    }

    // Agent
    public AgentConfig agent() {
	if (ArgUtil.isEmpty(agent)) {
	    agent = new AgentConfig();
	}
	return agent;
    }

    public AgentConfig getAgent() {
	return agent;
    }

    public void setAgent(AgentConfig agent) {
	this.agent = agent;
    }

    public PMConfiguration agent(AgentConfig agent) {
	this.agent = agent;
	return this;
    }

    // Config
    public Map<String, PMConfigurationObject> getMap() {
	return map;
    }

    public void setMap(Map<String, PMConfigurationObject> map) {
	this.map = map;
    }

    public SafeKeyHashMap<PMConfigurationObject> map() {
	if (ArgUtil.isEmpty(map)) {
	    map = new HashMap<String, PMConfigurationObject>();
	}
	return new SafeKeyHashMap<PMConfigurationObject>(map);
    }

    public PMConfigurationObject get(String key) {
	return map().getOrDefault(key, new PMConfigurationObject(key, null));
    }

    public PMConfigurationObject get(String key, Object value) {
	return map().getOrDefault(key, new PMConfigurationObject(key, value));
    }

    public PMConfiguration set(PMConfigurationObject map) {
	this.map().put(map.getKey(), map);
	return this;
    }

    public List<AChannelDetails> connectors() {
	List<AChannelDetails> list = new ArrayList<AChannelDetails>();
	if (this.facebook != null) {
	    for (Entry<String, FacebookConfigDetails> configEntry : this.facebook.entrySet()) {
		list.add(configEntry.getValue());
	    }
	}
	if (this.gupshup != null) {
	    for (Entry<String, GupShupConfigDetails> configEntry : this.gupshup.entrySet()) {
		list.add(configEntry.getValue());
	    }
	}

	if (this.twitter != null) {
	    for (Entry<String, TwitterConfigDetails> configEntry : this.twitter.entrySet()) {
		list.add(configEntry.getValue());
	    }
	}

	if (this.telegram != null) {
	    for (Entry<String, TelegramConfigDetails> configEntry : this.telegram.entrySet()) {
		list.add(configEntry.getValue());
	    }
	}
	return list;
    }

    public String getAccountKey() {
	if (!ArgUtil.is(this.accountKey)) {
	    this.accountKey = Random.randomAlphaNumeric(10);
	}
	return accountKey;
    }

    public void setAccountKey(String accountKey) {
	this.accountKey = accountKey;
    }

}
