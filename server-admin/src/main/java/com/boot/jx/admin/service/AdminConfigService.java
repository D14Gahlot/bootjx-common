package com.boot.jx.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.manager.ConfigBuilder;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.MapBuilder;

@Service
public class AdminConfigService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SharedConfigManager sharedConfigManager;

	@Autowired
	private PMClientConfig pmClientConfig;

	public List<Map<String, Object>> getAdminConfigs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		for (ConfigBuilder meta : ConfigBuilder.LIST) {

			switch (meta.getKey()) {
			case "postman.default.sender":
				list.add(MapBuilder.map().put("meta", meta)
						.put("config",
								new PMConfigurationObject("postman.default.sender", pmClientConfig.getDefaultSender()))
						.toMap());
				break;
			default:
				list.add(MapBuilder.map().put("meta", meta).put("config", doc.get(meta.getKey())).toMap());
				break;
			}
		}
		return list;
	}

	public void setAdminConfigs(PMConfigurationObject config) {
		ConnectorConfigDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);

		if (ArgUtil.isEmpty(doc)) {
			doc = new ConnectorConfigDoc();
			doc.setTenant(AppContextUtil.getTenant());
		}

		switch (config.getKey()) {
		case "postman.default.sender":
			doc.agent().setDefaultBotName(config.asString());
			break;
		default:
			PMConfigurationObject configDoc = doc.get(config.getKey());
			configDoc.setKey(config.getKey());
			configDoc.setValue(config.getValue());
			doc.set(configDoc);
			break;
		}
		mongoTemplate.save(doc);
		sharedConfigManager.clear();
	}

}
