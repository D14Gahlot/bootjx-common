package com.boot.jx.tunnel;

import java.io.IOException;
import java.io.Serializable;
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
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.exception.AmxException;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.tunnel.ITunnelDefs.ITunnelEventLimiter;
import com.boot.utils.ArgUtil;

public abstract class ATunnelEventLimiter<T> implements ITunnelEventLimiter {

    private static final String TUNNE_LIMITER_MAP = "tunne-limiter-map2-";
    private static final String TUNNE_LIMITER_Q = "tunne-limiter-q2-";
    Logger logger = LoggerService.getLogger(ATunnelEventLimiter.class);
    public static final int POLL_INTERVAL = 1 * 1000;

    public static class EventLimiterInfo implements Serializable {
	private static final long serialVersionUID = -8230113556466236531L;
	long timestamp;
	long throttleTime;
	String throttleKey;

	public String getThrottleKey() {
	    return throttleKey;
	}

	public void setThrottleKey(String throttleKey) {
	    this.throttleKey = throttleKey;
	}

	public long getTimestamp() {
	    return timestamp;
	}

	public void setTimestamp(long timestamp) {
	    this.timestamp = timestamp;
	}

	public long getThrottleTime() {
	    return throttleTime;
	}

	public void setThrottleTime(long throttleTime) {
	    this.throttleTime = throttleTime;
	}

    }

    LocalCachedMapOptions<String, TunnelMessage<T>> localCacheOptions = LocalCachedMapOptions
	    .<String, TunnelMessage<T>>defaults().evictionPolicy(EvictionPolicy.NONE).cacheSize(5000)
	    .reconnectionStrategy(ReconnectionStrategy.NONE).syncStrategy(SyncStrategy.INVALIDATE).timeToLive(10000)
	    .maxIdle(10000);

    @Autowired(required = false)
    RedissonClient redisson;

    @Autowired
    TunnelService tunnelService;

    private String eventClassName = DBEvent.class.getName();

    private RLocalCachedMap<String, TunnelMessage<T>> cache;
    private RQueue<EventLimiterInfo> queue;
    private RQueue<EventLimiterInfo> queue2;
    private RQueue<EventLimiterInfo> queue3;
    private RQueue<EventLimiterInfo> queue4;
    private RQueue<EventLimiterInfo> queue5;

    private RLocalCachedMap<String, TunnelMessage<T>> getCache() {
	if (cache == null) {
	    cache = redisson.getLocalCachedMap(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_MAP + this.eventClassName,
		    localCacheOptions);
	}
	return cache;
    }

    private RQueue<EventLimiterInfo> getQueue(int num) {
	switch (num) {
	case 2:
	    if (queue2 == null) {
		queue2 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "2-" + this.eventClassName);
	    }
	    return queue2;
	case 3:
	    if (queue3 == null) {
		queue3 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "3-" + this.eventClassName);
	    }
	    return queue3;
	case 4:
	    if (queue4 == null) {
		queue4 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "4-" + this.eventClassName);
	    }
	    return queue4;
	case 5:
	    if (queue5 == null) {
		queue5 = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + "5-" + this.eventClassName);
	    }
	    return queue5;
	default:
	    if (queue == null) {
		queue = redisson.getQueue(AppParam.APP_ENV.getValue() + TUNNE_LIMITER_Q + this.eventClassName);
	    }
	    return queue;
	}
    }

    @Override
    public String getName() {
	return eventClassName;
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

    /**
     * 
     * @param topic
     * @param event
     * @param throttleKey
     * @param throttleTime - time in seconds
     */
    @Async
    public void task(String topic, T event, String throttleKey, long throttleTime) {
	if (redisson == null) {
	    throw new AmxException("No Redisson Avaialble");
	}
	this.eventClassName = event.getClass().getName();

	// Push to Map
	AppContext context = AppContextUtil.getContext();
	TunnelMessage<T> tunnelMessage = new TunnelMessage<T>(event, context);
	tunnelMessage.setTopic(topic);
	RLocalCachedMap<String, TunnelMessage<T>> cache = getCache();
	cache.put(throttleKey, tunnelMessage);

	// Push To Turn Queue
	EventLimiterInfo info = new EventLimiterInfo();
	info.setTimestamp(tunnelMessage.getTimestamp());
	info.setThrottleTime(throttleTime * 1000);
	info.setThrottleKey(throttleKey);
	RQueue<EventLimiterInfo> limiterQ = getQueue(1);
	limiterQ.add(info);
	// logger.info("{} Q {} T:{}", this.eventClassName, getQueue(1).size(),
	// tunnelMessage.getTimestamp());

    }

    private void doTask(int pollQNum, int pushQNum, int batchSize) {
	if (!ArgUtil.is(this.eventClassName)) {
	    return;
	}
	if (redisson == null) {
	    return;
	}

	RQueue<EventLimiterInfo> limiterPollQ = getQueue(pollQNum);
	// logger.info("{} Q-{} S-{} "
	// ,this.eventClassName,pollQNum,limiterPollQ.size());

	for (int i = 0; i < batchSize; i++) {
	    EventLimiterInfo info = limiterPollQ.poll();
	    if (ArgUtil.is(info)) {

		long matureCutoffStamp = System.currentTimeMillis() - info.getThrottleTime();

		if (info.getTimestamp() <= matureCutoffStamp) {
		    RLocalCachedMap<String, TunnelMessage<T>> cache = getCache();
		    try {
			TunnelMessage<T> latest = cache.get(info.getThrottleKey());
			if (ArgUtil.is(latest)) {
			    // logger.info("x {} {} {}", x.getTopic(), info.getThrottleKey(),
			    // x.getTimestamp());
			    if ((latest.getTimestamp() <= matureCutoffStamp)) {
				AppContextUtil.setContext(latest.getContext());
				AppContextUtil.init();
				tunnelService.task(latest.getTopic(), latest.getData());
				// logger.info("===== {} {} {}", x.getTopic(), info.getThrottleKey(), "==");
				// JsonUtil.toJson(x.getData()));
				logger.debug("Q:{}, Bi:{} T:{} Tk:{}", pollQNum, i, latest.getTopic(),
					info.getThrottleKey());
				AppContextUtil.clear();
				cache.fastRemove(info.getThrottleKey());
			    }
			}
		    } catch (Exception e) {
			logger.error("LIMITER EXCEPTION:" + info.getThrottleKey(), e);
		    }

		} else {
		    getQueue(pushQNum).add(info);
		}
	    } else {
		break;
	    }
	}

    }

    @Scheduled(fixedDelay = POLL_INTERVAL * 1)
    public void doTask1() throws IOException {
	doTask(1, 2, 5);
    }

    @Scheduled(fixedDelay = POLL_INTERVAL * 3)
    public void doTask2() throws IOException {
	doTask(2, 3, 5);
    }

    @Scheduled(fixedDelay = POLL_INTERVAL * 1)
    public void doTask3() throws IOException {
	doTask(3, 4, 5);
    }

    @Scheduled(fixedDelay = POLL_INTERVAL * 3)
    public void doTask4() throws IOException {
	doTask(4, 5, 5);
    }

    @Scheduled(fixedDelay = POLL_INTERVAL * 1)
    public void doTask5() throws IOException {
	doTask(5, 6, 5);
    }

}
