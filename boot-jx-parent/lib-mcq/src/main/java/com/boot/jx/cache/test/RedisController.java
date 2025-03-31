package com.boot.jx.cache.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.BoolRespModel;
import com.boot.jx.cache.test.RedisSampleTxCacheBox.RedisSampleData;
import com.boot.jx.tunnel.ITunnelDefs.ITaskLimiter;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.TunnelMQ;
import com.boot.jx.tunnel.TunnelMQ.TunnelMQEvent;
import com.boot.jx.tunnel.TunnelMessage;
import com.boot.jx.tunnel.TunnelService;
import com.boot.jx.tunnel.sys.SharedConfigManager;
import com.boot.jx.tunnel.sys.TunnelConstants;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@RestController
public class RedisController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisController.class);

	@Autowired
	private RedisSampleTxCacheBox redisSampleCacheBox;

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private TunnelMQ tunnelMQ;

	@Autowired(required = false)
	private List<ITaskLimiter> dbEventLimiters;

	@Autowired
	SharedConfigManager sharedConfigManager;

	@Autowired(required = false)
	RedisSampleTaskLimiter redisSampleTaskLimiter;

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
		return tunnelService.shout(TunnelConstants.Events.TEST_TOPIC, status);
	}

	@RequestMapping(value = "/pub/tunnel/task", method = RequestMethod.POST)
	public long taskPublish(@RequestBody TunnelMessage<Object> event,
			@RequestParam(value = "propagate", defaultValue = "false") boolean propagate) throws IOException {
		if (propagate) {
			return tunnelService.task(event.getTopic(), event.getData());
		} else {
			if (event.getContext() == null) {
				event.setContext(AppContextUtil.getContext());
			}
			return tunnelService.taskPublish(event.getTopic(), event.getData(), event.getContext());
		}
	}

	@RequestMapping(value = "/pub/tunnel/mq", method = RequestMethod.POST)
	public ApiResponse<Object, Object> tunnelMQ(@RequestBody TunnelMQEvent event,
			@RequestParam(value = "type", defaultValue = "push") String mqType) throws IOException {

		switch (mqType) {
		case "task":
			tunnelMQ.execute(event);
			break;
		case "start":
			tunnelMQ.run(event);
			break;
		case "push":
		default:
			tunnelMQ.push(event);
			break;
		}
		return ApiResponse.build();
	}

	@RequestMapping(value = "/pub/redis/test/task/limiter", method = RequestMethod.POST)
	public long cacheTestTaskLimiter(@RequestBody MapModel map) {
		if (ArgUtil.is(redisSampleTaskLimiter)) {
			redisSampleTaskLimiter
					.debounce(new TunnelTask().id(map.getString("id")).name("DEBOUNCE").intervalSeconds(5));
			redisSampleTaskLimiter
					.throttle(new TunnelTask().id(map.getString("id")).name("THROTTLE").intervalSeconds(5));
			return 1L;
		}
		return 0L;
	}

	@RequestMapping(value = "/pub/stats/tunnel-limiter", method = RequestMethod.GET)
	public Map<String, Object> getStats() {
		Map<String, Object> propMap = new HashMap<String, Object>();
		if (ArgUtil.is(dbEventLimiters)) {
			for (ITaskLimiter iTunnelEventLimiter : dbEventLimiters) {
				Map<String, Object> stats = iTunnelEventLimiter.getStats();
				if (ArgUtil.is(stats)) {
					propMap.put(iTunnelEventLimiter.getName(), stats);
				}
			}
		}
		return propMap;
	}

	@RequestMapping(value = "/pub/amx/config/shared/clear/all", method = RequestMethod.GET)
	public ApiResponse<BoolRespModel, Object> clearSharedConfig() {
		sharedConfigManager.clear();
		return ApiResponse.build(new BoolRespModel(true));
	}
}
