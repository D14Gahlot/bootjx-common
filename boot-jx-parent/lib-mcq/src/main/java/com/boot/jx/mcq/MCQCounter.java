package com.boot.jx.mcq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.mcq.MCQCounterCache.CounterDetails;
import com.boot.utils.TimeUtils;

@Service
public class MCQCounter {

	private Logger LOGGER = LoggerFactory.getLogger(MCQCounter.class);

	@Autowired
	private MCQCounterCache mcqCounterCache;

	public CounterDetails get(String key) {
		CounterDetails x = mcqCounterCache.getOrDefault(key);
		return x;
	}

	public CounterDetails get(String key, String expiry) {
		CounterDetails x = mcqCounterCache.getOrDefault(key);
		if (TimeUtils.isExpired(x.getStartstamp(), expiry)) {
			this.clear(key);
			return mcqCounterCache.getOrDefault(key);
		}
		return x;
	}

	public CounterDetails start(String key) {
		CounterDetails x = mcqCounterCache.getOrDefault(key);
		x.setRound(x.getRound() + 1);
		x.setCount(0);
		x.setStartstamp(System.currentTimeMillis());
		mcqCounterCache.put(key, x);
		return x;
	}

	public long waited(String key) {
		CounterDetails x = mcqCounterCache.getOrDefault(key);
		return System.currentTimeMillis() - x.getStartstamp();
	}

	public CounterDetails count(String key) {
		CounterDetails x = mcqCounterCache.getOrDefault(key);
		x.setCount(x.getCount() + 1);
		x.setTotal(x.getTotal() + 1);
		mcqCounterCache.put(key, x);
		return x;
	}

	public void clear(String key) {
		mcqCounterCache.remove(key);
	}

}
