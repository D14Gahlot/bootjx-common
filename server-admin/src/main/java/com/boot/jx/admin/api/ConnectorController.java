package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.ConnectorConfig;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

@RestController
public class ConnectorController {

	@Autowired
	MongoTemplate mongoTemplate;

	@RequestMapping(value = "/api/connector", method = { RequestMethod.GET })
	public ApiResponse<ConnectorConfigDoc, Object> getConfig() {
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> postConfig(@RequestBody ConnectorConfig config) {
		ConnectorConfigDoc doc = EntityDtoUtil.dtoToEntity(config, new ConnectorConfigDoc());
		doc.setTenant(AppContextUtil.getTenant());
		mongoTemplate.save(config);
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}

	@RequestMapping(value = "/api/connector/fb", method = { RequestMethod.POST })
	public ApiResponse<ConnectorConfigDoc, Object> postConfig(@RequestParam String pageId, @RequestParam String type,
			@RequestParam String verifyToken, @RequestParam String appSecret, @RequestParam String accessToken) {
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
		return ApiResponse.buildResults(mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class));
	}
}
