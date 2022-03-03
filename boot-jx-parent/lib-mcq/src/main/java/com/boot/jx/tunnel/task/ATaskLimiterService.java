package com.boot.jx.tunnel.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.jx.logger.LoggerService;
import com.boot.jx.tunnel.ITunnelDefs.ITaskLimiter;
import com.boot.utils.ArgUtil;

public abstract class ATaskLimiterService {

    Logger logger = LoggerService.getLogger(ATaskLimiterService.class);
    public static final int POLL_INTERVAL = 1 * 1000;

    @Autowired(required = false)
    RedissonClient redisson;

    @Autowired(required = false)
    private List<ITaskLimiter> aTaskLimiters;

    public Map<String, Object> getStats() {
	Map<String, Object> propMap = new HashMap<String, Object>();
	if (ArgUtil.is(aTaskLimiters)) {
	    for (ITaskLimiter iTunnelEventLimiter : aTaskLimiters) {
		Map<String, Object> stats = iTunnelEventLimiter.getStats();
		if (ArgUtil.is(stats)) {
		    propMap.put(iTunnelEventLimiter.getName(), stats);
		}
	    }
	}
	return propMap;
    }

    public void doTask(int pollQNum, int pushQNum, int batchSize) {
	if (redisson == null) {
	    return;
	}
	for (ITaskLimiter aTaskLimiter : aTaskLimiters) {
	    aTaskLimiter.doTask(pollQNum, pushQNum, batchSize);
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
