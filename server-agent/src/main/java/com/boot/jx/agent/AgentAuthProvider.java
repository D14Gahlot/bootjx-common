package com.boot.jx.agent;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AgentAuthProvider implements AuthenticationProvider {
	@Override
	public Authentication authenticate(Authentication auth) {
		String username = auth.getName();
		String password = null;
		if (auth.getCredentials() != null) {
			password = auth.getCredentials().toString();
		}
		return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), password, Collections.emptyList());
	}

	@Override
	public boolean supports(Class<?> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}

}
