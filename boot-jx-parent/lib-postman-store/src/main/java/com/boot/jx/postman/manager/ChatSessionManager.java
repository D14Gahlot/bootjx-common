package com.boot.jx.postman.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.store.MessageStore.EVENTS;
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

    public boolean resolveSession(ChatSessionDoc session) {
	if (!ArgUtil.isEmptyValue(session.getResolveSessionStamp())) {
	    return false;
	}
	session = sessionStore.resolveSession(session);
	logManager.log(session, EVENTS.STATUS_CHANGED, session.getStatus(),
		PMConstants.CHAT_STATUS.RESOLVED.toString());
	return true;
    }

    public boolean closeSession(ChatSessionDoc session) {
	if (!session.isActive()) {
	    return false;
	}
	session = sessionStore.closeSession(session);
	logManager.log(session, EVENTS.STATUS_CHANGED, session.getStatus(), PMConstants.CHAT_STATUS.CLOSED.toString());
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
	    logManager.log(sessionDoc, EVENTS.STATUS_CHANGED, oldStatus, status.toString());
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
	    logManager.log(sessionDoc, EVENTS.TAG_REMOVED, removedItems.toArray(new String[0]));
	}

	List<String> addedItems = new ArrayList<String>(newList);
	addedItems.removeAll(oldList);
	if (ArgUtil.is(addedItems)) {
	    updated = true;
	    logManager.log(sessionDoc, EVENTS.TAG_ADDED, addedItems.toArray(new String[0]));
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

}
