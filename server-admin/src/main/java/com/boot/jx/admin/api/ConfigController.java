package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.agent.AgentConfig;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

@RestController
public class ConfigController {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	SharedConfigManager sharedConfigManager;

	@RequestMapping(value = "/api/connector", method = { RequestMethod.GET })
	public ApiResponse<ConnectorConfigDoc, Object> getConfig() {
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> postConfig(@RequestBody PMConfiguration config) {
		ConnectorConfigDoc doc = EntityDtoUtil.dtoToEntity(config, new ConnectorConfigDoc());
		doc.setTenant(AppContextUtil.getTenant());
		mongoTemplate.save(config);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector/fb", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> addFacebookConfig(@RequestParam String pageId,
			@RequestParam String type, @RequestParam String verifyToken, @RequestParam String appSecret,
			@RequestParam String accessToken) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		FacebookConfig fbconfig = new FacebookConfig();
		fbconfig.setPageId(pageId);
		fbconfig.setType(type);
		fbconfig.setVerifyToken(verifyToken);
		fbconfig.setAccessToken(accessToken);
		fbconfig.setAppSecret(appSecret);
		doc.facebook(fbconfig);
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector/tw", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> addTwitterConfig(@RequestParam String handler,
			@RequestParam String type, @RequestParam String consumerKey, @RequestParam String consumerSecret,
			@RequestParam String accessTokenSecret, @RequestParam String accessToken,
			@RequestParam(required = false) String envName, @RequestParam String webhookUrl) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		TwitterConfig fbconfig = new TwitterConfig();
		fbconfig.setHandler(handler);
		fbconfig.setType(type);
		fbconfig.setEnvName(envName);
		fbconfig.setAccessToken(accessToken);
		fbconfig.setAccessTokenSecret(accessTokenSecret);
		fbconfig.setConsumerKey(consumerKey);
		fbconfig.setConsumerSecret(consumerSecret);
		fbconfig.setWebhookUrl(webhookUrl);
		doc.twitter(fbconfig);
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector/tg", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> addTelegramConfig(@RequestParam String handler,
			@RequestParam String type, @RequestParam String accessToken, @RequestParam(required = false) String envName,
			@RequestParam String webhookUrl) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		TelegramConfig fbconfig = new TelegramConfig();
		fbconfig.setHandler(handler);
		fbconfig.setType(type);
		fbconfig.setAccessToken(accessToken);
		fbconfig.setWebhookUrl(webhookUrl);
		doc.telegram(fbconfig);
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector/gs", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> addWAConfig(@RequestParam String number,
			@RequestParam String notifyId, @RequestParam String chatId, @RequestParam String chatPass,
			@RequestParam String notifyPass) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		GupShupConfig fbconfig = new GupShupConfig();
		fbconfig.setNumber(number);
		fbconfig.setChatId(chatId);
		fbconfig.setChatPass(chatPass);
		fbconfig.setNotifyId(notifyId);
		fbconfig.setNotifyPass(notifyPass);

		doc.gupshup(fbconfig);
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/config/set", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> addConfig(@RequestBody PMConfigurationObject map) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}
		doc.set(map);
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/config/agent", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> setDefaultBotName(@RequestBody AgentConfig config) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		AgentConfig existing = doc.agent();
		if (ArgUtil.is(config.getDefaultBotName())) {
			existing.setDefaultBotName(config.getDefaultBotName());
		}
		mongoTemplate.save(existing);
		sharedConfigManager.clear();
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}
}
