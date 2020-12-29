package com.boot.jx;

import java.util.Map;

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
