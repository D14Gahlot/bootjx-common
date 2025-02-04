package com.boot.jx.common.config;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.cache.CacheBox;
import com.boot.jx.common.config.CONFIG_FEATURES_KEY.PLANS;
import com.boot.jx.def.ICacheBox;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMGateKeeper;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;

@Component
public class PMGeteKeeperImpl implements PMGateKeeper {

	private static final long EXPIRY_TIME = 24;

	@Autowired
	private PMEnvironment environment;

	private ICacheBox<String> taskStatus;
	private String jobName;

	@Autowired(required = false)
	private RedissonClient redisson;

	private String getJobName() {
		if (this.jobName == null) {
			this.jobName = ClazzUtil.getUltimateClassName(this) + "V13";
		}
		return this.jobName;
	}

	public ICacheBox<String> taskStatus() {
		if (taskStatus == null) {
			this.taskStatus = CacheBox.getInstance("QTE-TASK-M-" + getJobName(), redisson);
		}
		return this.taskStatus;
	}

	@Override
	public boolean canSendMessage(OutboxMessage outboxMessage) {
		if (!environment.featureEntry(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND)
				.asBoolean(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND.getDefaultValue())) {
			outboxMessage.logs().add("Outbound Restricted");
			return false;
		}

		String plan = environment.featureEntry(CONFIG_FEATURES_KEY.PLAN)
				.asString(CONFIG_FEATURES_KEY.PLAN.getDefaultValue());

		if (ArgUtil.is(plan, PLANS.BLOCKED)) {
			outboxMessage.logs().add("Account Blocked");
			return false;
		}

		if (ArgUtil.is(plan, PLANS.FREEMIUM)) {

			Integer freemiumDauLimit = environment.featureEntry(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND_DAU_FREEMIUM)
					.asInteger(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND_DAU_FREEMIUM.getDefaultValue());

			String key = "gklimiter:" + AppContextUtil.getTenant() + ":dau:" + freemiumDauLimit;
			RSetCache<String> customerSet = redisson.getSetCache(key);

			// Add customer with per-entry expiry (24 hours)
			boolean isNewCustomer = customerSet.add(outboxMessage.contact().getContactId(), EXPIRY_TIME,
					TimeUnit.HOURS);

			if (isNewCustomer) {
				// Allow if total unique customers are within the limit
				if (customerSet.size() > freemiumDauLimit) {
					outboxMessage.logs().add("Daily quota exceeded");
					return false;
				}
			}

		}

		return true;
	}

}
