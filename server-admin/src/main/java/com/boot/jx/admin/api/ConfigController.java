package com.boot.jx.admin.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.ConfigManagerImpl;
import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc.CompanyTokenKeyDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc.CompanyVarsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigController {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private ConfigManagerImpl configManager;

	@Autowired
	public PMEnvironment pmEnvironment;

	@Autowired
	public RestService restService;

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelType}", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> saveChannelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType,
			@RequestBody Map<String, Object> data) {
		return ApiResponse.buildResults(configManager.saveChannelConfig(channelType.toString(), data));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel", method = { RequestMethod.PATCH })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> modifyChannelConfig(@RequestBody ModelPatches req) {
		return ApiResponse.buildResults(configManager.patchChannelConfig(req));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> getChannelConfig(@PathVariable String channelId) {
		return ApiResponse.buildResults(configManager.getChannelConfig(channelId));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}/{action}", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> updateChannelConfig(@PathVariable String channelId,
			@PathVariable String action) {
		return ApiResponse.buildResults(configManager.updateChannelConfig(channelId, action));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.DELETE })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> deleteChannelConfig(@PathVariable String channelId) {
		return ApiResponse.buildResults(configManager.updateChannelConfig(channelId, "remove"));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.GET })
	public ApiResponse<ClientApp, Object> createClientApiKey() {
		return ApiResponse.buildResults(pmEnvironment.config().listApps());
	}

	@JsonView(PMEnvironment.OneTimeVisibleProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.POST })
	public ApiResponse<ClientAppConfigDoc, Object> createClientApiKey(@RequestBody ClientAppConfigDoc clientApiKey) {
		return ApiResponse.buildData(configManager.save(clientApiKey));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = "/api/config/clientapikey", method = { RequestMethod.PATCH })
	public ApiResponse<ClientApp, Object> patchClientApiKey(@RequestBody ModelPatches req)
			throws InstantiationException, IllegalAccessException {
		return ApiResponse.buildResults(configManager.patchClientApiKey(req));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/clientapikey/{appId}", "/api/config/clientapikey" },
			method = { RequestMethod.DELETE })
	public ApiResponse<ClientAppConfigDoc, Object> deleteClientApiKey(@PathVariable(required = false) String appId,
			@RequestParam(required = false) String id) {
		id = ArgUtil.nonEmpty(id, appId);
		ClientAppConfigDoc clientApiKey = new ClientAppConfigDoc();
		clientApiKey.setId(id);
		return ApiResponse.buildResults(configManager.remove(clientApiKey));
	}

	/***************************
	 * Inbound Queues
	 ***************************/

	@Deprecated
	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/inbound_queue" }, method = { RequestMethod.GET })
	public ApiResponse<ClientAppConfigDoc, Object> getInboundQueues() {
		return ApiResponse.buildResults(mongoTemplate.findAll(ClientAppConfigDoc.class));
	}

	/***************************
	 * CompanyVars
	 ***************************/

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/companyvar" }, method = { RequestMethod.GET })
	public ApiResponse<CompanyVarsConfigDoc, Object> getCompanyVars() {
		return ApiResponse.buildResults(mongoTemplate.findAll(CompanyVarsConfigDoc.class));
	}

	@JsonView(PMEnvironment.OneTimeVisibleProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/companyvar" }, method = { RequestMethod.POST })
	public ApiResponse<CompanyVarsConfigDoc, Object> updateCompanyVars(@RequestBody CompanyVarsConfigDoc companyVar) {
		return ApiResponse.buildData(configManager.save(companyVar));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/companyvar" }, method = { RequestMethod.DELETE })
	public ApiResponse<CompanyVarsConfigDoc, Object> removeCompanyVars(@RequestParam String id) {
		CompanyVarsConfigDoc companyVar = new CompanyVarsConfigDoc();
		companyVar.setId(id);
		return ApiResponse.buildResults(configManager.remove(companyVar));
	}

	/***************************
	 * CompanyTokenKeys
	 ***************************/

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/tokenkey" }, method = { RequestMethod.GET })
	public ApiResponse<CompanyTokenKeyDoc, Object> getCompanyTokenKeys() {
		return ApiResponse.buildResults(mongoTemplate.findAll(CompanyTokenKeyDoc.class));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/tokenkey" }, method = { RequestMethod.POST })
	public ApiResponse<CompanyTokenKeyDoc, Object> updateCompanyTokenKeys(@RequestBody CompanyTokenKeyDoc companyVar) {
		return ApiResponse.buildData(configManager.save(companyVar));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/tokenkey" }, method = { RequestMethod.DELETE })
	public ApiResponse<CompanyTokenKeyDoc, Object> removeCompanyTokenKeys(@RequestParam String id) {
		CompanyTokenKeyDoc companyVar = new CompanyTokenKeyDoc();
		companyVar.setId(id);
		return ApiResponse.buildResults(configManager.remove(companyVar));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = "/api/config/tokenkey", method = { RequestMethod.PATCH })
	public ApiResponse<CompanyTokenKeyDoc, Object> updateTokeKeys(@RequestBody ModelPatches patch)
			throws InstantiationException, IllegalAccessException {
		return ApiResponse.buildResults(configManager.patch(patch, CompanyTokenKeyDoc.class));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/tokenkey/gpt/models" }, method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getCompanyTokenKeysGPTModels(@RequestParam String id,
			@RequestParam String apiKey) {
		if (!ArgUtil.is(apiKey) && ArgUtil.is(id)) {
			CompanyTokenKeyDoc x = configManager.findById(id, CompanyTokenKeyDoc.class);
			apiKey = ArgUtil.parseAsString(x.secret().getOrDefault("apiKey", Constants.DEFAULT_STRING));
		}
		if (ArgUtil.is(apiKey)) {
			return ApiResponse.buildResults(restService.ajax("https://api.openai.com/v1/models").authBearer(apiKey)
					.get().asMapModel().entry("data").asListOfMap());
		}
		return ApiResponse.build();
	}

}
