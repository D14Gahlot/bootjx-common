package com.boot.jx.mcq;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.cache.CacheBox;
import com.boot.jx.mcq.MCQCounterCache.CounterDetails;

/**
 * The Class LoggedInUsers.
 */
@Component
public class MCQCounterCache extends CacheBox<CounterDetails> {

	public static class CounterDetails implements Serializable {
		public CounterDetails() {
			super();
			this.count = 0;
			this.total = 0;
			this.round = 0;
			this.startstamp = 0L;
			this.data = new HashMap<String, Object>();
		}

		private static final long serialVersionUID = -1890855298240630667L;
		private Integer count;
		private Integer total;
		private Integer round;
		private long startstamp;
		Map<String, Object> data;

		public long getStartstamp() {
			return startstamp;
		}

		public void setStartstamp(long startstamp) {
			this.startstamp = startstamp;
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public Integer getRound() {
			return round;
		}

		public void setRound(Integer round) {
			this.round = round;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public long waited() {
			return (System.currentTimeMillis() - this.getStartstamp()) / 1000;
		}

		public Integer getTotal() {
			return total;
		}

		public void setTotal(Integer total) {
			this.total = total;
		}
	}

	/**
	 * Instantiates a new logged in users.
	 */
	public MCQCounterCache() {
		super("MCQLockerCacheCounterDetails");
	}

	@Override
	public CounterDetails getDefault() {
		return new CounterDetails();
	}

}
