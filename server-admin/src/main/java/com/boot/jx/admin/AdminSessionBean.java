package com.boot.jx.admin;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.AppCommonAuthFilter.AppCommonAuthUser;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AdminSessionBean extends AppCommonAuthUser implements AuditDetailProvider, Serializable {

    private static final long serialVersionUID = 3090820592497487481L;
    private AgentResponseAuthDto profile;

    public AgentResponseAuthDto getProfile() {
	return profile;
    }

    public void setProfile(AgentResponseAuthDto profile) {
	this.profile = profile;
    }

    @Override
    public String getAuditUser() {
	if (ArgUtil.is(this.profile)) {
	    return this.profile.getAgent_code();
	}
	return null;
    }

}
