package com.boot.jx.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.common.config.AppCommonAuthFilter.ACCESS_RULES;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ClientAppConfigConstants;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.ConfigConstants.PERMS_KEY;
import com.boot.jx.common.config.ConfigManagerImpl;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.CHAT_STATE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.HSMContentType;
import com.boot.jx.postman.doc.HSMLanguage;
import com.boot.jx.postman.doc.HSMMessageType;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.config.PermsConfigDoc;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.utils.ArgUtil;
//import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigOptionMetaController {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private CDNBuilder cdnBuilder;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	AppConfig appConfig;

	// Meta APIS
	@RequestMapping(value = "/api/meta/message_types", method = { RequestMethod.GET })
	public ApiResponse<HSMMessageType, Object> messageType() {
		return ApiResponse.buildResults(HSMMessageType.values());
	}

	@RequestMapping(value = "/api/meta/message_content_types", method = { RequestMethod.GET })
	public ApiResponse<HSMContentType, Object> messageContentType() {
		return ApiResponse.buildResults(HSMContentType.values());
	}

	@RequestMapping(value = "/api/meta/langs", method = { RequestMethod.GET })
	public ApiResponse<HSMLanguage, Object> languages() {
		return ApiResponse.buildResults(HSMLanguage.values());
	}

	@RequestMapping(value = "/api/meta/channel_types", method = { RequestMethod.GET })
	public ApiResponse<AChannelDetails, Object> channel() {
		return ApiResponse.buildResults(new ArrayList<AChannelDetails>(ChannelPluginProvider.DETAILS_MAPPING.values()));
	}

	@RequestMapping(value = "/api/meta/channel_configs/{channelType}", method = { RequestMethod.GET })
	public ApiResponse<ConfigMeta, Object> channelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType) {
		ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.PLUGIN_MAPPING
				.get(channelType.toString());
		List<ConfigMeta> configs = plugin.listConfigMeta(pmEnvironment);
		return ApiResponse.buildResults(configs);
	}

	@RequestMapping(value = "/api/meta/app_types", method = { RequestMethod.GET })
	public ApiResponse<APP_TYPE, Object> appTypes() {
		return ApiResponse.buildResults(PMConstants.APP_TYPE.values());
	}

	@RequestMapping(value = "/api/meta/app_types/{appType}/config", method = { RequestMethod.GET })
	public ApiResponse<ConfigMeta, Object> appTypeConfig(@PathVariable APP_TYPE appType) {
		return ApiResponse.buildResults(ClientAppConfigConstants.APP_CONFIGS.getOrDefault(appType, new ConfigMeta[0]));
	}

	@RequestMapping(value = "/api/meta/app_types/common/config", method = { RequestMethod.GET })
	public ApiResponse<ConfigMeta, Object> appTypeConfigCommon() {
		return ApiResponse.buildResults(ClientAppConfigConstants.APP_CONFIGS_COMMON);
	}

	// Option APIS
	@JsonView(PMEnvironment.PublicProperty.class)
	@RequestMapping(value = { "/api/options/channels" }, method = { RequestMethod.GET })
	@ResponseBody
	public ApiResponse<AChannelConfig, Object> listActiveLanes(
			@RequestParam(required = false) ContactType contactType) {
		if (ArgUtil.is(contactType)) {
			return ApiResponse.buildResults(pmEnvironment.config().listChannels().stream()
					.filter(channel -> channel.equals(contactType)).collect(Collectors.toList()));
		}
		List<AChannelConfig> x = pmEnvironment.config().listChannels();
		// System.out.println(JsonUtil.toJson(x));
		return ApiResponse.buildResults(x);
	}

	@RequestMapping(value = "/api/options/tmpl/hsm", method = { RequestMethod.GET })
	public ApiResponse<HSMTemplateDoc, Object> listPushTemplateslistHsmTmpl() {
		return ApiResponse.buildResults(commonMongoTemplate.findAll(HSMTemplateDoc.class));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/options/inbound_queue" }, method = { RequestMethod.GET })
	public ApiResponse<ClientApp, Object> getInboundQueues(@RequestParam(required = false) CHAT_MODE mode,
			@RequestParam(required = false) APP_TYPE type) {
		if (ArgUtil.is(type))
			return ApiResponse.buildResults(pmEnvironment.config().listApps().stream().filter(app -> app.equals(type))
					.collect(Collectors.toList()));
		else if (ArgUtil.is(mode))
			return ApiResponse.buildResults(pmEnvironment.config().listApps().stream().filter(app -> app.equals(mode))
					.collect(Collectors.toList()));
		return ApiResponse.buildResults(pmEnvironment.config().listApps());
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/options/agent_queue" }, method = { RequestMethod.GET })
	public ApiResponse<ClientApp, Object> getAgentQueues() {
		return ApiResponse.buildResults(
				pmEnvironment.config().listApps().stream().filter(ClientApp::isAgentApp).collect(Collectors.toList()));
	}

	// Config APIS
	@Autowired
	private ConfigManagerImpl configManager;

	// PREFS
	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER_FOR_MASTER_DOMAIN, ACCESS_RULES.ONLY_DOMAIN_ADMIN })
	@RequestMapping(value = "/api/config", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, Object> setConfig(@RequestBody PMConfigurationObject map) {
		configManager.save(map);
		return ApiResponse.buildResults(configManager.getSetupConfigs());
	}

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER_FOR_MASTER_DOMAIN, ACCESS_RULES.ONLY_DOMAIN_ADMIN })
	@RequestMapping(value = "/api/config", method = { RequestMethod.PUT })
	public ApiResponse<Map<String, Object>, Object> setConfig(@RequestParam String key, @RequestParam String value,
			@RequestParam(defaultValue = "false") boolean shared) {
		PMConfigurationObject map = new PMConfigurationObject();
		map.setKey(key);
		map.setValue(value);
		map.setShared(shared);
		return setConfig(map);
	}

	@RequestMapping(value = "/api/config", method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getConfig(@RequestParam(required = false) String key,
			@RequestParam(required = false) boolean refresh) {
		if (refresh) {
			configManager.refresh();
		}
		return ApiResponse.buildResults(configManager.getConfigs(key));
	}

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER_FOR_MASTER_DOMAIN })
	@RequestMapping(value = "/api/config", method = { RequestMethod.DELETE })
	public ApiResponse<Map<String, Object>, Object> deleteConfig(@RequestParam(required = false) String key) {
		configManager.deleteAdminConfigs(key);
		return ApiResponse.buildResults(configManager.getSetupConfigs());
	}

	@RequestMapping(value = { "/api/config/app" }, method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getAppConfigs() {
		return ApiResponse.buildResults(configManager.getAppConfigs());
	}

	@RequestMapping(value = { "/api/config/setup" }, method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getSetupConfigs() {
		return ApiResponse.buildResults(configManager.getSetupConfigs());
	}

	@RequestMapping(value = "/api/config/cdn", method = { RequestMethod.POST })
	public ApiResponse<PMConfigurationObject, Object> updateCDN(@RequestParam(required = false) String url,
			@RequestParam(required = false) String version,
			@RequestParam(required = false, defaultValue = "false") boolean beta) {

		String domainServer = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString();
		String key = beta ? "mry.cdn.url.beta" : "mry.cdn.url";

		PMConfigurationObject config = pmEnvironment.keyEntry(key);
		config.setKey(key);

		String oldUrl = config.asString();
		config.setServer(domainServer);

		if (ArgUtil.is(version) && ArgUtil.is(oldUrl)) {
			url = cdnBuilder.updateVersion(oldUrl, version);
		}

		if (ArgUtil.is(url)) {
			config.setValue(url);
			configManager.save(config);
		}

		return ApiResponse.buildResults(config);
	}

	/**************
	 * PERMS
	 ************/

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER })
	@RequestMapping(value = "/api/perm", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, Object> setPerm(@RequestBody PermsConfigDoc map) {
		configManager.savePerm(map);
		return ApiResponse.buildResults(configManager.getPerms());
	}

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER })
	@RequestMapping(value = "/api/perm", method = { RequestMethod.PUT })
	public ApiResponse<Map<String, Object>, Object> setPerm(@RequestParam PERMS_KEY key, @RequestParam String value,
			@RequestParam(defaultValue = "false") boolean shared) {
		PermsConfigDoc map = new PermsConfigDoc();
		map.setKey(key.getKey());
		map.setValue(value);
		map.setShared(shared);
		return setPerm(map);
	}

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER })
	@RequestMapping(value = "/api/perm/{key}", method = { RequestMethod.POST })
	public ApiResponse<Map<String, Object>, Object> setPerm(@PathVariable("key") PERMS_KEY key,
			@RequestBody PermsConfigDoc map) {
		map.setKey(key.getKey());
		return setPerm(map);
	}

	@RequestMapping(value = "/api/perm", method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getPerm(@RequestParam(required = false) PERMS_KEY key) {
		return ApiResponse.buildResults(configManager.getPerm(key));
	}

	@ApiRequest(rules = { ACCESS_RULES.ONLY_DUPERUSER })
	@RequestMapping(value = "/api/perm", method = { RequestMethod.DELETE })
	public ApiResponse<Map<String, Object>, Object> deletePerm(@RequestParam(required = false) PERMS_KEY key) {
		configManager.deletePerm(key);
		return ApiResponse.buildResults(configManager.getPerms());
	}

	@RequestMapping(value = "/api/meta/chat_states", method = { RequestMethod.GET })
	public ApiResponse<CHAT_STATE, Object> chatStates() {
		return ApiResponse.buildResults(PMConstants.CHAT_STATE.values());
	}

	@RequestMapping(value = "/api/meta/chat_status", method = { RequestMethod.GET })
	public ApiResponse<CHAT_STATUS, Object> chatStatus() {
		return ApiResponse.buildResults(PMConstants.CHAT_STATUS.values());

	}

	@Autowired
	AWSFileStore fileStore;

	@RequestMapping(value = "/api/media/{bucket}", method = { RequestMethod.POST })
	public CommonFile createBucketMedia(@PathVariable String bucket,
			@RequestParam(name = "file", required = true) MultipartFile file,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "folder", required = false) String folder) {
		String file_name = ArgUtil.nonEmpty(folder, UUID.randomUUID().toString());
		String folder_path = ArgUtil.nonEmpty(name, UUID.randomUUID().toString());

		CommonFile commonfile = fileStore.upload1(file,
				String.format("%s",name, AppContextUtil.getTenant(), file_name), folder_path);

		return commonfile;

	}
}
