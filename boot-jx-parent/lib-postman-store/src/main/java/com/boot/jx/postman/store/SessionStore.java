package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatUserProfileDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageSession;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.TimeUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Component
public class SessionStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionStore.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	public ChatContactDoc getContact(InboxMessage inboxMessage) {
		String contactId = PostManUtil.createContactId(inboxMessage);
		ChatContactDoc chatContactDoc = mongoTemplate.findById(contactId, ChatContactDoc.class);
		return chatContactDoc;
	}

	public ChatContactDoc getContact(String contactId) {
		return mongoTemplate.findById(contactId, ChatContactDoc.class);
	}

	public ChatContactDoc save(ChatContactDoc chatContactDoc) {
		mongoTemplate.save(chatContactDoc);
		return chatContactDoc;
	}

	public ChatSessionDoc getSession(String sessionId) {
		return mongoTemplate.findById(sessionId, ChatSessionDoc.class);
	}

	public ChatSessionDoc getValidSession(String sessionId) {
		ChatSessionDoc chatSessionDoc = mongoTemplate.findById(sessionId, ChatSessionDoc.class);
		if ((ArgUtil.isEmpty(chatSessionDoc)
				|| TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), chatSessionTimeout)
				|| !chatSessionDoc.isActive())) {
			return null;
		}
		return chatSessionDoc;
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
			chatSessionDoc = getValidSession(sessionId);
		}

		if ((ArgUtil.isEmpty(chatSessionDoc)
				|| TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), chatSessionTimeout)
				|| !chatSessionDoc.isActive())) {

			closeActiveSessionsMulty(contactId);

			// SESSION CREATION
			chatSessionDoc = new ChatSessionDoc();
			chatSessionDoc.setContactId(contactId);
			chatSessionDoc.setContactType(ArgUtil.parseAsString(inboxMessage.getContactType()));
			chatSessionDoc.setChannel(inboxMessage.getChannel());
			chatSessionDoc.setLane(inboxMessage.getLane());

			// SESSION UPDATE
			chatSessionDoc.setActive(true);
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			save(chatSessionDoc);

			// CONTACT CREATION
			if (ArgUtil.isEmpty(chatContactDoc) || ArgUtil.isEmpty(chatContactDoc.getCsid())
					|| ArgUtil.isEmpty(chatContactDoc.getLane())) {
				chatContactDoc = new ChatContactDoc();
				chatContactDoc.setContactId(contactId);
				chatContactDoc.setContactType(ArgUtil.parseAsString(inboxMessage.getContactType()));
				chatContactDoc.setChannelType(inboxMessage.getChannel());
				chatContactDoc.setCsid(inboxMessage.getFrom());
				chatContactDoc.setLane(inboxMessage.getLane());
			}
			// CONTACT UPDATE
			chatContactDoc.setSessionId(chatSessionDoc.getSessionId());
			save(chatContactDoc);

		} else {
			// SESSION UPDATE
			chatSessionDoc.setActive(true);
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			save(chatSessionDoc);
		}

		inboxMessage.setSessionId(chatSessionDoc.getSessionId());
		inboxMessage.session().setAgent(chatSessionDoc.getAssignedToAgent());
		inboxMessage.session().setDept(chatSessionDoc.getAssignedToDept());
		inboxMessage.session().setMode(chatSessionDoc.getMode());
		inboxMessage.session().setResolved(chatSessionDoc.isResolved());

		return chatSessionDoc;
	}

	public InboxMessage toInboxMessage(ChatSessionDoc session) {
		ChatContactDoc contact = getContact(session.getContactId());
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ArgUtil.parseAsEnumT(contact.getContactType(), ContactType.class));
		inboxMessage.setChannel(contact.getChannelType());
		inboxMessage.setLane(ArgUtil.nonEmpty(session.getLane(), contact.getLane()));
		inboxMessage.setFrom(contact.getCsid());
		inboxMessage.setFromName(contact.getName());
		inboxMessage.setSessionId(contact.getSessionId());
		inboxMessage.setContactId(contact.getContactId());
		return inboxMessage;
	}

	public boolean closeActiveSessionsMulty(String contactId) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("contactId").is(contactId).and("active").is(true));
		Update update = new Update().set("active", false).set("closeSessionStamp", System.currentTimeMillis());
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

	public List<ChatSessionDoc> findChatSessionDocByQuery(Query query) {
		return mongoTemplate.find(query, ChatSessionDoc.class);
	}

	public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(String agentCode, String agentDept) {
		Query query2 = new Query();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);

		query2.addCriteria(Criteria.where("assignedToDept").in(PMStoreConstants.NO_DEPT, agentDept).and("active")
				.is(true).and("lastInComingStamp").gt(cal.getTimeInMillis()).andOperator(
						// Is not assigned to any agent or assigned to said agent
						new Criteria().orOperator(Criteria.where("assignedToAgent").exists(false),
								Criteria.where("assignedToAgent").is(agentCode)),
						// Is not resolved yet
						new Criteria().orOperator(Criteria.where("resolved").exists(false),
								Criteria.where("resolved").is(false))

				));
		// LOGGER.info(query2.toString());
		return mongoTemplate.find(query2, ChatSessionDoc.class);
	}

	public List<ChatSessionDoc> findChatSessionContactId(String contactId) {
		ChatContactDoc contact = getContact(contactId);

		List<ChatContactDoc> contacts = null;
		if (ArgUtil.is(contact) && !ArgUtil.areEmpty(contact.getPhone(), contact.getEmail())) {
			Query query1 = new Query();
			List<Criteria> orExpression = new ArrayList<Criteria>();
			if (ArgUtil.is(contact.getPhone())) {
				orExpression.add(Criteria.where("phone").is(contact.getPhone()));
			}
			if (ArgUtil.is(contact.getEmail())) {
				orExpression.add(Criteria.where("email").is(contact.getEmail()));
			}
			if (ArgUtil.is(contact.getProfileId())) {
				orExpression.add(Criteria.where("profileId").is(contact.getProfileId()));
			}
			query1.addCriteria(new Criteria().orOperator(orExpression.toArray(new Criteria[orExpression.size()])));
			contacts = mongoTemplate.find(query1, ChatContactDoc.class);
		}

		Query query2 = new Query();
		List<Criteria> orExpression = new ArrayList<Criteria>();

		orExpression.add(Criteria.where("contactId").is(contactId));
		if (ArgUtil.is(contacts)) {
			for (ChatContactDoc chatContactDoc : contacts) {
				orExpression.add(Criteria.where("contactId").is(chatContactDoc.getContactId()));
			}
		}
		query2.addCriteria(new Criteria().orOperator(orExpression.toArray(new Criteria[orExpression.size()])));
		// LOGGER.info(query2.toString());
		return mongoTemplate.find(query2, ChatSessionDoc.class);
	}

	public void save(ChatSessionDoc chatSessionDoc) {
		try {
			if (ArgUtil.isEmpty(chatSessionDoc.getStartSessionStamp()) || chatSessionDoc.getStartSessionStamp() == 0L) {
				chatSessionDoc.setStartSessionStamp(System.currentTimeMillis());
			}
			mongoTemplate.save(chatSessionDoc);
		} catch (Exception e) {
			ChatSessionDoc chatSessionDoc2 = mongoTemplate.findById(chatSessionDoc.getSessionId(),
					ChatSessionDoc.class);
			LOGGER.error(chatSessionDoc.getVersion() + " ~ " + chatSessionDoc2.getVersion(), e);
			if (chatSessionDoc.getVersion() == null) {
				// chatSessionDoc.setVersion(0);
				mongoTemplate.save(chatSessionDoc);
			} else {
				// chatSessionDoc.setVersion(chatSessionDoc2.getVersion()+1);
				mongoTemplate.save(chatSessionDoc);
			}
		}
	}

	public ChatSessionDoc initSession(ChatSessionDoc chatSessionDoc) {
		chatSessionDoc.setInitd(true);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("initd", chatSessionDoc.isInitd());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc resolveSession(ChatSessionDoc chatSessionDoc) {
		// Old Way of Doing it
		chatSessionDoc.setResolveSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setResolved(false);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("resolveSessionStamp", chatSessionDoc.getResolveSessionStamp());
		builder.set("resolved", chatSessionDoc.isResolved());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc closeSession(ChatSessionDoc chatSessionDoc) {
		chatSessionDoc.setCloseSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setActive(false);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("closeSessionStamp", chatSessionDoc.getCloseSessionStamp());
		builder.set("active", chatSessionDoc.isActive());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc botScore(ChatSessionDoc chatSessionDoc, Integer botScore) {
		chatSessionDoc.setBotScore(botScore);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("botScore", chatSessionDoc.getBotScore());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc agentScore(ChatSessionDoc chatSessionDoc, Integer agentScore) {
		chatSessionDoc.setAgentScore(agentScore);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("agentScore", chatSessionDoc.getAgentScore());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatUserProfileDoc save(ChatUserProfileDoc doc) {
		mongoTemplate.save(doc);
		return doc;
	}

	public ChatUserProfileDoc save(ChatUserProfileDTO profile) {
		ChatUserProfileDoc doc = EntityDtoUtil.dtoToEntity(profile, new ChatUserProfileDoc());
		doc.setId(profile.getProfileId());
		return save(doc);
	}

	public void updateResponseTime(ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.isNone(chatSessionDoc.getFistResponseStamp())) {
			chatSessionDoc.setFistResponseStamp(System.currentTimeMillis());
		}
		chatSessionDoc.setLastResponseStamp(System.currentTimeMillis());

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("fistResponseStamp", chatSessionDoc.getFistResponseStamp());
		builder.set("lastResponseStamp", chatSessionDoc.getLastResponseStamp());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	}

	public void assignToAgent(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {

		if (!ArgUtil.areEqual(chatSessionDoc.getAssignedToDept(), agentDept)) {
			chatSessionDoc.setAssignedDeptStamp(System.currentTimeMillis());
		}
		chatSessionDoc.setMode("AGENT");
		chatSessionDoc.setAssignedToDept(agentDept);
		chatSessionDoc.setAssignedAgentStamp(System.currentTimeMillis());
		chatSessionDoc.setAssignedToAgent(agentCode);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToDept", chatSessionDoc.getAssignedToDept());
		builder.set("assignedDeptStamp", chatSessionDoc.getAssignedDeptStamp());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		builder.set("assignedAgentStamp", chatSessionDoc.getAssignedAgentStamp());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	}

	public void assignToBot(ChatSessionDoc chatSessionDoc, String botName) {
		chatSessionDoc.setMode("BOT");
		chatSessionDoc.setAssignedToAgent(botName);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	}
}
