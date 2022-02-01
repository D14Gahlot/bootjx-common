package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppSharedConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMEnvironmentProvider;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.CompanyVarsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.ConfigStore;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.model.SafeKeyHashMap;
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
    public PMConfigurationModel local() {
	String tnt = AppContextUtil.getTenant();
	if (localConfigMap.containsKey(tnt)) {
	    return localConfigMap.get(tnt);
	}

	if (ArgUtil.is(configStore)) {
	    PMConfigurationDoc prefs = getPMConfigurationDoc();

	    List<PrefsConfigDoc> prefsConfigs = configStore.findAll(PrefsConfigDoc.class);
	    for (PrefsConfigDoc prefsConfig : prefsConfigs) {
		prefs.setPref(prefsConfig);
	    }

	    List<ChannelConfigDoc> channels = configStore.findAll(ChannelConfigDoc.class);
	    for (ChannelConfigDoc channel : channels) {
		channel.setDomain(tnt);
		prefs.channels(channel);
	    }

	    List<ClientKeyConfigDoc> clientKeys = configStore.findAll(ClientKeyConfigDoc.class);
	    for (ClientKeyConfigDoc clientKey : clientKeys) {
		prefs.clientApiKey(clientKey);
	    }

	    List<CompanyVarsConfigDoc> companyVars = configStore.findAll(CompanyVarsConfigDoc.class);

	    SafeKeyHashMap<Object> company = prefs.globalVars();

	    for (CompanyVarsConfigDoc companyVar : companyVars) {
		company.put(companyVar.getKey(), companyVar.getValue());
	    }

	    if (ArgUtil.is(prefs)) {
		prefs.setUpdateStamp(System.currentTimeMillis());
		localConfigMap.put(tnt, prefs);
	    }

	    if (Tenants.isDefault(tnt)) {
		PMConfigurationDoc newSharedConfiguration = new PMConfigurationDoc();
		for (Entry<String, PMConfigurationObject> entry : prefs.prefs().entrySet()) {
		    // if (entry.getValue().isShared()) {
		    newSharedConfiguration.setPref(entry.getValue());
		    // }
		}
		List<ChannelConfigDoc> sandboxChannels = configStore.findAll(ChannelConfigDoc.class);
		for (ChannelConfigDoc channel : sandboxChannels) {
		    channel.setDomain(tnt);
		    if (channel.isSandbox() || channel.isShared()) {
			newSharedConfiguration.channels(channel);
		    }
		}

		sharedConfiguration = newSharedConfiguration;
	    }

	    return prefs;
	}
	return null;
    }

    public void configInternal(ChannelConfig config) {
	ChannelConfigDoc doc = EntityDtoUtil.dtoToEntity(config, new ChannelConfigDoc());
	doc.setId(StringUtils.toLowerCase(doc.getChannelId()));
	configStore.saveChannelConfig(doc);
    }

    @Override
    public void addChannel(ChannelConfig config) {
	configInternal(config);
    }

    @Override
    public void updateChannel(ChannelConfig config, String action) {
	if (ArgUtil.is(config)) {
	    ChannelConfigDoc configDoc = EntityDtoUtil.dtoToEntity(config, new ChannelConfigDoc());
	    if ("remove".equalsIgnoreCase(action)) {
		configStore.remove(configDoc);
		PMConfigurationDoc doc = getPMConfigurationDoc();
		doc.channels().remove(config.getChannelId());
		configStore.save(doc);
	    } else if ("disable".equalsIgnoreCase(action)) {
		configDoc.disabled(true);
		configStore.save(configDoc);
	    } else if ("enable".equalsIgnoreCase(action)) {
		configDoc.disabled(false);
		configStore.save(configDoc);
	    } else if ("sandbox_enable".equalsIgnoreCase(action)) {
		configDoc.setSandbox(true);
		configStore.save(configDoc);
	    } else if ("sandbox_disable".equalsIgnoreCase(action)) {
		configDoc.setSandbox(false);
		configStore.save(configDoc);
	    } else if ("shared_enable".equalsIgnoreCase(action)) {
		configDoc.setShared(true);
		configStore.save(configDoc);
	    } else if ("shared_disable".equalsIgnoreCase(action)) {
		configDoc.setShared(false);
		configStore.save(configDoc);
	    }
	} else {
	    System.out.println("No Channel to delete");
	}
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
    public PMConfigurationModel shared() {
	return sharedConfiguration;
    }

    @Override
    public void initConfig() {
	String sessionId = UniqueID.generateString();
	AppContextUtil.setSessionId(sessionId);
	AppContextUtil.getTraceId(true, true);
	AppContextUtil.resetTraceTime();
	AppContextUtil.init();
	local();
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
