package com.boot.jx.scope;

import com.boot.jx.dict.Tenant;
import com.boot.utils.ContextUtil;

public class TenantContextHolder {

	public static final String TENANT = "tnt";

	public static void setCurrent(Tenant site) {
		ContextUtil.map().put(TENANT, site);
	}

	public static void setCurrent(String siteId) {
		ContextUtil.map().put(TENANT, fromString(siteId, Tenant.DEFAULT));
	}

	public static void setCurrent(String siteId, Tenant defaultTnt) {
		ContextUtil.map().put(TENANT, fromString(siteId, defaultTnt));
	}

	public static void setDefault() {
		ContextUtil.map().put(TENANT, Tenant.DEFAULT);
	}

	private static Tenant currentSite(boolean returnDefault, Tenant defaultTnt) {
		Object site = ContextUtil.map().get(TENANT);
		if (site == null) {
			if (returnDefault) {
				return defaultTnt;
			} else {
				return null;
			}
		}
		return (Tenant) site;
	}

	/**
	 * Returns Current Tenant/Site OR {@link Tenant#DEFAULT} if no mathicng found in
	 * case returnDefault is TRUE
	 * 
	 * @param returnDefault - to return Tenant#DEFAULT in case no current is set
	 * @return
	 */
	public static Tenant currentSite(boolean returnDefault) {
		return currentSite(returnDefault, Tenant.DEFAULT);
	}

	/**
	 * Returns Current Tenant/Site OR defaultTnt if null OR {@link Tenant#DEFAULT}
	 * if no mathicng found
	 * 
	 * @param defaultTnt
	 * @return
	 */
	public static Tenant currentSite(Tenant defaultTnt) {
		return currentSite(true, Tenant.DEFAULT);
	}

	/**
	 * Returns Current Tenant/Site OR defaultTnt if null OR {@link Tenant#DEFAULT}
	 * if no mathicng found
	 * 
	 * @param defaultTnt
	 * @return
	 */
	public static Tenant currentSite(String defaultTnt) {
		return currentSite(fromString(defaultTnt, Tenant.DEFAULT));
	}

	/**
	 * Returns Current Tenant/Site OR {@link Tenant#DEFAULT} if no mathicng found
	 * 
	 * @return
	 */
	public static Tenant currentSite() {
		return currentSite(true);
	}

	public static Tenant fromString(String siteId, Tenant defaultTnt) {
		return Tenant.fromString(siteId, defaultTnt);
	}

}
