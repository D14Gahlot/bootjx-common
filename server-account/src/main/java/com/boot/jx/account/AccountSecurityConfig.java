package com.boot.jx.account;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.boot.utils.Urly;

@Configuration
@EnableWebSecurity
public class AccountSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] CONTEXTS = new String[] { "common", "account", "partner", "front", "cpanel" };

    @Autowired
    private LogoutHandler agentLogoutHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry sec = http.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		// Swagger
		.and().authorizeRequests().antMatchers("/swagger-ui.html", "/favicon.ico").permitAll();

	for (String context : CONTEXTS) {
	    // Publics Calls
	    sec = sec.and().authorizeRequests().antMatchers("/" + context + "/pub/**").permitAll()
		    // Auth, login, register
		    .and().authorizeRequests().antMatchers("/" + context + "/auth/**").permitAll()
		    // API Calls
		    .and().authorizeRequests().antMatchers("/" + context + "/api/**").authenticated()
		    // App Pages
		    .and().authorizeRequests().antMatchers("/" + context + "/app/**").authenticated()
		    // Rest of the pages
		    .and().authorizeRequests().antMatchers("/" + context + "/**").authenticated()
		    // DOT extensions
		    .and().authorizeRequests().antMatchers("/" + context + "/.**").authenticated();
	}

	// Login Forms
	sec.and().formLogin().loginPage("/front/auth/login").successHandler(successHandler()).permitAll()
		.failureUrl("/front/auth/login?error").permitAll()
		// .loginProcessingUrl("/auth/login/submit").permitAll()
		// Logout Pages
		.and().logout().permitAll().addLogoutHandler(agentLogoutHandler).logoutUrl("/common/auth/logout")
		.logoutSuccessHandler(logoutSuccessHandler()).logoutSuccessUrl("/front/auth/login?logout")
		.deleteCookies("JSESSIONID", "JXSESSIONID", "ACCTSESSIONID").invalidateHttpSession(true).permitAll()
		.and().exceptionHandling().accessDeniedPage("/403").and().csrf().disable().headers().disable();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
	SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
	handler.setUseReferer(true);
	return handler;
    }

    public LogoutSuccessHandler logoutSuccessHandler() {
	return new LogoutSuccessHandler() {
	    @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		    Authentication authentication) throws IOException, ServletException {

		String referrer = request.getHeader("referer");
		try {
		    referrer = Urly.parse(referrer).getRelativeURL();
		    for (String context : CONTEXTS) {
			if (referrer.startsWith(context, 1)) {
			    redirectResponse(request, response,
				    "/" + context + "/auth/login?logout?_=" + System.currentTimeMillis());
			}
		    }

		} catch (MalformedURLException | URISyntaxException e) {
		    e.printStackTrace();
		}

	    }

	    private void redirectResponse(HttpServletRequest request, HttpServletResponse response,
		    String destination) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", destination);
	    }
	};
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
	web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/assets/**",
		"/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
		"/swagger-ui.html", "/webjars/**", "/favicon.ico");
    }
}
