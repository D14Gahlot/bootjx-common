package com.boot.jx.mcq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.mcq.shedlock.LockingTaskExecutor.Task;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;

@Service
public class MCQLocker {

	private Logger LOGGER = LoggerFactory.getLogger(MCQLocker.class);
	private Map<String, String> leaders = Collections.synchronizedMap(new HashMap<String, String>());
	private Object lock = new Object();

	@Autowired(required = false)
	private RedissonClient redisson;

	@Autowired
	private AppConfig appConfig;

	boolean leader;

	/**
	 * 
	 * @param queue
	 * @param maxAge in millis
	 * @param myId
	 * @return
	 */
	private String challenge(String queue, long maxAge, String id) {
		if (redisson == null) {
			return null;
		}
		RMapCache<String, String> map = redisson.getMapCache("MCQFLAGS");

		String previousLeader = map.putIfAbsent(queue, id, maxAge, TimeUnit.MILLISECONDS);
		if (previousLeader == null) {
			return id;
		}
		return previousLeader;
	}

	public boolean lead(Candidate candidate) {
		String winnerId = challenge(candidate.queue(), candidate.maxAge(), candidate.getId());
		if (candidate.getId().equals(winnerId)) {
			candidate.setLeader(true);
		} else {
			candidate.setLeader(false);
		}
		leaders.put(candidate.queue(), winnerId);
		return candidate.isLeader();
	}

	public boolean resign(Candidate candidate) {
		RMapCache<String, String> map = redisson.getMapCache("MCQFLAGS");
		if (candidate.getId().equals(leaders.get(candidate.queue()))) {
			if (candidate.getId().equals(map.get(candidate.queue()))) {
				map.put(candidate.queue(), candidate.getId(), candidate.fixedDelay(), TimeUnit.MILLISECONDS);
			}
			leaders.remove(candidate.queue());
		}
		return true;
	}

	public void executeWithLock(Runnable task, Candidate lock) {
		try {
			executeWithLock((Task) task::run, lock);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable throwable) {
			// Should not happen
			throw new IllegalStateException(throwable);
		}
	}

	public boolean isTenant(Candidate candidate) {
		if (!ArgUtil.isEmpty(candidate.tenant()) && !Tenants.NONE_STR.equals(candidate.tenant())) {
			if (!ArgUtil.areEqual(appConfig.getDefaultTenant(), candidate.tenant())) {
				return false;
			}
		}
		return true;
	}

	public void executeWithLock(Task task, Candidate lock) throws Throwable {
		AppContextUtil.init();
		if (isTenant(lock) && lead(lock)) {
			try {
				LOGGER.debug("Locked {} FD:{}", lock.queue(), lock.fixedDelay());
				task.call();
			} finally {
				resign(lock);
				LOGGER.debug("Unlocked {}.", lock.queue());
			}
		} else {
			LOGGER.debug("Not executing {}. It's locked.", lock.queue());
		}
	}

}
