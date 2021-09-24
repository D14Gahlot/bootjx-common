package com.boot.jx.common.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppSharedConfig;
import com.boot.jx.AppParam;

@Component
public class AppSharedConfigImpl implements AppSharedConfig {

    @Autowired
    private AppConfig appConfig;

    @Override
    public Map<String, Object> getExternalConfig(Map<String, Object> config) {
	config.put(AppParam.JAX_LOGGER_URL.getProperty(), appConfig.getLoggerURL());
	return config;
    };
}
