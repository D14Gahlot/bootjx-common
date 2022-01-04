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
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.Random;

public class PMConfiguration implements Serializable {

    private static final long serialVersionUID = -5432956433673368768L;

    private Map<String, ChannelConfig> channels;
    private Map<String, ClientApiKey> clientApiKeys;

    private Map<String, PMConfigurationObject> prefs;
    private Map<String, Object> companyVars;

    private AgentConfig agent;
    private String accountKey;

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

    public PMConfigurationObject getPref(String key) {
	return prefs().getOrDefault(key, new PMConfigurationObject(key, null));
    }

    public PMConfigurationObject getPref(String key, Object value) {
	return prefs().getOrDefault(key, new PMConfigurationObject(key, value));
    }

    public PMConfiguration setPref(PMConfigurationObject map) {
	this.prefs().put(map.getKey(), map);
	return this;
    }

    public List<AChannelDetails> listChannels() {
	List<AChannelDetails> list = new ArrayList<AChannelDetails>();
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

    public SafeKeyHashMap<Object> global() {
	if (ArgUtil.isEmpty(companyVars)) {
	    companyVars = new HashMap<String, Object>();
	}
	return new SafeKeyHashMap<Object>(companyVars);
    }

}
