package com.boot.jx.sample;

import org.springframework.stereotype.Component;

import com.boot.jx.dict.Tenant;
import com.boot.jx.scope.TenantSpecific;

@Component
@TenantSpecific({ Tenant.OMN, Tenant.KWT2 })
public class CalcLibOMN implements CalcLib {

	@Override
	public String getRSName() {
		return "TenantKwt_D is Here";
	}

}
