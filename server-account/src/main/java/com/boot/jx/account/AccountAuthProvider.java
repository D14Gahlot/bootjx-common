package com.boot.jx.account;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountAuthProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication auth) {
	String username = auth.getName();
	String password = null;
	if (auth.getCredentials() != null) {
	    password = auth.getCredentials().toString();
	}
	return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> auth) {
	return auth.equals(UsernamePasswordAuthenticationToken.class);
    }

}
