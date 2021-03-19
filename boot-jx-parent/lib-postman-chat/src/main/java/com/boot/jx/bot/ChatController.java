package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.agent.AgentService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.doc.ChatPromise.PromiseCondition;
import com.boot.jx.postman.doc.ChatPromise.Result;
import com.boot.jx.postman.doc.ChatPromise.State;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

public class ChatController {

	@Autowired
	protected ChatService chatService;

	@Autowired
	private ChatContext chatContext;

	@Autowired
	private AgentService agentService;

	public void reply(String message) {
		try {
			chatService.reply(new OutboxMessage().message(message));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void reply(OutboxMessage message) {
		try {
			message.session().setAgent(chatService.getClient().getDefaultSender());
			chatService.reply(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void send(OutboxMessage waMessage) {
		waMessage.session().setAgent(chatService.getClient().getDefaultSender());
		chatService.send(chatContext.getContact(), waMessage);
	}

	public ApiResponse<InboxMessage, Object> assignToAgent(String deptName) {
		try {
			return agentService.assignToAgent(deptName);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ApiResponse<InboxMessage, Object> assignToAgent() {
		return assignToAgent(null);
	}

	public void botScore(Integer botScore) {
		chatService.botScore(chatContext.getChatSession(), botScore);
	}

	public void next(String key) {
		chatService.getChatContext().meta().setNextHandler(key);
	}

	public boolean previous(String key) {
		return ArgUtil.areEqual(chatService.getChatContext().meta().getPrevHandler(), key);
	}

	public ChatPromise require(String key, ChatPromise.PromiseCondition... conditions) {
		if (ArgUtil.is(conditions)) {
			for (PromiseCondition condition : conditions) {
				if (condition.isCondition()) {
					ChatPromise promise = new ChatPromise();
					promise.setState(State.COMPLETED);
					promise.setResult(condition.getResult());
					return promise;
				}
			}
		}

		ChatPromise promise = chatContext.meta().promise().get(key);
		if (ArgUtil.is(promise) && State.RETURNED.equals(promise.getState())) {
			promise.setState(State.COMPLETED);
			return promise;
		} else {
			promise = new ChatPromise();
			promise.setSource(chatContext.getCurrentHandler());

			if (ArgUtil.is(chatContext.getInboxMessage())) {
				promise.setMessage(chatContext.getInboxMessage().getMessage());
				promise.setMessageId(chatContext.getInboxMessage().getMessageId());
			}

			promise.setResult(Result.NONE);
			promise.setState(State.CREATED);
			promise.setTarget(key);
			chatContext.meta().promise().put(key, promise);
			throw new ChatException(key).targetHandler(key);
		}
	}

	public ChatPromise resolve(String key) {
		ChatPromise promise = chatContext.meta().promise().get(key);
		if (ArgUtil.is(promise)) {
			promise.setResult(Result.RESOLVED);
			promise.setState(State.CAPTURED);
		}
		return promise;
	}

	public ChatPromise reject(String key) {
		ChatPromise x = chatContext.meta().promise().get(key);
		if (ArgUtil.is(x)) {
			x.setResult(Result.REJECTED);
			x.setState(State.CAPTURED);
		}
		return x;
	}

}
