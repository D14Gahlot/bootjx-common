package com.boot.jx.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.AgentService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.bot.BotEngine;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Component
public class InBoundService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundService.class);

	@Autowired(required = false)
	private InBoundHandler inBoundHandler;

	@Autowired(required = false)
	private InBoundFilter inBoundFilter;

	@Autowired
	private BotEngine botEngine;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private AgentService agentService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

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

		if (ArgUtil.isEmpty(inboxMessageOriginal.getSessionId())) {
			sessionStore.createSession(inboxMessageOriginal);
		}

		if (ArgUtil.isEmpty(inboxMessageOriginal.getMessageId())) {
			messageStore.create(inboxMessageOriginal);
		}

		if (ArgUtil.isEmpty(inBoundFilter) || inBoundFilter.onFilter(inboxMessageOriginal)) {
			if (ArgUtil.is(inBoundHandler)) {
				inBoundHandler.onHandle(inboxMessageOriginal);
			} else if (botEngine.isChatBotDefined() || chatClient.isChatDummyBotEnabled()) {
				botEngine.invokeMethodsAsync(inboxMessageOriginal);
			} else {
				chatClient.forward(inboxMessageOriginal);
			}
		}
		return inboxMessageOriginal;
	}

	public ApiResponse<InboxMessage, ?> assignToAgent(InboxMessage inboxMessageOriginal) {
		return agentService.assignToAgent(inboxMessageOriginal);
	}
}
