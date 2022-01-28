package com.boot.jx.account;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.common.config.AppCommonAuthFilter.AppCommonAuthUser;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountSessionBean extends AppCommonAuthUser implements AuditDetailProvider, Serializable {

    private static final long serialVersionUID = 3090820592497487481L;
    private BusinessUserDoc account;

    @Override
    public String getAuditUser() {
	if (ArgUtil.is(this.account)) {
	    return this.account.getContact().getEmail();
	}
	return null;
    }

    public BusinessUserDoc domainUser() {
	return account;
    }

    public void domainUser(BusinessUserDoc account) {
	this.account = account;
    }

}
