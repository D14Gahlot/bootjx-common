package com.boot.jx.common.config;

import java.util.concurrent.TimeUnit;
import java.util.Map;

import org.redisson.api.RAtomicLong;
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
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.HSMTemplateDoc;

@Component
public class PMGateKeeperImpl implements PMGateKeeper {

	private static final long EXPIRY_TIME = 24;
	private static final Logger LOGGER = LoggerFactory.getLogger(PMGateKeeperImpl.class);

	@Autowired
	private PMEnvironment environment;
	
	@Autowired
	private SessionStore sessionStore;
	
	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

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
		
		//get contact doc
		ChatContactDoc chatContactDoc = sessionStore.getContact(outboxMessage.contact().getContactId());
		
		//check subs. if contact exists 
		if(chatContactDoc != null && !checkSubscription(chatContactDoc, outboxMessage)) {
			return false;
		}
		
		if (!environment.config().featureEntry(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND)
				.asBoolean(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND.getDefaultValue())) {
			outboxMessage.logs().add("Outbound Restricted");
			outboxMessage.status(Status.BLCKD);
			return false;
		}

		String plan = environment.config().featureEntry(CONFIG_FEATURES_KEY.PLAN)
				.asString(CONFIG_FEATURES_KEY.PLAN.getDefaultValue());

		if (ArgUtil.is(plan, PLANS.BLOCKED)) {
			outboxMessage.logs().add("Account Blocked");
			outboxMessage.status(Status.BLCKD);
			return false;
		}

		if (ArgUtil.is(plan, PLANS.FREEMIUM)) {
			Integer freemiumDauLimit = environment.config()
					.featureEntry(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND_DAU_FREEMIUM)
					.asInteger(CONFIG_FEATURES_KEY.MESSAGE_OUTBOUND_DAU_FREEMIUM.getDefaultValue());

			String key = "gklimiter:" + AppContextUtil.getTenant() + ":dau:" + freemiumDauLimit;
			RSetCache<String> customerSet = redisson.getSetCache(key);

			// Add customer with per-entry expiry (24 hours)
			boolean isNewCustomer = customerSet.add(outboxMessage.contact().getContactId(), EXPIRY_TIME,
					TimeUnit.HOURS);

			if (isNewCustomer) {
				// Allow if total unique customers are within the limit
				if (customerSet.size() > freemiumDauLimit) {
					outboxMessage.logs().add("Daily quota (outbound) exceeded");
					outboxMessage.status(Status.LIMIT);
					return false;
				}
			}

		}

