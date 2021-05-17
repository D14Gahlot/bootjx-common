package com.boot.jx.cache.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.BoolRespModel;
import com.boot.jx.cache.test.RedisSampleTxCacheBox.RedisSampleData;
import com.boot.jx.tunnel.DBEvent;
import com.boot.jx.tunnel.TunnelDBEventLimiter;
import com.boot.jx.tunnel.TunnelService;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.tunnel.sys.SysTunnelEventsDict;

@RestController
public class RedisController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisController.class);

	@Autowired
	private RedisSampleTxCacheBox redisSampleCacheBox;

	@Autowired
	private TunnelService tunnelService;

	@Autowired(required = false)
	private TunnelDBEventLimiter dbEventLimiter;

	@Autowired
	SharedConfigManager sharedConfigManager;

	@RequestMapping(value = "/pub/redis/test", method = RequestMethod.PUT)
	public RedisSampleData cacheTestGet(@RequestBody RedisSampleData status) {
		redisSampleCacheBox.fastPut(status);
		return status;
	}

	@RequestMapping(value = "/pub/redis/test", method = RequestMethod.GET)
	public RedisSampleData cacheTestGet() {
		return redisSampleCacheBox.get();
	}

	@RequestMapping(value = "/pub/redis/test", method = RequestMethod.POST)
	public long cacheTestPost(@RequestBody RedisSampleData status) {
		return tunnelService.shout(SysTunnelEventsDict.Names.TEST_TOPIC, status);
	}

	@RequestMapping(value = "/pub/stats/tunne-limiter", method = RequestMethod.GET)
	public Map<String, Object> getStats() {
		Map<String, Object> propMap = new HashMap<String, Object>();
		if (dbEventLimiter != null) {
			propMap = dbEventLimiter.getStats();
		}
		return propMap;
	}

	@RequestMapping(value = "/pub/amx/config/shared/clear/all", method = RequestMethod.GET)
	public ApiResponse<BoolRespModel, Object> clearSharedConfig() {
		sharedConfigManager.clear();
		return ApiResponse.build(new BoolRespModel(true));
	}
}
