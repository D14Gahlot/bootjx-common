package com.boot.jx.postman.query;

import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.jx.postman.doc.ChatSessionDoc;

public class ChatSessionQuery extends DocQueryBuilder<ChatSessionDoc> {

	public ChatSessionQuery(ChatSessionDoc doc) {
		super(doc);
		whereId(doc.getSessionId());
	}

	public ChatSessionQuery setActive(boolean active) {
		this.doc.setActive(active);
		this.set("active", active);
		return this;
	}

	public ChatSessionQuery setLastInComingStamp(long timestamp) {
		this.doc.setLastInComingStamp(timestamp);
		this.set("lastInComingStamp", timestamp);
		return this;
	}

	public ChatSessionQuery setLastResponseStamp(long timestamp) {
		this.doc.setLastResponseStamp(timestamp);
		this.set("lastResponseStamp", timestamp);
		return this;
	}

}
