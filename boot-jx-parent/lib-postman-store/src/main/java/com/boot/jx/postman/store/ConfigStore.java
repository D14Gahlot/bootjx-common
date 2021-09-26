package com.boot.jx.postman.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;

@Component
public class ConfigStore extends CommonMongoTemplateAbstract {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStore.class);

    public void saveConfiguration(PMConfigurationDoc doc) {
	doc.getAccountKey(); // Populate Keys of not exists
	save(doc);
    }

    public void savePrefsConfig(PrefsConfigDoc prefsConfigDoc) {
	save(prefsConfigDoc);
    }

    public void saveChannelConfig(ChannelConfigDoc doc) {
	doc.getChannelKey(); // Populate Keys of not exists
	save(doc);
    }

    public void saveClientKeyConfig(ClientKeyConfigDoc clientApiKey) {
	try {
	    save(clientApiKey);
	} catch (Exception e) {
	    LOGGER.error("saveClientKeyConfig", e);
	}
    }

}
