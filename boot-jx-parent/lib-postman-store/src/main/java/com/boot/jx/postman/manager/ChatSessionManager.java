package com.boot.jx.postman.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.boot.jx.AppConfig;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.MongoUtils;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationWrappper;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_ASSIGN_GROUP;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.CHAT_STATE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMConstants.PROPERTIES;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.model.ext.InBoundEvent.SessionRouted;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.TimeUtils;

@Component
public class ChatSessionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatSessionManager.class);

	@Autowired
	private ChatLogger logManager;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMDomainConfig pmDomainConfig;

	@Autowired
	public PMClientConfig pmClientConfig;

	@Autowired
	public MessageContext messageContext;

	@Autowired
	public AppConfig appConfig;

	public InBoundEvent updateStatus(ChatSessionDoc session, PMConstants.CHAT_STATUS status) {
		if (!ArgUtil.is(status)) {
			return null;
		}

		String oldStatus = session.getStatus();
		if (status.toString().equalsIgnoreCase(oldStatus)) {
			return null;
		}

		InBoundEvent inBoundEvent = new InBoundEvent().eventCode(InBoundEvent.EVENT_TYPE.SESSION_STATUS);
		inBoundEvent.sessionRouted = new SessionRouted();
		inBoundEvent.sessionId = session.getSessionId();
		inBoundEvent.contactId = session.getContactId();
		inBoundEvent.contact().copyFrom(session.contact());
		sessionStore.changeStatus(session, status);
		logManager.event(session, EVENTS.STATUS_CHANGED, oldStatus, status.toString());
		sessionStore.updateMessageFromSession(session, inBoundEvent);
		return inBoundEvent;
	}

	public NodeEntry<InBoundEvent> resolveSession(ChatSessionDoc session) {
		NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();

		if (!ArgUtil.isEmptyValue(session.getResolveSessionStamp())) {
			return eventEntry;
		}

		session = sessionStore.resolveSession(session);
		InBoundEvent inBoundEvent = new InBoundEvent().eventCode(InBoundEvent.EVENT_TYPE.SESSION_STATUS);
		inBoundEvent.sessionRouted = new SessionRouted();
		inBoundEvent.sessionId = session.getSessionId();
		inBoundEvent.contactId = session.getContactId();
		inBoundEvent.contact().copyFrom(session.contact());
		logManager.event(session, EVENTS.STATUS_CHANGED, session.getStatus(),
				PMConstants.CHAT_STATUS.RESOLVED.toString());
		sessionStore.updateMessageFromSession(session, inBoundEvent);
		return eventEntry.value(inBoundEvent);
	}

	public InBoundEvent closeSession(ChatSessionDoc session) {
		if (!session.isActive()) {
			return null;
		}

		InBoundEvent inBoundEvent = new InBoundEvent().eventCode(InBoundEvent.EVENT_TYPE.SESSION_CLOSED);
		inBoundEvent.sessionRouted = new SessionRouted();
		inBoundEvent.sessionId = session.getSessionId();
		inBoundEvent.contactId = session.getContactId();
		inBoundEvent.contact().copyFrom(session.contact());

		session = sessionStore.closeSession(session);
		logManager.event(session, EVENTS.STATUS_CHANGED, session.getStatus(),
				PMConstants.CHAT_STATUS.CLOSED.toString());

		sessionStore.updateMessageFromSession(session, inBoundEvent);
		return inBoundEvent;
	}

	public boolean updateSessionTags(ChatSessionDoc sessionDoc, List<QuickTag> tags) {
		if (tags == null) {
			return false;
		}

		List<String> oldList = sessionDoc.tagId();
		List<String> newList = new ArrayList<String>();
		for (QuickTag tag : tags) {
			newList.add(tag.getId());
		}
		newList = CollectionUtil.distinct(newList);
		sessionStore.updateQuickTags(sessionDoc, newList);

		boolean updated = false;
		// LOGS
		List<String> removedItems = new ArrayList<String>(oldList);
		removedItems.removeAll(newList);
		if (ArgUtil.is(removedItems)) {
			updated = true;
			logManager.event(sessionDoc, EVENTS.TAG_REMOVED, removedItems.toArray(new String[0]));
		}

		List<String> addedItems = new ArrayList<String>(newList);
		addedItems.removeAll(oldList);
		if (ArgUtil.is(addedItems)) {
			updated = true;
			logManager.event(sessionDoc, EVENTS.TAG_ADDED, addedItems.toArray(new String[0]));
		}
		return updated;
	}

	public List<ChatSessionDoc> searchBy(List<CHAT_STATUS> status, List<QuickTag> tags, long fromStamp, long toStamp) {
		List<String> newList = new ArrayList<String>();
		for (QuickTag tag : tags) {
			newList.add(tag.getId());
		}
		// return sessionStore.findByStatusOrQuickTag(status, newList, fromStamp,
		// toStamp);
		return sessionStore.findByStatusOrQuickTagV2(status, tags, fromStamp, toStamp);
	}

	public List<ChatSessionDoc> searchByV1(List<CHAT_STATUS> status, List<QuickTag> tags, long fromStamp,
			long toStamp) {
		List<String> newList = new ArrayList<String>();
		for (QuickTag tag : tags) {
			newList.add(tag.getId());
		}
		return sessionStore.findByStatusOrQuickTagV1(status, newList, fromStamp, toStamp);
	}

	public List<ChatSessionDoc> findChatSessionsByQuery(SessionSearchQuery query, Criteria searchCriteria) {

		Query query2 = new Query();

		List<Criteria> criterias = new ArrayList<Criteria>();

		Criteria primaryCriteria = new Criteria();// .where("primary").is(true);

		criterias.add(searchCriteria);

		if (query.hasClosed()) {
			criterias.add(Criteria.where("active").is(false).and("resolved").is(true));
		} else if (query.contains(CHAT_STATUS.RESOLVED)) {
			criterias.add(Criteria.where("resolved").is(true));
		} else if (query.contains(CHAT_STATE.OUTBOUND)) {
			query.add(CHAT_MODE.AGENT);
			criterias.add(Criteria.where("active").is(true).and("msg.lastInBoundMsg").exists(false)
					.orOperator(Criteria.where("resolved").exists(false), Criteria.where("resolved").is(false)));
		} else if (query.hasExpired()) {
			Calendar expiryWatermark = Calendar.getInstance();
			expiryWatermark.setTimeInMillis(
					expiryWatermark.getTimeInMillis() - TimeUtils.toMillis(pmClientConfig.getChatSessionTimeout()));
			long expiryWatermarkHour = expiryWatermark.getTimeInMillis() / TimeUtils.Constants.MILLIS_IN_HOUR;
			criterias.add(Criteria.where("active").is(true)//
					.andOperator(//
							new Criteria().orOperator(//
									Criteria.where("resolved").exists(false), Criteria.where("resolved").is(false)))
					.orOperator(//
							Criteria.where("updated.hour").lte(expiryWatermarkHour), //
							Criteria.where("lastInComingStamp").lte(expiryWatermark.getTimeInMillis())
									.and("msg.lastInBoundMsg").exists(true)//
					));
		} else if (query.contains(CHAT_ASSIGN_GROUP.UNASSIGNED)) {
			query.add(CHAT_MODE.AGENT);
			criterias.add(new Criteria().orOperator(Criteria.where("assignedToAgent").is(null),
					Criteria.where("assignedToAgent").exists(false)));
		} else if (query.contains(CHAT_STATE.ACTIVE)) {
			// query.add(CHAT_MODE.AGENT);
			criterias.add(new Criteria() //
					.andOperator(Criteria.where("active").is(true) //
							.orOperator(Criteria.where("resolved").exists(false), Criteria.where("resolved").is(false)))
					.and("msg.lastInBoundMsg").exists(true)//
			);
		}

		if (query.containsAny(CHAT_STATE.UNATTENDED, CHAT_STATE.WAITING_LONG, CHAT_STATE.WAITING,
				CHAT_STATE.NEED_ATTENTION)) {

			query.add(CHAT_MODE.AGENT);

			Calendar chatIdle = Calendar.getInstance();
			chatIdle.setTimeInMillis(chatIdle.getTimeInMillis() - pmDomainConfig.getChatIdleTimeout().asMillis() * 2);

			criterias
					.add(new Criteria()
							.andOperator(Criteria.where("active").is(true).orOperator(
									Criteria.where("resolved").exists(false), Criteria.where("resolved").is(false)))
							.orOperator(
									// Last message was inbound and outbound is older
									Criteria.where("msg.lastMsg.type").is("I").and("msg.lastOutBoundMsg.timestamp")
											.lte(chatIdle.getTimeInMillis()),
									// Last message was inbound and No outbound
									Criteria.where("msg.lastMsg.type").is("I").and("msg.lastMsg.timestamp")
											.lte(chatIdle.getTimeInMillis()).and("msg.lastOutBoundMsg").exists(false),
									// Last message was not from agent and its older
									Criteria.where("msg.lastMsg.route.senderType").ne(MESSAGE_SENDER_TYPE.AGENT)
											.and("msg.lastMsg.timestamp").lte(chatIdle.getTimeInMillis())
							//
							));

		}

		if (query.contactTypes().size() > 0) {
			List<Criteria> contactCriteris = new ArrayList<Criteria>();
			for (ContactType contactType : query.contactTypes()) {
				contactCriteris.add(Criteria.where("contactType").is(contactType));
			}
			criterias.add(new Criteria().orOperator(contactCriteris.toArray(new Criteria[contactCriteris.size()])));
		}

		if (query.channels().size() > 0) {
			List<Criteria> channelCriteris = new ArrayList<Criteria>();
			for (String channel : query.channels()) {
				String[] c = channel.split(":");
				channelCriteris.add(Criteria.where("contact.channelType").is(c[0]).and("contact.lane").is(c[1]));
			}
			criterias.add(new Criteria().orOperator(channelCriteris.toArray(new Criteria[channelCriteris.size()])));
		}

		if (query.tags().size() > 0) {
			MultiValueMap<String, String> tags = new LinkedMultiValueMap<String, String>();
			for (QuickTag tag : query.tags()) {
				tags.add(tag.getCategory(), tag.getId());
			}
			for (Entry<String, List<String>> tagEntry : tags.entrySet()) {
				criterias.add(Criteria.where("tagId").in(tagEntry.getValue()));
			}
		}

		if (query.contains(CHAT_ASSIGN_GROUP.ORG)) {

			List<Criteria> orgCriterias = new ArrayList<Criteria>();

			if (!query.contains(CHAT_ASSIGN_GROUP.TEAM)) {
				// Not Assigned to MyTeam
				orgCriterias.add(Criteria.where("assignedToDept").ne(query.agentDept));
			}
			// Assigned to No-Org
			orgCriterias.add(Criteria.where("assignedToDept").is(null));
			orgCriterias.add(Criteria.where("assignedToDept").exists(false));

			criterias.add(MongoUtils.anyCriteria(orgCriterias));

		} else if (query.contains(CHAT_ASSIGN_GROUP.TEAM)) {
			Criteria teamCriteria = Criteria.where("assignedToDept").is(query.agentDept);

			if (!query.contains(CHAT_ASSIGN_GROUP.ME)) {
				// Not Assigned to ME
				teamCriteria.and("assignedToAgent").ne(query.agentCode);
			}

			criterias.add(teamCriteria);
		} else if (query.contains(CHAT_ASSIGN_GROUP.ME)) {
			// primaryCriteria = primaryCriteria.and("assignedToDept").is(query.agentDept);
			criterias.add(new Criteria().orOperator(
					// Assigned to Me
					Criteria.where("assignedToAgent").is(query.agentCode),
					// Assigned to None
					Criteria.where("assignedToDept").is(query.agentDept).and("assignedToAgent").is(null),
					Criteria.where("assignedToDept").is(query.agentDept).and("assignedToAgent").exists(false)
			//
			));
		}

		if (ArgUtil.is(query.modes())) {
			primaryCriteria = primaryCriteria.and("mode").in(query.modes());
		}

		int limit = Math.min(query.limit == 0 ? 50 : query.limit, pmDomainConfig.getAgentHistoryCount().asInteger(150));
		if (ArgUtil.isEqual(appConfig.getAppType(), "ADMIN")) {
			limit = Math.min(query.limit == 0 ? 100 : query.limit, 50000);
		}

		query2.addCriteria(
				// Only Agent Chats
				primaryCriteria
						// Add Selected Criteria
						.andOperator(criterias.toArray(new Criteria[criterias.size()])))
				// Limit
				.with(new Sort(Direction.DESC, "updated.hour")).limit(limit);
		// System.out.println(query2.toString());
		// if (LOGGER.isDebugEnabled()) {
		ApiResponseUtil.addLog(query2.toString());
		// }
		return sessionStore.find(CommonMongoQueryBuilder.collection(ChatSessionDoc.class).query(query2)
				.skipDBRefByNames("lastMsg", "lastInBoundMsg", "lastOutBoundMsg"));
	}

	public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(SessionSearchQuery query, String agentCode,
			String agentDept, long period) {

		period = Math.min(DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD_MAX, period);
		Calendar timeout = Calendar.getInstance();
		timeout.setTimeInMillis(timeout.getTimeInMillis() - period);
		long watermarkStampDay = timeout.getTimeInMillis() / TimeUtils.Constants.MILLIS_IN_DAY;

		timeout.setTimeInMillis(timeout.getTimeInMillis() - period);
		long graceStamp = timeout.getTimeInMillis();

		timeout.setTimeInMillis(timeout.getTimeInMillis() - DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD_MAX * 5);
		long searchableFromDay = timeout.getTimeInMillis() / TimeUtils.Constants.MILLIS_IN_DAY; // Searchable
																								// FromDate
																								// Limit
		Criteria rangeCriteria = Criteria.where("primary").is(true);

		if (ArgUtil.is(query.text)) {

			rangeCriteria.and("updated.day").gte(searchableFromDay).orOperator(
					// Check all fields
					Criteria.where("contactId").regex("" + query.text + "", "i"),
					Criteria.where("contactName").regex("" + query.text + "", "i"), // @Deprecated
					Criteria.where("contact.name").regex("" + query.text + "", "i"),
					Criteria.where("contact.phone").regex("" + query.text + "", "i"),
					Criteria.where("contact.email").regex("" + query.text + "", "i"));
		} else {
			// Within Watermark
			rangeCriteria.and("updated.day").gte(watermarkStampDay).orOperator(
					// Customer has replied within CustomerCareWindow
					Criteria.where("lastInComingStamp").gt(graceStamp),
					// Agent Has been Assigned to it
					Criteria.where("lastOutGoingStamp").gt(graceStamp));
		}

		query.agentCode = agentCode;
		query.agentDept = agentDept;
		return findChatSessionsByQuery(query, rangeCriteria);
	}

	public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(SessionSearchQuery query, String agentCode,
			String agentDept) {
		long historyPeriod = pmDomainConfig.getAgentHistoryPeriod().asLong(0L);
		if (historyPeriod > 0L && query.contains(CHAT_ASSIGN_GROUP.HISTORY)) {
			return findChatSessionDocByAgentAndUnAssigned(query, agentCode, agentDept,
					PMConstants.DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD + historyPeriod);
		} else if (historyPeriod > 0L && (query.contains(CHAT_STATE.EXPIRED) || query.contains(CHAT_STATE.CLOSED))) {
			return findChatSessionDocByAgentAndUnAssigned(query, agentCode, agentDept,
					PMConstants.DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD + historyPeriod);
		}

		// Agent Access level
		CHAT_ASSIGN_GROUP accessLevel = pmEnvironment.keyEntry(PROPERTIES.POSTMAN_AGENT_TAB_LEVEL)
				.asEnum(CHAT_ASSIGN_GROUP.class);

		if (!ArgUtil.is(accessLevel)) {
			if (pmEnvironment.keyEntry(PROPERTIES.POSTMAN_AGENT_TAB_ORG).asBoolean(false)) {
				accessLevel = CHAT_ASSIGN_GROUP.ORG;
			} else {
				accessLevel = CHAT_ASSIGN_GROUP.TEAM;
			}
		}

		if (!query.containsAny(CHAT_ASSIGN_GROUP.ORG, CHAT_ASSIGN_GROUP.TEAM, CHAT_ASSIGN_GROUP.ME)) {
			if (ArgUtil.is(accessLevel)) {
				query.add(accessLevel);
			}
		} else {
			switch (accessLevel) {
			case TEAM:
				query.tabs().remove(CHAT_ASSIGN_GROUP.ORG);
				break;
			case ME:
				query.tabs().remove(CHAT_ASSIGN_GROUP.ORG);
				query.tabs().remove(CHAT_ASSIGN_GROUP.TEAM);
				break;
			default:
				break;
			}
		}
//
		if (query.contains(CHAT_ASSIGN_GROUP.ME)) {
			if (!query.hasClosed() && !query.hasExpired()) {
				query.add(CHAT_MODE.AGENT);
			}
		} else if (query.contains(CHAT_ASSIGN_GROUP.TEAM)) {
			if (!query.hasClosed() && !query.hasExpired()) {
				query.add(CHAT_MODE.AGENT);
			}
		} else if (query.contains(CHAT_ASSIGN_GROUP.ORG)) {
			if (!pmEnvironment.keyEntry(PROPERTIES.POSTMAN_AGENT_TAB_NONAGENT).asBoolean(false)) {
				query.add(CHAT_MODE.AGENT);
			}
		}

		return findChatSessionDocByAgentAndUnAssigned(query, agentCode, agentDept,
				DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD * 3 / 2);
	}

	public List<ChatSessionDoc> searchPrimary(String search) {
		Criteria c = Criteria.where("primary").is(true); // Lane should be fixed
		if (ArgUtil.is(search)) {
			c = c.orOperator(
					// Check all fields
					Criteria.where("contactId").regex("" + search + "", "i"),
					Criteria.where("contactName").regex("" + search + "", "i"),
					Criteria.where("contact.name").regex("" + search + "", "i"),
					Criteria.where("contact.phone").regex("" + search + "", "i"),
					Criteria.where("contact.email").regex("" + search + "", "i"));
		}
		Query query = new Query()
				// New Criteria
				.addCriteria(c);
		return sessionStore.find(query, ChatSessionDoc.class);
	}

	public InBoundEvent initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		InBoundEvent inBoundEvent = new InBoundEvent().eventCode(InBoundEvent.EVENT_TYPE.SESSION_INIT);
		inBoundEvent.sessionId = session.getSessionId();
		inBoundEvent.contactId = session.getContactId();
		session = sessionStore.initSession(session);
		return inBoundEvent;
	}

	/**
	 * 
	 * @param chatSessionDoc
	 * @param pmArgs         { assignedToQueue, note}
	 * @return
	 */
	public InBoundEvent assignToQueue(ChatSessionDoc chatSessionDoc, PMArgs pmArgs) {
		
		String queueCode = pmArgs.getAssignToQueueCode();

		InBoundEvent inBoundEvent = new InBoundEvent().eventCode(InBoundEvent.EVENT_TYPE.SESSION_ROUTED);
		inBoundEvent.sessionRouted = new SessionRouted();
		inBoundEvent.sessionId = chatSessionDoc.getSessionId();
		inBoundEvent.contactId = chatSessionDoc.getContactId();
		inBoundEvent.contact().copyFrom(chatSessionDoc.contact());
		sessionStore.updateMessageFromSession(chatSessionDoc, inBoundEvent);

		String sourceQueue = chatSessionDoc.getAssignedToQueue();
		String chatStatus = chatSessionDoc.getStatus();

		if (ArgUtil.is(chatSessionDoc.getContactId())) {
			inBoundEvent.contact().setContactId(inBoundEvent.contactId);
		}

		if (!ArgUtil.is(chatSessionDoc)) {
			LOGGER.error("Session Cannot Be Empty for queueCode {}", queueCode);
			return inBoundEvent;
		}

		if (ArgUtil.is(queueCode)) {
			PMConfigurationWrappper config = pmEnvironment.config();
			ClientApp apiKeyConfig = config.clientApiKey(queueCode);
			if (ArgUtil.is(apiKeyConfig)) {
				queueCode = apiKeyConfig.getQueue();
				chatSessionDoc.setAssignedToQueue(queueCode);
				APP_TYPE appType = APP_TYPE.from(apiKeyConfig.getAppType());
				chatSessionDoc.setMode(appType.getMode().name());

				inBoundEvent.sessionRouted.routingId = PostManUtil.ROUTING_ID(chatSessionDoc.getSessionId(),
						apiKeyConfig.getQueue());

				chatSessionDoc.setRoutingId(inBoundEvent.sessionRouted.routingId);
				chatSessionDoc.setStatus(CHAT_STATUS.OPEN.toString());
			} else {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("queue").codeKey("INVALID_QUEUE")
						.description("Invalid Queue Code " + queueCode));
				return inBoundEvent;
			}
		} else {
			chatSessionDoc.setAssignedToQueue(null);
			chatSessionDoc.setMode(null);
		}
		MongoQueryBuilder<ChatSessionDoc> builder = MongoQueryBuilder.collection(ChatSessionDoc.class)
				.whereId(chatSessionDoc.getSessionId());
		builder.set("assignedToQueue", chatSessionDoc.getAssignedToQueue());
		builder.set("mode", chatSessionDoc.getMode());
		builder.set("routingId", chatSessionDoc.getRoutingId());
		builder.set("status", chatSessionDoc.getStatus());
		sessionStore.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);

		if (ArgUtil.is(pmArgs.getNote())) {
			logManager.note(chatSessionDoc, new OutboxMessage().message(pmArgs.getNote()));
		}

		logManager.event(chatSessionDoc, EVENTS.ASGND_TO_QUEUE, queueCode);

		if (ArgUtil.not(sourceQueue) || !ArgUtil.is(chatStatus)) {
			inBoundEvent.sessionRouted.sessionStart = true;
		} else {
			inBoundEvent.sessionRouted.sourceQueue = sourceQueue;
		}

		inBoundEvent.sessionRouted.targetQueue = chatSessionDoc.getAssignedToQueue();

		sessionStore.updateMessageFromSession(chatSessionDoc, inBoundEvent);

		if (inBoundEvent.sessionRouted.sessionStart) {
			MessageDoc doc = logManager.event(inBoundEvent, queueCode, EVENTS.ON_SESSION_START,
					inBoundEvent.sessionRouted);
			inBoundEvent.setEventId(doc.getMessageId());
		} else {
			MessageDoc doc = logManager.event(inBoundEvent, queueCode, EVENTS.ON_SESSION_ROUTE,
					inBoundEvent.sessionRouted);
			inBoundEvent.setEventId(doc.getMessageId());
		}

		return inBoundEvent;
	}

	public InBoundEvent assignToQueue(ChatSessionDoc chatSessionDoc, String queueCode) {
		return this.assignToQueue(chatSessionDoc, new PMArgs().assignToQueueCode(queueCode));
	}

	public InBoundEvent assignToQueue(String sessionId, String queueCode) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		return this.assignToQueue(sessionDoc, queueCode);
	}

}
