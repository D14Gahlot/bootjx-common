package com.boot.jx.rbac.session;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.boot.jx.logger.LoggerService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ConditionalOnProperty(name = "spring.session.store-type", havingValue = "redis")
@EnableRedisHttpSession
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisSessionConfig {

    private static final Logger LOGGER = LoggerService.getLogger(RedisSessionConfig.class);

    @Value("${spring.session.store-type}")
    private String sessionStoreType;

    @Autowired
    private RedisProperties redisProperties;

//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//	if (ArgUtil.is(redisProperties.getSentinel())) {
//	    RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
//		    .master(redisProperties.getSentinel().getMaster());
//
//	    StringUtils.toList(redisProperties.getSentinel().getNodes())
//		    .forEach(s -> sentinelConfig.sentinel(s, redisProperties.getPort()));
//
//	    return new LettuceConnectionFactory(sentinelConfig);
//
//	} else if (ArgUtil.is(redisProperties.getCluster())) {
//	    RedisClusterConfiguration clusterconfig = new RedisClusterConfiguration();
//	    redisProperties.getCluster().getNodes()
//		    .forEach(s -> clusterconfig.clusterNode(s, redisProperties.getPort()));
//	    clusterconfig.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
//
//	    return new LettuceConnectionFactory(clusterconfig);
//	} else {
//	    return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
//	}
//    }

    @Bean
    ObjectMapper redisObjectMapper() {
	ObjectMapper objectMapper = new ObjectMapper();
	objectMapper.registerModule(new JavaTimeModule());
	// objectMapper.registerModule(new Jdk8Module());
	objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
	objectMapper.setDateFormat(new ISO8601DateFormat());
	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	return objectMapper;
    }

    @Bean
    public ConfigureRedisAction configureRedisAction() {
	LOGGER.info("Preventing auto-configuration in secured environments.");
	return ConfigureRedisAction.NO_OP;
    }

    @PostConstruct
    public void init() {
	LOGGER.info("spring.session.store-type=none turns spring session off.");
	LOGGER.info("Redis Session Replication is turned {}.", sessionStoreType.equals("redis") ? "ON" : "OFF");
    }

}
