package com.boot.jx.contak;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.boot.jx.common.config.AppCommonAuthFilter.AppCommonAuthUser;
import com.boot.jx.contak.doc.ContakMembershipDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.postman.PMConstants;
import com.boot.utils.ArgUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ContakSessionBean extends AppCommonAuthUser implements Serializable {

	private static final long serialVersionUID = 3090820592497487481L;
	private ContakUserDoc account;
	private List<ContakMembershipDoc> memberships;

	public ContakUserDoc domainUser() {
		return account;
	}

	public void domainUser(ContakUserDoc account) {
		this.account = account;
	}

	public void memberships(List<ContakMembershipDoc> memberships) {
		this.memberships = memberships;
	}

	@Override
	public String getAuthUser() {
		if (ArgUtil.is(this.account)) {
			return this.account.getEmail();
		}
		return PMConstants.DEFAULT.NO_USER;
	}

	public boolean hasAdminAccesTo(String companyId) {

		if (!ArgUtil.is(this.domainUser())) {
			return false;
		}

		if (this.role().contains(PMConstants.USER_ROLE.DUPER_USER)) {
			return true;
		}

		for (ContakMembershipDoc domainDoc : this.memberships) {
			if (ArgUtil.isEqual(domainDoc.getCompany().companyId, companyId)) {
				return true;
			}
		}

		return false;
	}

}
