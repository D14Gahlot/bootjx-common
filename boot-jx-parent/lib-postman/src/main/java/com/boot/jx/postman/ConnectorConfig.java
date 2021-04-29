package com.boot.jx.postman;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.utils.ArgUtil;

public class ConnectorConfig implements Serializable {

	private static final long serialVersionUID = -5432956433673368768L;

	Map<String, FacebookConfig> facebook;

	public Map<String, FacebookConfig> getFacebook() {
		return facebook;
	}

	public void setFacebook(Map<String, FacebookConfig> facebook) {
		this.facebook = facebook;
	}

	public FacebookConfig facebook(String pageId) {
		return facebook.get(pageId);
	}

	public Map<String, FacebookConfig> facebook() {
		if (ArgUtil.isEmpty(facebook)) {
			facebook = new HashMap<String, FacebookConfig>();
		}
		return facebook;
	}

	public ConnectorConfig facebook(FacebookConfig config) {
		this.facebook().put(config.getPageId(), config);
		return this;
	}
}
