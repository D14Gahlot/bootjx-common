package com.boot.jx.postman.store;

import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;

@Component
public class ConfigStore extends CommonMongoTemplateAbstract {

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

}
