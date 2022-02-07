package com.boot.jx.postman.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.SessionMessage;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.utils.PostManUtil;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.TimeUtils;

@Component
public class ChatSessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSessionManager.class);

    @Autowired
    private LogManager logManager;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private PMEnvironment pmEnvironment;

    public boolean resolveSession(ChatSessionDoc session) {
	if (!ArgUtil.isEmptyValue(session.getResolveSessionStamp())) {
	    return false;
	}
	session = sessionStore.resolveSession(session);
	logManager.event(session, EVENTS.STATUS_CHANGED, session.getStatus(),
		PMConstants.CHAT_STATUS.RESOLVED.toString());
	return true;
    }

    public boolean closeSession(ChatSessionDoc session) {
	if (!session.isActive()) {
	    return false;
	}
	session = sessionStore.closeSession(session);
	logManager.event(session, EVENTS.STATUS_CHANGED, session.getStatus(), PMConstants.CHAT_STATUS.CLOSED.toString());
	return true;
    }

    public boolean updateSessionStatus(ChatSessionDoc sessionDoc, PMConstants.CHAT_STATUS status) {
	if (!ArgUtil.is(status)) {
	    return false;
	}

	String oldStatus = sessionDoc.getStatus();
	if (status.toString().equalsIgnoreCase(oldStatus)) {
	    return false;
	}
	if (status == PMConstants.CHAT_STATUS.RESOLVED) {
	    return this.resolveSession(sessionDoc);
	} else if (status == PMConstants.CHAT_STATUS.CLOSED) {
	    return this.closeSession(sessionDoc);
	} else {
	    sessionStore.changeStatus(sessionDoc, status);
	    logManager.event(sessionDoc, EVENTS.STATUS_CHANGED, oldStatus, status.toString());
	}
	return true;
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
	return sessionStore.findByStatusOrQuickTag(status, newList, fromStamp, toStamp);
    }

    public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(String agentCode, String agentDept,
	    String search, long period) {
	Query query2 = new Query();
	Calendar timeout = Calendar.getInstance();
	timeout.setTimeInMillis(timeout.getTimeInMillis() - period);
	long watermarkStamp = timeout.getTimeInMillis();
	long watermarkStampDay = timeout.getTimeInMillis() / TimeUtils.Constants.MILLIS_IN_DAY;

	timeout.setTimeInMillis(timeout.getTimeInMillis() - period);
	long graceStamp = timeout.getTimeInMillis();

	Criteria localCriteria = new Criteria().andOperator(
		// is Active
		Criteria.where("active").is(true),
		// Agent Session Start
		// Criteria.where("agentSessionStamp").gt(watermarkStamp),
		new Criteria().orOperator(
			//
			Criteria.where("agentSessionStamp").gt(watermarkStamp),
			// @deprecated condition
			Criteria.where("updatedStamp").gt(watermarkStamp),
			// new Condition
			Criteria.where("updated.day").gt(watermarkStampDay)),
		// Criteria.where("updatedStamp").gt(watermarkStamp),
		// Additional Stamps
		new Criteria().andOperator(
			//
			new Criteria().orOperator(
				// Customer has replied within CustomerCareWindow
				Criteria.where("lastInComingStamp").gt(graceStamp),
				// Agent Has been Assigned to it
				Criteria.where("lastOutGoingStamp").gt(graceStamp)),
			// Is not resolved yet
			new Criteria().orOperator(Criteria.where("resolved").exists(false),
				Criteria.where("resolved").is(false)))

	);

	if (ArgUtil.is(search)) {
	    search = search.replace("*", "").trim();
	    Criteria archiveCriteria = Criteria.where("primary").is(true).orOperator(
		    // Check all fields
		    Criteria.where("contactId").regex("" + search + "", "i"),
		    Criteria.where("contactName").regex("" + search + "", "i"),
		    Criteria.where("contact.name").regex("" + search + "", "i"),
		    Criteria.where("contact.phone").regex("" + search + "", "i"),
		    Criteria.where("contact.email").regex("" + search + "", "i"));

	    query2.addCriteria(Criteria.where("mode").is("AGENT").orOperator(localCriteria, archiveCriteria));
	} else {
	    query2.addCriteria(Criteria.where("mode").is("AGENT").andOperator(localCriteria));
	}
	query2.with(new Sort(Direction.DESC, "updated.day")).limit(100);
	// System.out.println(query2.toString());
	LOGGER.debug(query2.toString());
	return sessionStore.find(query2, ChatSessionDoc.class);
    }

    public List<ChatSessionDoc> findChatSessionDocByAgentAndUnAssigned(String agentCode, String agentDept,
	    String search) {
	return findChatSessionDocByAgentAndUnAssigned(agentCode, agentDept, search,
		DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD);
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

    public ChatSessionDoc assignToQueue(ChatSessionDoc chatSessionDoc, String queueCode) {
	if (!ArgUtil.is(chatSessionDoc)) {
	    LOGGER.error("Session Cannot Be Empty for queueCode {}", queueCode);
	    return chatSessionDoc;
	}

	if (ArgUtil.is(queueCode)) {
	    PMConfigurationModel config = pmEnvironment.local();
	    ClientApp apiKeyConfig = config.clientApiKey(queueCode);
	    if (ArgUtil.is(apiKeyConfig)) {
		queueCode = apiKeyConfig.getQueue();
		chatSessionDoc.setAssignedToQueue(queueCode);
		chatSessionDoc.setMode(apiKeyConfig.getAppType());
	    } else {
		ApiResponseUtil.throwInputException(new ApiFieldError().field("queue").codeKey("INVALID_QUEUE")
			.description("Invalid Queue Code " + queueCode));
		return chatSessionDoc;
	    }
	} else {
	    chatSessionDoc.setAssignedToQueue(null);
	    chatSessionDoc.setMode(null);
	}
	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(chatSessionDoc.getSessionId());
	builder.set("assignedToQueue", chatSessionDoc.getAssignedToQueue());
	builder.set("mode", chatSessionDoc.getMode());
	sessionStore.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
	logManager.event(chatSessionDoc, EVENTS.ASGND_TO_QUEUE, queueCode);
	return chatSessionDoc;

    }

    public ChatSessionDoc assignToQueue(String sessionId, String queueCode) {
	ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
	return this.assignToQueue(sessionDoc, queueCode);
    }

}
