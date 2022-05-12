package com.boot.jx.tnt.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.rest.RestService;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
public class TenantClientResolver extends TenantResolver {

	private static final String NODOMAIN = "nodomain";
	public static final Map<String, String> tntMapping = new HashMap<String, String>();
	public static final Pattern pattern = Pattern.compile("^(.+?)-(.+?)-(.+?)-(.+?)-(.+?)$");

	@Autowired
	AppConfig appConfig;

	@Autowired
	RestService restService;

	@Value("${mry.account.url}")
	String accountUrl;

	@Value("${default.tenant.static}")
	String tenantStatic;

	public String resolve(String tnt) {

		if (ArgUtil.is(tenantStatic)) {
			return tenantStatic;
		}

		if (!ArgUtil.is(tnt)) {
			return NODOMAIN;
		}

		String mappedTnt = tntMapping.get(tnt);

		if (ArgUtil.is(mappedTnt) && !NODOMAIN.equalsIgnoreCase(mappedTnt)) {
			return mappedTnt;
		}

		if (!appConfig.isProdMode() && ArgUtil.is(tnt)) {
			Matcher matcher = pattern.matcher(tnt);
			if (matcher.find()) {
				return "demo";
			}
		}

		if (ArgUtil.is(accountUrl) && !Tenants.isDefault(tnt) && false) {
			try {
				MapModel resp = restService.ajax(accountUrl).path("/partner/pub/domain/exists")
						.queryParam("tnt", Tenants.getDefault()).queryParam("domain", tnt).get().asMapModel();
				if (resp.keyEntry("meta").is(tnt)) {
					tntMapping.put(tnt, tnt);
					return tnt;
				} else {
					tntMapping.put(tnt, NODOMAIN);
					return NODOMAIN;
				}
			} catch (Exception e) {
				e.printStackTrace();
				tntMapping.put(tnt, NODOMAIN);
				return tnt;
			}
		} else {
			tntMapping.put(tnt, tnt);
		}
		return tnt;
	}

	@Override
	public boolean isValid() {
		String tnt = AppContextUtil.getTenant();
		String mappedTnt = tntMapping.get(tnt);
		return ArgUtil.is(mappedTnt) && mappedTnt.equalsIgnoreCase(tnt);
	}

	static {
		tntMapping.put("app", "app");
		tntMapping.put("api", "app");
		tntMapping.put("local", "local");
		tntMapping.put("a6f6-18-134-58-184", "pranjal");
	}
}
