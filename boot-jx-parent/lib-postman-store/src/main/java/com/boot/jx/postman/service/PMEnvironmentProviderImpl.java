package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppSharedConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMEnvironmentProvider;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.store.ConfigStore;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.UniqueID;

@Component
public class PMEnvironmentProviderImpl implements PMEnvironmentProvider, AppSharedConfig {

    private Map<String, PMConfigurationDoc> localConfigMap = new HashMap<String, PMConfigurationDoc>();

    PMConfigurationDoc sharedConfiguration = null;

    @Autowired(required = false)
    private ConfigStore configStore;

    @Override
    public PMConfiguration config() {
	String tnt = AppContextUtil.getTenant();
	if (localConfigMap.containsKey(tnt)) {
	    return localConfigMap.get(tnt);
	}
	if (ArgUtil.is(configStore)) {
	    PMConfigurationDoc prefs = getPMConfigurationDoc();

	    List<PrefsConfigDoc> prefsConfigs = configStore.findAll(PrefsConfigDoc.class);
	    for (PrefsConfigDoc prefsConfig : prefsConfigs) {
		prefs.set(prefsConfig);
	    }

	    List<ChannelConfigDoc> channels = configStore.findAll(ChannelConfigDoc.class);
	    for (ChannelConfigDoc channel : channels) {
		prefs.channels(channel);
	    }

	    List<ClientKeyConfigDoc> clientKeys = configStore.findAll(ClientKeyConfigDoc.class);
	    for (ClientKeyConfigDoc clientKey : clientKeys) {
		prefs.clientApiKey(clientKey);
	    }

	    if (ArgUtil.is(prefs)) {
		localConfigMap.put(tnt, prefs);
	    }

	    if (Tenants.isDefault(tnt)) {
		PMConfigurationDoc newSharedConfiguration = new PMConfigurationDoc();
		for (Entry<String, PMConfigurationObject> entry : prefs.map().entrySet()) {
		    //if (entry.getValue().isShared()) {
			newSharedConfiguration.set(entry.getValue());
		    //}
		}
		sharedConfiguration = newSharedConfiguration;
	    }

	    return prefs;
	}
	return null;
    }

    @Deprecated
    public void config(PMConfiguration config) {
	if (ArgUtil.is(configStore)) {
	    for (Entry<String, ChannelPlugin<? extends AChannelDetails>> pluginEntry : ChannelPluginProvider.MAP
		    .entrySet()) {
		ChannelPlugin<? extends AChannelDetails> plugin = pluginEntry.getValue();
		Map<String, ? extends AChannelDetails> multipleDetails = plugin.getDetails(config);
		if (ArgUtil.is(multipleDetails)) {
		    for (Entry<String, ? extends AChannelDetails> configEntry : multipleDetails.entrySet()) {
			configInternal(plugin.fromDetails(new ChannelConfigDoc(), configEntry.getValue()));
		    }
		}

	    }

	    PMConfigurationDoc doc = EntityDtoUtil.dtoToEntity(config, new PMConfigurationDoc());
	    doc.setTenant(AppContextUtil.getTenant());
	    configStore.saveConfiguration(doc);
	}

    }

    public void configInternal(ChannelConfig config) {
	ChannelConfigDoc doc = EntityDtoUtil.dtoToEntity(config, new ChannelConfigDoc());
	doc.setId(StringUtils.toLowerCase(doc.getChannelId()));
	if (config.isDisabled()) {
	    doc.setDisabled(config.isDisabled());
	} else
	    doc.setDisabled(false);
	configStore.saveChannelConfig(doc);
    }

    @Override
    public void config(ChannelConfig config) {
	configInternal(config);

	// @Deperecated - Start
	// This code is only for backward compatibility not to be written for New
	// Channels
	PMConfigurationDoc doc = getPMConfigurationDoc();
	ChannelPlugin<?> plugin = ChannelPluginProvider.MAP.get(config.getChannelType());
	if (ArgUtil.is(plugin)) {
	    plugin.setConfig(doc, config);
	}
	configStore.save(doc);
	// @Deperecated - Ends
    }

    @Override
    public void remove(ChannelConfig config) {
	ChannelConfigDoc configDoc = EntityDtoUtil.dtoToEntity(config, new ChannelConfigDoc());
	configStore.remove(configDoc);
	PMConfigurationDoc doc = getPMConfigurationDoc();
	doc.channels().remove(config.getChannelId());
	configStore.save(doc);
    }

    private PMConfigurationDoc getPMConfigurationDoc() {
	PMConfigurationDoc doc = configStore.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
	if (ArgUtil.isEmpty(doc)) {
	    doc = new PMConfigurationDoc();
	    doc.setTenant(AppContextUtil.getTenant());
	}
	return doc;
    }

    @Override
    public PMConfiguration shared() {
	return sharedConfiguration;
    }

    @Override
    public void initConfig() {
	String sessionId = UniqueID.generateString();
	AppContextUtil.setSessionId(sessionId);
	AppContextUtil.getTraceId(true, true);
	AppContextUtil.resetTraceTime();
	AppContextUtil.init();
	config();
    }

    @Override
    public void clear(Map<String, String> map) {
	String tnt = AppContextUtil.getTenant();
	localConfigMap.remove(tnt);
	if (Tenants.isDefault(tnt)) {
	    this.initConfig();
	}
    }
}
