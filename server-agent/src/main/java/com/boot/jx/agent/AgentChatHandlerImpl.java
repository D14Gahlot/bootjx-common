package com.boot.jx.agent;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class AgentChatHandlerImpl implements AgentChatHandler {

	public static final Logger LOGGER = LoggerService.getLogger(AgentChatHandlerImpl.class);

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionManager chatSessionManager;

	@Autowired
	private ChatLogger logManager;

	@Autowired
	private ChatService chatService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private ChatArchiveService chatArchive;

	@Autowired
	private ChatArchiveBuilder chatArchiveBuilder;

	@Autowired
	private AgentStore agentStore;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private PMClientConfig chatClientConfig;

	private AgentSessionDoc getAgentSessonAssigned(PMArgs inboxMessage) {

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

	public AgentSessionDoc getAgentSessonAssignedAndActive(PMArgs inboxMessage) {
		AgentSessionDoc avaialbleAgent = getAgentSessonAssigned(inboxMessage);
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

	@Override
	public PMArgs doAssign(ChatSessionDoc chatSessionDoc, PMArgs params) {

		if (!ArgUtil.is(chatSessionDoc.getAssignedToQueue())) {
			chatSessionManager.assignToQueue(chatSessionDoc,
					environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_AGENT_QUEUE)
							.asString(PMConstants.DEFAULT.AGENT_QUEUE_CODE));
		}

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

	/**
	 * 
	 * Assigned by DeptCode and AgentCode & public event also if session is already
	 * assigned to same agent, if yes then no action
	 * 
	 * @param chatSessionDoc
	 * @param agentDept
	 * @param agentCode
	 */
	public void onAssign(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {
		if (!ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)
				|| !ArgUtil.areEqual(chatSessionDoc.getAssignedToDept(), agentDept)) {

			this.doAssign(chatSessionDoc, new PMArgs().assignToDeptCode(agentDept).assignToAgentCode(agentCode));

// 	    chatSessionService.assignSessionToAgent(chatSessionDoc);
//	    MessageDoc messageDoc = logManager.event(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_AGENT, agentCode,
//		    agentDept);
//	    stompTunnelService.sendToAll(PostManUtil.ON_DEPT_ASSIGN_TOPIC(agentDept),
//		    chatArchiveBuilder.sessionDTO().from(chatSessionDoc).withContact()
//			    .isAssigned(chatSessionDoc.getAssignedToAgent()).addMessage(messageDoc).get());
		}
	}

	/**
	 * Assigned by-Agent to-Self
	 * 
	 * @param selfAgent
	 * @param chatSessionDoc
	 */
	public void onAssign(AgentSessionDoc avaialbleAgent, ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.is(avaialbleAgent)) {
			this.onAssign(chatSessionDoc, avaialbleAgent.getAgentDept(), avaialbleAgent.getAgentCode());
		}
	}

	/**
	 * Assigned by-Agent to-Agent
	 * 
	 * @param agentDoc
	 * @param chatSessionDoc
	 */
	public void onAssign(AgentDoc agentDoc, ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.is(agentDoc)) {
			String deptCode = agentStore.findDepartmentCodeById(agentDoc.getDept_id());
			onAssign(chatSessionDoc, deptCode, agentDoc.getAgent_code());
		}
	}

	public ChatMessageDTO exitAgentMode(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		chatSessionService.resolveSession(chatSessionDoc);
		MessageDoc messageDoc = null;
		if (ArgUtil.is(outboxMessage)) {
			messageDoc = chatService.send(chatSessionDoc, outboxMessage);
		}
		return chatArchive.getMessage(messageDoc, chatSessionDoc);
	}

	public ChatSessionDTO updateChatSessionStatus(String sessionId, PMConstants.CHAT_STATUS status) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		if (chatSessionService.updateSessionStatus(sessionDoc, status).exists()) {
			ChatSessionDTO dto = chatArchive.getChatSession(sessionDoc);
			stompTunnelService.sendToTag(sessionDoc.getAssignedToDept(), "/chat/session/update", dto);
			return dto;
		}
		return chatArchive.getChatSession(sessionDoc);
	}

	@Override
	public InboxMessage onMessageReceive(InboxMessage inboxMessage) {
		MessageDoc messageDoc = messageStore.findOrCreateMessageDoc(inboxMessage);
		ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
		messageDto.setName(inboxMessage.getFromName());
		stompTunnelService.sendToTag(inboxMessage.session().getDept(), "/message/receive/new", messageDto);
		if (ArgUtil.is(inboxMessage.getMessage()) && inboxMessage.getMessage().equalsIgnoreCase("/exit_chat")) {
			ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
			exitAgentMode(chatSessionDoc, null);
			logManager.event(inboxMessage, MessageStore.EVENTS.UNASGND);
		}
		return inboxMessage;
	}

	public ChatMessageDTO onSend(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		outboxMessage.session().setDept(agentSession.getAgentDept());
		outboxMessage.session().setAgent(agentSession.getAgentCode());

		String action = ChatCommands.getCommand(outboxMessage);
		if (ArgUtil.is(action)) {
			outboxMessage.setAction(action);
			switch (action) {
			case CHAT_SESSION_ACTIONS.RESOLVE:
				return this.exitAgentMode(sessionDoc, outboxMessage);
			case CHAT_SESSION_ACTIONS.ADD_STICKY_NOTE:
				return this.addStickyNote(sessionDoc, outboxMessage);
			default:
				break;
			}
		} else {
			sessionStore.updateResponseTime(sessionDoc);
			MessageDoc messageDoc = chatService.send(sessionDoc, outboxMessage);
			return chatArchive.getMessage(messageDoc, sessionDoc);
		}
		return new ChatMessageDTO();
	}

	public ChatMessageDTO addStickyNote(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		MessageDoc messageDoc = logManager.note(chatSessionDoc, outboxMessage);
		ChatMessageDTO messageDto = chatArchive.getMessage(messageDoc, chatSessionDoc);
		stompTunnelService.sendToTag(chatSessionDoc.getAssignedToDept(), "/message/sent/new", messageDto);
		return messageDto;
	}

}
