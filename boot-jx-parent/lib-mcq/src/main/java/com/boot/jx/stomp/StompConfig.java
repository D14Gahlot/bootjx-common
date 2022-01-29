package com.boot.jx.stomp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boot.jx.cache.CacheBox.StringCacheBox;

@Configuration
public class StompConfig {
    @Bean("http2sessionUIdMap")
    public StringCacheBox http2sessionUIdMap() {
	return new StringCacheBox("http2sessionUIdMap");
    }

    @Bean("http2stompUIdMap")
    public StringCacheBox http2stompUIdMap() {
	return new StringCacheBox("http2stompUIdMap");
    }

    @Bean("ws2httpMap")
    public StringCacheBox ws2httpMap() {
	return new StringCacheBox("ws2httpMap");
    }

}
