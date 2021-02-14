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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Order(99)
public class AgentSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AgentLogoutHandler agentLogoutHandler;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				// Publics Calls
				.and().authorizeRequests().antMatchers("/pub/**").permitAll()
				.and().authorizeRequests().antMatchers("/ext/**").permitAll()
				// Login Calls
				.and().authorizeRequests().antMatchers("/auth/**").permitAll()
				// API Calls
				.and().authorizeRequests().antMatchers("/api/**").authenticated()
				// App Pages
				.and().authorizeRequests().antMatchers("**").authenticated().and().authorizeRequests()
				.antMatchers("/.**").authenticated()
				// Login Forms
				.and().formLogin().loginPage("/auth/login").successHandler(successHandler()).permitAll()
				.failureUrl("/auth/login?error").permitAll()
				// .loginProcessingUrl("/auth/login/submit").permitAll()
				// Logout Pages
				.and().logout().permitAll().addLogoutHandler(agentLogoutHandler).logoutUrl("/auth/logout")
				.logoutSuccessUrl("/auth/login?logout").deleteCookies("JSESSIONID").invalidateHttpSession(true)
				.permitAll().and().exceptionHandling().accessDeniedPage("/403").and().csrf().disable().headers()
				.disable();
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
