package com.boot.jx.scope.tnt;

public abstract class AbstractTenantService {

	public String getTenant() {
		return TenantContextHolder.currentSite();
	}

}