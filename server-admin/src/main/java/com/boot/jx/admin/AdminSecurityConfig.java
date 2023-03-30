package com.boot.jx.admin;

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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.boot.utils.Urly;

@Configuration
@EnableWebSecurity
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private LogoutHandler agentLogoutHandler;

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
				.antMatchers("/**").authenticated().and().authorizeRequests().antMatchers("/.**").authenticated()
				// Login Forms
				.and().formLogin().loginPage("/auth/login").successHandler(successHandler()).permitAll()
				.failureUrl("/auth/login?error").permitAll()
				// .loginProcessingUrl("/auth/login/submit").permitAll()
				// Logout Pages
				.and().logout().permitAll().addLogoutHandler(agentLogoutHandler).logoutUrl("/auth/logout")
				.logoutSuccessUrl("/auth/login?logout").deleteCookies("JSESSIONID", "JXSESSIONID", "ADMINSESSIONID")
				.invalidateHttpSession(true).permitAll().and().exceptionHandling().accessDeniedPage("/403").and().csrf()
				.disable().headers().disable();
	}

	@Bean
	public AuthenticationSuccessHandler successHandler() {
		SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
		handler.setUseReferer(true);
		return handler;
	}

	public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint(String loginFormUrl) {
		return new LoginUrlAuthenticationEntryPoint(loginFormUrl) {
			@Override
			protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) {
				String continueParamValue = UrlUtils.buildRequestUrl(request);
				String redirect = super.determineUrlToUseForThisRequest(request, response, exception);
				return UriComponentsBuilder.fromPath(redirect).queryParam("referer", continueParamValue).toUriString();
			}
		};
	}

	public LogoutSuccessHandler logoutSuccessHandler() {
		return new LogoutSuccessHandler() {
			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				String referrer = request.getHeader("referer");
				try {
					referrer = Urly.parse(referrer).getRelativeURL();
					redirectResponse(request, response, "/auth/login?logout?_=" + System.currentTimeMillis());
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
				.withUser("admin1").password(passwordEncoder().encode("admin1")).roles("ADMIN");
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
