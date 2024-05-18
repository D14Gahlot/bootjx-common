package com.boot.jx.admin;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.postman.PMConstants;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSessionBean extends AppAuthModels.AppCommonAuthUser
		implements Serializable, JsonIgnoreUnknown, JsonIgnoreNull {

	private static final long serialVersionUID = 3090820592497487481L;

	public boolean isLoggedIn() {
		return ArgUtil.is(getProfile());
	}

	@Override
	public String getAuthUser() {
		if (ArgUtil.is(this.getProfile())) {
			if (getProfile().isSuperAdmin()) {
				return String.format("%s:%s", this.getProfile().code(), this.getProfile().email());
			} else {
				return this.getProfile().code();
			}
		}
		return PMConstants.DEFAULT.NO_USER;
	}

	public Object getUserSharedProfile() {
		return getProfile();
	}
}
