package com.boot.jx.postman;

import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestContextListener;

import com.boot.jx.app.CommonAppLauncher;

/**
 * The Class PostManApplication.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.boot.jx" })
@EnableAsync(proxyTargetClass = true)
public class PostManApplication extends CommonAppLauncher {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		launch(PostManApplication.class, args);
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
