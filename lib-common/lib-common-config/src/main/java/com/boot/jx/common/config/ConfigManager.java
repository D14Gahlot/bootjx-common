package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.ChannelConfig;
import com.boot.jx.postman.ClientApiKey;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ClientApiKeyDoc;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.MapBuilder;

@Service
@PropertySource("classpath:application.app.properties")
public class ConfigManager {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SharedConfigManager sharedConfigManager;

    @Autowired
    private PMEnvironment pmEnvironment;

    public List<Map<String, Object>> getAdminConfigs() {
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

	for (ConfigBuilder meta : ConfigBuilder.LIST) {

	    switch (meta.getKey()) {

	    case "postman.bot.name":
	    case "postman.default.sender":

		PMConfigurationObject configObject = pmEnvironment.get("postman.bot.name");

		if (!ArgUtil.is(configObject.getValue())) {
		    configObject.setValue(pmEnvironment.config().agent().getDefaultBotName());
		}

		list.add(MapBuilder.map().put("meta", meta).put("config", configObject).toMap());

		break;
	    default:
		list.add(MapBuilder.map().put("meta", meta).put("config", pmEnvironment.get(meta.getKey())).toMap());
		break;
	    }
	}
	return list;
    }

    public void save(PMConfigurationObject config) {
	PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

	if (ArgUtil.isEmpty(doc)) {
	    doc = new PMConfigurationDoc();
	    doc.setTenant(AppContextUtil.getTenant());
	}

	switch (config.getKey()) {
	case "postman.bot.name":
	case "postman.default.sender":
	    doc.agent().setDefaultBotName(config.asString());
	default:
	    PMConfigurationObject configDoc = doc.get(config.getKey());
	    configDoc.setKey(config.getKey());
	    configDoc.setValue(config.getValue());
	    doc.set(configDoc);
	    break;
	}
	mongoTemplate.save(doc);
	sharedConfigManager.clear();
    }

    public void saveConfigs(PMConfiguration config) {
	pmEnvironment.config(config);
	sharedConfigManager.clear();
    }

    public void save(ChannelConfig config) {
	pmEnvironment.config(config);
	sharedConfigManager.clear();
    }

    public ClientApiKeyDoc save(ClientApiKeyDoc clientApiKey) {
	clientApiKey.setKey(ClientApiKey.generateApiKey());
	mongoTemplate.save(clientApiKey);
	sharedConfigManager.clear();
	return clientApiKey;
    }

}
