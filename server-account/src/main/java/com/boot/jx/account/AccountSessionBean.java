package com.boot.jx.account;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.common.models.AppAuthModels.AppCommonAuthUser;
import com.boot.jx.postman.PMConstants;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountSessionBean extends AppAuthModels.AppCommonAuthUser implements Serializable, JsonIgnoreUnknown, JsonIgnoreNull {

	private static final long serialVersionUID = 3090820592497487481L;
	private BusinessUserDoc account;

	public BusinessUserDoc domainUser() {
		return account;
	}

	public void domainUser(BusinessUserDoc account) {
		this.account = account;
	}

	@Override
	public String getAuthUser() {
		if (ArgUtil.is(this.account)) {
			return this.account.getContact().getEmail();
		}
		return PMConstants.DEFAULT.NO_USER;
	}

	public boolean hasAdminAccesTo(String domain) {

		if (!ArgUtil.is(this.domainUser())) {
			return false;
		}

		if (this.role().contains(PMConstants.USER_ROLE.DUPER_USER)) {
			return true;
		}

		for (DomainDoc domainDoc : this.domainUser().getDomains()) {
			if (ArgUtil.isEqual(domainDoc.getDomain(), domain)) {
				return true;
			}
		}

		return false;
	}

}
