package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.BasicChatDataStore;
import com.boot.jx.postman.store.BasicChatDataStore.BasicChatSessionData;
import com.boot.jx.postman.store.BasicChatDataStore.BasicChatUserData;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.scope.ThreadScoped;

@Component
@ThreadScoped
public class ChatContext {

    private String currentHandler;

    private ChatMeta meta;

    private InboxMessage inboxMessage;
    private ChatContactDoc chatContactDoc;
    private ChatSessionDoc chatSessionDoc;

    @Autowired
    private SessionStore sessionStore;

    private BasicChatDataStore chatDataStore;

    public BasicChatDataStore getDataStore() {
	if (chatDataStore == null) {
	    chatDataStore = new BasicChatDataStore();
	}
	return chatDataStore;
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

    public BasicChatUserData getUserData() {
	return getDataStore().getUserData();
    }

    public BasicChatSessionData sessionData() {
	return getDataStore().getSessionData();
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

}
