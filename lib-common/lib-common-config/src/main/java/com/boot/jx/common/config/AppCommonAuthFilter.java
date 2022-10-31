package com.boot.jx.common.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthFilter;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.scope.tnt.Tenants.Tenant;
import com.boot.utils.ArgUtil;

@Component
public class AppCommonAuthFilter implements AppAuthFilter {

	public static class ACCESS_RULES {
		public static final String ONLY_DUPERUSER = "ONLY_DUPERUSER";

		/**
		 * If domain is master domain
		 */
		public static final String ONLY_DUPERUSER_FOR_MASTER_DOMAIN = "ONLY_DUPERUSER_FOR_MASTER_DOMAIN";

		/**
		 * Only domain admin can do this
		 */
		public static final String ONLY_DOMAIN_ADMIN = "ONLY_DOMAIN_ADMIN";
	}

	public static abstract class AppCommonAuthUser implements AppAuthUser {

		private Set<String> role;
		private Set<String> domain;

		public boolean hasAccess(ApiRequestDetail apiRequest, CommonHttpRequest req) {
			return true;
		}

		public Set<String> getRole() {
			return role;
		}

		public void setRole(Set<String> role) {
			this.role = role;
		}

		public Set<String> role() {
			if (this.role == null) {
				this.role = new HashSet<String>();
			}
			return this.role;
		}

		public void addRole(String... roles) {
			for (String newRole : roles) {
				this.role().add(newRole);
			}
		}

		public boolean hasRoleAny(String... roles) {
			this.role = this.role();
			for (String newRole : roles) {
				if (this.role.contains(newRole)) {
					return true;
				}
			}
			return false;
		}

		public boolean hasRoleAll(String... roles) {
			this.role = this.role();
			for (String newRole : roles) {
				if (!this.role.contains(newRole)) {
					return false;
				}
			}
			return true;
		}

		public Set<String> getDomain() {
			return domain;
		}

		public void setDomain(Set<String> domain) {
			this.domain = domain;
		}

		public void addDomain(String... domains) {
			if (this.domain == null) {
				this.domain = new HashSet<String>();
			}
			for (String newDomain : domains) {
				this.domain.add(newDomain);
			}
		}

	}

	@Autowired(required = false)
	private AppCommonAuthUser appCommonAuthUser;

	@Override
	public boolean filterAppRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {
		if (apiRequest.getRules().contains(ACCESS_RULES.ONLY_DUPERUSER)) {
			if (!ArgUtil.is(appCommonAuthUser)) {
				return false;
			}
			return (appCommonAuthUser != null) && appCommonAuthUser.role().contains(PMConstants.USER_ROLE.DUPER_USER);
		} else if (apiRequest.getRules().contains(ACCESS_RULES.ONLY_DUPERUSER_FOR_MASTER_DOMAIN)
				&& Tenants.isDefault(AppContextUtil.getTenant())) {
			return (appCommonAuthUser != null) && appCommonAuthUser.role().contains(PMConstants.USER_ROLE.DUPER_USER);
		} else if (apiRequest.getRules().contains(ACCESS_RULES.ONLY_DOMAIN_ADMIN)) {
			return (appCommonAuthUser != null) && appCommonAuthUser.role().contains(PMConstants.USER_ROLE.ADMIN);
		} else
			return true;
	}

}
