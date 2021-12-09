package com.boot.jx.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.AppCommonAuthFilter.ACCESS_RULES;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.HSMContentType;
import com.boot.jx.postman.doc.HSMLanguage;
import com.boot.jx.postman.doc.HSMMessageType;
import com.boot.jx.postman.doc.HSMTemplate;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigOptionMetaController {

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private CDNBuilder cdnBuilder;

    @Autowired
    CommonMongoTemplate commonMongoTemplate;

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
	ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.PLUGIN_MAPPING.get(channelType.toString());
	List<ConfigMeta> configs = plugin.listConfigMeta(pmEnvironment);
	return ApiResponse.buildResults(configs);
    }

    // Option APIS

    @JsonView(PMEnvironment.PublicProperty.class)
    @RequestMapping(value = { "/api/options/channels" }, method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> listActiveLanes() {
	return ApiResponse.buildResults(pmEnvironment.config().listChannels());
    }

    @RequestMapping(value = "/api/options/tmpl/hsm", method = { RequestMethod.GET })
    public ApiResponse<HSMTemplate, Object> listPushTemplateslistHsmTmpl() {
	return ApiResponse.buildResults(commonMongoTemplate.findAll(HSMTemplate.class));
    }

    // Config APIS
    @Autowired
    private ConfigManager configManager;

    @RequestMapping(value = "/api/config", method = { RequestMethod.POST })
    public ApiResponse<Map<String, Object>, Object> setConfig(@RequestBody PMConfigurationObject map) {
	configManager.save(map);
	return ApiResponse.buildResults(configManager.getSetupConfigs());
    }

    @RequestMapping(value = "/api/config", method = { RequestMethod.GET })
    public ApiResponse<Map<String, Object>, Object> getConfig(@RequestParam(required = false) String key) {
	return ApiResponse.buildResults(configManager.getConfigs(key));
    }

    @ApiRequest(rules = ACCESS_RULES.ONLY_DUPERUSER)
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
	PMConfigurationObject config = pmEnvironment.keyEntry(beta ? "mry.cdn.url.beta" : "mry.cdn.url");
	String oldUrl = config.asString();

	if (ArgUtil.is(version) && ArgUtil.is(oldUrl)) {
	    url = cdnBuilder.updateVersion(oldUrl, version);
	}

	if (ArgUtil.is(url)) {
	    config.setValue(url);
	    configManager.save(config);
	}

	return ApiResponse.buildResults(config);
    }

}
