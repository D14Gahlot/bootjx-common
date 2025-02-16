package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.boot.jx.AppConfigPackage.AppSharedConfigChange;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.model.ModelPatch;
import com.boot.jx.model.ModelPatch.ModelPatchCommand;
import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.client.CommonServiceClient;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.FeaturesConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.doc.config.UserPrefsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc.CompanyTokenKeyDoc;
import com.boot.jx.postman.manager.ConfigManager;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.store.ConfigMaster;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;
import com.boot.utils.StringUtils;

@Service
public class ConfigManagerImpl implements ConfigManager {

	public static Logger LOGGER = LoggerService.getLogger(ConfigManagerImpl.class);

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

	@Autowired
	private CommonServiceClient commonServiceClient;

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
				list.add(MapBuilder.map().put("meta", meta).put("domain", pmEnvironment.local().keyEntry(meta.getKey())) // Domain
						.put("shared", pmEnvironment.shared().keyEntry(meta.getKey())) // Shared
						.put("config", pmEnvironment.keyEntry(meta.getKey())) // Resolved
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

	@Override
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

			if (ArgUtil.is(config.getKey(), CONFIG_SETUP_KEY.POSTMAN_TIMEZONE_OFFSET.getKey())) {
				commonServiceClient.publishTimezoneUpdatedEvent(prefsConfigDoc.asString());
			}
		}
		configStore.saveConfiguration(doc);
		this.refresh();
	}

	@Override
	public ClientAppConfigDoc save(ClientAppConfigDoc clientApiKey) {
		ClientApp app = pmEnvironment.config().clientApiKey(clientApiKey.getQueue());
		if (ArgUtil.is(app) && app.isReadOnly()) {
			ApiResponseUtil.throwUnAuthorizedException("ReadOnly App");
		}
		if (ArgUtil.is(app)) {
			app.secret().putAll(clientApiKey.secret());
			clientApiKey.secret().putAll(app.secret());
		}
		configStore.saveClientKeyConfig(clientApiKey);
		this.refresh();
		return clientApiKey;
	}

	public ClientApp patchClientApiKey(ModelPatches patches) throws InstantiationException, IllegalAccessException {
		ClientApp app = pmEnvironment.config().clientApiKey(patches.getId());

		if (ArgUtil.is(app) && app.isReadOnly()) {
			ApiResponseUtil.throwUnAuthorizedException("ReadOnly App");
		}

		if (!ArgUtil.is(patches.getPatches())) {
			return (ClientAppConfigDoc) app;
		}

		configStore.patch(patches, ClientAppConfigDoc.class);
		this.refresh();
		return (ClientAppConfigDoc) app;
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

	public <T extends VarsConfigDoc> T patch(ModelPatches patches, Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		T thisConfig = configStore.findById(patches.getId(), clazz);

		if (!ArgUtil.is(patches.getPatches())) {
			return thisConfig;
		}

		Optional<ModelPatch> primaryPatch = patches.getPatches().stream()
				.filter(patch -> "primary".equals(patch.getField())).findFirst();
		if (primaryPatch.isPresent()) {
			if (ArgUtil.is(thisConfig) && ArgUtil.is(thisConfig.getType())) {
				configStore.updateMulti(MQB.select(clazz).where("type", thisConfig.getType()).set("primary", false));
			}
		}
		configStore.patch(patches, CompanyTokenKeyDoc.class);
		this.refresh();
		return thisConfig;
	}

	public ChannelConfig getChannelConfig(String channelId) {
		if (ArgUtil.is(channelId)) {
			ChannelConfig channelConfig = configStore.findById(channelId, ChannelConfigDoc.class);
			if (!ArgUtil.is(channelConfig)) {
				return null;
			}
			ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.get(channelConfig.getChannelType());
			plugin.updatePluginSpecs(channelConfig);
			if (!channelConfig.isReadOnly()) {
				if (plugin.isWebhookManual()) {
					PMConfigurationModel config = pmEnvironment.local();
					if (!ArgUtil.is(channelConfig.getWebhookUrl())) {
						channelConfig.setWebhookUrl(pmClientConfig.getWebhookBase(channelConfig, null));
						channelConfig.setCallbackPath(
								PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelConfig));
					}
				}
			}
			return channelConfig;
		}
		return null;
	}

	@Override
	public void save(ChannelConfig config) {
		// LOGGER.info("save");
		pmEnvironment.addChannel(config);
		this.refresh(ChannelConfigDoc.DOCUMENT_NAME, config.getChannelId());
		config = connectorHandlerFactory.onChannelUpdate(config.getChannelType(), config.getLane());

	}

	@Override
	public ChannelConfig saveChannelConfig(String channelType, Map<String, Object> data) {
		LOGGER.info("onChannelUpdate:{}", channelType);
		MapModel map = MapModel.from(data);
		ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.get(channelType);
		String channelId = map.getString("channelId");
		String lane = map.getString("lane");
		String apiVersion = map.getString("apiVersion");
		String channelConfigTempId = map.getString("channelConfigTempId");
		String masterChannelId = map.getString("masterChannelId");

		boolean isAutoCreated = map.entry("isAutoCreated").asBoolean(Boolean.FALSE);

		if (ArgUtil.is(data)) {
			ChannelConfig config = pmEnvironment.local().channel(channelId);
			if (config == null) {
				config = new ChannelConfig();
				config.setLane(lane);
				config.setAutoCreated(isAutoCreated);
				config.setApiVersion(apiVersion);
				config.setChannelConfigTempId(channelConfigTempId);
				config.setMasterChannelId(masterChannelId);
			}
			plugin.importChannelConfigFromMap(config, map, channelType);
			save(config);
			channelId = config.getChannelId();

		}
		return getChannelConfig(channelId);
	}

	@Override
	public ChannelConfig patchChannelConfig(ModelPatches req) {
		String channelId = req.getId();
		ChannelConfig config = configStore.findById(channelId, ChannelConfigDoc.class);
		MapModel map = MapModel.from(JsonUtil.toMap(config));
		for (ModelPatch patch : req.getPatches()) {
			if (ModelPatchCommand.SET.equals(patch.getCommand())) {
				map.put(JsonPath.at(patch.getField()), patch.getValue());
			} else if (ModelPatchCommand.REMOVE.equals(patch.getCommand())) {
				map.remove(JsonPath.at(patch.getField()));
			}
		}
		return saveChannelConfig(config.getChannelType(), map.map());
	}

	@Override
	@Async
	public void saveForDomain(ChannelConfig config, String domain) {
		LOGGER.info("onChannelUpdate");
		AppContextUtil.clear();
		AppContextUtil.setTenant(domain);
		AppContextUtil.init();
		Map<String, Object> map = JsonUtil.toMap(config);
		map.remove("id");
		map.remove("channelId");
		this.saveChannelConfig(config.getChannelType(), map);
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
			this.refresh(ChannelConfigDoc.DOCUMENT_NAME, channelId);
			return channelConfig;
		}
		return null;
	}

	public List<Map<String, Object>> getFeature() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (ConfigMeta meta : ConfigConstants.PERMS_CONFIG_LIST) {
			list.add(MapBuilder.map().put("meta", meta).put("config", pmEnvironment.featureEntry(meta.getKey()))
					.toMap());
		}
		return list;
	}

	public List<Map<String, Object>> getFeature(CONFIG_FEATURES_KEY key) {
		if (!ArgUtil.is(key)) {
			return this.getFeature();
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		BuilderMap mapBuilder = MapBuilder.map();
		mapBuilder.put("meta", new ConfigMeta().key(key.getKey()));
		for (ConfigMeta meta : ConfigConstants.PERMS_CONFIG_LIST) {
			if (meta.getKey().equals(key)) {
				mapBuilder.put("meta", meta);
			}
		}
		mapBuilder.put("domain", this.pmEnvironment.local().featureEntry(key)) // Domain
				.put("shared", this.pmEnvironment.shared().featureEntry(key)) // Shared
				.put("config", this.pmEnvironment.featureEntry(key)) // Resolved
		;
		list.add(mapBuilder.toMap());
		return list;
	}

	public void saveFeature(FeaturesConfigDoc config) {
		PMConfigurationObject configObject = pmEnvironment.local().featureEntry(config.getKey());
		configObject.setKey(config.getKey());
		configObject.setValue(config.getValue());
		configObject.setShared(config.isShared());
		configObject.setDomain(AppContextUtil.getTenant());
		configObject.setServer(pmCommonConfig.getServiceServer());

		FeaturesConfigDoc prefsConfigDoc = new FeaturesConfigDoc();
		prefsConfigDoc.setId(configObject.getKey() + "." + pmCommonConfig.getServiceServer());
		prefsConfigDoc = EntityDtoUtil.dtoToEntity(configObject, prefsConfigDoc);
		configStore.saveFeatureConfig(prefsConfigDoc);
		this.refresh();
	}

	public void deletePerm(CONFIG_FEATURES_KEY key) {
		pmEnvironment.local().features().remove(key);
		FeaturesConfigDoc prefsConfigDoc = new FeaturesConfigDoc();
		prefsConfigDoc.setKey(key.getKey());
		prefsConfigDoc.setId(prefsConfigDoc.getKey() + "." + pmCommonConfig.getServiceServer());
		configStore.remove(prefsConfigDoc);
		this.refresh();
	}

	@Override
	public void refresh() {
		sharedConfigManager.clear();
	}

	@Override
	public void refresh(String configType, String configId) {
		AppSharedConfigChange change = new AppSharedConfigChange();
		change.setConfigId(configId);
		change.setConfigType(configType);
		sharedConfigManager.clear(change);
	}

	@Autowired(required = false)
	private AppAuthModels.AppCommonAuthUser appCommonAuthUser;

	public void saveUserPrefs(UserPrefsConfigDoc config, boolean domainLevel) {
		if (!domainLevel) {
			config.setUser("#");
		} else {
			config.setUser(ArgUtil.is(appCommonAuthUser) ? appCommonAuthUser.getAuthUser() : null);
		}
		if (ArgUtil.is(config.getUser())) {
			config.setId(StringUtils.toLowerCase(config.getKey() + "." + config.getUser()));
			UserPrefsConfigDoc configObject = configStore.findByIdOrDefault(config.getId(), config);
			configObject.setKey(config.getKey());
			configObject.setValue(config.getValue());
			configObject.setShared(config.isShared());
			configObject.setDomain(AppContextUtil.getTenant());
			configObject.setServer(pmCommonConfig.getServiceServer());
			configStore.save(configObject);
		}
	}

	public List<UserPrefsConfigDoc> getUserPrefs() {
		String authUser = ArgUtil.is(appCommonAuthUser) ? appCommonAuthUser.getAuthUser() : null;
		List<UserPrefsConfigDoc> prefs = configStore.collection(UserPrefsConfigDoc.class)
				.with(Criteria.where("user").in(authUser, "#")).find().asList();
		return prefs;
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
