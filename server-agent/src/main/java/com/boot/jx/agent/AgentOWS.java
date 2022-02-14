package com.boot.jx.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.web.context.request.RequestContextListener;

import com.boot.jx.session.SmartHttpSessionStrategy;

/**
 * The Class WebApplication.
 */
@ServletComponentScan
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan("com.boot.jx")
@EnableAsync(proxyTargetClass = true)
@EnableCaching
public class AgentOWS extends SpringBootServletInitializer {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(AgentOWS.class, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.boot.web.support.SpringBootServletInitializer#configure(
     * org.springframework.boot.builder.SpringApplicationBuilder)
     */
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
	return applicationBuilder.sources(AgentOWS.class);
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
	registration.setOrder(securityProperties.getFilterOrder());
	return registration;
    }

    @Bean
    public RequestContextListener requestContextListener() {
	return new RequestContextListener();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
	return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

}