		return true;
	}

	@Override
	public boolean canSendTemplateMedia(OutboxMessage outboxMessage) {
		
		//get contact doc
		ChatContactDoc chatContactDoc = sessionStore.getContact(outboxMessage.contact().getContactId());
				
		//check subs. if contact exists 
		if(chatContactDoc != null && !checkSubscription(chatContactDoc, outboxMessage)) {
			return false;
		}

		Boolean defaultValue = Boolean.TRUE.equals(CONFIG_FEATURES_KEY.MSG_MEDIA_TEMPLATE.getDefaultValue());
		if (!environment.config().featureEntry(CONFIG_FEATURES_KEY.MSG_MEDIA_TEMPLATE).asBoolean(defaultValue)) {
			outboxMessage.logs().add("Templated Media Restricted");
			outboxMessage.status(Status.BLCKD);
			return false;
		}

		String plan = environment.config().featureEntry(CONFIG_FEATURES_KEY.PLAN)
				.asString(CONFIG_FEATURES_KEY.PLAN.getDefaultValue());

		if (ArgUtil.is(plan, PLANS.BLOCKED)) {
			outboxMessage.logs().add("Account Blocked");
			outboxMessage.status(Status.BLCKD);
			return false;
		}

		if (ArgUtil.is(plan, PLANS.FREEMIUM)) {
			Integer freemiumMediaLimit = environment.config()
					.featureEntry(CONFIG_FEATURES_KEY.MSG_MEDIA_TEMPLATE_FREEMIUM)
					.asInteger(CONFIG_FEATURES_KEY.MSG_MEDIA_TEMPLATE_FREEMIUM.getDefaultValue());

			String key = "gklimiter:" + AppContextUtil.getTenant() + ":tmplmedia:" + freemiumMediaLimit;

			RAtomicLong mediaCounter = redisson.getAtomicLong(key);
			// If it's the first increment today, set an expiry for 24 hours
			if (mediaCounter.get() == 0) {
				mediaCounter.expire(EXPIRY_TIME, TimeUnit.HOURS);
			}

			// Increment count and check if it exceeds the limit
			long currentCount = mediaCounter.incrementAndGet();
			if (currentCount > freemiumMediaLimit) {
				outboxMessage.logs().add("Daily quota (Template Media) exceeded");
				outboxMessage.status(Status.LIMIT);
				return false;
			}

		}

		return true;
	}
	
	public boolean checkSubscription(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
		
			Map<String, Boolean> subscriptions = chatContactDoc.getSubscriptions();
			
			LOGGER.debug("Checking subscription for contact: {} with subscriptions: {}", chatContactDoc.getContactId(), subscriptions);
			
			//no subscription object allow the message
			if(subscriptions == null || subscriptions.isEmpty()) {
				LOGGER.info("No subscriptions found for contact:{} , allowing message", chatContactDoc.getContactId());
				return true;
			}
			
			//1st check if all exists 
			if(subscriptions.containsKey("all")) {
				// if exists use its value
				boolean allowed = Boolean.TRUE.equals(subscriptions.get("all"));
				if(!allowed) {
					LOGGER.info("Message blocked for contact: {} - 'all' subscription is false", chatContactDoc.getContactId());
					outboxMessage.logs().add("Message blocked - 'all' subscription is false");
					outboxMessage.status(Status.BLCKD);
				}else {
					LOGGER.info("Message allowed for contact: {} - 'all' subscription is true", chatContactDoc.getContactId());
				}
				return allowed ;
			}
			
			// If 'all' is not present, check template category
			// Get template category from HSMTemplateDoc
			String templateCategory = null;
			if (outboxMessage.getHsm() != null && outboxMessage.getHsm().getId() != null) {
				HSMTemplateDoc templateDoc = commonMongoTemplate.findById(outboxMessage.getHsm().getId(), HSMTemplateDoc.class);
				if (templateDoc != null) {
					templateCategory = templateDoc.getCategoryType().toLowerCase();
					LOGGER.debug("Found template category from HSMTemplateDoc: {}", templateCategory);
				}
			}
						
			if (templateCategory == null) {
				LOGGER.warn("No template category found in HSMTemplateDoc, blocking by default");
				outboxMessage.logs().add("Message blocked - no template category found");
				outboxMessage.status(Status.BLCKD);
				return false;
			}
						
			// Check if the template category exists in subscriptions
			if (subscriptions.containsKey(templateCategory)) {
				boolean allowed = Boolean.TRUE.equals(subscriptions.get(templateCategory));
				if (allowed) {
					LOGGER.info("Message allowed for contact: {} - subscribed to template category: {}", chatContactDoc.getContactId(), templateCategory);
					return true;
				} else {
					LOGGER.info("Message blocked for contact: {} - not subscribed to template category: {}", chatContactDoc.getContactId(), templateCategory);
					outboxMessage.logs().add("Message blocked - not subscribed to template category: " + templateCategory);
					outboxMessage.status(Status.BLCKD);
					return false;
				}
			}
						
			// If we reach here, block the message
			LOGGER.info("Message blocked for contact: {} - template category not found in subscriptions: {}", chatContactDoc.getContactId(), templateCategory);
			outboxMessage.logs().add("Message blocked - template category not found in subscriptions: " + templateCategory);
			outboxMessage.status(Status.BLCKD);
			return false;
						
			
		}catch(Exception e) {
			LOGGER.error("Error checking subscription for contact:{} - Error: {}",chatContactDoc.getContactId(), e.getMessage(), e);
			return true ; //allow in case of any error for now
		}
		
	}

}
