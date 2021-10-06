package com.boot.jx.tnt.custom;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.utils.ArgUtil;

@Component
public class TenantClientResolver extends TenantResolver {

    public static final Map<String, String> tntMapping = new HashMap<String, String>();

    @Autowired
    AppConfig appConfig;

    public String resolve(String tnt) {
	if (ArgUtil.is(tnt) && tntMapping.containsKey(tnt)) {
	    return tntMapping.get(tnt);
	}

	String mappedTnt = appConfig.prop("tenant." + tnt);
	if (ArgUtil.is(mappedTnt)) {
	    return mappedTnt;
	}

	return tnt;
    }

    static {
	tntMapping.put("app", "app");
	tntMapping.put("api", "app");
	tntMapping.put("local", "local");
	tntMapping.put("8d5c-115-111-75-48", "app");
	tntMapping.put("1a11-115-111-75-27", "app");
	tntMapping.put("5a99-115-111-75-27", "app");

    }
}
