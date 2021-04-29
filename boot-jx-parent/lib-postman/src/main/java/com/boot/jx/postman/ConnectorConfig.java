package com.boot.jx.postman;

import java.io.Serializable;
import java.util.Map;

import com.boot.jx.postman.fb.FacebookConfig;

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

}
