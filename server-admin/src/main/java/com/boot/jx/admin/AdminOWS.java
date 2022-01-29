package com.boot.jx.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestContextListener;

/**
 * The Class WebApplication.
 */
@ServletComponentScan
@SpringBootApplication
@ComponentScan("com.boot.jx")
@EnableAsync(proxyTargetClass = true)
@EnableCaching
public class AdminOWS extends SpringBootServletInitializer {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(AdminOWS.class, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.boot.web.support.SpringBootServletInitializer#configure(
     * org.springframework.boot.builder.SpringApplicationBuilder)
     */
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
	return applicationBuilder.sources(AdminOWS.class);
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
