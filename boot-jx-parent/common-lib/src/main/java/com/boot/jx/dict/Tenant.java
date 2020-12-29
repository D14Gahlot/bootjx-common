package com.boot.jx.dict;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.boot.utils.ArgUtil;

public enum Tenant {

	/** Dev Environments **/
	KWT2("KW", 91, "Kuwait Alt", Currency.KWD, Language.AR),
	BRNDEV("BH", 104, "Bahrain dev", Currency.BHD, Language.AR),
	OMNDEV("OM", 82, "oman dev", Currency.OMR, Language.AR),

	KWT("KW", 91, "Kuwait", Currency.KWD, Language.AR), BHR("BH", 104, "Bahrain", Currency.BHD, Language.AR),
	OMN("OM", 82, "Oman", Currency.OMR, Language.AR),

	KWTV2("KW", 91, "Kuwait", Currency.KWD, Language.AR),

	NONE("none", 0, null, null, Language.EN);

	public static Tenant DEFAULT = KWT;

	public static Map<String, Tenant> mapping = new HashMap<String, Tenant>();
	public static final Pattern pattern = Pattern.compile("^(.+?)-(.+?)$");
	public static final Map<Integer, Tenant> countryIdToCountryMap = new HashMap<>();

	static {
		// Additional Mappings
		// mapping.put("app-devq", KWT2);
		for (Tenant site : Tenant.values()) {
			mapping.put(site.toString().toLowerCase(), site);
			mapping.put(site.getId().toLowerCase(), site);
			mapping.put("app-" + site.toString().toLowerCase(), site);
			countryIdToCountryMap.put(site.getCountryId(), site);
		}

	}

	private String id;
	private String code;
	private boolean tenant;
	private Currency currency;
	public final Integer countryId;
	public Language defaultLang;

	public boolean isTenant() {
		return tenant;
	}

	public Language defaultLang() {
		return defaultLang;
	}

	public void setTenant(boolean tenant) {
		this.tenant = tenant;
	}

	Tenant(String id, int code, String name, boolean isTenantApp, Currency currency, Language defaultLang) {
		this.id = id;
		this.countryId = code;
		this.code = ArgUtil.parseAsString(code);
		this.tenant = isTenantApp;
		this.currency = currency;
		this.defaultLang = defaultLang;
	}

	Tenant(String id, int code, String name) {
		this(id, code, name, true, null, Language.EN);
	}

	Tenant(String id, int code, String name, boolean isTenantApp) {
		this(id, code, name, isTenantApp, null, Language.EN);
	}

	Tenant(String id, int code, String name, Currency currency, Language defaultLang) {
		this(id, code, name, true, currency, defaultLang);
	}

	Tenant() {
		this(null, 0, null);
	}

	public String getId() {
		return id;
	}

	public String getISO2Code() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public BigDecimal getBDCode() {
		return new BigDecimal(code);
	}

	public static Tenant fromString(String siteId, Tenant defaultValue) {
		if (siteId != null) {
			String siteIdStr = siteId.toLowerCase();
			Matcher matcher = pattern.matcher(siteIdStr);
			if (matcher.find()) {
				siteIdStr = matcher.group(2);
			}

			if (mapping.containsKey(siteIdStr)) {
				return mapping.get(siteIdStr);
			}
			for (Tenant site : Tenant.values()) {
				if (site.toString().equalsIgnoreCase(siteIdStr)) {
					return site;
				}
			}
		}
		return defaultValue;
	}

	public static Tenant fromString(String siteId, Tenant defaultValue, boolean onlyTenant) {
		Tenant tnt = fromString(siteId, defaultValue);
		if (onlyTenant && (tnt != null && !tnt.isTenant())) {
			return defaultValue;
		}
		return tnt;
	}

	public static Tenant fromCountryId(Integer siteId) {
		return countryIdToCountryMap.get(siteId);
	}

	public static List<String> tenantStrings() {
		List<String> values = new ArrayList<>();
		for (Tenant site : Tenant.values()) {
			if (site.isTenant()) {
				values.add(site.toString());
			}
		}
		return values;
	}

	public Integer getCountryId() {
		return countryId;
	}

	public Currency getCurrency() {
		return currency;
	}

	public String toString() {
		return this.name();
	}

}
