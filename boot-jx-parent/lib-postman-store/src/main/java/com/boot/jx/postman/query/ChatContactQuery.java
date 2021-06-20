package com.boot.jx.postman.query;

import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.jx.postman.doc.ChatContactDoc;

public class ChatContactQuery extends DocQueryBuilder<ChatContactDoc> {

	public ChatContactQuery(ChatContactDoc doc) {
		super(doc);
		whereId(doc.getContactId());
	}

	public static ChatContactDoc newChatContactDoc(String contactId) {
		ChatContactDoc doc = new ChatContactDoc();
		doc.setContactId(contactId);
		return doc;
	}

	public ChatContactQuery(String contactId) {
		super(newChatContactDoc(contactId));
		whereId(contactId);
	}

	public ChatContactQuery setLastInBoundStamp(long timestamp) {
		this.doc.setLastInBoundStamp(timestamp);
		this.set("lastInBoundStamp", timestamp);
		return this;
	}

	public ChatContactQuery setLastOutBoundStamp(long timestamp) {
		this.doc.setLastOutBoundStamp(timestamp);
		this.set("lastOutBoundStamp", timestamp);
		return this;
	}

	public ChatContactQuery setContactId(String contactId) {
		this.doc.setContactId(contactId);
		this.set("contactId", contactId);
		return this;
	}

	public ChatContactQuery setContactType(String contactType) {
		this.doc.setContactType(contactType);
		this.set("contactType", contactType);
		return this;
	}

	public ChatContactQuery setChannelType(String channelType) {
		this.doc.setChannelType(channelType);
		this.set("channelType", channelType);
		return this;
	}

	public ChatContactQuery setCsid(String csid) {
		this.doc.setCsid(csid);
		this.set("csid", csid);
		return this;
	}

	public ChatContactQuery setLane(String lane) {
		this.doc.setLane(lane);
		this.set("lane", lane);
		return this;
	}

	public ChatContactQuery setSessionId(String sessionId) {
		this.doc.setSessionId(sessionId);
		this.set("sessionId", sessionId);
		return this;
	}

	public ChatContactQuery setLastOptInStamp(long lastOptInStamp) {
		this.doc.setLastOptInStamp(lastOptInStamp);
		this.set("lastOptInStamp", lastOptInStamp);
		return this;
	}

}
