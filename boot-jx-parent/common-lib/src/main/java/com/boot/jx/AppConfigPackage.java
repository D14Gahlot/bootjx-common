package com.boot.jx;

import java.util.Map;

public class AppConfigPackage {
    public interface AppCommonConfig {

	public Map<String, Object> toMap();

	public String getCdnServer();

	public Map<String, Object> appAttributes();

    }

    public interface AppSharedConfig {
	default void clear(Map<String, String> map) {
	    // DO NOTHING
	};

	default String name() {
	    return null;
	};

	default Map<String, Object> getExternalConfig(Map<String, Object> config) {
	    return config;
	};
    }

}
