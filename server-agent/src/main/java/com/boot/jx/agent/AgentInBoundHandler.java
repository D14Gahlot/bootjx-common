package com.boot.jx.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.DefaultChatBoundHandler;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.inbound.InBound.SessionAssginHandler;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.manager.LogManager;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMParams;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class AgentInBoundHandler extends DefaultChatBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInBoundHandler.class);

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    public PMDomainConfig pmDomainConfig;

    @Autowired
    public PMCommonConfig pmCommonConfig;

    @Autowired
    private AgentChatHandler agentChatHandler;

    @Autowired(required = false)
    private ChatService chatService;

    // @Autowired
    protected SessionAssginHandler sessionAssginHandler;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private LogManager logManager;

    @Autowired
    private StompTunnelService stompTunnelService;

    @Autowired
    private ChatArchiveBuilder chatArchiveBuilder;

    @Autowired
    private PMEnvironment environment;

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private AgentStore agentStore;

    @Autowired
    private DocumentUpdateListner documentUpdateListner;

    /**
     * EVENTS
     */

    @Override
    public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
	if (ArgUtil.isEmpty(inboxMessage.session().getMode()) && ArgUtil.isEmpty(inboxMessage.session().getQueue())) {
	    if (!ArgUtil.is(session)) {
		session = sessionStore.getSession(inboxMessage.getSessionId());
	    }
	    // InBoundEvent assignEvent = assignSessionToAgent(session, null, null).value();
	}
	agentChatHandler.onMessageReceive(inboxMessage);
    }

    private void onAssign(ChatSessionDoc session, InBoundEvent assignEvent) {
	try {

	    if (!ArgUtil.is(assignEvent.sessionAssigned().oldAgent)) {
		if (ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {
		    PMConfigurationObject transferReply = pmEnvironment
			    .keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
		    if (transferReply.exists()) {
			chatService.reply(session, new OutboxMessage().template(transferReply.asString()));
		    } else {
			chatService.reply(session, new OutboxMessage()
				.message("Connecting you to one of our customer representatives. Give us a moment."));
		    }
		} else {
		    PMConfigurationObject noAgentReply = pmEnvironment
			    .keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT);
		    if (noAgentReply.exists()) {
			chatService.reply(session, new OutboxMessage().template(noAgentReply.asString()));
		    } else {
			chatService.reply(session, new OutboxMessage().message(
				"All agents are busy or online, we will connect you whenever someone is available."));
		    }
		}

	    }

	} catch (Exception e) {
	    LOGGER.error("Error ONE while Connecting to Agent", e);
	    try {
		chatService.reply(session, new OutboxMessage().message(
			"We are having some issues trying connect you to one of our customer representatives. Please be patient"));
	    } catch (InterruptedException e1) {
		LOGGER.error("Error TWO  while Sending Failure", e1);
	    }
	}
    }

    @Override
    public void doHandle(MessageReport messageReport) {
	LOGGER.debug("No Handling Required for Status on AgentSide");
    }

    @Override
    public void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc) {
	super.onSessionRoute(inBoundEvent, sessionDoc);
    }

    /**
     * METHODS/ACTION
     */

    @Override
    public NodeEntry<InBoundEvent> assignSessionToAgent(PMParams params) {
	ChatSessionDoc chatSessionDoc = sessionStore.getSession(params.getSessionId());
	return assignSessionToAgent(chatSessionDoc, params);
    }

    @Override
    public NodeEntry<InBoundEvent> assignSessionToAgent(ChatSessionDoc session, String deptCode, String agentCode) {
	return assignSessionToAgent(session, new PMParams().sessionId(session.getSessionId()).contact(session.contact())
		.assignToDeptCode(deptCode).assignToAgentCode(agentCode));
    }

    private AgentSessionDoc getAgentSessonAssigned(PMParams inboxMessage) {

	String stickyLogic = environment.local().keyEntry("postman.agent.chat.stickysession")
		.asString(PMConstants.CHAT_SESSION_STICKY.NONE);
	long timeThen = System.currentTimeMillis() - chatClientConfig.getAgentSessionTimeout().toMillis();

	String lastAgent = null;
	LOGGER.debug("CHAT_SESSION_STICKY : {}", stickyLogic);
	if (!PMConstants.CHAT_SESSION_STICKY.NONE.equals(stickyLogic)) {
	    lastAgent = sessionStore.getLastAssignedAgent(inboxMessage.contact());
	    if (ArgUtil.is(lastAgent)) {
		LOGGER.debug("CHAT_SESSION_STICKY : lastAgent found {}", lastAgent);
		AgentSessionDoc agent = sessionStore.findById(lastAgent, AgentSessionDoc.class);
		if (ArgUtil.is(agent)) {
		    LOGGER.debug("CHAT_SESSION_STICKY : has session {}", agent);
		    if (PMConstants.CHAT_SESSION_STICKY.STRICT.equals(stickyLogic)) {
			LOGGER.debug("CHAT_SESSION_STICKY : because its strictly {}", agent);
			return agent;
		    }
		    if (PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE.equals(stickyLogic)) {
			if (ArgUtil.nullAsFalse(agent.getIsOnline()) && ArgUtil.nullAsFalse(agent.getIsLoggedIn())
				&& (agent.getLastOnlineStamp() > timeThen)) {
			    LOGGER.debug("CHAT_SESSION_STICKY : because its availanle {}", agent);
			    return agent;
			}
		    }
		}

	    }
	}

	String assignmentRule = environment.local().keyEntry("postman.agent.chat.assignment")
		.asString(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN);

	String assignedDept = ArgUtil.nonEmpty(inboxMessage.getAssignToDeptCode(),
		environment.local().agent().getDefaultTeamCode(), DEFAULT.NO_DEPT);
	inboxMessage.setAssignToDeptCode(assignedDept);

	LOGGER.debug("ASSIGNMENT_RULE : No Assignment {} {}", assignmentRule, assignedDept);

	if (PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT.equals(assignmentRule)) {
	    String defAgentCode = environment.local().agent().defaultAgent(assignedDept);
	    AgentSessionDoc agent = sessionStore.findById(defAgentCode, AgentSessionDoc.class);
	    LOGGER.debug("ASSIGNMENT_RULE : Default {} : {}", defAgentCode, agent);
	    return agent;
	}

	if (PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN.equals(assignmentRule)) {
	    Query query = new Query();
	    Criteria c = Criteria.where("isOnline").is(true).and("isLoggedIn").is(true).and("lastOnlineStamp")
		    .gt(timeThen).and("isEnabled").is(true);
	    if (ArgUtil.is(inboxMessage.getAssignToDeptCode())) {
		c.and("agentDept").is(assignedDept);
	    }
	    query.addCriteria(c).with(new Sort(Direction.ASC, "lastAssignStamp")).limit(1);

	    List<AgentSessionDoc> agents = sessionStore.find(query, AgentSessionDoc.class);

	    AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

	    String defAgentCode = environment.local().agent().defaultAgent(inboxMessage.getAssignToDeptCode());
	    if (ArgUtil.is(defAgentCode)) {
		for (AgentSessionDoc agentSessionDoc : agents) {
		    if (defAgentCode.equals(agentSessionDoc.getAgentCode())) {
			avaialbleAgent = agentSessionDoc;
			break;
		    }
		}
	    }
	    return avaialbleAgent;
	}

	return null;
    }

    public AgentSessionDoc getAgentSessonAssignedAndActive(PMParams inboxMessage) {
	AgentSessionDoc avaialbleAgent = this.getAgentSessonAssigned(inboxMessage);
	if (ArgUtil.is(avaialbleAgent)) {
	    AgentDoc agent = agentStore.findByCode(avaialbleAgent.getAgentCode());
	    if (agent.getIsEnabled()) {
		return avaialbleAgent;
	    }
	}
	return null;
    }

    public void assignToAgent(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {
	sessionStore.assignToAgent(chatSessionDoc, agentDept, agentCode);
	if (ArgUtil.is(agentCode)) {
	    CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentCode);
	    builder.set("lastAssignStamp", System.currentTimeMillis());
	    sessionStore.upsert(builder.getQuery(), builder.getUpdate(), AgentSessionDoc.class);
	    documentUpdateListner.onAgentSessionUpdate(agentCode);
	}
    }

    public PMParams doAssign(ChatSessionDoc chatSessionDoc, PMParams params) {

	if (ArgUtil.is(params.getAssignToDeptCode()) && ArgUtil.is(params.getAssignToAgentCode())) {
	    assignToAgent(chatSessionDoc, params.getAssignToDeptCode(), params.getAssignToAgentCode());
	    logManager.event(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_AGENT, params.getAssignToDeptCode(),
		    params.getAssignToAgentCode());
	} else {
	    AgentSessionDoc avaialbleAgent = getAgentSessonAssignedAndActive(params);

	    if (ArgUtil.is(avaialbleAgent)) {
		assignToAgent(chatSessionDoc, avaialbleAgent.getAgentDept(), avaialbleAgent.getAgentCode());
		logManager.event(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_AGENT, avaialbleAgent.getAgentCode(),
			avaialbleAgent.getAgentDept());

		params.setAssignToAgentCode(avaialbleAgent.getAgentCode());
		params.setAssignToDeptCode(avaialbleAgent.getAgentDept());
	    } else {
		assignToAgent(chatSessionDoc, params.getAssignToDeptCode(), null);
		logManager.event(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_DEPT, params.getAssignToDeptCode());
	    }

	}

	stompTunnelService.sendToAll(PostManUtil.ON_DEPT_ASSIGN_TOPIC(params.getAssignToDeptCode()), chatArchiveBuilder
		.sessionDTO().from(chatSessionDoc).withContact().isAssigned(params.getAssignToAgentCode()).get());

	return params;
    }

    public NodeEntry<InBoundEvent> assignSessionToAgent(ChatSessionDoc session, PMParams params) {
	NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();
	InBoundEvent agentAssignEvent = new InBoundEvent();
	agentAssignEvent.eventCode = InBoundEvent.SESSION_ASSIGNED;
	agentAssignEvent.sessionAssigned().oldAgent = session.getAssignedToAgent();
	agentAssignEvent.sessionAssigned().oldDept = session.getAssignedToDept();
	params = doAssign(session, params);
	if (ArgUtil.is(params)) {
	    agentAssignEvent.sessionAssigned().newDept = params.getAssignToDeptCode();
	    agentAssignEvent.sessionAssigned().newAgent = params.getAssignToAgentCode();
	}
	onAssign(session, agentAssignEvent);
	return eventEntry.value(agentAssignEvent);
    }


}
