package com.boot.jx.postman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestContextListener;

/**
 * The Class PostManApplication.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.boot.jx" })
@EnableAsync(proxyTargetClass = true)
public class PostManApplication {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(PostManApplication.class, args);
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
