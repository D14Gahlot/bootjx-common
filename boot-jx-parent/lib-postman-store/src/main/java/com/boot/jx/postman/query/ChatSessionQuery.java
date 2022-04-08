package com.boot.jx.postman.query;

import java.util.List;

import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.store.MessageStore;

public class ChatSessionQuery extends DocQueryBuilder<ChatSessionDoc> {

	public ChatSessionQuery(ChatSessionDoc doc) {
		super(doc);
	}

	public ChatSessionQuery(String docId) {
		super(docId);
	}

	@Override
	public ChatSessionDoc newDoc(String id) {
		ChatSessionDoc doc = new ChatSessionDoc();
		doc.setSessionId(id);
		return doc;
	}

	@Override
	public String getId(ChatSessionDoc doc) {
		return doc.getSessionId();
	}

	public Object get(String key) {
		return this.doc.store().get(key);
	}

	public ChatSessionQuery put(String key, String object) {
		this.doc.store().put(key, object);
		this.set("store." + key, object);
		return this;
	}

	public ChatSessionQuery remove(String key) {
		this.doc.store().remove(key);
		this.unset("store." + key);
		return this;
	}

	public ChatSessionQuery setActive(boolean active) {
		this.doc.setActive(active);
		this.set("active", active);
		return this;
	}

	public ChatSessionQuery setFirstInComingStamp(long firstInComingStamp) {
		this.doc.setFirstInComingStamp(firstInComingStamp);
		this.set("firstInComingStamp", firstInComingStamp);
		return this;
	}

	public ChatSessionQuery setFirstOutGoingStamp(long firstOutGoingStamp) {
		this.doc.setFirstOutGoingStamp(firstOutGoingStamp);
		this.set("firstOutGoingStamp", firstOutGoingStamp);
		return this;
	}

	public ChatSessionQuery setLastResponseStamp(long timestamp) {
		this.doc.setLastResponseStamp(timestamp);
		this.set("lastResponseStamp", timestamp);
		return this;
	}

	public ChatSessionQuery setLastInComingStamp(long timestamp) {
		this.doc.setLastInComingStamp(timestamp);
		this.set("lastInComingStamp", timestamp);
		return this;
	}

	public ChatSessionQuery setLastOutGoingStamp(long timestamp) {
		this.doc.setLastOutGoingStamp(timestamp);
		this.set("lastOutGoingStamp", timestamp);
		return this;
	}

	public ChatSessionQuery setContactName(String contactName) {
		this.doc.setContactName(contactName);
		this.set("contactName", contactName);
		this.doc.contact().setName(contactName);
		this.set("contact.name", contactName);
		return this;
	}

	public ChatSessionQuery setLastInBoundMsg(MessageDoc lastInBoundMsg, String contactType) {
		this.doc.setLastInBoundMsg(lastInBoundMsg);
		this.set("lastInBoundMsgId", lastInBoundMsg.getMessageId());
		this.ref("lastInBoundMsg", lastInBoundMsg.getMessageId(), MessageStore.getCollectionName(contactType));
		return this;
	}

	public ChatSessionQuery setLastOutBoundMsg(MessageDoc lastOutBoundMsg, String contactType) {
		this.doc.setLastOutBoundMsg(lastOutBoundMsg);
		this.ref("lastOutBoundMsg", lastOutBoundMsg.getMessageId(), MessageStore.getCollectionName(contactType));
		return this;
	}

	public ChatSessionQuery setLastMsg(MessageDoc lastMsg, String contactType) {
		// this.doc.setLastMsg(lastMsg);
		this.ref("lastMsg", lastMsg.getMessageId(), MessageStore.getCollectionName(contactType));
		return this;
	}

	public ChatSessionQuery setTagId(List<String> tagIds) {
		this.doc.setTagId(tagIds);
		this.set("tagId", tagIds);
		return this;
	}

	public ChatSessionQuery setQueue(String queue) {
		this.doc.setAssignedToQueue(queue);
		this.set("assignedToQueue", queue);
		return this;
	}

}
