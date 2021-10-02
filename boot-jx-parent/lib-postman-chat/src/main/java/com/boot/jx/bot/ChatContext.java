package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.DefaultChatContextStore;
import com.boot.jx.postman.store.IChatContextStore;
import com.boot.jx.postman.store.IChatContextStore.BasicChatContextSession;
import com.boot.jx.postman.store.IChatContextStore.BasicChatContextUser;
import com.boot.jx.postman.store.IChatContextStore.ChatContextStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.scope.ThreadScoped;
import com.boot.utils.ArgUtil;

@Component
@ThreadScoped
public class ChatContext {

	String currentHandler;
	String agent;

	private ChatMeta meta;
	private InboxMessage inboxMessage;
	private ChatContactDoc chatContactDoc;
	private ChatSessionDoc chatSessionDoc;

	@Autowired(required = false)
	private ChatContextStore<?, ?> store;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private DefaultChatContextStore defaultChatContextStore;

	public IChatContextStore<?, ?> getStore() {
		if (store == null) {
			return defaultChatContextStore;
		}
		return store;
	}

	public InboxMessage getInboxMessage() {
		return inboxMessage;
	}

	public void setInboxMessage(InboxMessage inboxMessage) {
		this.inboxMessage = inboxMessage;
	}

	public ChatContactDoc getContact() {
		if (chatContactDoc == null) {
			chatContactDoc = sessionStore.getContact(inboxMessage);
		}
		return chatContactDoc;
	}

	public ChatSessionDoc getChatSession() {
		if (chatSessionDoc == null) {
			chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
		}
		return chatSessionDoc;
	}

	@SuppressWarnings("unchecked")
	public <T extends BasicChatContextUser> T getUser() {
		return (T) getStore().getUser();
	}

	@SuppressWarnings("unchecked")
	public <T extends BasicChatContextUser> T getUser(Class<T> type) {
		return (T) getStore().getUser();
	}

	@SuppressWarnings("unchecked")
	public <T extends BasicChatContextSession> T getSession() {
		return (T) getStore().getSession();
	}

	@SuppressWarnings("unchecked")
	public <T extends BasicChatContextSession> T getSession(Class<T> type) {
		return (T) getStore().getSession();
	}

	public ChatContactDoc commitContact() {
		if (chatContactDoc != null) {
			sessionStore.save(chatContactDoc);
		}
		return null;
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

	public String getAgent() {
	    return agent;
	}

	public void setAgent(String agent) {
	    this.agent = agent;
	}

}
