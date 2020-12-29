package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.DefaultChatContextStore;
import com.boot.jx.postman.store.IChatContextStore;
import com.boot.jx.postman.store.IChatContextStore.ChatContextStore;
import com.boot.jx.scope.ThreadScoped;
import com.boot.utils.ArgUtil;

@Component
@ThreadScoped
public class ChatContext {

	String currentHandler;

	Object session;
	Object user;
	ChatMeta meta;
	InboxMessage inboxMessage;

	@Autowired(required = false)
	ChatContextStore<?, ?> store;

	@Autowired
	DefaultChatContextStore defaultChatContextStore;

	public IChatContextStore<?, ?> getStore() {
		if (store == null) {
			return defaultChatContextStore;
		}
		return store;
	}

	@Deprecated
	public void loadSession(Object session) {
		getStore().loadSession(session);
	}

	@Deprecated
	public void loadUser(Object user) {
		getStore().loadSession(user);
	}

	public InboxMessage getInboxMessage() {
		return inboxMessage;
	}

	public void setInboxMessage(InboxMessage inboxMessage) {
		this.inboxMessage = inboxMessage;
	}

	public ChatMeta meta() {
		if (this.meta == null) {
			this.meta = new ChatMeta();
		}
		return meta;
	}

	public ChatMeta getMeta() {
		return meta;
	}

	public void setMeta(ChatMeta meta) {
		this.meta = meta;
	}

	@Deprecated
	public String getNextHandler() {
		return this.meta().getNextHandler();
	}

	@Deprecated
	public void setNextHandler(String nextHandler) {
		this.meta().setNextHandler(nextHandler);
	}

	@Deprecated
	public String getPrevHandler() {
		return this.meta().getPrevHandler();
	}

	@Deprecated
	public void setPrevHandler(String prevHandler) {
		this.meta().setPrevHandler(prevHandler);
	}

	public String getCurrentHandler() {
		return currentHandler;
	}

	public void setCurrentHandler(String currentHandler) {
		this.currentHandler = currentHandler;
	}

	public String getMobile() {
		if (ArgUtil.is(this.inboxMessage)) {
			return this.inboxMessage.getFrom();
		}
		return null;
	}

}
