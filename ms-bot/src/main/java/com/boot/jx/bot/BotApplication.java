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
@EnableAutoConfiguration(exclude = {
		JmxAutoConfiguration.class, WebSocketAutoConfiguration.class, SitePreferenceAutoConfiguration.class,
		SpringApplicationAdminJmxAutoConfiguration.class, ValidationAutoConfiguration.class
})
@EnableAsync(proxyTargetClass = true)
public class BotApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(BotApplication.class);

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BotApplication.class)
				.run(args);
	}

}
