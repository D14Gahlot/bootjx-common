package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppSharedConfig;
import com.boot.jx.agent.AgentConfig;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment.PMEnvironmentProvider;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.utils.ArgUtil;

@Component
public class PMEnvironmentProviderImpl implements PMEnvironmentProvider, AppSharedConfig {

	private Map<String, ConnectorConfigDoc> connectors = new HashMap<String, ConnectorConfigDoc>();

	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Override
	public PMConfiguration config() {
		String tnt = AppContextUtil.getTenant();
		if (connectors.containsKey(tnt)) {
			return connectors.get(tnt);
		}
		if (ArgUtil.is(mongoTemplate)) {
			ConnectorConfigDoc x = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);
			if (ArgUtil.is(x)) {
				connectors.put(tnt, x);
			}
			return x;
		}
		return null;
	}

	@Override
	public void clear(Map<String, String> map) {
		String tnt = AppContextUtil.getTenant();
		connectors.remove(tnt);
	}

}
