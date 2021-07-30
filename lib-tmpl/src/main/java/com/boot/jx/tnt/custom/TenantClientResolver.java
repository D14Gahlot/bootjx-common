package com.boot.jx.tnt.custom;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.utils.ArgUtil;

@Component
public class TenantClientResolver extends TenantResolver {

    public static final Map<String, String> tntMapping = new HashMap<String, String>();

    public String resolve(String tnt) {
	if (ArgUtil.is(tnt) && tntMapping.containsKey(tnt)) {
	    return tntMapping.get(tnt);
	}
	return tnt;
    }

    static {
	tntMapping.put("app", "app");
	tntMapping.put("api", "app");
	tntMapping.put("local", "local");
    }
}
