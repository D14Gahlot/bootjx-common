package com.boot.jx.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AdminLogoutHandler agentLogoutHandler;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				// Publics Calls
				.and().authorizeRequests().antMatchers("/pub/**").permitAll()
				.and().authorizeRequests().antMatchers("/swagger-ui.html").permitAll()
				// Login Calls
				.and().authorizeRequests().antMatchers("/auth/**").permitAll()
				// API Calls
				.and().authorizeRequests().antMatchers("/api/**").authenticated()
				// App Pages
				.and().authorizeRequests().antMatchers("/app/**").authenticated().and().authorizeRequests()
				.antMatchers("/**").authenticated().and().authorizeRequests().antMatchers("/.**").authenticated()
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
				.withUser("admin1").password(passwordEncoder().encode("admin1")).roles("ADMIN").and()
				// Agent 2
				.withUser("admin2").password(passwordEncoder().encode("admin2")).roles("ADMIN").and()
				// Agent 3
				.withUser("admin3").password(passwordEncoder().encode("admin3")).roles("ADMIN");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/v2/api-docs",
				"/configuration/ui", "/swagger-resources/**", "/configuration/security", "/swagger-ui.html",
				"/webjars/**");
	}
}
