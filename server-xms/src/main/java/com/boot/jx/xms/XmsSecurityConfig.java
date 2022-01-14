package com.boot.jx.xms;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.boot.jx.exception.AmxApiError;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.exception.ExceptionMessageKey;
import com.boot.jx.swagger.MockParamBuilder;
import com.boot.jx.swagger.MockParamBuilder.MockParam;
import com.boot.utils.JsonUtil;

@Configuration
@EnableWebSecurity
@Order(99)
public class XmsSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
	httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		// Publics Calls
		.and().authorizeRequests().antMatchers("/pub/**").permitAll()
		// Login Calls
		.and().authorizeRequests().antMatchers("/auth/**").permitAll()
		// API Calls
		.and().authorizeRequests().antMatchers("/api/**").permitAll()
		// App Pages
		.and().authorizeRequests().antMatchers("/app/**").authenticated().and().authorizeRequests()
		.antMatchers("/.**").authenticated()
		// Login Forms
		.and().formLogin().loginPage("/auth/login").successHandler(successHandler()).permitAll()
		.failureUrl("/auth/login?error").permitAll()
		// .loginProcessingUrl("/auth/login/submit").permitAll()
		// Logout Pages
		.and().logout().permitAll().logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login?logout")
		.deleteCookies("JSESSIONID").invalidateHttpSession(true).permitAll().and().exceptionHandling()
		.accessDeniedPage("/403").and().csrf().disable().headers().disable();

	// Exception handling configuration

	httpSecurity.exceptionHandling().authenticationEntryPoint((request, response, e) -> {
	    response.setContentType("application/json;charset=UTF-8");
	    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	    AmxApiError apiError = new AmxApiError();
	    apiError.setHttpStatus(HttpStatus.FORBIDDEN);
	    apiError.setStatusKey(ApiStatusCodes.ACCESS_DENIED.toString());
	    apiError.setException(e.getClass().getName());
	    ExceptionMessageKey.resolveLocalMessage(apiError);
	    response.getWriter().write(JsonUtil.toJson(apiError));
	});

    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
	SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
	handler.setUseReferer(true);
	return handler;
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
	auth.inMemoryAuthentication()
		// Agent 1
		.withUser("agent1").password(passwordEncoder().encode("agent1")).roles("AGENT").and()
		// Agent 2
		.withUser("agent2").password(passwordEncoder().encode("agent2")).roles("AGENT").and()
		// Agent 3
		.withUser("agent3").password(passwordEncoder().encode("agent3")).roles("AGENT");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
	return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
	web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/assets/**",
		"/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
		"/swagger-ui.html", "/webjars/**", "/favicon.ico");
    }

    @Bean
    public MockParam swaggerApiKeyParam() {
	return new MockParamBuilder().name("x-api-key").description("API Key").defaultValue("")
		.parameterType(MockParamBuilder.MockParamType.HEADER).securityScheme("APIKEY").build();

    }

}
