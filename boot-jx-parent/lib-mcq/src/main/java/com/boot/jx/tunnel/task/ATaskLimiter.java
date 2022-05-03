package com.boot.jx.tunnel.task;

import java.util.HashMap;
import java.util.Map;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.ReconnectionStrategy;
import org.redisson.api.LocalCachedMapOptions.SyncStrategy;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.exception.AmxException;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.tunnel.ITunnelDefs.ITaskLimiter;
import com.boot.jx.tunnel.ITunnelDefs.TaskInfo;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.TunnelMessage;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;

public abstract class ATaskLimiter implements ITaskLimiter {

	private static final String TUNNE_LIMITER_MAP = "task-limiter-map3-";
	private static final String TUNNE_LIMITER_Q = "task-limiter-q3-";
	Logger logger = LoggerService.getLogger(ATaskLimiter.class);
	public static final int POLL_INTERVAL = 1 * 1000;

	LocalCachedMapOptions<String, TunnelMessage<TunnelTask>> localCacheOptions = LocalCachedMapOptions
			.<String, TunnelMessage<TunnelTask>>defaults().evictionPolicy(EvictionPolicy.NONE).cacheSize(5000)
			.reconnectionStrategy(ReconnectionStrategy.NONE).syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000)
			.maxIdle(10000);

	@Autowired(required = false)
	RedissonClient redisson;

	@Autowired
	TunnelService tunnelService;

	private String taskLimiterName;

	@Override
	public String getName() {
		if (this.taskLimiterName == null) {
			this.taskLimiterName = ClazzUtil.getUltimateClassName(this) + "V5";
		}
		return this.taskLimiterName;
	}

	private RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache;
	private RQueue<TaskInfo> queue;
	private RQueue<TaskInfo> queue2;
	private RQueue<TaskInfo> queue3;
	private RQueue<TaskInfo> queue4;
	private RQueue<TaskInfo> queue5;

	private RLocalCachedMap<String, TunnelMessage<TunnelTask>> getCache() {
		if (cache == null) {
			cache = redisson.getLocalCachedMap(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_MAP + this.getName(),
					localCacheOptions);
		}
		return cache;
	}

	private RQueue<TaskInfo> getQueue(int num) {
		switch (num) {
		case 2:
			if (queue2 == null) {
				queue2 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "2-" + this.getName());
			}
			return queue2;
		case 3:
			if (queue3 == null) {
				queue3 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "3-" + this.getName());
			}
			return queue3;
		case 4:
			if (queue4 == null) {
				queue4 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "4-" + this.getName());
			}
			return queue4;
		case 5:
			if (queue5 == null) {
				queue5 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "5-" + this.getName());
			}
			return queue5;
		default:
			if (queue == null) {
				queue = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + this.getName());
			}
			return queue;
		}
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("q1", getQueue(1).size());
		propMap.put("q2", getQueue(2).size());
		propMap.put("q3", getQueue(3).size());
		propMap.put("q4", getQueue(4).size());
		propMap.put("q5", getQueue(5).size());
		return propMap;
	}

	public void doTask(int pollQNum, int pushQNum, int batchSize) {
		if (!ArgUtil.is(this.getName())) {
			return;
		}
		if (redisson == null) {
			return;
		}

		RQueue<TaskInfo> limiterPollQ = getQueue(pollQNum);
		// logger.info("{} Q-{} S-{} "
		// ,this.eventClassName,pollQNum,limiterPollQ.size());

		for (int i = 0; i < batchSize; i++) {
			TaskInfo info = limiterPollQ.poll();
			if (ArgUtil.is(info)) {

				long now = System.currentTimeMillis();

				if (info.getMatureStamp() <= now) {
					RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache = getCache();
					try {
						TunnelMessage<TunnelTask> latest = cache.get(info.getKey());
						if (ArgUtil.is(latest)) {
							// logger.info("x {} {} {}", x.getTopic(), info.getThrottleKey(),
							// x.getTimestamp());
							if (ArgUtil.isEmpty(latest.getData())) {
								cache.fastRemove(info.getKey());
							} else if (latest.getData().getMatureStamp() <= now) {
								AppContextUtil.setContext(latest.getContext());
								AppContextUtil.init();
								try {
									this.doTask(latest.getData());
								} catch (Exception e) {
									logger.error("LIMITER TASK EXCEPTION:" + info.getInterval(), e);
								}
								logger.debug("Q:{}, Bi:{} T:{} Tk:{}", pollQNum, i, latest.getTopic(), info.getKey());
								AppContextUtil.clear();
								cache.fastRemove(info.getKey());
							}
						}
					} catch (Exception e) {
						logger.error("LIMITER EXCEPTION:" + info.getKey(), e);
					}

				} else {
					getQueue(pushQNum).add(info);
				}
			} else {
				break;
			}
		}
	}

	public abstract void doTask(TunnelTask task);

	@Async
	public void debounce(TunnelTask task) {
		if (redisson == null) {
			throw new AmxException("No Redisson Avaialble");
		}
		// Push to Map
		AppContext context = AppContextUtil.getContext();
		String taskUid = String.format("%s/%s/%s", context.getTenant(), task.getName(),
				ArgUtil.nonEmpty(task.getId(), context.getTraceId()));

		task.setMatureStamp(System.currentTimeMillis() + (task.getInterval()));

		TunnelMessage<TunnelTask> tunnelMessage = new TunnelMessage<TunnelTask>(task, context);
		tunnelMessage.setTopic(task.getName());
		RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache = getCache();
		cache.put(taskUid, tunnelMessage);

		// Push To Turn Queue
		TaskInfo info = new TaskInfo();
		info.setTimestamp(tunnelMessage.getTimestamp());
		info.setInterval(task.getInterval());
		info.setMatureStamp(task.getMatureStamp());
		info.setKey(taskUid);
		RQueue<TaskInfo> limiterQ = getQueue(1);
		limiterQ.add(info);
	}

	@Async
	public void throttle(TunnelTask task) {
		if (redisson == null) {
			throw new AmxException("No Redisson Avaialble");
		}
		// Push to Map
		AppContext context = AppContextUtil.getContext();

		task.setMatureStamp(
				((System.currentTimeMillis() + task.getInterval()) / task.getInterval() * task.getInterval()));

		String taskUid = String.format("%s/%s/%s/%d", context.getTenant(), task.getName(),
				ArgUtil.nonEmpty(task.getId(), context.getTraceId()), task.getMatureStamp());

		TunnelMessage<TunnelTask> tunnelMessage = new TunnelMessage<TunnelTask>(task, context);
		tunnelMessage.setTopic(task.getName());
		RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache = getCache();
		cache.put(taskUid, tunnelMessage);

		// Push To Turn Queue
		TaskInfo info = new TaskInfo();
		info.setTimestamp(tunnelMessage.getTimestamp());
		info.setInterval(task.getInterval());
		info.setMatureStamp(task.getMatureStamp());
		info.setKey(taskUid);
		RQueue<TaskInfo> limiterQ = getQueue(1);
		limiterQ.add(info);
	}

}
