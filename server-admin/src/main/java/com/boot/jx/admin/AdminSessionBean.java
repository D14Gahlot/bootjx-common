package com.boot.jx.admin;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.AppCommonAuthFilter.AppCommonAuthUser;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.postman.PMConstants;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSessionBean extends AppCommonAuthUser implements Serializable, JsonIgnoreUnknown, JsonIgnoreNull {

	private static final long serialVersionUID = 3090820592497487481L;
	private AgentResponseAuthDto profile;

	public AgentResponseAuthDto getProfile() {
		return profile;
	}

	public void setProfile(AgentResponseAuthDto profile) {
		this.profile = profile;
	}

	public boolean isLoggedIn() {
		return ArgUtil.is(getProfile());
	}

	@Override
	public String getAuthUser() {
		if (ArgUtil.is(this.profile)) {
			if (profile.isSuperAdmin()) {
				return String.format("%s:%s", this.profile.getAgent_code(), this.profile.getAgent_email());
			} else {
				return this.profile.getAgent_code();
			}
		}
		return PMConstants.DEFAULT.NO_USER;
	}

	public Object getUserSharedProfile() {
		return profile;
	}
}
