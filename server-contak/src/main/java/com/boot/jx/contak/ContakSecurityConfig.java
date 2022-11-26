package com.boot.jx.contak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.swagger.MockParamBuilder;
import com.boot.jx.swagger.MockParamBuilder.MockParam;
import com.boot.utils.ArgUtil;

@Component
public class ContakSecurityConfig implements AuditDetailProvider {

	@Configuration
	@EnableWebSecurity
	@Order(90)
	public static class StatelessWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Autowired
		private LogoutHandler logoutHandler;

		@Autowired
		private AuthenticationSuccessHandler successHandler;

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity.antMatcher("/panel/**").sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
					// Permit all
					// Publics Calls
					.and().authorizeRequests().antMatchers("/pub/**").permitAll() // Public URLs
					.and().authorizeRequests().antMatchers("/ext/**").permitAll() // External URLS
					.and().authorizeRequests().antMatchers("/int/**").permitAll() // Internal URLs
					.and().authorizeRequests().antMatchers("/swagger-ui.html").permitAll() // Swagger UI
					// Login Calls
					.and().authorizeRequests().antMatchers("/auth/**").permitAll()
					// API Calls
					.and().authorizeRequests().antMatchers("/api/**").authenticated()
					// App Pages
					.and().authorizeRequests().antMatchers("/app/**").authenticated().and().authorizeRequests()
					.antMatchers("**").authenticated().and().authorizeRequests().antMatchers("/.**").authenticated()
					// Login Forms
					.and().formLogin().loginPage("/auth/login").successHandler(successHandler).permitAll()
					.failureUrl("/auth/login?error").permitAll()
					// .loginProcessingUrl("/auth/login/submit").permitAll()
					// Logout Pages
					.and().logout().permitAll().addLogoutHandler(logoutHandler).logoutUrl("/auth/logout")
					.logoutSuccessUrl("/auth/login?logout")
					.deleteCookies("JSESSIONID", "JXSESSIONID", "CONTAKSESSIONID").invalidateHttpSession(true)
					.permitAll().and().exceptionHandling().accessDeniedPage("/403")
					// Gen stuff
					.and().csrf().disable().headers().disable();
		}

	}

	@Configuration
	@EnableWebSecurity
	@Order(95)
	public static class StompWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity.antMatcher("/stomp-tunnel/**")
					// .addFilterBefore(new SameSiteFilter(),
					// UsernamePasswordAuthenticationFilter.class)
					// filter that adds Same-Site cookie attribute (must be added in right place )
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
					// Permit all
					.and().authorizeRequests().antMatchers("/**").permitAll()
					// CSRF
					.and().csrf().disable().headers().disable();
		}

	}

	@Configuration
	@EnableWebSecurity
	@Order(99)
	public static class DefaultWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
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
	public AuthenticationSuccessHandler successHandler() {
		SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
		handler.setUseReferer(true);
		return handler;
	}

	@Bean
	public MockParam swaggerApiKeyParam() {
		return new MockParamBuilder().id("X_API_KEY").name("x-api-key").description("API Key").defaultValue("")
				.parameterType(MockParamBuilder.MockParamType.HEADER).securityScheme("X_API_KEY").build();

	}

	@Override
	public String getAuditUser() {
		ClientApp x = ContakVendorConfigurer.getClientApp();
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
