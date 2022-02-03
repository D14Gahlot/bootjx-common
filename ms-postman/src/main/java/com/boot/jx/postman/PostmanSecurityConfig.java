package com.boot.jx.postman;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

public class PostmanSecurityConfig {

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
		    .and().authorizeRequests().antMatchers("/**").permitAll();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/assets/**",
		    "/v2/**", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
		    "/swagger-ui.html", "/webjars/**", "/favicon.ico");
	}
    }

}
