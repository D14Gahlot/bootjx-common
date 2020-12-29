package com.boot.jx.tunnel;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.cache.MCQIndicator;
import com.boot.jx.logger.client.AuditServiceClient;
import com.boot.jx.logger.events.RequestTrackEvent;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class TunnelSubscriberFactory {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelSubscriberFactory.class);
	public static long TIME_TO_EXPIRE = 60L;
	public static TimeUnit UNIT_OF_TIME = TimeUnit.MINUTES;

	public static final String STATUS_WORKING = "W";
	public static final String STATUS_DONE = "D";
	public static long TIME_TO_EXPIRE_MILLIS = TIME_TO_EXPIRE * 60 * 1000;
	public static final SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
	public static final int RECURSION_MAX_DEPTH = 20;

	private AppConfig appConfig;

	public static <A extends Annotation> A getAnnotationProxyReady(Class<?> clazz, Class<A> annotationClass) {
		final A annotation = clazz.getAnnotation(annotationClass);
		if (annotation == null && (clazz.isSynthetic())) { // OK, this is probably proxy
			return getAnnotationProxyReady(clazz.getSuperclass(), annotationClass);
		} else {
			return annotation;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TunnelSubscriberFactory(List<ITunnelSubscriber> listeners,
			@Autowired(required = false) RedissonClient redisson,
			@Autowired(required = true) AppConfig appConfigLocal, @Autowired AppParam loadAppParams) {
		appConfig = appConfigLocal;
		LOGGER.info("Subscribing {} tunnel events in {}", listeners.size(), appConfigLocal.getAppEnv());
		if (redisson == null) {
			LOGGER.warn("Redisson Not avaiable for {} Listeners", listeners.size());
		} else {
			for (ITunnelSubscriber listener : listeners) {
				Class<?> c = AopProxyUtils.ultimateTargetClass(listener);
				ITunnelSubscriber listenerTarget = (ITunnelSubscriber) AopProxyUtils.getSingletonTarget(listener);
				// System.out.println("====="+c.getName());
				TunnelEventMapping tunnelEvent = getAnnotationProxyReady(c, TunnelEventMapping.class);
				String eventTopic = tunnelEvent.topic();

				if (ArgUtil.isEmpty(eventTopic)) {
					eventTopic = tunnelEvent.byEvent().getName();
				}

				if (ArgUtil.is(listener.getTopic())) {
					eventTopic = listener.getTopic();
				} else if (ArgUtil.is(listenerTarget) && ArgUtil.is(listenerTarget.getTopic())) {
					eventTopic = listenerTarget.getTopic();
				}

				boolean integrity = tunnelEvent.integrity();
				TunnelEventXchange scheme = tunnelEvent.scheme();
				if (scheme == TunnelEventXchange.TASK_WORKER) {
					this.addTaskWorker(eventTopic, redisson, listener, integrity, c.getClass().getName());
				} else if (scheme == TunnelEventXchange.TASK_LISTNER) {
					this.addTaskListner(eventTopic, redisson, listener, TunnelEventXchange.TASK_LISTNER, integrity,
							c.getClass().getName());
				} else if (scheme == TunnelEventXchange.AUDIT) {
					this.addAuditListener(eventTopic, redisson, listener, integrity, c.getClass().getName());
				} else if (scheme == TunnelEventXchange.SEND_LISTNER) {
					this.addQueuedListener(eventTopic, redisson, listener, integrity, c.getClass().getName());
				} else {
					this.addShoutListener(eventTopic, redisson, listener, integrity, c.getClass().getName());
				}
			}
		}

	}

	public static class WrapperML<M> implements MessageListener<TunnelMessage<M>> {

		ITunnelSubscriber<M> subscriber = null;
		boolean integrity = false;
		TunnelEventXchange exchange;

		public WrapperML(ITunnelSubscriber<M> subscriber, boolean integrity) {
			super();
			this.subscriber = subscriber;
			this.integrity = integrity;
		}

		public WrapperML(ITunnelSubscriber<M> subscriber, TunnelEventXchange exchange) {
			super();
			this.subscriber = subscriber;
			this.exchange = exchange;
		}

		public WrapperML(ITunnelSubscriber<M> subscriber, TunnelEventXchange exchange, boolean integrity) {
			super();
			this.subscriber = subscriber;
			this.exchange = exchange;
			this.integrity = integrity;
		}

		@Override
		public void onMessage(CharSequence channel, TunnelMessage<M> msg) {
			this.onMessageWrapper(ArgUtil.parseAsString(channel), msg);
		}

		public void onMessageWrapper(String channel, TunnelMessage<M> msg) {
			this.subscriber.onMessage(channel, msg.getData());
		}

		public ITunnelSubscriber<M> getSubscriber() {
			return subscriber;
		}

		public void setSubscriber(ITunnelSubscriber<M> subscriber) {
			this.subscriber = subscriber;
		}

	}

	public <M> void addShoutListener(String topic, RedissonClient redisson, ITunnelSubscriber<M> listener,
			boolean integrity, String listentName) {
		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.SHOUT_LISTNER.getTopic(topic));
		LOGGER.info("Subscription on Topic : {}",
				MCQIndicator.messageSubscribed(TunnelEventXchange.SHOUT_LISTNER.getTopic(topic)));
		topicQueue.addListener(TunnelMessage.class, new WrapperML<M>(listener, integrity) {
			@Override
			public void onMessageWrapper(String channel, TunnelMessage<M> msg) {
				MCQIndicator.messageRcvd(channel);
				AppContextUtil.clear();
				AppContext context = msg.getContext();
				AppContextUtil.setContext(context);
				AppContextUtil.init();
				if (this.integrity) {
					RMapCache<String, String> map = redisson
							.getMapCache(TunnelEventXchange.SHOUT_LISTNER.getStatusMap(topic));
					String integrityKey = appConfig.getAppName() + "#" + listentName + "#" + msg.getId();
					String prevObject = map.put(integrityKey, msg.getId(), TIME_TO_EXPIRE, UNIT_OF_TIME);
					if (prevObject == null) { // Hey I got it first :) OR it doesn't matter
						this.doMessage(channel, msg);
						map.put(integrityKey, "DONE", TIME_TO_EXPIRE, UNIT_OF_TIME);
					} else { // I hope, other guy (The Lucky Bugger) is doing his job, right.
						LOGGER.debug("IGNORED EVENT : {} : {}", channel, msg.getId());
						MCQIndicator.messageIgnored(channel);
					}
				} else {
					this.doMessage(channel, msg);
				}
			}

			public void doMessage(String channel, TunnelMessage<M> msg) {
				AuditServiceClient.trackStatic(
						new RequestTrackEvent(RequestTrackEvent.Type.SUB_IN, TunnelEventXchange.SHOUT_LISTNER, msg));
				try {
					this.subscriber.onMessage(channel, msg.getData());
					MCQIndicator.messageProcessed(channel);
				} catch (Exception e) {
					LOGGER.error("EXCEPTION EVENT " + channel + " : " + msg.getId(), e);
					MCQIndicator.messageException(channel);
				}
			}
		});
	}

	public <M> void addQueuedListener(String topicName, RedissonClient redisson, ITunnelSubscriber<M> listener,
			boolean integrity, String listenrName) {
		RTopic eventTopic = redisson.getTopic(TunnelEventXchange.SEND_LISTNER.getTopic(topicName));
		LOGGER.info("Subscription on Topic : {}",
				MCQIndicator.messageSubscribed(TunnelEventXchange.SEND_LISTNER.getTopic(topicName)));
		eventTopic.addListener(TunnelMessage.class, new WrapperML<M>(listener, integrity) {
			@Override
			public void onMessageWrapper(String channel, TunnelMessage<M> msg) {
				MCQIndicator.messageRcvd(channel);
				tryMessage(channel, msg);
				RQueue<TunnelMessage<M>> eventAltQueue = redisson
						.getQueue(TunnelEventXchange.SEND_LISTNER.getQueue(topicName));

				TunnelMessage<M> msg2 = pollSafely(channel, eventAltQueue, null);

				if (msg2 != null && !TimeUtils.isDead(msg2.getTimestamp(), TIME_TO_EXPIRE_MILLIS)) {
					tryMessage(channel, msg2);
				}
			}

			public boolean tryMessage(String channel, TunnelMessage<M> msg) {
				RMapCache<String, String> map = redisson
						.getMapCache(TunnelEventXchange.SEND_LISTNER.getStatusMap(topicName));
				String integrityKey = appConfig.getAppEnv() + "#" + listenrName + "#" + msg.getId();
				String prevObject = map.put(integrityKey, STATUS_WORKING, TIME_TO_EXPIRE, UNIT_OF_TIME);
				LOGGER.debug("tryMessage {} {}", integrityKey, prevObject);
				if (prevObject == null) { // Hey I got it first
					this.doMessage(channel, msg);
					map.put(integrityKey, STATUS_DONE, TIME_TO_EXPIRE, UNIT_OF_TIME);
					return true;
				} else { // I hope, other guy (The Lucky Bugger) is doing his job, right.
					LOGGER.debug("IGNORED EVENT : {} : {}", channel, msg.getId());
					MCQIndicator.messageIgnored(channel);
					return false;
				}
			}

			public void doMessage(String channel, TunnelMessage<M> msg) {
				AppContextUtil.clear();
				AppContextUtil.setContext(msg.getContext());
				AppContextUtil.init();
				AuditServiceClient.trackStatic(
						new RequestTrackEvent(RequestTrackEvent.Type.SUB_IN, TunnelEventXchange.SEND_LISTNER, msg));
				try {
					this.subscriber.onMessage(channel, msg.getData());
					MCQIndicator.messageProcessed(channel);
				} catch (Exception e) {
					LOGGER.error("EXCEPTION EVENT " + channel + " : " + msg.getId(), e);
					MCQIndicator.messageException(channel);
				}
			}
		});
	}

	public <M> void addTaskListner(String topic, RedissonClient redisson, ITunnelSubscriber<M> listener,
			TunnelEventXchange exchange, boolean integrity, String listentName) {
		RTopic taskListnerTopic = redisson.getTopic(TunnelEventXchange.TASK_LISTNER.getTopic(topic));
		LOGGER.info("Subscription on Topic : {}",
				MCQIndicator.messageSubscribed(TunnelEventXchange.TASK_LISTNER.getTopic(topic)));
		taskListnerTopic.addListener(TunnelMessage.class, new WrapperML<M>(listener, exchange, integrity) {
			@Override
			public void onMessageWrapper(String channel, TunnelMessage<M> msg) {
				MCQIndicator.messageRcvd(channel);
				if (ArgUtil.isEmpty(msg) || ArgUtil.isEmpty(msg.getId())) {
					LOGGER.warn("NULL msgId Rcvd for EVENT " + channel + " : ");
				}

				if (TunnelEventXchange.TASK_WORKER.equals(this.exchange)) {
					RQueue<TunnelMessage<M>> taskWorkerQueue = redisson
							.getQueue(TunnelEventXchange.TASK_WORKER.getQueue(topic));
					onTaskWorkerEvent(channel, taskWorkerQueue, msg.getId(), 0);
				} else {
					if (this.integrity) {
						RMapCache<String, String> map = redisson
								.getMapCache(TunnelEventXchange.TASK_LISTNER.getStatusMap(topic));
						String integrityKey = appConfig.getAppName() + "#" + listentName + "#" + msg.getId();
						String prevObject = map.put(integrityKey, msg.getId(), TIME_TO_EXPIRE, UNIT_OF_TIME);
						if (prevObject == null) { // Hey I got it first :) OR it doesn't matter
							this.doTask(channel, msg);
							map.put(integrityKey, "DONE", TIME_TO_EXPIRE, UNIT_OF_TIME);
						} else { // I hope, other guy (The Lucky Bugger) is doing his job, right.
							LOGGER.debug("IGNORED EVENT : {} : {}", channel, msg.getId());
							MCQIndicator.messageIgnored(channel);
						}
					} else {
						this.doTask(channel, msg);
					}

				}
			}

			private void onTaskWorkerEvent(String channel, RQueue<TunnelMessage<M>> topicMessageQueue, String msgId,
					int counter) {

				TunnelMessage<M> msg = pollSafely(channel, topicMessageQueue, msgId);
				if (msg == null) {
					return;
				}
				doTask(channel, msg);

				if (counter < RECURSION_MAX_DEPTH) {
					onTaskWorkerEvent(channel, topicMessageQueue, msgId, ++counter);
				}

			}

			private void doTask(String channel, TunnelMessage<M> msg) {
				if (msg == null) {
					return;
				}
				if (!TimeUtils.isDead(msg.getTimestamp(), TIME_TO_EXPIRE_MILLIS)) {
					AppContextUtil.clear();
					AppContext context = msg.getContext();
					AppContextUtil.setContext(context);
					AppContextUtil.init();
					AuditServiceClient.trackStatic(
							new RequestTrackEvent(RequestTrackEvent.Type.SUB_IN, this.exchange, msg));
					try {
						if (ArgUtil.isEmpty(msg.getData())) {
							LOGGER.warn("NULL Event Rcvd for EVENT " + channel + " : ");
						} else {
							this.subscriber.onMessage(channel, msg.getData());
							MCQIndicator.messageProcessed(channel);
						}
					} catch (Exception e) {
						LOGGER.error("EXCEPTION in EVENT " + channel + " : " + msg.getId(), e);
						MCQIndicator.messageException(channel);
					}
				}
			}

		});
	}

	public <M> void addTaskWorker(String topic, RedissonClient redisson, ITunnelSubscriber<M> listener,
			boolean integrity, String listentName) {
		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.TASK_WORKER.getTopic(topic));
		LOGGER.info("Subscription on Topic : {}",
				MCQIndicator.messageSubscribed(TunnelEventXchange.TASK_WORKER.getTopic(topic)));
		topicQueue.addListener(String.class, new MessageListener<String>() {

			@Override
			public void onMessage(CharSequence channel, String msg) {
				this.onMessageWrapper(ArgUtil.parseAsString(channel), msg);
			}

			public void onMessageWrapper(String channel, String msgId) {
				MCQIndicator.messageRcvd(channel);
				if (ArgUtil.isEmpty(msgId)) {
					LOGGER.warn("NULL msgId Rcvd for EVENT " + channel + " : ");
				}
				RQueue<TunnelMessage<M>> topicMessageQueue = redisson
						.getQueue(TunnelEventXchange.TASK_WORKER.getQueue(topic));
				onMessage(channel, topicMessageQueue, msgId, 0);
			}

			private void onMessage(String channel, RQueue<TunnelMessage<M>> topicMessageQueue, String msgId,
					int counter) {

				TunnelMessage<M> msg = pollSafely(channel, topicMessageQueue, msgId);
				if (msg == null) {
					return;
				}
				if (!TimeUtils.isDead(msg.getTimestamp(), TIME_TO_EXPIRE_MILLIS)) {
					AppContextUtil.clear();
					AppContext context = msg.getContext();
					AppContextUtil.setContext(context);
					AppContextUtil.init();
					AuditServiceClient.trackStatic(
							new RequestTrackEvent(RequestTrackEvent.Type.SUB_IN, TunnelEventXchange.TASK_WORKER, msg));
					try {
						if (ArgUtil.isEmpty(msg.getData())) {
							LOGGER.warn("NULL Event Rcvd for EVENT " + channel + " : ");
						} else {
							listener.onMessage(channel, msg.getData());
							MCQIndicator.messageProcessed(channel);
						}
					} catch (Exception e) {
						LOGGER.error("EXCEPTION in EVENT " + channel + " : " + msg.getId(), e);
						MCQIndicator.messageException(channel);
					}
				}
				if (counter < RECURSION_MAX_DEPTH) {
					onMessage(channel, topicMessageQueue, msgId, ++counter);
				}

			}

		});
	}

	public <M> void addAuditListener(String topic, RedissonClient redisson, ITunnelSubscriber<M> listener,
			boolean integrity, String listentName) {
		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.AUDIT.getTopic(topic));
		LOGGER.info("Subscription on Topic : {}",
				MCQIndicator.messageSubscribed(TunnelEventXchange.AUDIT.getTopic(topic)));
		topicQueue.addListener(String.class, new MessageListener<String>() {

			@Override
			public void onMessage(CharSequence channel, String msg) {
				this.onMessageWrapper(ArgUtil.parseAsString(channel), msg);
			}

			public void onMessageWrapper(String channel, String msgId) {
				MCQIndicator.messageRcvd(channel);
				if (ArgUtil.isEmpty(msgId)) {
					LOGGER.warn("NULL msgId Rcvd for EVENT " + channel + " : ");
				}
				RQueue<TunnelMessage<M>> topicMessageQueue = redisson
						.getQueue(TunnelEventXchange.AUDIT.getQueue(topic));
				onMessage(channel, topicMessageQueue, msgId, 0);
			}

			private void onMessage(String channel, RQueue<TunnelMessage<M>> topicMessageQueue, String msgId,
					int counter) {

				TunnelMessage<M> msg = pollSafely(channel, topicMessageQueue, msgId);
				if (msg != null) {
					AppContextUtil.clear();
					AppContext context = msg.getContext();
					AppContextUtil.setContext(context);
					AppContextUtil.init();
					try {
						if (ArgUtil.isEmpty(msg.getData())) {
							LOGGER.warn("NULL Event Rcvd for EVENT " + channel + " : ");
						} else {
							listener.onMessage(channel, msg.getData());
							MCQIndicator.messageProcessed(channel);
						}
					} catch (Exception e) {
						LOGGER.error("EXCEPTION in EVENT " + channel + " : " + msg.getId(), e);
						MCQIndicator.messageException(channel);
					}
					if (counter < RECURSION_MAX_DEPTH) {
						onMessage(channel, topicMessageQueue, msgId, ++counter);
					}
				}
			}

		});
	}

	private <M> TunnelMessage<M> pollSafely(String channel, RQueue<TunnelMessage<M>> topicMessageQueue, String msgId) {
		TunnelMessage<M> msg = null;
		try {
			msg = topicMessageQueue.poll();
		} catch (Exception e) {
			LOGGER.error("EXCEPTION in EVENT_POLL " + channel + " msg id : " + msgId, e);
		}
		return msg;
	}

}
