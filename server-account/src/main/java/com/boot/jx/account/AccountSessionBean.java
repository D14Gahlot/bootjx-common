package com.boot.jx.account;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.account.doc.DomainUserDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountSessionBean implements AuditDetailProvider {

    private static final long serialVersionUID = 3090820592497487481L;
    private DomainUserDoc account;
    private String role;

    @Override
    public String getAuditUser() {
	if (ArgUtil.is(this.account)) {
	    return this.account.getContact().getEmail();
	}
	return null;
    }

    public DomainUserDoc domainUser() {
	return account;
    }

    public void domainUser(DomainUserDoc account) {
	this.account = account;
    }

    public String getRole() {
	return role;
    }

    public void setRole(String role) {
	this.role = role;
    }

}
