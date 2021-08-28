package com.boot.jx.admin.api;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.service.AdminConfigService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.ChannelConfig;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ClientApiKeyDoc;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class ConfigController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AdminConfigService adminConfigService;

    @Autowired
    private PMEnvironment pmEnvironment;

    @RequestMapping(value = "/api/config", method = { RequestMethod.POST })
    public ApiResponse<Map<String, Object>, Object> setConfig(@RequestBody PMConfigurationObject map) {
	adminConfigService.save(map);
	return ApiResponse.buildResults(adminConfigService.getAdminConfigs());
    }

    @RequestMapping(value = "/api/config", method = { RequestMethod.GET })
    public ApiResponse<Map<String, Object>, Object> getConfig() {
	return ApiResponse.buildResults(adminConfigService.getAdminConfigs());
    }

    @RequestMapping(value = "/api/config/refresh", method = { RequestMethod.GET })
    public ApiResponse<PMConfiguration, Object> getConnnectors() {
	PMConfigurationDoc config = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
	adminConfigService.saveConfigs(config);
	return ApiResponse.buildResults(pmEnvironment.config());
    }

    @RequestMapping(value = "/api/config/fb", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addFacebookConfig(@RequestParam String pageId,
	    @RequestParam String type, @RequestParam String verifyToken, @RequestParam String appSecret,
	    @RequestParam String accessToken,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	FacebookConfig fbconfig = new FacebookConfig();
	fbconfig.setPageId(pageId);
	fbconfig.setType(type);
	fbconfig.setVerifyToken(verifyToken);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setAppSecret(appSecret);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @RequestMapping(value = "/api/config/tw", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addTwitterConfig(@RequestParam String handler,
	    @RequestParam String type, @RequestParam String consumerKey, @RequestParam String consumerSecret,
	    @RequestParam String accessTokenSecret, @RequestParam String accessToken,
	    @RequestParam(required = false) String envName, @RequestParam String webhookUrl,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	TwitterConfig fbconfig = new TwitterConfig();
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

    @RequestMapping(value = "/api/config/tg", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addTelegramConfig(@RequestParam String handler,
	    @RequestParam String type, @RequestParam String accessToken, @RequestParam(required = false) String envName,
	    @RequestParam String webhookUrl, @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	TelegramConfig fbconfig = new TelegramConfig();
	fbconfig.setHandler(handler);
	fbconfig.setType(type);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setWebhookUrl(webhookUrl);
	adminConfigService.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @RequestMapping(value = "/api/config/gs", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addWAConfig(@RequestParam String number,
	    @RequestParam(required = false) String notifyId, @RequestParam String chatId, @RequestParam String chatPass,
	    @RequestParam(required = false) String notifyPass,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	GupShupConfig fbconfig = new GupShupConfig();
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
	    Pattern pattern = Pattern.compile(
		    "(?<proto>.+)cdn.jsdelivr.net/gh/(?<org>.+)/(?<repo>.+)@(?<version>[-a-zA-Z0-9\\.]+)(?<path>.*)");
	    Matcher matcher = pattern.matcher(oldUrl);
	    if (matcher.find()) {
		String protoV = matcher.group("proto");
		String orgV = matcher.group("org");
		String repoV = matcher.group("repo");
		String versionV = matcher.group("version");
		String pathV = matcher.group("path");
		url = String.format("%scdn.jsdelivr.net/gh/%s/%s@%s%s", protoV, orgV, repoV, StringUtils.trim(version),
			pathV);
	    }
	}

	if (ArgUtil.is(url)) {
	    config.setValue(url);
	    adminConfigService.save(config);
	}

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
    public ApiResponse<ClientApiKeyDoc, Object> createClientApiKey() {
	return ApiResponse.buildResults(mongoTemplate.findAll(ClientApiKeyDoc.class));
    }

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.POST })
    public ApiResponse<ClientApiKeyDoc, Object> createClientApiKey(@RequestBody ClientApiKeyDoc clientApiKey) {
	return ApiResponse.buildData(adminConfigService.save(clientApiKey));
    }

}
