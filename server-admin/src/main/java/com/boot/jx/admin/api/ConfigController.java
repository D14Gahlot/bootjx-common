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
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.CompanyVarsConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigController {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private ConfigManager configManager;

	@Autowired
	public PMEnvironment pmEnvironment;

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelType}", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> saveChannelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType,
			@RequestBody Map<String, Object> data) {
		return ApiResponse.buildResults(configManager.saveChannelConfig(channelType.toString(), data));
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
	@RequestMapping(value = { "/api/config/clientapikey/{id}" }, method = { RequestMethod.DELETE })
	public ApiResponse<ClientAppConfigDoc, Object> deleteClientApiKey(@PathVariable String id) {
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

}
