package com.boot.jx.scope;

import com.boot.jx.dict.Tenant;

public abstract class AbstractTenantService {

	public Tenant getTenant() {
		return TenantContextHolder.currentSite();
	}
	
}