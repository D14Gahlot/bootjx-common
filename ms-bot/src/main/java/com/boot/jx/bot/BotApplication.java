package com.boot.jx.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mobile.SitePreferenceAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.WebSocketAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(value = "com.boot.jx")
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, WebSocketAutoConfiguration.class,
		SitePreferenceAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class,
		ValidationAutoConfiguration.class })
@EnableAsync(proxyTargetClass = true)
public class BotApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(BotApplication.class);

	public static void main(String[] args) throws Exception {

		String javaHomePath = System.getProperty("java.home");
		String keystore = javaHomePath + "/lib/security/cacerts";
		String storepass = "changeit";
		String storetype = "JKS";

		String[][] props = { { "javax.net.ssl.trustStore", keystore, }, { "javax.net.ssl.keyStore", keystore, },
				{ "javax.net.ssl.keyStorePassword", storepass, }, { "javax.net.ssl.keyStoreType", storetype, }, };
		for (int i = 0; i < props.length; i++) {
			System.getProperties().setProperty(props[i][0], props[i][1]);
		}

		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BotApplication.class).run(args);
	}

}
