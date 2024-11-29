package com.boot.jx.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.CONFIG_FEATURES_KEY;
import com.boot.jx.common.config.CONFIG_SETUP_KEY;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.inbound.InBound.ChatSessionEvents;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.client.CommonServiceClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.mitel.MitelClient;
import com.boot.jx.postman.model.ext.SessionBoundEvent;
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

	public static final String MITEL_ROUTER = "MITEL_ROUTER";

	public static final String MITEL_CLOSE_CHECK = "MITEL_CLOSE_CHECK";

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

	@Autowired
	private CommonServiceClient commonServiceClient;

	@Override
	public boolean isWorker() {
		return ArgUtil.isEqual(appConfig.getAppType(), "POSTMAN", "AGENT", "BOT");
	}

	private void debouncEvent(CONFIG_SETUP_KEY configSetupKey, String sessionid, SessionBoundEvent inBoundEvent,
			String sessionEventName) {
		long timeout = pmEnvironment.keyEntry(configSetupKey).asLong(0L);
		if (timeout > 0L) {
			if (pmEnvironment.featureEntry(CONFIG_FEATURES_KEY.EVENTS_TIMEOUT).asBoolean()) {
				// Only if this feature is there use chrono servre to set timeouts
				commonServiceClient.publishSessionBoundEvent(inBoundEvent);
			} else {
				TunnelTask task = new TunnelTask().name(sessionEventName).id(sessionid).intervalMinutes(timeout);
				this.debounce(task);
			}
		}
	}

	@Async
	public void setChatOutIdleTimeout(String sessionid, ClientApp app, SessionBoundEvent inBoundEvent) {
		if (app != null && (app.isAgentApp() || app.isCustomApp())) {
			boolean timeoutEnabled = pmEnvironment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT)
					.asBoolean(false);

			PMConfigurationObject frwrdQueue = pmEnvironment
					.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_QUEUE);

			if (timeoutEnabled && frwrdQueue.not(app.getQueue())) {
				debouncEvent(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL, sessionid, inBoundEvent,
						SessionEventTimer.CHAT_OUT_IDLE_TIMEOUT);
			}
		}
	}

	@Async
	public void setChatInIdleTimeout(String sessionid, ClientApp app, SessionBoundEvent outboundEvent) {
		if (app != null && (app.isAgentApp() || app.isCustomApp())) {
			boolean timeoutEnabledApp = app.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT)
					.asBoolean(false);
			if (timeoutEnabledApp) {
				MapPathEntry frwrdQueue = app.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE);
				if (frwrdQueue.not(app.getQueue())) {
					debouncEvent(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL, sessionid, outboundEvent,
							SessionEventTimer.CHAT_IN_IDLE_TIMEOUT);
				}
			} else if (app.isAgentApp()) {
				boolean timeoutEnabled = pmEnvironment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT)
						.asBoolean(false);
				PMConfigurationObject frwrdQueue = pmEnvironment
						.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_QUEUE);

				if (timeoutEnabled && frwrdQueue.not(app.getQueue())) {
					debouncEvent(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL, sessionid, outboundEvent,
							SessionEventTimer.CHAT_IN_IDLE_TIMEOUT);
				}
			}
		}
	}

	@Async
	public void setChatStatusTimeout(String sessionid, ClientApp app, SessionBoundEvent inBoundEvent) {
		if (app != null && (app.isCustomApp())) {
			if (pmEnvironment.featureEntry(CONFIG_FEATURES_KEY.EVENTS_TIMEOUT).asBoolean()) {
				// Only if this feature is there use chrono servre to set timeouts
				commonServiceClient.publishSessionBoundEvent(inBoundEvent);
			}
		}
	}

	@Async
	public void setChatOnRoute(String sessionid, ClientApp app) {
		setMitelRoutingCheck(sessionid, app);
	}

	public void setMitelRoutingCheck(String sessionid, ClientApp app) {
		if (app != null && app.equals(APP_TYPE.MITEL)) {
			TunnelTask task = new TunnelTask().name(SessionEventTimer.MITEL_ROUTER).id(sessionid).intervalSeconds(1L);
			task.data().put("sessionId", sessionid).put("queue", app.getQueue());
			this.debounce(task);
		}
	}

	private void setMitelClosingCheck(String sessionid, ClientApp app, long closeCheckTime, int counter, boolean now) {
		if (closeCheckTime > 0L || now) {
			TunnelTask closeTask = new TunnelTask().name(SessionEventTimer.MITEL_CLOSE_CHECK).id(sessionid)
					.intervalSeconds(now ? 0 : closeCheckTime);
			closeTask.data().put("sessionId", sessionid).put("queue", app.getQueue()).put("counter", counter);
			this.debounce(closeTask);
		}
	}

	public void setMitelClosingCheck(String sessionid, ClientApp app, boolean now) {
		if (app != null && app.equals(APP_TYPE.MITEL)) {
			long closeCheckTime = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.MITEL_SYNC_TIMER).asLong(0L);
			setMitelClosingCheck(sessionid, app, closeCheckTime, 0, now);
		}
	}

	public void setChatViewIdleTimeout(ChatSessionDoc sessionDoc, boolean now) {
		ClientApp app = messageContext.clientApp(sessionDoc.getAssignedToQueue());
		this.setMitelClosingCheck(sessionDoc.getSessionId(), app, now);
	}

	@Override
	public void doTaskSafely(TunnelTask task) {

		switch (task.getName()) {
		case MITEL_ROUTER:
			doMitelRouting(task);
			break;
		case MITEL_CLOSE_CHECK:
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
			long timeout = pmEnvironment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_OUT_IDLE_TIMEOUT_INTERVAL)
					.asLong(0L);
			if (ArgUtil.is(lastMsg) && PostManUtil.isInBound(lastMsg.getType()) // last message is also inbound
					&& (!ArgUtil.is(lastOutBoundMsg) // And ther is no outbound
							|| TimeUtils.isExpired(lastOutBoundMsg.getTimestamp(), timeout * 60000) // OR is older than
																									// interval
					)) {

				logManager.event(session, EVENTS.ON_SESSION_IDLE, session.getMode());
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

			ClientApp clientApp = pmEnvironment.config().clientApiKey(session.getAssignedToQueue());
			long timeout = clientApp.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL).asLong(
					pmEnvironment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_IN_IDLE_TIMEOUT_INTERVAL).asLong(0L));
			if (ArgUtil.is(lastMsg) && PostManUtil.isOutBound(lastMsg.getType()) // last message is also outbound
					&& (!ArgUtil.is(lastInBoundMsg) // And ther is no inbound
							|| TimeUtils.isExpired(lastInBoundMsg.getTimestamp(), timeout * 60000) // OR is older than
																									// interval
					)) {
				logManager.event(session, EVENTS.ON_SESSION_IDLE, "CUSTOMER");
				if (chatSessionEvents != null) {
					chatSessionEvents.onSessionIdleInBound(session);
				}
			}
		}

	}

	private void doMitelClosing(TunnelTask task) {
		MapModel data = task.data();
		int counter = data.getInteger("counter", 0);
		ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));
		ClientApp defaultClient = messageContext.clientApp(data.getString("queue", session.getAssignedToQueue()));

		MapModel meta = new MapModel(session.getMeta());
		MapPathEntry omidEntry = meta.pathEntry("mitel.omid");
		if (omidEntry.exists()) {
			String omid = omidEntry.asString();
			MapModel mitel = mitelClient.openMediaGetActive(defaultClient, session.contact(), session.getSessionId(),
					omid);

			if (!ArgUtil.is(mitel) || (mitel.keyEntry("id").exists()
					&& mitel.keyEntry("conversationState").in("Ended", "Abandoned"))) {
				chatSessionService.closeSession(session);
			} else if (counter < 5) {
				long closeCheckTime = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.MITEL_SYNC_TIMER).asLong(0L);
				this.setMitelClosingCheck(session.getSessionId(), defaultClient, closeCheckTime * 2, counter++, false);
			}
		}

	}

	private void doMitelRouting(TunnelTask task) {

		MapModel data = task.data();
		ChatSessionDoc session = sessionStore.getSession(data.getString("sessionId"));

		if (!sessionStore.isSessionValid(session)) {
			return;
		}

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
			q.set("meta.mitel.omid", newomid).set("meta.mitel.queueId", mitel.getString("queueId"));
			String[] mitelKeys = { "queueName", "queueId", "agentName", "agentId", "conversationState" };

			for (String mitelKey : mitelKeys) {
				String mitelValue = mitel.getString(mitelKey);
				q.set("meta.mitel." + mitelKey, mitelValue);
			}

			sessionStore.updateFirst(q);
		}

	}

}
