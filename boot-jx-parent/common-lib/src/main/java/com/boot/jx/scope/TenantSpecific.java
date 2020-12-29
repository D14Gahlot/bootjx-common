package com.boot.jx.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Lazy;

import com.boot.jx.dict.Tenant;

@Retention(RetentionPolicy.RUNTIME)
@Lazy
public @interface TenantSpecific {
	Tenant[] value() default Tenant.KWT;
}