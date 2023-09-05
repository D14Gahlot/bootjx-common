package com.boot.jx.xms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMContextUtil;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.swagger.MockParamBuilder;
import com.boot.jx.swagger.MockParamBuilder.MockParam;
import com.boot.utils.ArgUtil;

@Component
public class XmsSecurityConfig implements AuditDetailProvider {

	@Configuration
	@EnableWebSecurity
	@Order(90)
	public static class StatelessWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity.antMatcher("/ext/plugin/**").sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
					// Permit all
					.and().authorizeRequests().antMatchers("/**").permitAll()
					// CSRF
					.and().csrf().disable().headers().disable();
		}

	}

	@Configuration
	@EnableWebSecurity
	@Order(99)
	public static class SessionWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					// Permit all
					.and().authorizeRequests().antMatchers("/**").permitAll()
					// CSRF
					.and().csrf().disable().headers().disable();
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/assets/**",
					"/v2/**", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
					"/swagger-ui.html", "/webjars/**", "/favicon.ico");
		}
	}

	@Bean
	public MockParam swaggerApiKeyParam() {
		return new MockParamBuilder().id("X_API_KEY").name("x-api-key").description("API Key").defaultValue("")
				.parameterType(MockParamBuilder.MockParamType.HEADER).securityScheme("X_API_KEY").build();

	}

	@Override
	public String getAuditUser() {
		ClientApp x = PMContextUtil.clientApp();
		if (ArgUtil.is(x)) {
			return x.getKeyName();
		}
		return null;
	}

	@Override
	public AppAuthUser getAuthUser() {
		return null;
	}

}
