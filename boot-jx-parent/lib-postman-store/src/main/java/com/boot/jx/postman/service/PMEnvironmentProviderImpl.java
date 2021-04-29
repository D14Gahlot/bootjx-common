package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppSharedConfig;
import com.boot.jx.postman.ConnectorConfig;
import com.boot.jx.postman.PMEnvironment.PMEnvironmentProvider;
import com.boot.jx.postman.doc.ConnectorConfigDoc;
import com.boot.utils.ArgUtil;

@Component
public class PMEnvironmentProviderImpl implements PMEnvironmentProvider, AppSharedConfig {

	private Map<String, ConnectorConfigDoc> props = new HashMap<String, ConnectorConfigDoc>();

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public ConnectorConfig get() {
		String tnt = AppContextUtil.getTenant();
		if (props.containsKey(tnt)) {
			return props.get(tnt);
		}
		ConnectorConfigDoc x = mongoTemplate.findById(AppContextUtil.getTenant(), ConnectorConfigDoc.class);
		if (ArgUtil.is(x)) {
			props.put(tnt, x);
		}
		return x;
	}

	@Override
	public void clear(Map<String, String> map) {
		String tnt = AppContextUtil.getTenant();
		props.remove(tnt);
	}

}
