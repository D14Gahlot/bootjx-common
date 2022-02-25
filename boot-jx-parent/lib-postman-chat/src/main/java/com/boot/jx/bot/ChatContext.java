package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.BasicChatDataStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.scope.ThreadScoped;

@Component
@ThreadScoped
public class ChatContext {

    private String currentHandler;

    private ChatMeta meta;

    private InboxMessage inboxMessage;
    private ChatContactQuery chatContactQuery;
    private ChatSessionQuery chatSessionQuery;

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

    public ChatContactQuery contact() {
	if (chatContactQuery == null) {
	    ChatContactDoc chatContactDoc = sessionStore.getContact(inboxMessage);
	    chatContactQuery = new ChatContactQuery(chatContactDoc);
	}
	return chatContactQuery;
    }

    public ChatSessionQuery session() {
	if (chatSessionQuery == null) {
	    ChatSessionDoc chatSessionDoc;
	    chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
	    chatSessionQuery = new ChatSessionQuery(chatSessionDoc);
	}
	return chatSessionQuery;
    }

    public ChatContactDoc commitContact() {
	if (chatContactQuery != null) {
	    sessionStore.update(chatContactQuery);
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
