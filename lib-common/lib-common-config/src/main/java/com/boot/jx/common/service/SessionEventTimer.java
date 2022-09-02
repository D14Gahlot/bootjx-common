package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;

@Component
public class SessionEventTimer extends ATaskLimiter {

	@Autowired
	private MitelClient mitelClient;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageContext messageContext;

	@Lazy
	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Override
	public void doTaskSafely(TunnelTask task) {

		switch (task.getName()) {
		case "MITEL_ROUTER":
			doMitelRouting(task);
			break;
		case "MITEL_CLOSE_CHECK":
			doMitelClosing(task);
			break;
		case "CHAT_IN_IDLE_TIMEOUT":
			doChatInIdelTimeout(task);
			break;
		default:
			break;
		}

	}

	private void doChatInIdelTimeout(TunnelTask task) {
		MapModel data = task.data();
		ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));

		if (sessionStore.isSessionValid(session)) {
			PMConfigurationObject frwrdQueue = pmEnvironment
					.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE);
			if (frwrdQueue.exists()) {
				chatSessionService.routeSession(session, new PMArgs().assignToQueueCode(frwrdQueue.asString()));
			}
		}

	}

	private void doMitelClosing(TunnelTask task) {
		MapModel data = task.data();
		ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));
		ClientApp defaultClient = messageContext.clientApp(data.getString("queue"), null);

		MapModel meta = new MapModel(session.getMeta());
		MapPathEntry omidEntry = meta.pathEntry("mitel.omid");
		String omid = omidEntry.asString();
		MapModel mitel = mitelClient.openMediaGetActive(defaultClient, session.contact(), session.getSessionId(), omid);

		if (!ArgUtil.is(mitel) || mitel.keyEntry("id").exists()) {
			chatSessionService.closeSession(session);
		}
	}

	private void doMitelRouting(TunnelTask task) {

		MapModel data = task.data();
		ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));
		ClientApp defaultClient = messageContext.clientApp(data.getString("queue"), null);

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
