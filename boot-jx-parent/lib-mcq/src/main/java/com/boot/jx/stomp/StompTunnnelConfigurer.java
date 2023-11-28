package com.boot.jx.stomp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import com.boot.jx.http.RequestType;

@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty("app.stomp")
public class StompTunnnelConfigurer extends AbstractWebSocketMessageBrokerConfigurer
		implements com.boot.jx.http.ApiRequestConfig {

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

	@Override
	public RequestType from(HttpServletRequest req, RequestType reqType) {
		if (req.getRequestURI().contains("/stomp-tunnel/")) {
			return RequestType.STOMP;
		}
		return null;
	}

}
