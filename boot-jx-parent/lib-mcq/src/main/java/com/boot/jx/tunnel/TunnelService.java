package com.boot.jx.tunnel;

import java.io.UnsupportedEncodingException;

import org.nustaq.serialization.FSTConfiguration;
import org.redisson.api.RQueue;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.client.AuditServiceClient;
import com.boot.jx.logger.events.RequestTrackEvent;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class TunnelService implements ITunnelService {

	private static Logger LOGGER = LoggerFactory.getLogger(TunnelService.class);

	@Autowired(required = false)
	RedissonClient redisson;

	/**
	 * For broadcast purpose, it will send event to all the listeners which are
	 * listening, actively, messages are NOT QUEUED, so there's no guarantee of
	 * messages being deliver if client goes down at the time of shout was
	 * triggered.
	 *
	 * Though all the listeners can be informed, qualification is done based on
	 * {@link TunnelEventXchange}
	 *
	 * @reliable false
	 * @uniqueness once per listener-instance in network per event
	 */
	@Override
	public <T> long shout(String topic, T messagePayload) {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return 0L;
		}
		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.SHOUT_LISTNER.getTopic(topic));
		long startTime = System.currentTimeMillis();

		AppContextUtil.setTraceTime(startTime);
		AppContext context = AppContextUtil.getContext();

		TunnelMessage<T> message = new TunnelMessage<T>(messagePayload, context);
		message.setTopic(topic);

		AuditServiceClient.trackStatic(
				new RequestTrackEvent(RequestTrackEvent.Type.PUB_OUT, TunnelEventXchange.SHOUT_LISTNER, message));
		return topicQueue.publish(message);
	}

	@Override
	public <T> long shout(ITunnelEventsDict topic, T messagePayload) {
		return this.shout(topic.name(), messagePayload);
	}

	/**
	 * @see #shout(String, Object)
	 * @param event
	 * @return
	 *
	 * @reliable false
	 * @uniqueness once per listener-instance in network per event
	 */
	@Override
	public <E extends ITunnelEvent> long shout(E event) {
		return this.shout(event.getClass().getName(), event);
	}

	/**
	 * For broadcast purpose, it will send event to all the listeners which are
	 * listening, actively, messages are QUEUED, so there's guarantee of messages
	 * being deliver if client goes down at the time of SEND was triggered.
	 *
	 * Though all the listeners can be informed, qualification is done based on
	 * {@link TunnelEventXchange}
	 *
	 * @reliable true
	 * @uniqueness once per listener-class in network per event
	 *
	 */
	@Override
	public <T> long send(String topic, T messagePayload) {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return 0L;
		}
		AppContext context = AppContextUtil.getContext();
		TunnelMessage<T> message = new TunnelMessage<T>(messagePayload, context);
		message.setTopic(topic);

		RQueue<TunnelMessage<T>> queue = redisson.getQueue(TunnelEventXchange.SEND_LISTNER.getQueue(topic));
		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.SEND_LISTNER.getTopic(topic));

		AuditServiceClient.trackStatic(
				new RequestTrackEvent(RequestTrackEvent.Type.PUB_OUT, TunnelEventXchange.SEND_LISTNER, message));
		queue.add(message);
		return topicQueue.publish(message);
	}

	@Override
	@Async
	public <T> void taskEnd(String topic, T messagePayload, Thread parentThread) throws InterruptedException {
		parentThread.join(10 * 1000); // wait for parent thread to die for max 10 sec
		task(topic, messagePayload);
	}

	/**
	 * To assign a job to one of the worker, subscriber to this can be of two types
	 * : TASK_WORKER & TASK_LISTNER
	 *
	 *
	 * Multiple TASK_LISTNER can listen to event and act upon, but only one of the
	 * workers will receive the event
	 *
	 *
	 * @param topic          - name of task
	 * @param messagePayload - data to be used for task
	 * @return - unique message id
	 *
	 * @reliable true for workers
	 * @uniqueness only one WORKER and multiple LISTNERS will execute per event
	 */
	@Override
	public <T> long task(String topic, T messagePayload) {
		if (redisson == null) {
			return 0L;
		}
		AppContext context = AppContextUtil.getContext();
		TunnelMessage<T> message = new TunnelMessage<T>(messagePayload, context);
		message.setTopic(topic);

		RQueue<TunnelMessage<T>> queue = redisson.getQueue(TunnelEventXchange.TASK_WORKER.getQueue(topic));
		RTopic taskWorkerTopic = redisson.getTopic(TunnelEventXchange.TASK_WORKER.getTopic(topic));
		RTopic taskListnerPublisher = redisson.getTopic(TunnelEventXchange.TASK_LISTNER.getTopic(topic));

		AuditServiceClient.trackStatic(
				new RequestTrackEvent(RequestTrackEvent.Type.PUB_OUT, TunnelEventXchange.TASK_WORKER, message));
		debugEvent(message);

		queue.add(message);
		taskListnerPublisher.publish(message);
		return taskWorkerTopic.publish(message.getId());
	}

	public static <T> void debugEvent(TunnelMessage<T> message) {
		if (LOGGER.isDebugEnabled()) {
			String messageJson = JsonUtil.toJson(message);
			LOGGER.info("====== {}", messageJson);
			TunnelMessage<T> message2 = JsonUtil.fromJson(messageJson, new TypeReference<TunnelMessage<T>>() {
			});
			LOGGER.info("====== {}", JsonUtil.toJson(message2));

			try {
				FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
				byte[] bytes = conf.asByteArray(message);
				String messageJson2 = new String(bytes, "UTF-8");
				LOGGER.info("F====== {}", JsonUtil.toJson(messageJson2));
				TunnelMessage<T> message3 = (TunnelMessage<T>) conf.asObject(bytes);
				LOGGER.info("F====== {}", JsonUtil.toJson(message3));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * To assign a job to one of the worker, based on TaskEvent class, no need to
	 * pass task name, each TaskEvent class is treated as unique task
	 *
	 * @param event - task event it has be unique
	 * @return
	 */
	@Override
	public <E extends ITunnelEvent> long task(E event) {
		return this.task(event.getClass().getName(), event);
	}

	/**
	 * For audit purpose only, this will make sure, your log gets delivered to only
	 * one client qualified with {@link TunnelEventMapping#scheme()} as
	 * {@link TunnelEventXchange#AUDIT}
	 */
	@Override
	public <T> long audit(String topic, T messagePayload) {
		if (redisson == null) {
			return 0L;
		}
		AppContext context = AppContextUtil.getContext();
		TunnelMessage<T> message = new TunnelMessage<T>(messagePayload, context);
		message.setTopic(topic);

		RQueue<TunnelMessage<T>> queue = redisson.getQueue(TunnelEventXchange.AUDIT.getQueue(topic));
		queue.add(message);

		RTopic topicQueue = redisson.getTopic(TunnelEventXchange.AUDIT.getTopic(topic));
		return topicQueue.publish(message.getId());
	}

	@Override
	public <T> TunnelQueue<T> getQueue(String queueName) {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return null;
		}
		return new TunnelQueueImpl<T>(redisson.getQueue(queueName));
	}

}
