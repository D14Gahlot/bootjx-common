package com.boot.jx.common.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.boot.jx.auth.AuthStateManager;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;

public class AppAuthModels {

	public static interface AppCommonAuthUserProfile {

		public boolean isAdmin();

		public boolean isSuperAdmin();

		public String code();

		public String email();

		public String name();

		public boolean isEnabled();

	}

	public static abstract class AppCommonAuthUser extends AuthStateManager implements AppAuthUser, Serializable {

		private static final long serialVersionUID = 5305577408399630086L;
		private Set<String> role;
		private Set<String> domain;
		private AppCommonAuthUserProfile profile;

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

		public Object getUserSharedProfile() {
			return this.getAuthUser();
		}

		public AppCommonAuthUserProfile getProfile() {
			return profile;
		}

		public void setProfile(AppCommonAuthUserProfile profile) {
			this.profile = profile;
		}

	}

	public static class ACCESS_RULES {
		public static final String ONLY_DUPERUSER = "ONLY_DUPERUSER";

		public static final String ONLY_SUPERDEV = "ONLY_SUPERDEV";

		/**
		 * If domain is master domain
		 */
		public static final String ONLY_DUPERUSER_FOR_MASTER_DOMAIN = "ONLY_DUPERUSER_FOR_MASTER_DOMAIN";

		/**
		 * Only domain admin can do this
		 */
		public static final String ONLY_DOMAIN_ADMIN = "ONLY_DOMAIN_ADMIN";
	}

}
