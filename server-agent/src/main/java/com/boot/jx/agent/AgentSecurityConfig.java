package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@Order(99)
public class AgentSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LogoutHandler logoutHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		// Publics Calls
		.and().authorizeRequests().antMatchers("/pub/**").permitAll() // Public URLs
		.and().authorizeRequests().antMatchers("/ext/**").permitAll() // External URLS
		.and().authorizeRequests().antMatchers("/int/**").permitAll() // Internal URLs
		.and().authorizeRequests().antMatchers("/stomp-tunnel/**").permitAll() // Stomp Calls
		.and().authorizeRequests().antMatchers("/swagger-ui.html").permitAll() // Swagger UI
		// Login Calls
		.and().authorizeRequests().antMatchers("/auth/**").permitAll()
		// API Calls
		.and().authorizeRequests().antMatchers("/api/**").authenticated()
		// App Pages
		.and().authorizeRequests().antMatchers("/app/**").authenticated().and().authorizeRequests()
		.antMatchers("**").authenticated().and().authorizeRequests().antMatchers("/.**").authenticated()
		// Login Forms
		.and().formLogin().loginPage("/auth/login").successHandler(successHandler()).permitAll()
		.failureUrl("/auth/login?error").permitAll()
		// .loginProcessingUrl("/auth/login/submit").permitAll()
		// Logout Pages
		.and().logout().permitAll().addLogoutHandler(logoutHandler).logoutUrl("/auth/logout")
		.logoutSuccessUrl("/auth/login?logout").deleteCookies("JSESSIONID", "JXSESSIONID", "AGENTSESSIONID")
		.invalidateHttpSession(true).permitAll().and().exceptionHandling().accessDeniedPage("/403").and().csrf()
		.disable().headers().disable();
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
		"/swagger-ui.html", "/webjars/**");
    }
}
