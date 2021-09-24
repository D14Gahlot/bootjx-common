package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.InputType;
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

    public static final List<ConfigMeta> CONFIG_LIST = new ArrayList<ConfigMeta>();

    @Autowired
    private CommonMongoTemplate mongoTemplate;

    @Autowired
    ConfigStore configStore;

    @Autowired
    private SharedConfigManager sharedConfigManager;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

    @Autowired
    PMClientConfig pmClientConfig;

    public List<Map<String, Object>> getAdminConfigs() {
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

	for (ConfigMeta meta : CONFIG_LIST) {

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

    public List<Map<String, Object>> getAdminConfigs(String key) {
	if (!ArgUtil.is(key)) {
	    return getAdminConfigs();
	}

	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	BuilderMap mapBuilder = MapBuilder.map();

	for (ConfigMeta meta : CONFIG_LIST) {
	    if (meta.getKey().equals(key)) {
		mapBuilder.put("meta", meta);
	    } else {
		mapBuilder.put("meta", new ConfigMeta().key(key));
	    }
	    mapBuilder

		    .put("domain", pmEnvironment.config().get(key)) // Domain
		    .put("shared", pmEnvironment.shared().get(key)) // Shared
		    .put("config", pmEnvironment.get(key)) // Resolved
	    ;
	}

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
	    doc.map().remove(key);
	    PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
	    prefsConfigDoc.setId(key);
	    configStore.remove(prefsConfigDoc);
	    break;
	}

	configStore.save(doc);
	sharedConfigManager.clear();
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
	    configObject.setShared(config.isShared());

	    doc.set(configObject);

	    PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
	    prefsConfigDoc.setId(configObject.getKey());
	    prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
	    configStore.savePrefsConfig(prefsConfigDoc);

	    break;
	}

	configStore.saveConfiguration(doc);
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

    public ChannelConfig getChannelConfig(String channelId) {
	if (ArgUtil.is(channelId)) {
	    ChannelConfigDoc channelConfig = mongoTemplate.findById(channelId, ChannelConfigDoc.class);
	    if (ArgUtil.is(channelConfig)) {
		PMConfiguration config = pmEnvironment.config();
		if (!ArgUtil.is(channelConfig.getWebhookUrl())) {
		    channelConfig.setWebhookUrl(pmClientConfig.getWebhookUrl(channelConfig));
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

    static {
	CONFIG_LIST.add(new ConfigMeta("Bot Name", "postman.bot.name"));
	CONFIG_LIST.add(new ConfigMeta("Contact Details Provider Webhook", "postman.contact.details.url"));

	CONFIG_LIST.add(new ConfigMeta("Chat Tag Enabled", "chat.tag.enabled").optionsOnOff());

	CONFIG_LIST.add(new ConfigMeta("Chat Session Timeout", "postman.chat.session.timeout").optionValues("8hr",
		"12hr", "16hr", "20hr", "24hr"));

	CONFIG_LIST.add(new ConfigMeta("Chat Alert Timer", "postman.chat.idle.timeout").optionValues("5min", "10min",
		"15min", "20min", "25min", "30min"));

	CONFIG_LIST.add(new ConfigMeta("Agent can initiate new chat", "postman.agent.chat.init").optionsOnOff());

	CONFIG_LIST.add(new ConfigMeta("Agent Panel Color Scheme", "postman.agent.scheme.color")
		.inputType(InputType.COLOR).defaultValue("#4b56c0"));
    }

}
