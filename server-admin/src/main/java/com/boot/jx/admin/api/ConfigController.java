package com.boot.jx.admin.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConfigManager adminConfigService;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private ConfigManager configManager;

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelType}", method = { RequestMethod.POST })
    public ApiResponse<ChannelConfig, Object> saveChannelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled,
	    @RequestBody Map<String, Object> data) {
	return ApiResponse.buildResults(configManager.saveChannelConfig(channelType.toString(), disabled, data));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.GET })
    public ApiResponse<ChannelConfig, Object> getChannelConfig(@PathVariable String channelId) {
	return ApiResponse.buildResults(configManager.getChannelConfig(channelId));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.DELETE })
    public ApiResponse<ChannelConfig, Object> deleteChannelConfig(@PathVariable String channelId) {
	return ApiResponse.buildResults(configManager.removeChannelConfig(channelId));
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.GET })
    public ApiResponse<ClientKeyConfigDoc, Object> createClientApiKey() {
	return ApiResponse.buildResults(mongoTemplate.findAll(ClientKeyConfigDoc.class));
    }

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.POST })
    public ApiResponse<ClientKeyConfigDoc, Object> createClientApiKey(@RequestBody ClientKeyConfigDoc clientApiKey) {
	return ApiResponse.buildData(adminConfigService.save(clientApiKey));
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.DELETE })
    public ApiResponse<ClientKeyConfigDoc, Object> deleteClientApiKey(@RequestParam String id) {
	ClientKeyConfigDoc clientApiKey = new ClientKeyConfigDoc();
	clientApiKey.setId(id);
	return ApiResponse.buildResults(adminConfigService.remove(clientApiKey));
    }

}
