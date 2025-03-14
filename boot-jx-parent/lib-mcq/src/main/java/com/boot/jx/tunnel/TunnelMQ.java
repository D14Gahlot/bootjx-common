package com.boot.jx.tunnel;

import java.util.Collections;

import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.tunnel.ITunnelDefs.ITunnelEvent;
import com.boot.jx.tunnel.TunnelSubscriberFactory.TunnelSubscriberHolder;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Service
public class TunnelMQ {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelMQ.class);

	@Autowired(required = false)
	RedissonClient redissonClient;

	@Autowired(required = false)
	TunnelSubscriberFactory tunnelSubscriberFactory;

	@Autowired
	private AppConfig appConfig;

	public static class TunnelMQEvent implements ITunnelEvent {
		private static final long serialVersionUID = -6340411712891735145L;
		public String name;
		public String to = "*";

		public Object data;

		public TunnelMQEvent data(Object data) {
			this.data = data;
			return this;
		}

		public TunnelMQEvent name(String name) {
			this.name = name;
			return this;
		}

		public TunnelMQEvent to(String to) {
			this.to = to;
			return this;
		}

		public static TunnelMQEvent create(String name) {
			return new TunnelMQEvent().name(name);
		}
	}

	/**
	 * 
	 * Will trigger run method of job
	 * 
	 * @param job
	 * @param data
	 */

	public void start(TunnelMQEvent event) {

	}

	/**
	 * 
	 * Will trigger execute method of job
	 * 
	 * @param job
	 * @param data
	 */
	public void task(TunnelMQEvent event) {

	}

	/**
	 * Will trigger poll method of job
	 * 
	 * @param topic
	 * @param data
	 */
	public void push(TunnelMQEvent event) {
		String queueName = String.format("eq:app:%s:topic:%s", event.to, event.name);
		AppContext context = AppContextUtil.getContext();
		TunnelMessage<Object> message = new TunnelMessage<Object>(event.data, context);
		message.setTopic(queueName);
		this.pushToQueue(message);
	}

	@Scheduled(fixedDelay = 1000)
	public void queuePoller() {
		if (redissonClient != null) {
			for (String topic : tunnelSubscriberFactory.getSubscriberList()) {
				TunnelSubscriberHolder<?> holder = tunnelSubscriberFactory.getSubscriberHolders().get(topic);
				if (ArgUtil.is(holder) && ArgUtil.is(holder.pollParamType)) {
					executeOnQueue("*", topic, holder);
					executeOnQueue(appConfig.getAppType(), topic, holder);
				}
			}
		}
	}

	private <M> void executeOnQueue(String app, String topic, TunnelSubscriberHolder<M> holder) {
		// System.out.println(String.format("eq:app:%s:topic:%s", app, topic));
		String queueName = String.format("eq:app:%s:topic:%s", app, topic);
		while (true) {
			try {
				String message = redissonClient.getScript(StringCodec.INSTANCE).eval(RScript.Mode.READ_WRITE,
						"return redis.call('RPOP', KEYS[1])", RScript.ReturnType.VALUE,
						Collections.singletonList(queueName));
				if (message != null) {
					holder.pushed(message);
				} else {
					break;
				}
			} catch (Exception e) {
				LOGGER.error("PollException:" + queueName, e);
				break;
			}
		}
	}

	private void pushToQueue(TunnelMessage<Object> message) {
		redissonClient.getScript(StringCodec.INSTANCE).eval(RScript.Mode.READ_WRITE,
				"redis.call('LPUSH', KEYS[1], ARGV[1])", RScript.ReturnType.VALUE,
				Collections.singletonList(message.getTopic()), JsonUtil.toJson(message));
	}
}
