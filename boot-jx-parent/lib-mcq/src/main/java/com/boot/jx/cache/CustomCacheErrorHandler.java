package com.boot.jx.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomCacheErrorHandler implements CacheErrorHandler {

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		System.out.println("handleCacheGetError");
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		System.out.println("handleCachePutError");
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		System.out.println("handleCacheEvictError");
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		System.out.println("handleCacheClearError");
	}

}
