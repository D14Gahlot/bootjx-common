package com.boot.jx.tunnel.task;

import java.util.Collections;
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
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;

public abstract class ATaskLimiter implements ITaskLimiter {

	private static final String TUNNE_LIMITER_MAP = "task-limiter-map3-";
	private static final String TUNNE_LIMITER_Q = "task-limiter-q3-";
	Logger logger = LoggerService.getLogger(ATaskLimiter.class);
	public static final int POLL_INTERVAL_MIN = 60 * 1000;

	LocalCachedMapOptions<String, TunnelMessage<TunnelTask>> localCacheOptions = LocalCachedMapOptions
			.<String, TunnelMessage<TunnelTask>>defaults().evictionPolicy(EvictionPolicy.NONE).cacheSize(10000)
			.reconnectionStrategy(ReconnectionStrategy.NONE).syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000)
			.maxIdle(10000);

	@Autowired(required = false)
	private RedissonClient redisson;

	private String taskLimiterName;

	public boolean isWorker() {
		return true;
	}

	@Override
	public String getName() {
		if (this.taskLimiterName == null) {
			this.taskLimiterName = ClazzUtil.getUltimateClassName(this) + "V" + getVersion();
		}
		return this.taskLimiterName;
	}

	private RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache;

	private Map<Integer, RQueue<TaskInfo>> queues = Collections
			.synchronizedMap(new HashMap<Integer, RQueue<TaskInfo>>());

	public String getVersion() {
		return "3";
	}

	private RLocalCachedMap<String, TunnelMessage<TunnelTask>> getCache() {
		if (cache == null) {
			cache = redisson.getLocalCachedMap(
					AppParam.APP_ENV.getValue() + TUNNE_LIMITER_MAP + getVersion() + this.getName(), localCacheOptions);
		}
		return cache;
	}

	private RQueue<TaskInfo> getQueue(int num) {
		RQueue<TaskInfo> queue = queues.get(num);
		if (!ArgUtil.is(queue)) {
			queue = redisson.getQueue(
					AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + getVersion() + num + "-" + this.getName());
			queues.put(num, queue);
		}
		return queue;
	}

	private RQueue<TaskInfo> getQueue(int fastQ, int slowQ, long diff) {
		if (diff > POLL_INTERVAL_MIN) {
			return getQueue(slowQ);
		} else
			return getQueue(fastQ);
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

	public void doTask(int pollQNum, int pushQNum, int pushQ10Num, int batchSize) {

		if (!isWorker()) {
			return;
		}

		if (!ArgUtil.is(this.getName())) {
			return;
		}
		if (redisson == null) {
			return;
		}

		RQueue<TaskInfo> limiterPollQ = getQueue(pollQNum);
		// logger.info("{} Q-{} S-{} ", this.getName(), pollQNum, limiterPollQ.size());
		int size = limiterPollQ.size();

		for (int i = 0; i < batchSize; i++) {
			TaskInfo info = limiterPollQ.poll();
			if (ArgUtil.is(info)) {

				long now = System.currentTimeMillis();

				if (info.getMatureStamp() <= now) {
					RLocalCachedMap<String, TunnelMessage<TunnelTask>> cache = getCache();
					try {
						TunnelMessage<TunnelTask> latest = cache.remove(info.getKey());
						if (ArgUtil.is(latest)) {
							// logger.info("x {} {} {}", x.getTopic(), info.getThrottleKey(),
							// x.getTimestamp());
							if (ArgUtil.isEmpty(latest.getData())) {
								cache.fastRemove(info.getKey());
							} else if (latest.getData().getMatureStamp() <= now) {
								AppContextUtil.setContext(latest.getContext());
								AppContextUtil.init();
								boolean passed = true;
								try {
									logger.debug("===========EXECUTED======{} x {}", size, info.getKey());
									passed = this.doTask(latest.getData());
								} catch (Exception e) {
									logger.error("LIMITER TASK EXCEPTION:" + info.getInterval(), e);
								}
								logger.debug("Q:{}, Bi:{} T:{} Tk:{}", pollQNum, i, latest.getTopic(), info.getKey());
								if (passed) {
									cache.fastRemove(info.getKey());
								} else {
									cache.putIfAbsent(info.getKey(), latest);
									getQueue(pushQNum).add(info);
								}
								AppContextUtil.clear();
							} else {
								cache.putIfAbsent(info.getKey(), latest);
							}
						}
					} catch (Exception e) {
						logger.error("LIMITER EXCEPTION:" + info.getKey(), e);
					}

				} else {
					getQueue(pushQNum, pushQ10Num, info.getMatureStamp() - now).add(info);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * 
	 * @param task
	 */
	public void doTaskSafely(TunnelTask task) {

	}

	/**
	 * 
	 * @param task
	 * @return - should return false in case task has failed and you want it to be
	 *         re-attempted till it passes. Warning - use carefully - if task keeps
	 *         failing it can cause infinite look.
	 */
	public boolean doTask(TunnelTask task) {
		this.doTaskSafely(task);
		return true;
	}

	@Async
	@Override
	public void debounce(TunnelTask task) {
		if (redisson == null) {
			throw new AmxException("No Redisson Avaialble");
		}
		// Push to Map
		AppContext context = AppContextUtil.getContext();
		String taskUid = String.format("%s/%s/%s", context.getTenant(), task.getName(),
				ArgUtil.nonEmpty(task.getId(), context.getTraceId()));

		long now = System.currentTimeMillis();
		task.setMatureStamp(now + (task.getInterval()));

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
		RQueue<TaskInfo> limiterQ = getQueue(1, 10, task.getMatureStamp() - now);
		limiterQ.add(info);
		logger.debug("===========debounce={}", info.getKey());
	}

	@Async
	@Override
	public void throttle(TunnelTask task) {
		if (redisson == null) {
			throw new AmxException("No Redisson Avaialble");
		}
		// Push to Map
		AppContext context = AppContextUtil.getContext();

		long now = System.currentTimeMillis();

		task.setMatureStamp(((now + task.getInterval()) / task.getInterval() * task.getInterval()));

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
		RQueue<TaskInfo> limiterQ = getQueue(1, 10, task.getMatureStamp() - now);
		limiterQ.add(info);
		logger.debug("===========throttle={}", info.getKey());
	}

}
