package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocStore;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder.CommonMongoCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatUserProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_MODE;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
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
public class SessionStore extends CommonDocStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionStore.class);

	@Autowired
	public MongoTemplate mongoTemplate;

	@Autowired
	public CommonMongoTemplate commonMongoTemplate;

	@Autowired
	public PMClientConfig pmClientConfig;

	public ChatContactDoc getContact(SessionMessage inboxMessage) {
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

	public ChatContactDoc findOrCreate(ChatContactDoc chatContactDocQuery) {
		ChatContactDoc chatContactDoc = null;
		Contactable contact = PostManUtil.updateContact(chatContactDocQuery);

		if (ArgUtil.isEmpty(contact.getContactId())) {
			return null;
		}
		chatContactDoc = mongoTemplate.findById(contact.getContactId(), ChatContactDoc.class);

		ChatContactQuery chatContactQuery = ArgUtil.is(chatContactDoc) ? new ChatContactQuery(chatContactDoc)
				: new ChatContactQuery(contact.getContactId());

		if (!ArgUtil.is(chatContactDoc) // chatContactDoc is not found
				&& ArgUtil.is(contact.getCsid()) // csid is present
				&& ArgUtil.is(chatContactDocQuery.getLane()) // lane is present
		) {
			chatContactDoc = mongoTemplate.findOne(Query.query(CommonMongoQueryBuilder.CommonMongoCriteria
					// CSID should match
					.where("csid").is(contact.getCsid())
					// Lane must match
					.and("lane").is(chatContactDocQuery.getLane())), ChatContactDoc.class);
			if (!ArgUtil.is(chatContactDoc)) {
				chatContactDoc = new ChatContactDoc();
				chatContactQuery.setContactId(chatContactDocQuery.getContactId());
				chatContactQuery.setContactType(chatContactDocQuery.getContactType());
				chatContactQuery.setChannel(chatContactDocQuery.getChannel());
				chatContactQuery.setCsid(chatContactDocQuery.getCsid());
				chatContactQuery.setLane(chatContactDocQuery.getLane());
				commonMongoTemplate.upsert(chatContactQuery);
			}
		}
		return chatContactDoc;
	}

	public ChatSessionDoc getSession(String sessionId) {
		return mongoTemplate.findById(sessionId, ChatSessionDoc.class);
	}

	public boolean isSessionValid(ChatSessionDoc chatSessionDoc) {
		if ((ArgUtil.isEmpty(chatSessionDoc)
				|| TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), pmClientConfig.getChatSessionTimeout())
				|| !chatSessionDoc.isActive())) {
			return false;
		}
		return true;
	}

	public ChatSessionDoc getValidSession(String sessionId) {
		ChatSessionDoc chatSessionDoc = mongoTemplate.findById(sessionId, ChatSessionDoc.class);
		isSessionValid(chatSessionDoc);
		return chatSessionDoc;
	}

	public ChatSessionDoc createSession(ChatContactDoc chatContactDoc) {
		ChatSessionDoc chatSessionDoc = new ChatSessionDoc();
		chatSessionDoc.setContactId(chatContactDoc.getContactId());
		chatSessionDoc.setContactType(chatContactDoc.getContactType());
		chatSessionDoc.setChannel(chatContactDoc.getChannel());
		chatSessionDoc.setLane(chatContactDoc.getLane());
		save(chatSessionDoc);
		return chatSessionDoc;
	}

	private ChatSessionDoc createSession(IMessage inboxMessage) {
		Contactable contact = PostManUtil.updateContact(inboxMessage.contact());

		if (ArgUtil.isEmpty(contact.getContactId())) {
			return null;
		}

		String contactId = contact.getContactId();

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

		ChatContactQuery chatContactQuery = ArgUtil.is(chatContactDoc) ? new ChatContactQuery(chatContactDoc)
				: new ChatContactQuery(contactId);

		if (!isSessionValid(chatSessionDoc)) {

			closeActiveSessionsMulty(contactId);

			// SESSION CREATION
			chatSessionDoc = new ChatSessionDoc();
			chatSessionDoc.setContactId(contactId);
			chatSessionDoc.setContactType(ArgUtil.parseAsString(inboxMessage.contact().type()));
			chatSessionDoc.setChannel(inboxMessage.contact().getChannel());
			chatSessionDoc.setLane(inboxMessage.contact().getLane());

			// SESSION UPDATE
			chatSessionDoc.setActive(true);

			if (PostManUtil.isInBound(inboxMessage.getType())) {
				chatSessionDoc.setLastInComingStamp(inboxMessage.getTimestamp());
				chatContactQuery.setLastInBoundStamp(inboxMessage.getTimestamp());
			} else if (PostManUtil.isOutBound(inboxMessage.getType())) {
				chatSessionDoc.setLastResponseStamp(inboxMessage.getTimestamp());
				chatContactQuery.setLastOutBoundStamp(inboxMessage.getTimestamp());
			}

			save(chatSessionDoc);
			chatContactQuery.setSessionId(chatSessionDoc.getSessionId());

			// CONTACT CREATION - needs creation or updation if
			if (ArgUtil.isEmpty(chatContactDoc) // chatContactDo not found
					|| ArgUtil.isEmpty(chatContactDoc.getContactType()) // or contactType is missing
					|| ArgUtil.isEmpty(chatContactDoc.getCsid()) // or csid is missing
					|| ArgUtil.isEmpty(chatContactDoc.getLane()) // or lane is missing
					|| ArgUtil.isEmpty(chatContactDoc.getChannel())) { // or channel is mssing
				chatContactQuery.setContactId(contactId);
				chatContactQuery.setContactType(ArgUtil.parseAsString(inboxMessage.contact().type()));
				chatContactQuery.setChannelType(inboxMessage.contact().getChannel());
				chatContactQuery.setCsid(inboxMessage.contact().getCsid());
				chatContactQuery.setLane(inboxMessage.contact().getLane());
				commonMongoTemplate.upsert(chatContactQuery);
			} else {
				// CONTACT UPDATE
				commonMongoTemplate.updateFirst(chatContactQuery);
			}
		} else {
			// SESSION UPDATE
			ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);

			chatSessionDocQuery.setActive(true);

			if (PostManUtil.isInBound(inboxMessage.getType())) {
				chatSessionDocQuery.setLastInComingStamp(inboxMessage.getTimestamp());
				chatContactQuery.setLastInBoundStamp(inboxMessage.getTimestamp());
			} else if (PostManUtil.isOutBound(inboxMessage.getType())) {
				chatSessionDocQuery.setLastResponseStamp(inboxMessage.getTimestamp());
				chatContactQuery.setLastOutBoundStamp(inboxMessage.getTimestamp());
			}
			commonMongoTemplate.updateFirst(chatSessionDocQuery);
			commonMongoTemplate.updateFirst(chatContactQuery);
		}
		return chatSessionDoc;
	}

	public ChatSessionDoc linkSession(IMessage inboxMessage) {
		ChatSessionDoc chatSessionDoc = this.createSession(inboxMessage);
		if (ArgUtil.is(chatSessionDoc)) {
			inboxMessage.contact().setContactId(chatSessionDoc.getContactId());
			inboxMessage.setSessionId(chatSessionDoc.getSessionId());
			inboxMessage.session().setAgent(chatSessionDoc.getAssignedToAgent());
			inboxMessage.session().setDept(chatSessionDoc.getAssignedToDept());
			inboxMessage.session().setMode(chatSessionDoc.getMode());
			inboxMessage.session().setResolved(chatSessionDoc.isResolved());
		}
		return chatSessionDoc;
	}

	public SessionMessage toSessionMessage(ChatSessionDoc session) {
		ChatContactDoc contact = getContact(session.getContactId());
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.contact().setContactType(contact.getContactType());
		inboxMessage.contact().setChannel(contact.getChannel());
		inboxMessage.contact().setLane(ArgUtil.nonEmpty(session.getLane(), contact.getLane()));
		inboxMessage.setFrom(contact.getCsid());
		inboxMessage.setFromName(contact.getName());
		inboxMessage.setSessionId(contact.getSessionId());
		inboxMessage.contact().setContactId(contact.getContactId());

		inboxMessage.session().setMode(session.getMode());
		inboxMessage.session().setAgent(session.getAssignedToAgent());
		inboxMessage.session().setDept(session.getAssignedToDept());

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

	public void expireChatSession() {
		Calendar cal = Calendar.getInstance();
		int offsetOur = (int) ((cal.getTimeInMillis() / 3600)
				% (TimeUtils.toHours(pmClientConfig.getChatSessionTimeout()) / 2));
		if (offsetOur == 0) {
			cal.add(Calendar.HOUR, -1 * (int) TimeUtils.toHours(pmClientConfig.getChatSessionTimeout()));
			CommonMongoQueryBuilder cmqb = new CommonMongoQueryBuilder()
					.with(Criteria.where("active").is(true).and("lastInComingStamp").lt(cal.getTimeInMillis())
							.andOperator(new Criteria().orOperator(Criteria.where("resolved").exists(false),
									Criteria.where("resolved").is(false))))
					.set("expired", true).set("active", false).set("closeSessionStamp", System.currentTimeMillis());
			mongoTemplate.updateFirst(cmqb.getQuery(), cmqb.getUpdate(), ChatSessionDoc.class);
		}
	}

	public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(String agentCode, String agentDept) {
		Query query2 = new Query();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		query2.addCriteria(Criteria.where("active").is(true).and("mode").is("AGENT").and("lastInComingStamp")
				.gt(cal.getTimeInMillis()).andOperator(
				// Is not assigned to any agent or assigned to said agent
//						new Criteria().orOperator(Criteria.where("assignedToAgent").exists(false),
//								Criteria.where("assignedToAgent").is(null),
//								Criteria.where("assignedToAgent").is(agentCode)),
						// Is not resolved yet
						new Criteria().orOperator(Criteria.where("resolved").exists(false),
								Criteria.where("resolved").is(false))

				));
		// LOGGER.info(query2.toString());
		return mongoTemplate.find(query2, ChatSessionDoc.class);
	}

	public List<ChatSessionDoc> findSimilarChatSessionForContactId(String contactId) {
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

	public List<ChatSessionDoc> findActiveChatSessionForContactId(String contactId) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("contactId").is(contactId).and("active").is(true));
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

	public ChatSessionDoc initSession(ChatSessionDoc chatSessionDoc, ChatContactDoc contact) {
		chatSessionDoc.setInitd(true);
		chatSessionDoc.setContactName(contact.getName());

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("initd", chatSessionDoc.isInitd());
		builder.set("contactName", chatSessionDoc.getContactName());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc changeStatus(ChatSessionDoc chatSessionDoc, CHAT_STATUS status) {
		// Old Way of Doing it
		chatSessionDoc.setStatus(status.toString());
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("status", status.toString());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc resolveSession(ChatSessionDoc chatSessionDoc) {
		// Old Way of Doing it
		chatSessionDoc.setResolveSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setResolved(true);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("resolveSessionStamp", chatSessionDoc.getResolveSessionStamp());
		builder.set("resolved", chatSessionDoc.isResolved());
		builder.set("status", CHAT_STATUS.RESOLVED);
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
		return chatSessionDoc;
	}

	public ChatSessionDoc closeSession(ChatSessionDoc chatSessionDoc) {
		chatSessionDoc.setCloseSessionStamp(System.currentTimeMillis());
		chatSessionDoc.setActive(false);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("closeSessionStamp", chatSessionDoc.getCloseSessionStamp());
		builder.set("active", chatSessionDoc.isActive());
		builder.set("status", CHAT_STATUS.CLOSED);
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		return chatSessionDoc;
	}

	public ChatSessionDoc deleteSession(ChatSessionDoc chatSessionDoc) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder()
				.with(CommonMongoCriteria.whereId(chatSessionDoc.getSessionId()).and("channel").is("IMPORT"));
		mongoTemplate.remove(builder.getQuery(), ChatSessionDoc.class);

		CommonMongoQueryBuilder builder2 = new CommonMongoQueryBuilder()
				.with(CommonMongoCriteria.where("sessionId").is(chatSessionDoc.getSessionId()));
		mongoTemplate.remove(builder2.getQuery(), MessageDoc.class,
				MessageStore.getCollectionName(chatSessionDoc.getContactType()));
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
		if (ArgUtil.isEmptyValue(chatSessionDoc.getFistResponseStamp())) {
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
		chatSessionDoc.setMode(CHAT_MODE.AGENT.toString());
		chatSessionDoc.setAssignedToDept(agentDept);
		chatSessionDoc.setAssignedAgentStamp(System.currentTimeMillis());
		chatSessionDoc.setAssignedToAgent(agentCode);

		if (chatSessionDoc.getAgentSessionStamp() == 0L) {
			chatSessionDoc.setAgentSessionStamp(chatSessionDoc.getAssignedAgentStamp());
		}

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToDept", chatSessionDoc.getAssignedToDept());
		builder.set("assignedDeptStamp", chatSessionDoc.getAssignedDeptStamp());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		builder.set("assignedAgentStamp", chatSessionDoc.getAssignedAgentStamp());
		builder.set("agentSessionStamp", chatSessionDoc.getAgentSessionStamp());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	}

	public void assignToBot(ChatSessionDoc chatSessionDoc, String botName) {
		chatSessionDoc.setMode(CHAT_MODE.BOT.toString());
		chatSessionDoc.setAssignedToAgent(botName);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("assignedToAgent", chatSessionDoc.getAssignedToAgent());
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

	}

}
