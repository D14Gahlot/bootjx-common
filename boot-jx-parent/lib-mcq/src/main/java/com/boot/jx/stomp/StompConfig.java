package com.boot.jx.stomp;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boot.jx.cache.CacheBox;

@Configuration
public class StompConfig {

	static LocalCachedMapOptions<String, String> localCacheOptions = LocalCachedMapOptions.<String, String>defaults()
			.evictionPolicy(EvictionPolicy.LRU).cacheSize(0).reconnectionStrategy(ReconnectionStrategy.CLEAR)
			.syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000).maxIdle(10000);

	public class StompSessionCacheBox extends CacheBox<String> {

		public StompSessionCacheBox(String name) {
			super(StringCacheBox.class.getName() + name + "V4", 4);
		}

		public LocalCachedMapOptions<String, String> options() {
			return localCacheOptions;
		}
	}

	@Bean("http2GSessionIdMap")
	public StompSessionCacheBox http2GSessionIdMap() {
		return new StompSessionCacheBox("http2GSessionIdMap");
	}

	@Bean("http2stompUIdMap")
	public StompSessionCacheBox http2stompUIdMap() {
		return new StompSessionCacheBox("http2stompUIdMap");
	}

	@Bean("ws2xSessionMap")
	public StompSessionCacheBox ws2xSessionMap() {
		return new StompSessionCacheBox("ws2xSessionMap");
	}

	@Bean("ws2jSessionMap")
	public StompSessionCacheBox ws2jSessionMap() {
		return new StompSessionCacheBox("ws2jSessionMap");
	}

}
