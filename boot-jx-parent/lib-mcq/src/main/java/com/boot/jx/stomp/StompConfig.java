package com.boot.jx.stomp;

import java.io.Serializable;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boot.jx.cache.CacheBox;

@Configuration
public class StompConfig {

	public class CommonStompSessionCacheBox<T> extends CacheBox<T> {

		LocalCachedMapOptions<String, T> localCacheOptions = LocalCachedMapOptions.<String, T>defaults()
				.evictionPolicy(EvictionPolicy.LRU).cacheSize(0).reconnectionStrategy(ReconnectionStrategy.CLEAR)
				.syncStrategy(SyncStrategy.INVALIDATE).timeToLive(60000).maxIdle(500000);

		public CommonStompSessionCacheBox(String name, int version) {
			super(name, version);
		}

		public LocalCachedMapOptions<String, T> options() {
			return localCacheOptions;
		}
	}

	public static class StompSession implements Serializable {

		private static final long serialVersionUID = 2457062425857422747L;

		String xsessionId;
		String jsessionId;
		String prefix;
		String[] tags;
		String tenantToken;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getXsessionId() {
			return xsessionId;
		}

		public void setXsessionId(String xSessionId) {
			this.xsessionId = xSessionId;
		}

		public String[] getTags() {
			return tags;
		}

		public void setTags(String[] tags) {
			this.tags = tags;
		}

		public String getTenantToken() {
			return tenantToken;
		}

		public void setTenantToken(String tenantToken) {
			this.tenantToken = tenantToken;
		}

		public String getJsessionId() {
			return jsessionId;
		}

		public void setJsessionId(String jsessionId) {
			this.jsessionId = jsessionId;
		}

	}

	public class StompSessionIndexes extends CommonStompSessionCacheBox<String> {
		public StompSessionIndexes(String name) {
			super(StompSessionIndexes.class.getName() + "#" + name + "V4", 4);
		}
	}

	public class StompSessionDetails extends CommonStompSessionCacheBox<StompSession> {
		/**
		 * Instantiates a new logged in users.
		 */
		public StompSessionDetails(String name) {
			super(StompSessionIndexes.class.getName() + "#" + name + "V4", 4);
		}

	}

	@Bean("http2GSessionIdMap")
	public StompSessionIndexes http2GSessionIdMap() {
		return new StompSessionIndexes("http2GSessionIdMap");
	}

	@Bean("http2stompUIdMap")
	public StompSessionIndexes http2stompUIdMap() {
		return new StompSessionIndexes("http2stompUIdMap");
	}

	@Bean("ws2xSessionMap")
	public StompSessionIndexes ws2xSessionMap() {
		return new StompSessionIndexes("ws2xSessionMap");
	}

	@Bean("ws2jSessionMap")
	public StompSessionIndexes ws2jSessionMap() {
		return new StompSessionIndexes("ws2jSessionMap");
	}

	@Bean("stompSessionDetails")
	public StompSessionDetails stompSessionDetails() {
		return new StompSessionDetails("stompSessionDetails");
	}

}
