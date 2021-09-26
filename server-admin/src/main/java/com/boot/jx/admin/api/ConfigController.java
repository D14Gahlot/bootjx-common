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

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.fb.FacebookConfigDetails;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.tg.TelegramConfigDetails;
import com.boot.jx.postman.tw.TwitterConfigDetails;
import com.boot.utils.ArgUtil;
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

    @Autowired
    private CDNBuilder cdnBuilder;

    @RequestMapping(value = "/api/config", method = { RequestMethod.POST })
    public ApiResponse<Map<String, Object>, Object> setConfig(@RequestBody PMConfigurationObject map) {
	adminConfigService.save(map);
	return ApiResponse.buildResults(adminConfigService.getAdminConfigs());
    }

    @ResponseBody
    @RequestMapping(value = "/api/config", method = { RequestMethod.GET })
    public ApiResponse<Map<String, Object>, Object> getConfig(@RequestParam(required = false) String key) {
	return ApiResponse.buildResults(configManager.getAdminConfigs(key));
    }

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

    @Deprecated
    @RequestMapping(value = "/api/config/refresh", method = { RequestMethod.GET })
    public ApiResponse<PMConfiguration, Object> getConnnectors() {
	PMConfigurationDoc config = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
	adminConfigService.saveConfigs(config);
	return ApiResponse.buildResults(pmEnvironment.config());
    }

    @Deprecated
    @RequestMapping(value = "/api/config/fb", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addFacebookConfig(@RequestParam String pageId,
	    @RequestParam String type, @RequestParam String verifyToken, @RequestParam String appSecret,
	    @RequestParam String accessToken,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	FacebookConfigDetails fbconfig = new FacebookConfigDetails();
	fbconfig.setPageId(pageId);
	fbconfig.setType(type);
	fbconfig.setVerifyToken(verifyToken);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setAppSecret(appSecret);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @Deprecated
    @RequestMapping(value = "/api/config/tw", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addTwitterConfig(@RequestParam String handler,
	    @RequestParam String type, @RequestParam String consumerKey, @RequestParam String consumerSecret,
	    @RequestParam String accessTokenSecret, @RequestParam String accessToken,
	    @RequestParam(required = false) String envName, @RequestParam String webhookUrl,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	TwitterConfigDetails fbconfig = new TwitterConfigDetails();
	fbconfig.setHandler(handler);
	fbconfig.setType(type);
	fbconfig.setEnvName(envName);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setAccessTokenSecret(accessTokenSecret);
	fbconfig.setConsumerKey(consumerKey);
	fbconfig.setConsumerSecret(consumerSecret);
	fbconfig.setWebhookUrl(webhookUrl);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @Deprecated
    @RequestMapping(value = "/api/config/tg", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addTelegramConfig(@RequestParam String handler,
	    @RequestParam String type, @RequestParam String accessToken, @RequestParam(required = false) String envName,
	    @RequestParam String webhookUrl, @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	TelegramConfigDetails fbconfig = new TelegramConfigDetails();
	fbconfig.setHandler(handler);
	fbconfig.setType(type);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setWebhookUrl(webhookUrl);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @Deprecated
    @RequestMapping(value = "/api/config/gs", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addWAConfig(@RequestParam String number,
	    @RequestParam(required = false) String notifyId, @RequestParam String chatId, @RequestParam String chatPass,
	    @RequestParam(required = false) String notifyPass,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	GupShupConfigDetails fbconfig = new GupShupConfigDetails();
	fbconfig.setNumber(number);
	fbconfig.setChatId(chatId);
	fbconfig.setChatPass(chatPass);
	fbconfig.setNotifyId(notifyId);
	fbconfig.setNotifyPass(notifyPass);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @RequestMapping(value = "/api/config/cdn", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationObject, Object> updateCDN(@RequestParam(required = false) String url,
	    @RequestParam(required = false) String version) {
	PMConfigurationObject config = pmEnvironment.get("mry.cdn.url");
	String oldUrl = config.asString();

	if (ArgUtil.is(version) && ArgUtil.is(oldUrl)) {
	    url = cdnBuilder.updateVersion(oldUrl, version);
	}

	if (ArgUtil.is(url)) {
	    config.setValue(url);
	    configManager.save(config);
	}

	cdnBuilder.update();

	return ApiResponse.buildResults(config);
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/options/lanes" }, method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> listActiveLanes() {
	return ApiResponse.buildResults(pmEnvironment.config().connectors());
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
