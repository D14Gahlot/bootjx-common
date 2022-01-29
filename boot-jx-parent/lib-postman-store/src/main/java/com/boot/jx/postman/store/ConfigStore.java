package com.boot.jx.postman.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.CompanyVarsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.mongodb.WriteResult;
import com.mongodb.client.result.DeleteResult;

@Component
public class ConfigStore extends CommonMongoTemplateAbstract {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStore.class);

    public void saveConfiguration(PMConfigurationDoc doc) {
	doc.getAccountKey(); // Populate Keys of not exists
	save(doc);
    }

    public void savePrefsConfig(PrefsConfigDoc prefsConfigDoc) {
	save(prefsConfigDoc);
	log(prefsConfigDoc, "updated");
    }

    public void saveChannelConfig(ChannelConfigDoc doc) {
	doc.getChannelKey(); // Populate Keys of not exists
	save(doc);
	log(doc, "updated");
    }

    public void saveClientKeyConfig(ClientKeyConfigDoc clientApiKey) {
	try {
	    boolean generated = false;
	    if (!ArgUtil.is(clientApiKey.getId()) || !ArgUtil.is(clientApiKey.getKey())) {
		clientApiKey.setKey(PostManUtil.UNIQUE_API_KEY());
		generated = true;
	    } else {
		ClientKeyConfigDoc oldDoc = findByIdString(clientApiKey.getId(), ClientKeyConfigDoc.class);
		clientApiKey.setKey(oldDoc.getKey());
	    }
	    save(clientApiKey);
	    log(clientApiKey, "updated");
	    if (generated == false) {
		clientApiKey.setKey("");
	    }
	} catch (Exception e) {
	    LOGGER.error("saveClientKeyConfig", e);
	}
    }

    public DeleteResult remove(Object object) {
	DeleteResult r = super.remove(object);
	log(object, "deleted");
	return r;
    }

    public void saveCompanyVar(CompanyVarsConfigDoc companyVarsConfig) {
	try {
	    save(companyVarsConfig);
	    log(companyVarsConfig, "updated");
	} catch (Exception e) {
	    LOGGER.error("saveClientKeyConfig", e);
	}
    }

}
