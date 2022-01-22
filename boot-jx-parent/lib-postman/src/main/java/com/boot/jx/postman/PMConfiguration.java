package com.boot.jx.postman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.agent.AgentConfig;
import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.model.MapModel.NodeEntry;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.Random;

public interface PMConfiguration extends Serializable {

    public ChannelConfig channel(String channelId);

    public NodeEntry<Object> keyEntry(String string);

    public List<AChannelConfig> listChannels();

    public static class PMConfigurationModel implements PMConfiguration {

	private static final long serialVersionUID = -5432956433673368768L;

	private Map<String, ChannelConfig> channels;
	private Map<String, ClientApiKey> clientApiKeys;

	private Map<String, PMConfigurationObject> prefs;
	private Map<String, Object> globalVars;

	private AgentConfig agent;
	private String accountKey;
	private long updateStamp;

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

	public ChannelConfig channel(String channelId) {
	    if (!ArgUtil.is(channelId)) {
		return null;
	    }
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
	public Map<String, PMConfigurationObject> getPrefs() {
	    return prefs;
	}

	public void setPrefs(Map<String, PMConfigurationObject> prefs) {
	    this.prefs = prefs;
	}

	public SafeKeyHashMap<PMConfigurationObject> prefs() {
	    if (ArgUtil.isEmpty(prefs)) {
		prefs = new HashMap<String, PMConfigurationObject>();
	    }
	    return new SafeKeyHashMap<PMConfigurationObject>(prefs);
	}

	public PMConfigurationObject keyEntry(String key) {
	    return prefs().getOrDefault(key, new PMConfigurationObject(key, null));
	}

	public PMConfigurationObject getPref(String key, Object value) {
	    return prefs().getOrDefault(key, new PMConfigurationObject(key, value));
	}

	public PMConfiguration setPref(PMConfigurationObject map) {
	    this.prefs().put(map.getKey(), map);
	    return this;
	}

	public List<AChannelConfig> listChannels() {
	    List<AChannelConfig> list = new ArrayList<AChannelConfig>();
	    for (Entry<String, ChannelConfig> aChannelDetails : this.channels().entrySet()) {
		list.add(aChannelDetails.getValue());
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

	public long getUpdateStamp() {
	    return updateStamp;
	}

	public void setUpdateStamp(long updateStamp) {
	    this.updateStamp = updateStamp;
	}

	public SafeKeyHashMap<Object> globalVars() {
	    if (ArgUtil.isEmpty(globalVars)) {
		globalVars = new HashMap<String, Object>();
	    }
	    return new SafeKeyHashMap<Object>(globalVars);
	}
    }

    public static PMConfigurationModel instance() {
	return new PMConfigurationModel();
    }

    public static class PMConfigurationWrappper implements PMConfiguration {

	private static final long serialVersionUID = -5108277431528919820L;
	private static PMConfigurationModel DEFAULT = PMConfiguration.instance();

	private PMConfigurationModel local;
	private PMConfigurationModel shared;

	private AppConfig appConfig;

	public PMConfigurationWrappper local(PMConfigurationModel local) {
	    this.local = local;
	    return this;
	}

	public PMConfigurationWrappper shared(PMConfigurationModel shared) {
	    this.shared = shared;
	    return this;
	}

	public PMConfigurationWrappper appConfig(AppConfig appConfig) {
	    this.appConfig = appConfig;
	    return this;
	}

	public PMConfigurationModel local() {
	    return (this.local != null) ? this.local : DEFAULT;
	}

	public PMConfigurationModel shared() {
	    return (this.shared != null) ? this.shared : DEFAULT;
	}

	@Override
	public ChannelConfig channel(String channelId) {
	    ChannelConfig x = this.local().channel(channelId);
	    if (ArgUtil.is(x)) {
		return x;
	    }
	    return this.shared().channel(channelId);
	}

	@Override
	public PMConfigurationObject keyEntry(String key) {
	    PMConfigurationObject configObject = this.local().prefs().get(key);
	    String tnt = AppContextUtil.getTenant();
	    if (ArgUtil.isEmpty(configObject) && !Tenants.isDefault(tnt)) {
		PMConfigurationObject sharedConfigObject = this.shared().prefs().get(key);
		if (ArgUtil.is(sharedConfigObject)) {
		    return sharedConfigObject;
		}
	    }

	    if (ArgUtil.isEmpty(configObject)) {
		String value = appConfig.prop(key);
		configObject = new PMConfigurationObject(key, value);
		// this.config().map().put(key, configObject);
	    }

	    return configObject;
	}

	@Override
	public List<AChannelConfig> listChannels() {
	    List<AChannelConfig> list = this.local().listChannels();
	    if (keyEntry("postman.chat.channel.sandbox").asBoolean()) {
		list.addAll(this.shared().listChannels());
	    }
	    return list;
	}

    }

}
