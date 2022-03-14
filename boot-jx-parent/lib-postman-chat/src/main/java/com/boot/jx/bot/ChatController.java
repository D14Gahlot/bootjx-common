package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.boot.jx.agent.AgentService;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatPromise;
import com.boot.jx.postman.doc.ChatPromise.PromiseCondition;
import com.boot.jx.postman.doc.ChatPromise.Result;
import com.boot.jx.postman.doc.ChatPromise.State;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMParams;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;

public class ChatController {

    @Autowired
    protected ChatService chatService;

    @Autowired
    private ChatContext chatContext;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @Autowired
    private SessionStore sessionStore;

    @Lazy
    @Autowired
    private ChatSessionService chatSessionService;

    public void reply(String message) {
	try {
	    chatService.reply(chatContext.getInboxMessage(), new OutboxMessage().message(message));
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public void reply(OutboxMessage message) {
	try {
	    message.session().setAgent(chatService.getClientConfig().getDefaultSender());
	    chatService.reply(chatContext.getInboxMessage(), message);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public void send(OutboxMessage waMessage) {
	waMessage.session().setAgent(chatService.getClientConfig().getDefaultSender());
	if (ArgUtil.is(waMessage.getContact())) {
	    ChatContactDoc chatContactDoc = sessionStore.getContact(waMessage);
	    chatService.send(chatContactDoc, waMessage);
	} else {
	    chatService.send(chatContext.contact().getDoc(), waMessage);
	}
    }

    public NodeEntry<InBoundEvent> assignToAgent(String deptCode) {
	InboxMessage inboxMessage = chatContext.getInboxMessage();
	PMParams params = new PMParams();
	if (ArgUtil.is(inboxMessage)) {
	    params.assignToDeptCode(deptCode).contact(inboxMessage.contact()).sessionId(inboxMessage.getSessionId());
	    inboxMessage.session().setDept(deptCode);
	}
	return chatSessionService.assignSessionToAgent(params);
    }

    public NodeEntry<InBoundEvent> assignToAgent() {
	return assignToAgent(null);
    }

    public void botScore(Integer botScore) {
	chatService.botScore(chatContext.session().getDoc(), botScore);
    }

    public void agentScore(Integer agentScore) {
	chatService.agentScore(chatContext.session().getDoc(), agentScore);
    }

    public void resolveSession() {
	chatSessionManager.resolveSession(chatContext.session().getDoc());
    }

    public void closeSession() {
	chatSessionManager.closeSession(chatContext.session().getDoc());
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

    public void routeSession(String queueCode) {
	chatSessionService.routeSession(chatContext.session().getDoc(), queueCode, null);
    }

}
