package com.boot.jx.postman.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class ChatSessionManager {

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
}
