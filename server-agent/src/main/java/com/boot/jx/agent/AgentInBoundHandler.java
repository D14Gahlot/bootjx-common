package com.boot.jx.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.DefaultChatBoundHandler;
import com.boot.jx.common.service.SessionEventTimer;
import com.boot.jx.inbound.InBound.SessionAssginHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapEntry;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;

@Component
public class AgentInBoundHandler extends DefaultChatBoundHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentInBoundHandler.class);

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private SessionEventTimer sessionEventTimer;

	@Autowired
	private AgentChatHandler agentChatHandler;

	@Autowired(required = false)
	private ChatService chatService;

	// @Autowired
	protected SessionAssginHandler sessionAssginHandler;

	@Autowired
	private SessionStore sessionStore;

	/**
	 * EVENTS
	 */
	@Override
	public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
		ClientApp defaultClient = context().clientApp(inboxMessage.session().getQueue(), inboxMessage.contact());

		if (ArgUtil.isEmpty(inboxMessage.session().getMode()) && ArgUtil.isEmpty(inboxMessage.session().getQueue())) {
			if (!ArgUtil.is(session)) {
				session = sessionStore.getSession(inboxMessage.getSessionId());
			}
			// InBoundEvent assignEvent = assignSessionToAgent(session, null, null).value();
		}
		agentChatHandler.onMessageReceive(inboxMessage);

		if (ArgUtil.is(defaultClient)) {
			APP_TYPE appType = APP_TYPE.from(defaultClient.getAppType());
			if (ArgUtil.is(session) && APP_TYPE.MITEL.equals(appType)) {
				mitelRouting(session, defaultClient, 5);
			}
		}

	}

	private MapEntry getTemplate(MapModel props, String propKey, ConfigConstants.SETUP_KEY KEY) {
		MapEntry talk2agent = props.keyEntry(propKey);
		if (talk2agent.exists()) {
			return talk2agent;
		}
		return pmEnvironment.keyEntry(KEY);
	}

	private void onAssign(ChatSessionDoc session, InBoundEvent assignEvent) {
		OutboxMessage oMsg = new OutboxMessage();
		try {
			ClientApp app = this.context().clientApp();

			if (ArgUtil.not(app)) {
				logManager.addTrace(assignEvent, "NoQueueFound", session.contact());
				app = this.context().clientApp(PMConstants.DEFAULT.AGENT_QUEUE_CODE, session.contact());
			}

			MapModel props = MapModel.from(app.props());

			oMsg.route().setQueueCode(app.getQueue());
			oMsg.route().setSendMode(app.getAppMode());
			oMsg.route().setSenderApp(app.getAppType());
			oMsg.route().setSenderType(MESSAGE_SENDER_TYPE.SYSTEM);

			if (!ArgUtil.is(assignEvent.sessionAssigned().oldAgent)
					&& ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {

				MapEntry templ = getTemplate(props, "agent_connected",
						ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
				if (templ.exists()) {
					chatService.reply(session, oMsg.template(templ.asString()));
					return;
				}
			} else if (!ArgUtil.is(assignEvent.sessionAssigned().oldAgent)
					&& !ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {

				MapEntry templ = getTemplate(props, "agent_notfound",
						ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT);
				if (templ.exists()) {
					chatService.reply(session, oMsg.template(templ.asString()));
					return;
				}
			} else {

				MapEntry templ = props.keyEntry("agent_transfer");
				if (templ.exists()) {
					chatService.reply(session, oMsg.template(templ.asString()));
					return;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error ONE while Connecting to Agent", e);
			logManager.error(assignEvent, e);
			try {
				chatService.reply(session, oMsg.message(
						"We are having some issues trying connect you to one of our customer representatives. Please be patient"));
			} catch (InterruptedException e1) {
				LOGGER.error("Error TWO  while Sending Failure", e1);
				logManager.error(assignEvent, e1);
			}
		}
	}

	/**
	 * METHODS/ACTION
	 */
	@Override
	public NodeEntry<InBoundEvent> assignSessionToAgent(PMArgs params, ChatSessionDoc session) {
		NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();
		InBoundEvent agentAssignEvent = new InBoundEvent();
		agentAssignEvent.eventCode = InBoundEvent.SESSION_ASSIGNED;
		agentAssignEvent.sessionId = session.getSessionId();
		agentAssignEvent.sessionAssigned().oldAgent = session.getAssignedToAgent();
		agentAssignEvent.sessionAssigned().oldDept = session.getAssignedToDept();
		params = agentChatHandler.doAssign(session, params);
		if (ArgUtil.is(params)) {
			agentAssignEvent.sessionAssigned().newDept = params.getAssignToDeptCode();
			agentAssignEvent.sessionAssigned().newAgent = params.getAssignToAgentCode();
		}
		onAssign(session, agentAssignEvent);
		return eventEntry.value(agentAssignEvent);
	}

	@Override
	public CHAT_MODE mode() {
		return CHAT_MODE.AGENT;
	}

	@Override
	public void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		ClientApp targetAppQueue = context().clientApp(inBoundEvent.sessionRouted.targetQueue, null);
		MapModel props = new MapModel(targetAppQueue.props());
		AppContextUtil.setActorId(targetAppQueue.getQueue());
		assignSessionToAgent(new PMArgs()
				.assignToDeptCode(ArgUtil.nonEmpty(pmArgs.getAssignToDeptCode(), props.getString("deptCode")))
				.assignToAgentCode(ArgUtil.nonEmpty(pmArgs.getAssignToAgentCode(), props.getString("agentCode")))
				.assignToSkillCodes(pmArgs.getAssignToSkillCodes()), sessionDoc);
		APP_TYPE appType = APP_TYPE.from(targetAppQueue.getAppType());
		if (APP_TYPE.MITEL.equals(appType)) {
			try {
				mitelRouting(sessionDoc, targetAppQueue, 1);
			} catch (Exception e) {
				logManager.error(inBoundEvent, e);
			}
		}
	}

	public void mitelRouting(ChatSessionDoc session, ClientApp defaultClient, int delay) {
		MapModel meta = new MapModel(session.getMeta());
		MapPathEntry omidEntry = meta.pathEntry("mitel.omid");
		String omid = omidEntry.asString();
		TunnelTask task = new TunnelTask().name("MITEL_ROUTER").id(session.getSessionId()).intervalSeconds(delay);
		task.data().put("sessionId", session.getSessionId()).put("omid", omid).put("queue", defaultClient.getQueue());
		sessionEventTimer.debounce(task);
		// sessionRouter.doTask(task);

		TunnelTask closeTask = new TunnelTask().name("MITEL_CLOSE_CHECK").id(session.getSessionId())
				.intervalSeconds(60 * 10);
		closeTask.data().put("sessionId", session.getSessionId()).put("omid", omid).put("queue",
				defaultClient.getQueue());
		sessionEventTimer.debounce(closeTask);
		// sessionRouter.doTask(closeTask);
	}

}
