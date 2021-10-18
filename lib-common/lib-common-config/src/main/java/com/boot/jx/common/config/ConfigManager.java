package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.store.ConfigStore;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;

@Service
@PropertySource("classpath:application.app.properties")
public class ConfigManager {

    @Autowired
    private CommonMongoTemplate mongoTemplate;

    @Autowired
    public ConfigStore configStore;

    @Autowired
    private SharedConfigManager sharedConfigManager;

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

    @Autowired
    PMClientConfig pmClientConfig;

    public List<Map<String, Object>> getSetupConfigs() {
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	for (ConfigMeta meta : ConfigConstants.SETUP_CONFIG_LIST) {

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

    public List<Map<String, Object>> getAppConfigs() {
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	for (Entry<String, String> entry : ConfigConstants.APP_CONFIG.entrySet()) {
	    list.add(MapBuilder.map().put("config", pmEnvironment.get(entry.getKey())).toMap());
	}
	return list;
    }

    public List<Map<String, Object>> getConfigs(String key) {
	if (!ArgUtil.is(key)) {
	    return this.getSetupConfigs();
	}

	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	BuilderMap mapBuilder = MapBuilder.map();

	mapBuilder.put("meta", new ConfigMeta().key(key));
	for (ConfigMeta meta : ConfigConstants.SETUP_CONFIG_LIST) {
	    if (meta.getKey().equals(key)) {
		mapBuilder.put("meta", meta);
	    }
	}
	mapBuilder.put("domain", this.pmEnvironment.config().getPref(key)) // Domain
		.put("shared", this.pmEnvironment.shared().getPref(key)) // Shared
		.put("config", this.pmEnvironment.get(key)) // Resolved
	;

	list.add(mapBuilder.toMap());

	return list;
    }

    public void deleteAdminConfigs(String key) {
	PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

	if (ArgUtil.isEmpty(doc)) {
	    doc = new PMConfigurationDoc();
	    doc.setTenant(AppContextUtil.getTenant());
	}

	switch (key) {
	case "postman.bot.name":
	case "postman.default.sender":
	    doc.agent().setDefaultBotName(null);
	default:
	    doc.prefs().remove(key);
	    PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
	    prefsConfigDoc.setId(key);
	    configStore.remove(prefsConfigDoc);
	    break;
	}

	configStore.save(doc);
	this.refresh();
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
	    PMConfigurationObject configObject = doc.getPref(config.getKey());
	    configObject.setKey(config.getKey());
	    configObject.setValue(config.getValue());
	    configObject.setShared(config.isShared());

	    doc.setPref(configObject);

	    PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
	    prefsConfigDoc.setId(configObject.getKey());
	    prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
	    configStore.savePrefsConfig(prefsConfigDoc);

	    break;
	}

	configStore.saveConfiguration(doc);
	this.refresh();
    }

    @Deprecated
    public void saveConfigs(PMConfiguration config) {
	pmEnvironment.config(config);
	this.refresh();
    }

    public void save(ChannelConfig config) {
	pmEnvironment.config(config);
	this.refresh();
	connectorHandlerFactory.registerWebHook(config.getChannelType(), config.getLane());
    }

    public ClientKeyConfigDoc save(ClientKeyConfigDoc clientApiKey) {
	clientApiKey.setKey(PostManUtil.UNIQUE_API_KEY());
	configStore.saveClientKeyConfig(clientApiKey);
	this.refresh();
	return clientApiKey;
    }

    public ClientKeyConfigDoc remove(ClientKeyConfigDoc clientApiKey) {
	mongoTemplate.remove(clientApiKey);
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

    public ChannelConfig getChannelConfig(String channelId) {
	if (ArgUtil.is(channelId)) {
	    ChannelConfigDoc channelConfig = mongoTemplate.findById(channelId, ChannelConfigDoc.class);
	    if (ArgUtil.is(channelConfig)) {
		PMConfiguration config = pmEnvironment.config();
		if (!ArgUtil.is(channelConfig.getWebhookUrl())) {
		    channelConfig.setWebhookUrl(pmClientConfig.getWebhookBase(channelConfig));
		}
		channelConfig.setCallbackPath(PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelConfig));
		return channelConfig;
	    }
	}
	return null;
    }

    public ChannelConfig saveChannelConfig(String channelType, boolean disabled, Map<String, Object> data) {
	MapModel map = MapModel.from(data);
	ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.MAP.get(channelType);
	String channelId = map.getString("channelId");
	if (ArgUtil.is(data)) {
	    AChannelDetails configDetails = null;
	    if (ArgUtil.is(channelId)) {
		ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
		configDetails = plugin.getDetails(channelConfig);
		plugin.extractChannelDetailsFromMap(configDetails, map, channelType);
	    } else {
		configDetails = plugin.getChannelDetailsFromMap(map);
	    }
	    configDetails.setName(map.getString("name", configDetails.getName()));
	    configDetails.setChannelKey(map.getString("channelKey", configDetails.getChannelKey()));
	    save(configDetails, disabled);
	}
	return getChannelConfig(channelId);
    }

    public ChannelConfig removeChannelConfig(String channelId) {
	if (ArgUtil.is(channelId)) {
	    ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
	    pmEnvironment.remove(channelConfig);
	    this.refresh();
	    return channelConfig;
	}
	return null;
    }

    public void refresh() {
	sharedConfigManager.clear();
    }

    @Autowired
    private Environment environment;

    @SuppressWarnings("rawtypes")
    @PostConstruct
    public void init() {
	for (org.springframework.core.env.PropertySource<?> propertySource : ((ConfigurableEnvironment) environment)
		.getPropertySources()) {
	    if (propertySource instanceof EnumerablePropertySource) {
		for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
		    for (String prefix : ConfigConstants.APP_CONFIG_PREFIX) {
			if (key.startsWith(prefix)) {
			    String shortKey = key.replace("mry.prop.", "");
			    String newKey = shortKey.replaceAll("[\\.@\\-$]", "_").toUpperCase();
			    ConfigConstants.APP_CONFIG.put(key, "PROP_" + newKey);
			}
		    }
		}
	    }
	}
    }

}
