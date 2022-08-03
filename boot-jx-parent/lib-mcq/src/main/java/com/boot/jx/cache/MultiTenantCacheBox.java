package com.boot.jx.cache;

import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.redisson.api.RLocalCachedMap;
import org.slf4j.Logger;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class MultiTenantCacheBox<T> extends CacheBox<T> {

	private static Logger LOGGER = LoggerService.getLogger(CacheBox.class);

	LocalCachedMapOptions<String, T> localCacheOptions = LocalCachedMapOptions.<String, T>defaults()
			.evictionPolicy(EvictionPolicy.NONE).cacheSize(1000).reconnectionStrategy(ReconnectionStrategy.CLEAR)
			.syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000).maxIdle(10000);

	RemovalListener<String, RLocalCachedMap<String, T>> removalListener = new RemovalListener<String, RLocalCachedMap<String, T>>() {
		public void onRemoval(RemovalNotification<String, RLocalCachedMap<String, T>> removal) {
			try {
				RLocalCachedMap<String, T> conn = removal.getValue();
				conn.destroy(); // tear down properly
			} catch (Exception e) {
				LOGGER.error("Error while destoying RLocalCachedMap for " + removal.getKey(), e);
			}
		}
	};

	private Cache<String, RLocalCachedMap<String, T>> perTenant = CacheBuilder.newBuilder().maximumSize(1000)
			.removalListener(removalListener).build();

	public MultiTenantCacheBox(String name, int version) {
		super(name, version);
	}

	public RLocalCachedMap<String, T> map() {
		if (redisson != null) {
			String tenant = AppContextUtil.getTenant();
			@Nullable
			RLocalCachedMap<String, T> tenantCache = perTenant.getIfPresent(tenant);
			if (tenantCache == null) {
				String localCacheName = String.format("%s-%s.%s-%s-%s.%s", AppParam.APP_ENV.getValue(), tenant,
						AppParam.APP_VENV.getValue(),
						(ArgUtil.isEmpty(getCahceName()) ? getClazzName() : getCahceName()),
						CacheRedisConfiguration.CODEC_VERSION, version());
				tenantCache = redisson.getLocalCachedMap(localCacheName, options());
				perTenant.put(tenant, tenantCache);
			}
			return tenantCache;
		}
		return null;
	}
}
