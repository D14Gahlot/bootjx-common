package com.boot.jx.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthFilter;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;

@Component
public class AppCommonAuthFilter implements AppAuthFilter {

	@Autowired(required = false)
	private AppAuthModels.AppCommonAuthUser appCommonAuthUser;

	@Override
	public boolean filterAppRequest(ApiRequestDetail apiRequest, CommonHttpRequest req, String traceId) {
		AppAuthModels.AppCommonAuthUser appCommonAuthUserLocal = appCommonAuthUser;
		if (apiRequest.getRules().contains(AppAuthModels.ACCESS_RULES.ONLY_DUPERUSER)) {
			if (!ArgUtil.is(appCommonAuthUserLocal)) {
				return false;
			}
			return (appCommonAuthUserLocal != null)
					&& appCommonAuthUserLocal.role().contains(PMConstants.USER_ROLE.DUPER_USER);
		} else if (apiRequest.getRules().contains(AppAuthModels.ACCESS_RULES.ONLY_SUPERDEV)) {
			if (!ArgUtil.is(appCommonAuthUserLocal)) {
				return false;
			}
			return (appCommonAuthUserLocal.role().contains(PMConstants.USER_ROLE.DUPER_USER)
					|| appCommonAuthUserLocal.role().contains(PMConstants.USER_ROLE.SUPER_DEV));
		} else if (apiRequest.getRules().contains(AppAuthModels.ACCESS_RULES.ONLY_DUPERUSER_FOR_MASTER_DOMAIN)
				&& Tenants.isDefault(AppContextUtil.getTenant())) {
			return (appCommonAuthUserLocal != null)
					&& appCommonAuthUserLocal.role().contains(PMConstants.USER_ROLE.DUPER_USER);
		} else if (apiRequest.getRules().contains(AppAuthModels.ACCESS_RULES.ONLY_DOMAIN_ADMIN)) {
			return (appCommonAuthUserLocal != null)
					&& appCommonAuthUserLocal.role().contains(PMConstants.USER_ROLE.ADMIN);
		} else
			return true;
	}

}
