package com.boot.jx.stomp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.boot.jx.cache.CacheBox;
import com.boot.jx.cache.CacheBox.StringCacheBox;

@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty("app.stomp")
public class StompSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
	config.enableSimpleBroker("/topic", "/queue", "/tag");
	config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
	registry.addEndpoint("/stomp-tunnel").setAllowedOrigins("*").withSockJS()
		.setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1.4.0/dist/sockjs.min.js") // https://www.gitmemory.com/issue/sockjs/sockjs-client/488/534647358
		.setInterceptors(httpSessionIdHandshakeInterceptor());
    }

    @Bean
    public StompHttpHandshakeInterceptor httpSessionIdHandshakeInterceptor() {
	return new StompHttpHandshakeInterceptor();
    }

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
