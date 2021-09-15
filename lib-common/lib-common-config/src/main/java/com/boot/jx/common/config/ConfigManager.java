package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.postman.ClientApiKey;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.wa360.WA360ConfigDetails;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
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

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

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
	    PMConfigurationObject configObject = doc.get(config.getKey());
	    configObject.setKey(config.getKey());
	    configObject.setValue(config.getValue());
	    doc.set(configObject);

	    PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
	    prefsConfigDoc.setId(configObject.getKey());
	    prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
	    mongoTemplate.save(prefsConfigDoc);

	    break;
	}

	mongoTemplate.save(doc);
	sharedConfigManager.clear();
    }

    @Deprecated
    public void saveConfigs(PMConfiguration config) {
	pmEnvironment.config(config);
	sharedConfigManager.clear();
    }

    public void save(ChannelConfig config) {
	pmEnvironment.config(config);
	sharedConfigManager.clear();
	connectorHandlerFactory.registerWebHook(config.getChannelType(), config.getLane());
    }

    public ClientKeyConfigDoc save(ClientKeyConfigDoc clientApiKey) {
	clientApiKey.setKey(PostManUtil.UNIQUE_API_KEY());
	mongoTemplate.save(clientApiKey);
	sharedConfigManager.clear();
	return clientApiKey;
    }

    public void save(AChannelDetails details, boolean disabled) {
	ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.MAP.get(details.getChannelType());
	if (ArgUtil.is(plugin)) {
	    ChannelConfig config = new ChannelConfig();
	    plugin.fromDetails(config, details);
	    config.disabled(disabled);
	    save(config);
	}
    }

}
