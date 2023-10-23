package com.boot.jx.postman.manager;

import com.boot.jx.postman.doc.config.ClientAppConfigDoc;

public interface ConfigManager {
	ClientAppConfigDoc save(ClientAppConfigDoc xo);

	void refresh();
}
