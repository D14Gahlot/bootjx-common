package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;

@Component
public class SessionRouter extends ATaskLimiter {

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private PMDomainConfig pmDomainConfig;

    @Autowired
    private MitelClient mitelClient;

    @Autowired
    private SessionStore sessionStore;

    public ClientApp getDefaultInboundApp(String assignedQueue, Contactable contactable) {
	ClientApp defaultClient = null;
	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.config().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }
	}

	if (!ArgUtil.is(contactable)) {
	    return defaultClient;
	}

	assignedQueue = pmDomainConfig.getDefaultInboundQueue(contactable);

	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.config().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }
	}

	return defaultClient;
    }

    @Override
    public void doTask(TunnelTask task) {

	switch (task.getName()) {
	case "MITEL_ROUTER":
	    mitelRouting(task);
	    break;
	default:
	    break;
	}

    }

    private void mitelRouting(TunnelTask task) {

	MapModel data = task.data();
	ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));
	ClientApp defaultClient = getDefaultInboundApp(data.getString("queue"), null);

	MapModel meta = new MapModel(session.getMeta());
	MapPathEntry omidEntry = meta.pathEntry("mitel.omid");
	String omid = omidEntry.asString();
	MapModel mitel = mitelClient.resend(defaultClient, session.contact(), session.getSessionId(), omid);
	String newomid = mitel.getString("id");

	if (!ArgUtil.areEqual(newomid, omid)) {
	    ChatSessionQuery q = new ChatSessionQuery(session);
	    omidEntry.save(newomid);
	    session.setMeta(meta.map());
	    q.set("meta.mitel.omid", newomid).set("meta.mitel.queue_id", mitel.getString("queueId"));
	    sessionStore.updateFirst(q);
	}

    }

}
