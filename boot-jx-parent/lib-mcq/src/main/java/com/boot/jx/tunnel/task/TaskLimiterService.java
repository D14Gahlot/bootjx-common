package com.boot.jx.tunnel.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.LoggerService;
import com.boot.jx.tunnel.ITunnelDefs.ITaskLimiter;
import com.boot.utils.ArgUtil;

@Component
public class TaskLimiterService {

	Logger logger = LoggerService.getLogger(TaskLimiterService.class);
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

	public void doTask(int pollQNum, int pushQNum, int pushQ10Num, int batchSize) {
		if (redisson == null) {
			return;
		}
		for (ITaskLimiter aTaskLimiter : aTaskLimiters) {
			aTaskLimiter.doTask(pollQNum, pushQNum, pushQ10Num, batchSize);
		}
	}

	// FAST TASKS
	@Scheduled(fixedDelay = POLL_INTERVAL * 1)
	public void doTask1() throws IOException {
		doTask(1, 2, 10, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 3)
	public void doTask2() throws IOException {
		doTask(2, 3, 10, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 1)
	public void doTask3() throws IOException {
		doTask(3, 4, 10, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 3)
	public void doTask4() throws IOException {
		doTask(4, 5, 10, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 1)
	public void doTask5() throws IOException {
		doTask(5, 6, 10, 5);
	}

	// SLOW TASKS
	@Scheduled(fixedDelay = POLL_INTERVAL * 10)
	public void doTask10() throws IOException {
		doTask(10, 4, 20, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 20)
	public void doTask20() throws IOException {
		doTask(20, 4, 30, 5);
	}

	@Scheduled(fixedDelay = POLL_INTERVAL * 30)
	public void doTask30() throws IOException {
		doTask(30, 4, 10, 5);
	}
}
