package com.boot.jx.postman;

import com.boot.jx.AppContextUtil;

public class PMContextUtil {

	public static ClientApp clientApp() {
		return AppContextUtil.get("XmsVendorConfigurer:ClientApp");
	}

	public static ClientApp clientApp(ClientApp clientApp) {
		AppContextUtil.set("XmsVendorConfigurer:ClientApp", clientApp);
		return clientApp;
	}

}
