package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.client.GupShupNotifyClient;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.doc.ChatPromise.PromiseCondition;
import com.boot.jx.postman.doc.ChatPromise.Result;
import com.boot.jx.postman.doc.ChatPromise.State;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

public class ChatController {

	@Autowired
	ChatService botService;

	@Autowired
	GupShupChatClient gupShupChatClient;

	@Autowired
	GupShupNotifyClient gupShupNotifyClient;

	@Autowired
	ChatContext chatContext;

	public void reply(String message) {
		try {
			botService.reply(new OutboxMessage().message(message));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void reply(OutboxMessage message) {
		try {
			botService.reply(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void send(OutboxMessage waMessage) {
		try {
			botService.send(waMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void assignToAgent(String deptName) {
		try {
			botService.assignToAgent(deptName);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void assignToAgent() {
		assignToAgent(null);
	}

	public void next(String key) {
		botService.getChatContext().setNextHandler(key);
	}

	public boolean previous(String key) {
		return ArgUtil.areEqual(botService.getChatContext().getPrevHandler(), key);
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
			promise.setMessage(chatContext.getInboxMessage().getMessage());
			promise.setMessageId(chatContext.getInboxMessage().getMessageId());
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
