package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatMeta;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.store.BasicChatDataStore;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.scope.ThreadScoped;

@Component
@ThreadScoped
public class ChatContext {

    private String currentHandler;

    private ChatMeta meta;

    private InboxMessage inboxMessage;

    @Autowired
    private MessageContext messageContext;

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
	messageContext.setMessage(inboxMessage);
    }

    public ChatContactQuery contact() {
	return messageContext.contact();
    }

    public ChatContactDoc commit() {
	return messageContext.commit();
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
