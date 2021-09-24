package com.boot.jx.account.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.postman.PMConfiguration;
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
import com.boot.jx.postman.wa360.WA360ConfigDetails;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@Controller
@RequestMapping("/cpanel")
public class CPanelController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccountAdminService accountAdminService;

    @Autowired
    private AccountSessionBean sessionBean;

    @Autowired
    private CDNBuilder cdnBuilder;

    @RequestMapping(value = { "/app", "/app/**", "/app/*" }, method = { RequestMethod.POST, RequestMethod.GET })
    public String cpanel(Model model, @RequestParam(required = false) String authToken) {

	model.addAllAttributes(appCommonConfig.appAttributes());

	Authentication auth = AccountAdminService.getAuthentication();

	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_ROLE", sessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}

	model.addAttribute("APP", "account");

	return "app-cpanel";
    }

    @ResponseBody
    @RequestMapping(value = "/api/config", method = { RequestMethod.POST })
    public ApiResponse<Map<String, Object>, Object> setConfig(@RequestBody PMConfigurationObject map) {
	configManager.save(map);
	return ApiResponse.buildResults(configManager.getAdminConfigs());
    }

    @ResponseBody
    @RequestMapping(value = "/api/config", method = { RequestMethod.GET })
    public ApiResponse<Map<String, Object>, Object> getConfig() {
	return ApiResponse.buildResults(configManager.getAdminConfigs());
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/{key}", method = { RequestMethod.GET })
    public ApiResponse<Map<String, Object>, Object> getConfig(@PathVariable String key) {
	return ApiResponse.buildResults(configManager.getAdminConfigs(key));
    }

    @Deprecated
    @ResponseBody
    @RequestMapping(value = "/api/config/refresh", method = { RequestMethod.GET })
    public ApiResponse<PMConfiguration, Object> getConnnectors() {
	PMConfigurationDoc config = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
	configManager.saveConfigs(config);
	return ApiResponse.buildResults(pmEnvironment.config());
    }

    @ResponseBody
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
	configManager.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @ResponseBody
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
	configManager.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/tg", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addTelegramConfig(@RequestParam String handler,
	    @RequestParam String type, @RequestParam String accessToken, @RequestParam(required = false) String envName,
	    @RequestParam String webhookUrl, @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	TelegramConfigDetails fbconfig = new TelegramConfigDetails();
	fbconfig.setHandler(handler);
	fbconfig.setType(type);
	fbconfig.setAccessToken(accessToken);
	fbconfig.setWebhookUrl(webhookUrl);
	configManager.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @ResponseBody
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
	configManager.save(new ChannelConfig().from(fbconfig).disabled(disabled));
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/wa360", method = { RequestMethod.POST })
    public ApiResponse<PMConfigurationDoc, Object> addWA360Config(@RequestParam String number,
	    @RequestParam String apiKey, @RequestParam(defaultValue = "false", required = false) boolean disabled,
	    @RequestParam String webhookUrl) {
	WA360ConfigDetails config = new WA360ConfigDetails();
	config.setNumber(number);
	config.setApiKey(apiKey);
	config.setWebhookUrl(webhookUrl);
	configManager.save(config, disabled);
	return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class));
    }

    @ResponseBody
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
	return ApiResponse.buildData(configManager.save(clientApiKey));
    }

}
