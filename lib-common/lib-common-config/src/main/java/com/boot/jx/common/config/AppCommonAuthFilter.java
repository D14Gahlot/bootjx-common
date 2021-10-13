package com.boot.jx.common.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthFilter;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.utils.ArgUtil;

@Component
public class AppCommonAuthFilter implements AppAuthFilter {

    public static class ACCESS_RULES {
	public static final String ONLY_DUPERUSER = "ONLY_DUPERUSER";
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

	public void addRole(String... roles) {
	    if (this.role == null) {
		this.role = new HashSet<String>();
	    }
	    for (String newRole : roles) {
		this.role.add(newRole);
	    }
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

	    return appCommonAuthUser.getRole().contains(PMConstants.USER_ROLE.DUPER_USER);
	}

	return true;
    }

}
