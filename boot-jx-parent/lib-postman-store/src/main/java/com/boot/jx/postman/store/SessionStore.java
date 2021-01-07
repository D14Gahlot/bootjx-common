package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Component
public class SessionStore {

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	public ChatContactDoc getContact(InboxMessage inboxMessage) {
		String contactId = PostManUtil.createContactId(inboxMessage);
		ChatContactDoc chatContactDoc = mongoTemplate.findById(contactId, ChatContactDoc.class);
		return chatContactDoc;
	}

	public ChatSessionDoc createSession(InboxMessage inboxMessage) {
		String contactId = PostManUtil.createContactId(inboxMessage);
		inboxMessage.setContactId(contactId);

		String sessionId = inboxMessage.getSessionId();

		ChatContactDoc chatContactDoc = null;
		ChatSessionDoc chatSessionDoc = null;

		if (ArgUtil.isEmpty(sessionId)) {
			chatContactDoc = mongoTemplate.findById(contactId, ChatContactDoc.class);
			if (ArgUtil.is(chatContactDoc)) {
				sessionId = chatContactDoc.getSessionId();
			}
		}

		if (ArgUtil.is(sessionId)) {
			chatSessionDoc = mongoTemplate.findById(sessionId, ChatSessionDoc.class);
		}

		if (ArgUtil.isEmpty(chatSessionDoc)
				|| TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), chatSessionTimeout)) {

			closeActiveSessionsMulty(contactId);

			// SESSION CREATION
			chatSessionDoc = new ChatSessionDoc();
			chatSessionDoc.setContactId(contactId);

			// SESSION UPDATE
			chatSessionDoc.setActive(true);
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			mongoTemplate.save(chatSessionDoc);

			// CONTACT CREATION
			if (ArgUtil.isEmpty(chatContactDoc)) {
				chatContactDoc = new ChatContactDoc();
				chatContactDoc.setContactId(contactId);
				chatContactDoc.setContactType(ArgUtil.parseAsString(inboxMessage.getContactType()));
			}
			// CONTACT UPDATE
			chatContactDoc.setSessionId(chatSessionDoc.getSessionId());
			mongoTemplate.save(chatContactDoc);

		} else {
			// SESSION UPDATE
			chatSessionDoc.setActive(true);
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			mongoTemplate.save(chatSessionDoc);
		}

		inboxMessage.setSessionId(chatSessionDoc.getSessionId());
		inboxMessage.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		inboxMessage.setAssignedToDept(chatSessionDoc.getAssignedToDept());
		return chatSessionDoc;
	}

	public boolean closeActiveSessionsMulty(String contactId) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("contactId").is(contactId).and("active").is(true));
		Update update = Update.update("active", false);
		mongoTemplate.updateMulti(query2, update, ChatSessionDoc.class);
		return true;
	}

	public boolean closeActiveSessionsBulk(String contactId) {
		DBCollection collection = mongoTemplate.getCollection(mongoTemplate.getCollectionName(ChatSessionDoc.class));
		BulkWriteOperation bulk = collection.initializeOrderedBulkOperation();

		List<DBObject> criteria = new ArrayList<DBObject>();
		criteria.add(new BasicDBObject("contactId", contactId));
		criteria.add(new BasicDBObject("active", true));
		bulk.find(new BasicDBObject("$and", criteria))
				.update(new BasicDBObject(new BasicDBObject("$set", new BasicDBObject("active", false))));
		BulkWriteResult writeResult = bulk.execute();
		return true;
	}

	public List<ChatSessionDoc> findChatSessionDocByAgent(String agentCode) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("assignedToAgent").is(agentCode).and("active").is(true));
		return mongoTemplate.find(query2, ChatSessionDoc.class);
	}

	public ChatSessionDoc initSession(ChatSessionDoc chatSessionDoc) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("sessionId").is(chatSessionDoc.getSessionId()));
		Update update = Update.update("initd", true);
		mongoTemplate.updateMulti(query2, update, ChatSessionDoc.class);
		return chatSessionDoc;
	}
}
