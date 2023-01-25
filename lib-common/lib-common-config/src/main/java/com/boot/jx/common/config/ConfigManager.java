package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.common.config.ConfigConstants.PERMS_KEY;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.PermsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.store.ConfigMaster;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;

@Service
public class ConfigManager {

	@Autowired
	public ConfigMaster configStore;

	@Autowired
	private SharedConfigManager sharedConfigManager;

	@Autowired
	public PMEnvironment pmEnvironment;

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Autowired
	private PMCommonConfigImpl pmCommonConfig;

	public <T> T findById(Object id, Class<T> entityClass) {
		return configStore.findById(id, entityClass);
	}

	public List<Map<String, Object>> getSetupConfigs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (ConfigMeta meta : ConfigConstants.SETUP_CONFIG_LIST) {

			switch (meta.getKey()) {

			case "postman.bot.name":
			case "postman.default.sender":

				PMConfigurationObject configObject = pmEnvironment.keyEntry("postman.bot.name");

				if (!ArgUtil.is(configObject.getValue())) {
					configObject.setValue(pmEnvironment.local().agent().getDefaultBotName());
				}

				list.add(MapBuilder.map().put("meta", meta).put("config", configObject).toMap());

				break;
			default:
				list.add(MapBuilder.map().put("meta", meta).put("config", pmEnvironment.keyEntry(meta.getKey()))
						.toMap());
				break;
			}
		}
		return list;
	}

	public List<Map<String, Object>> getAppConfigs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Entry<String, String> entry : ConfigConstants.APP_CONFIG.entrySet()) {
			list.add(MapBuilder.map().put("config", pmEnvironment.keyEntry(entry.getKey())).toMap());
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
		mapBuilder.put("domain", this.pmEnvironment.local().keyEntry(key)) // Domain
				.put("shared", this.pmEnvironment.shared().keyEntry(key)) // Shared
				.put("config", this.pmEnvironment.keyEntry(key)) // Resolved
		;

		list.add(mapBuilder.toMap());

		return list;
	}

	public void deleteAdminConfigs(String key) {
		PMConfigurationDoc doc = configStore.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

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
			prefsConfigDoc.setKey(key);
			prefsConfigDoc.setId(prefsConfigDoc.getKey() + "." + pmCommonConfig.getServiceServer());
			configStore.remove(prefsConfigDoc);
			break;
		}

		configStore.save(doc);
		this.refresh();
	}

	public void save(PMConfigurationObject config) {
		PMConfigurationDoc doc = configStore.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new PMConfigurationDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		switch (config.getKey()) {
		case "postman.bot.name":
		case "postman.default.sender":
			doc.agent().setDefaultBotName(config.asString());
		default:
			PMConfigurationObject configObject = doc.keyEntry(config.getKey());
			configObject.setKey(config.getKey());
			configObject.setValue(config.getValue());
			configObject.setShared(config.isShared());
			configObject.setDomain(AppContextUtil.getTenant());
			configObject.setServer(pmCommonConfig.getServiceServer());

			doc.setPref(configObject);

			PrefsConfigDoc prefsConfigDoc = new PrefsConfigDoc();
			prefsConfigDoc.setId(configObject.getKey() + "." + pmCommonConfig.getServiceServer());
			prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
			configStore.savePrefsConfig(prefsConfigDoc);

			break;
		}
		configStore.saveConfiguration(doc);
		this.refresh();
	}

	public ClientAppConfigDoc save(ClientAppConfigDoc clientApiKey) {
		ClientApp app = pmEnvironment.config().clientApiKey(clientApiKey.getQueue());
		if (ArgUtil.is(app) && app.isReadOnly()) {
			ApiResponseUtil.throwUnAuthorizedException("ReadOnly App");
		}
		configStore.saveClientKeyConfig(clientApiKey);
		this.refresh();
		return clientApiKey;
	}

	public ClientAppConfigDoc updateClientAppConfig(ClientApp app, String key, Object value) {
		if (ArgUtil.is(app) && app.isReadOnly()) {
			ApiResponseUtil.throwUnAuthorizedException("ReadOnly App");
		}
		ClientAppConfigDoc clientApiKey = configStore.updateClientAppConfig(app, key, value);
		this.refresh();
		return clientApiKey;
	}

	public ClientAppConfigDoc remove(ClientAppConfigDoc clientApiKey) {
		ClientApp app = pmEnvironment.config().clientApiKey(clientApiKey.getId());
		if (ArgUtil.is(app) && app.isReadOnly()) {
			ApiResponseUtil.throwUnAuthorizedException("ReadOnly App");
		}
		configStore.remove(clientApiKey);
		this.refresh();
		return clientApiKey;
	}

	public <T extends VarsConfigDoc> T save(T companyVarsConfig) {
		configStore.saveCompanyVar(companyVarsConfig);
		this.refresh();
		return companyVarsConfig;
	}

	public <T extends VarsConfigDoc> T remove(T companyVarsConfig) {
		configStore.remove(companyVarsConfig);
		this.refresh();
		return companyVarsConfig;
	}

	public ChannelConfig getChannelConfig(String channelId) {
		if (ArgUtil.is(channelId)) {
			ChannelConfigDoc channelConfig = configStore.findById(channelId, ChannelConfigDoc.class);
			if (!ArgUtil.is(channelConfig)) {
				return null;
			}
			ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.PLUGIN_MAPPING
					.get(channelConfig.getChannelType());
			plugin.updatePluginSpecs(channelConfig);
			if (!channelConfig.isReadOnly()) {
				if (plugin.isWebhookManual()) {
					PMConfigurationModel config = pmEnvironment.local();
					if (!ArgUtil.is(channelConfig.getWebhookUrl())) {
						channelConfig.setWebhookUrl(pmClientConfig.getWebhookBase(channelConfig));
						channelConfig.setCallbackPath(
								PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelConfig));
					}
				}
			}
			return channelConfig;
		}
		return null;
	}

	public void save(ChannelConfig config) {
		pmEnvironment.addChannel(config);
		this.refresh();
		connectorHandlerFactory.onChannelUpdate(config.getChannelType(), config.getLane());
	}

	public ChannelConfig saveChannelConfig(String channelType, Map<String, Object> data) {
		MapModel map = MapModel.from(data);
		ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.PLUGIN_MAPPING.get(channelType);
		String channelId = map.getString("channelId");
		if (ArgUtil.is(data)) {
			ChannelConfig config = pmEnvironment.local().channel(channelId);
			if (config == null) {
				config = new ChannelConfig();
			}
			plugin.importChannelConfigFromMap(config, map, channelType);
			save(config);
			channelId = config.getChannelId();

		}
		return getChannelConfig(channelId);
	}

	public ChannelConfig updateChannelConfig(String channelId, String action) {
		if (ArgUtil.is(channelId)) {
			ChannelConfig channelConfig = pmEnvironment.local().channel(channelId);

			if (!ArgUtil.is(channelConfig)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, "Invalid Channel");
			}

			if (channelConfig.isReadOnly()) {
				ApiResponseUtil.throwUnAuthorizedException("Cannot Edit Sandbox Channel");
			}
			pmEnvironment.updateChannel(channelConfig, action);
			this.refresh();
			return channelConfig;
		}
		return null;
	}

	public List<Map<String, Object>> getPerms() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (ConfigMeta meta : ConfigConstants.PERMS_CONFIG_LIST) {
			list.add(MapBuilder.map().put("meta", meta).put("config", pmEnvironment.permEntry(meta.getKey())).toMap());
		}
		return list;
	}

	public List<Map<String, Object>> getPerm(PERMS_KEY key) {
		if (!ArgUtil.is(key)) {
			return this.getPerms();
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		BuilderMap mapBuilder = MapBuilder.map();
		mapBuilder.put("meta", new ConfigMeta().key(key.getKey()));
		for (ConfigMeta meta : ConfigConstants.PERMS_CONFIG_LIST) {
			if (meta.getKey().equals(key)) {
				mapBuilder.put("meta", meta);
			}
		}
		mapBuilder.put("domain", this.pmEnvironment.local().permEntry(key)) // Domain
				.put("shared", this.pmEnvironment.shared().permEntry(key)) // Shared
				.put("config", this.pmEnvironment.permEntry(key)) // Resolved
		;
		list.add(mapBuilder.toMap());
		return list;
	}

	public void savePerm(PermsConfigDoc config) {
		PMConfigurationObject configObject = pmEnvironment.local().permEntry(config.getKey());
		configObject.setKey(config.getKey());
		configObject.setValue(config.getValue());
		configObject.setShared(config.isShared());
		configObject.setDomain(AppContextUtil.getTenant());
		configObject.setServer(pmCommonConfig.getServiceServer());

		PermsConfigDoc prefsConfigDoc = new PermsConfigDoc();
		prefsConfigDoc.setId(configObject.getKey() + "." + pmCommonConfig.getServiceServer());
		prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
		configStore.savePermConfig(prefsConfigDoc);
		this.refresh();
	}

	public void deletePerm(PERMS_KEY key) {
		pmEnvironment.local().perms().remove(key);
		PermsConfigDoc prefsConfigDoc = new PermsConfigDoc();
		prefsConfigDoc.setKey(key.getKey());
		prefsConfigDoc.setId(prefsConfigDoc.getKey() + "." + pmCommonConfig.getServiceServer());
		configStore.remove(prefsConfigDoc);
		this.refresh();
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
