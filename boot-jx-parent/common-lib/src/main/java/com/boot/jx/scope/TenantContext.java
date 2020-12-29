package com.boot.jx.scope;

import java.util.List;

import com.boot.common.ScopedBeanFactory;
import com.boot.jx.dict.Tenant;

public class TenantContext<T> extends ScopedBeanFactory<Tenant, T> {

	private static final long serialVersionUID = 4007091611441725719L;

	public TenantContext(List<T> libs) {
		super(libs);
	}

	@Override
	public Tenant[] getKeys(T lib) {
		TenantSpecific annotation = lib.getClass().getAnnotation(TenantSpecific.class);
		if (annotation != null) {
			return annotation.value();
		}
		return null;
	}

	@Override
	public Tenant getKey() {
		return TenantContextHolder.currentSite();
	}
}
