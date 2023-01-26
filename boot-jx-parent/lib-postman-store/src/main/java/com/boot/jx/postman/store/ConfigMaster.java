package com.boot.jx.postman.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.PermsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc;
import com.mongodb.WriteResult;

@Component
public class ConfigMaster extends CommonMongoTemplateAbstract {

	@Autowired(required = false)
	private ConfigStore configStore;

	public void saveChannelConfig(ChannelConfigDoc configDoc) {
		configStore.saveChannelConfig(configDoc);
		configStore.saveMaster(configDoc);
	}

	public void saveChannelConfig(ChannelConfigDoc configDoc, String action) {
		configStore.saveChannelConfig(configDoc, action);
		configStore.saveMaster(configDoc);
	}

	public WriteResult remove(ChannelConfigDoc configDoc) {
		WriteResult r = configStore.remove(configDoc);
		configDoc.setDeleted(true);
		configDoc.setDisabled(true);
		configStore.saveMaster(configDoc);
		return r;
	}

	public void savePrefsConfig(PrefsConfigDoc prefsConfigDoc) {
		configStore.savePrefsConfig(prefsConfigDoc);
		configStore.saveMaster(prefsConfigDoc);
	}

	public void saveConfiguration(PMConfigurationDoc doc) {
		configStore.saveConfiguration(doc);
	}

	public void saveClientKeyConfig(ClientAppConfigDoc clientApiKey) {
		configStore.saveClientKeyConfig(clientApiKey);
	}

	public <T extends VarsConfigDoc> void saveCompanyVar(T companyVarsConfig) {
		configStore.saveCompanyVar(companyVarsConfig);
	}

	public void savePermConfig(PermsConfigDoc permConfigDoc) {
		configStore.savePermConfig(permConfigDoc);
		configStore.saveMaster(permConfigDoc);
	}

}
