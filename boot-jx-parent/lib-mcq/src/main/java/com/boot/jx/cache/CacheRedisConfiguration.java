package com.boot.jx.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Cluster;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.boot.jx.def.CacheForSessionKey;
import com.boot.jx.def.CacheForTenantKey;
import com.boot.jx.def.CacheForThisKey;
import com.boot.jx.def.CacheForUserKey;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;

@Configuration
// @EnableRedissonHttpSession
@ConditionalOnProperty("app.cache")
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class CacheRedisConfiguration
// extends AbstractHttpSessionApplicationInitializer
{
	// @Bean
	// public LettuceConnectionFactory connectionFactory() {
	// return new LettuceConnectionFactory(); // <2>
	// }

	@Autowired
	private RedisProperties redisProperties;

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private String port;

	public static enum CODEC {
		FST, JACKSON, DEFAULT
	}

	public static final CODEC CODEC_SELECTED = CODEC.FST;
	public static final String CODEC_VERSION = ArgUtil.parseAsString(CODEC_SELECTED.ordinal() + 1);

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redisson() throws IOException {
		// Config config = new Config();
		// config.useClusterServers().addNodeAddress("redis://10.28.42.83:6379");
		// config.useSingleServer().setAddress("redis://" + host + ":" + port);
		// return Redisson.create(config);

		Config config = new Config();
		// sentinel
		if (redisProperties.getSentinel() != null) {
			SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
			sentinelServersConfig.setMasterName(redisProperties.getSentinel().getMaster());
			redisProperties.getSentinel().getNodes();
			sentinelServersConfig.addSentinelAddress(redisProperties.getSentinel().getNodes().split(","));
			sentinelServersConfig.setDatabase(redisProperties.getDatabase());
			if (redisProperties.getPassword() != null) {
				sentinelServersConfig.setPassword(redisProperties.getPassword());
			}
		} else if (redisProperties.getCluster() != null) {
			ClusterServersConfig singleServerConfig = config.useClusterServers();
			Cluster cluster = redisProperties.getCluster();
			singleServerConfig.setScanInterval(2000) // cluster state scan interval in milliseconds
					// use "rediss://" for SSL connection
					.addNodeAddress(CollectionUtil.asArray(cluster.getNodes()));
		} else { // single server
			SingleServerConfig singleServerConfig = config.useSingleServer();
			// format as redis://127.0.0.1:7181 or rediss://127.0.0.1:7181 for SSL
			String schema = redisProperties.isSsl() ? "rediss://" : "redis://";
			singleServerConfig.setAddress(schema + redisProperties.getHost() + ":" + redisProperties.getPort());
			singleServerConfig.setDatabase(redisProperties.getDatabase());
			if (redisProperties.getPassword() != null) {
				singleServerConfig.setPassword(redisProperties.getPassword());
			}
			singleServerConfig.setIdleConnectionTimeout(10 * 1000);
			singleServerConfig.setReconnectionTimeout(3000);

		}
		if (CODEC.JACKSON.equals(CODEC_SELECTED)) {
			org.redisson.codec.JsonJacksonCodec codec = new org.redisson.codec.JsonJacksonCodec(
					JsonUtil.createRawMapper("forRedis"));
			config.setCodec(codec);
		} else if (CODEC.FST.equals(CODEC_SELECTED)) {
			// FSTConfiguration x = FSTConfiguration.createDefaultConfiguration();
			// x.setSerializerRegistryDelegate(new MyFSTSerializerRegistryDelegate());
			// org.redisson.codec.FstCodec codec = new org.redisson.codec.FstCodec(x);
			org.redisson.codec.FstCodec codec = new org.redisson.codec.FstCodec();
			config.setCodec(codec);
		}

		return Redisson.create(config);
	}

	@Bean
	CacheManager cacheManager(@Autowired(required = false) RedissonClient redissonClient) {
		Map<String, CacheConfig> config = new HashMap<String, CacheConfig>();
		// create "testMap" cache with ttl = 10 minutes and maxIdleTime = 5 minutes
		config.put(CacheForUserKey.CACHE, new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
		config.put(CacheForThisKey.CACHE, new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
		config.put(CacheForSessionKey.CACHE, new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
		config.put(CacheForTenantKey.CACHE, new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));

		return new RedissonSpringCacheManager(redissonClient, config);
	}

}
