package com.boot.jx.inbound;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.redisson.api.RBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.bot.BotEngine;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Component
public class InBoundService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundService.class);

	@Autowired(required = false)
	private InBoundHandler inBoundHandler;

	@Autowired
	private BotEngine botEngine;

	/**
	 * Invoke the methods with matching {@link ChatMapping#events()} and
	 * {@link ChatMapping#pattern()} in events received from Slack/Facebook.
	 *
	 * @param event received from facebook
	 */
	@Async
	public void invokeMethodsAsync(InboxMessage inboxMessageOriginal) {
		invokeMethods(inboxMessageOriginal);
	}

	public InboxMessage invokeMethods(InboxMessage inboxMessageOriginal) {
		if (ArgUtil.is(inBoundHandler)) {
			inBoundHandler.onMessage(inboxMessageOriginal);
		} else if (botEngine.isChatBotDefined()) {
			botEngine.invokeMethodsAsync(inboxMessageOriginal);
		}
		return inboxMessageOriginal;
	}


}
