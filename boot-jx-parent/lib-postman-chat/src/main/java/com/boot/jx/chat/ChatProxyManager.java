package com.boot.jx.chat;

import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppSharedConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.cache.CacheBox;
import com.boot.jx.def.ICacheBox;
import com.boot.utils.ArgUtil;

@Component
public class ChatProxyManager implements AppSharedConfig {

	@Autowired(required = false)
	private RedissonClient redisson;

	private Map<String, String> proxyManager;

	public Map<String, String> proxy() {
		if (redisson != null && proxyManager == null) {
			this.proxyManager = new HashMap<String, String>();
			// this.proxyManager = CacheBox.getInstance("InBoundService-Proxy", redisson);
		}
		return this.proxyManager;
	}

	public void put(String contactId, String proxy) {
		proxy().put(contactId, proxy);
	}

	public void fastRemove(String contactId) {
		proxy().remove(contactId);
	}

	public String get(String contactId) {
		return proxy().get(contactId);
	}

	// Holder
	private CacheBox<String> holdManager;

	public ICacheBox<String> hold() {
		if (redisson != null && holdManager == null) {
			this.holdManager = CacheBox.getInstance("InBoundService-Hold-v2", redisson);
		}
		return this.holdManager;
	}

	public void hold(String contactId) {
		hold().put(contactId, "HOLDING");
	}

	public void release(String contactId) {
		hold().put(contactId, "RELEASING");
	}

	public boolean onhold(String contactId) {
		String status = hold().get(contactId);
		return ArgUtil.isEqual(status, "HOLDING");
	}

	@Override
	public void clear(Map<String, String> map) {
		String tnt = AppContextUtil.getTenant();

	}
}
