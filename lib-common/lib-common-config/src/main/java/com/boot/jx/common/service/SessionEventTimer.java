package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.ChatSessionEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class SessionEventTimer extends ATaskLimiter {

	public static final String CHAT_OUT_IDLE_TIMEOUT = "CHAT_OUT_IDLE_TIMEOUT";

	public static final String CHAT_IN_IDLE_TIMEOUT = "CHAT_IN_IDLE_TIMEOUT";

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

	@Autowired
	private ChatLogger logManager;

	@Autowired
	AppConfig appConfig;

	@Lazy
	@Autowired
	private ChatSessionEvents chatSessionEvents;

	@Override
	public boolean isWorker() {
		return ArgUtil.isEqual(appConfig.getAppType(), "POSTMAN", "AGENT", "BOT");
	}

	@Async
	public void setChatOutIdleTimeout(String sessionid, ClientApp app) {
		if (app != null && app.isAgentApp()) {
			boolean timeoutEnabled = pmEnvironment
					.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT).asBoolean(false);
			if (timeoutEnabled) {
				long timeout = pmEnvironment
						.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL).asLong(0L);
				if (timeout > 0L) {
					TunnelTask task = new TunnelTask().name(SessionEventTimer.CHAT_OUT_IDLE_TIMEOUT).id(sessionid)
							.intervalMinutes(timeout);
					this.debounce(task);
				}
			}
		}
	}

	@Async
	public void setChatInIdleTimeout(String sessionid, ClientApp app) {
		if (app != null && app.isAgentApp()) {
			boolean timeoutEnabled = pmEnvironment
					.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT).asBoolean(false);
			if (timeoutEnabled) {
				long timeout = pmEnvironment
						.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL).asLong(0L);
				if (timeout > 0L) {
					TunnelTask task = new TunnelTask().name(SessionEventTimer.CHAT_IN_IDLE_TIMEOUT).id(sessionid)
							.intervalMinutes(timeout);
					this.debounce(task);
				}
			}
		}
	}

	@Override
	public void doTaskSafely(TunnelTask task) {

		switch (task.getName()) {
		case "MITEL_ROUTER":
			doMitelRouting(task);
			break;
		case "MITEL_CLOSE_CHECK":
			doMitelClosing(task);
			break;
		case CHAT_OUT_IDLE_TIMEOUT:
			doChatOutIdleTimeout(task);
			break;
		case CHAT_IN_IDLE_TIMEOUT:
			doChatInIdleTimeout(task);
			break;
		default:
			break;
		}

	}

	private void doChatOutIdleTimeout(TunnelTask task) {
		ChatSessionDoc session = sessionStore.getSession(task.getId());
		if (sessionStore.isSessionValid(session)) {
			ChatMessageDTO lastMsg = session.lastMsg();
			ChatMessageDTO lastOutBoundMsg = session.lastOutBoundMsg();
			long timeout = pmEnvironment
					.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL).asLong(0L);
			if (ArgUtil.is(lastMsg) && PostManUtil.isInBound(lastMsg.getType()) // last message is also inbound
					&& (!ArgUtil.is(lastOutBoundMsg) // And ther is no outbound
							|| TimeUtils.isExpired(lastOutBoundMsg.getTimestamp(), timeout * 60000) // OR is older than
																									// interval
					)) {

				logManager.event(session, EVENTS.ON_SESSION_IDLE);
				if (chatSessionEvents != null) {
					chatSessionEvents.onSessionIdleOutBound(session);
				}
			}
		}
	}

	private void doChatInIdleTimeout(TunnelTask task) {
		ChatSessionDoc session = sessionStore.getSession(task.getId());
		if (sessionStore.isSessionValid(session)) {
			ChatMessageDTO lastMsg = session.lastMsg();
			ChatMessageDTO lastInBoundMsg = session.lastInBoundMsg();
			long timeout = pmEnvironment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL)
					.asLong(0L);
			if (ArgUtil.is(lastMsg) && PostManUtil.isOutBound(lastMsg.getType()) // last message is also inbound
					&& (!ArgUtil.is(lastInBoundMsg) // And ther is no outbound
							|| TimeUtils.isExpired(lastInBoundMsg.getTimestamp(), timeout * 60000) // OR is older than
																									// interval
					)) {
				logManager.event(session, EVENTS.ON_SESSION_IDLE);
				if (chatSessionEvents != null) {
					chatSessionEvents.onSessionIdleInBound(session);
				}
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
