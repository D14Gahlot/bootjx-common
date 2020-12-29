package com.boot.jx.cache.test;

import org.springframework.stereotype.Component;

import com.boot.jx.cache.CacheBox;

@Component
public class RedisSampleCacheBox extends CacheBox<String> {

	@Override
	public String getDefault() {
		return "DEFAULT";
	}

}