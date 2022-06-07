package com.boot.jx.agent;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.AppCommonAuthFilter.AppCommonAuthUser;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMConstants;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AgentSessionBean extends AppCommonAuthUser implements Serializable {

	private static final long serialVersionUID = 5850744656958653564L;
	private String agentCode;
	private String agentDept;

	private boolean isLoggedIn;

	private boolean isOnline;
	private boolean isAway;

	private long lastOnlineStamp;

	private long lastSyncStamp;

	private boolean isDirty;

	private AgentResponseAuthDto profile;

	public String getAgentCode() {
		return agentCode;
	}

	public void setAgentCode(String agentCode) {
		this.agentCode = agentCode;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		this.isDirty = true;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
		this.isDirty = true;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public long getLastOnlineStamp() {
		return lastOnlineStamp;
	}

	public void setLastOnlineStamp(long lastOnlineStamp) {
		this.lastOnlineStamp = lastOnlineStamp;
		this.isDirty = true;
	}

	public String getAgentDept() {
		return agentDept;
	}

	public void setAgentDept(String agentDept) {
		this.agentDept = agentDept;
	}

	public AgentResponseAuthDto getProfile() {
		return profile;
	}

	public void setProfile(AgentResponseAuthDto profile) {
		this.profile = profile;
	}

	public long getLastSyncStamp() {
		return lastSyncStamp;
	}

	public void setLastSyncStamp(long lastSyncStamp) {
		this.lastSyncStamp = lastSyncStamp;
	}

	public boolean isAdmin() {
		return getProfile().isAdmin();
	}

	public boolean isAway() {
		return isAway;
	}

	public void setAway(boolean isAway) {
		this.isAway = isAway;
	}

	@Override
	public String getAuthUser() {
		if (ArgUtil.is(this.agentCode)) {
			return this.agentCode;
		} else if (ArgUtil.is(this.profile)) {
			return this.profile.getAgent_code();
		}
		return PMConstants.DEFAULT.NO_USER;
	}

}
