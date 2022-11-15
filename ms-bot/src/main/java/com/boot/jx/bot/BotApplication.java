package com.boot.jx.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
@ComponentScan(value = "com.boot.jx")
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = { JmxAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class,
	ValidationAutoConfiguration.class })
@EnableAsync(proxyTargetClass = true)
public class BotApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotApplication.class);

    public static void main(String[] args) throws Exception {

	ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BotApplication.class).run(args);
    }

    /**
     * Security filter chain registration.
     *
     * @param securityProperties the security properties
     * @return the delegating filter proxy registration bean
     */
    @Bean
    @ConditionalOnBean(name = "checkSession")
    public DelegatingFilterProxyRegistrationBean securityFilterChainRegistration(
	    SecurityProperties securityProperties) {
	DelegatingFilterProxyRegistrationBean registration = new DelegatingFilterProxyRegistrationBean("checkSession");
	registration.setOrder(securityProperties.getFilter().getOrder());
	return registration;
    }

    @Bean
    public RequestContextListener requestContextListener() {
	return new RequestContextListener();
    }

}
