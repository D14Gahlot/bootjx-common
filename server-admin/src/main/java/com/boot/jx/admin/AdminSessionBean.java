package com.boot.jx.admin;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.AppCommonSessionBean;
import com.boot.jx.common.dto.AgentResponseAuthDto;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSessionBean extends AppCommonSessionBean {

	private static final long serialVersionUID = 3090820592497487481L;
	private AgentResponseAuthDto profile;

	public AgentResponseAuthDto getProfile() {
		return profile;
	}

	public void setProfile(AgentResponseAuthDto profile) {
		this.profile = profile;
	}

}
