package com.boot.jx.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableEncryptableProperties
@PropertySource("classpath:application.app.properties")
public class PMEnvironmentConfigImpl implements PMDomainConfig {

    @Autowired
    private PMEnvironment environment;

    @Override
    public String getDefaultInboundQueue() {
	return environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_QUEUE).asString();
    }

}
