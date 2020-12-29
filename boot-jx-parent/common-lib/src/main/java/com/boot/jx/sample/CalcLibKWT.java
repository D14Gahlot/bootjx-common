package com.boot.jx.sample;

import org.springframework.stereotype.Component;

import com.boot.jx.dict.Tenant;
import com.boot.jx.scope.TenantSpecific;

@Component
@TenantSpecific(Tenant.KWT)
public class CalcLibKWT implements CalcLib {

	@Override
	public String getRSName() {
		return "TenantKwt is Here";
	}

}
