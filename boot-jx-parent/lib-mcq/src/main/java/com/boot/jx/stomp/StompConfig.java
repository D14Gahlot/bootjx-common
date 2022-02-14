package com.boot.jx.stomp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boot.jx.cache.CacheBox.StringCacheBox;

@Configuration
public class StompConfig {
    @Bean("http2GSessionIdMap")
    public StringCacheBox http2GSessionIdMap() {
	return new StringCacheBox("http2GSessionIdMap");
    }

    @Bean("http2stompUIdMap")
    public StringCacheBox http2stompUIdMap() {
	return new StringCacheBox("http2stompUIdMap");
    }

    @Bean("ws2xSessionMap")
    public StringCacheBox ws2xSessionMap() {
	return new StringCacheBox("ws2xSessionMap");
    }

    @Bean("ws2jSessionMap")
    public StringCacheBox ws2jSessionMap() {
	return new StringCacheBox("ws2jSessionMap");
    }

}
