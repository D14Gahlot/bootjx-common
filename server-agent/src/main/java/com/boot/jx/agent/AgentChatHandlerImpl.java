package com.boot.jx.agent;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.bind;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.MongoUtils;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompQuery;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
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

	@Autowired
	MessageContext messageContext;

	@Autowired
	TmplClient tmplClient;

	private AgentSessionDoc getAgentSessonAssigned(PMArgs params) {

		String stickyLogic = environment.local().keyEntry("postman.agent.chat.stickysession")
				.asString(PMConstants.CHAT_SESSION_STICKY.NONE);
		long timeThen = System.currentTimeMillis() - chatClientConfig.getAgentSessionTimeout().toMillis();

		// Relationship Manager Agent Sticky Logic
		String rmStickyLogic = environment.local().keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_STICKY_RMAGENT)
				.asString(PMConstants.CHAT_SESSION_STICKY.NONE);
		String rmAgent = null;

		LOGGER.debug("CHAT_RM_STICKY : {}", rmStickyLogic);
		if (!PMConstants.CHAT_SESSION_STICKY.NONE.equals(rmStickyLogic)) {
			ChatContactDoc c = sessionStore.getContact(params.contact());
			if (ArgUtil.is(c) && ArgUtil.is(c.profile().getId())) {
				CustomerProfileDoc profile = sessionStore.findById(c.profile().getId(), CustomerProfileDoc.class);
				if (profile != null && ArgUtil.is(profile.rmCode)) {
					rmAgent = profile.rmCode;
				}
			}
			if (ArgUtil.is(rmAgent)) {
				LOGGER.debug("CHAT_RM_STICKY : lastAgent found {}", rmAgent);
				AgentSessionDoc agent = sessionStore.findById(rmAgent, AgentSessionDoc.class);
				if (ArgUtil.is(agent)) {
					LOGGER.debug("CHAT_RM_STICKY : has session {}", agent);
					if (PMConstants.CHAT_SESSION_STICKY.STRICT.equals(rmStickyLogic)) {
						LOGGER.debug("CHAT_SESSION_STICKY : because its strictly {}", agent);
						return agent;
					}
					if (PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE.equals(rmStickyLogic)) {
						if (ArgUtil.nullAsFalse(agent.getIsOnline()) && ArgUtil.nullAsFalse(agent.getIsLoggedIn())
								&& (agent.getLastOnlineStamp() > timeThen)) {
							LOGGER.debug("CHAT_RM_STICKY : because its availanle {}", agent);
							return agent;
						}
					}
				}

			}

		}

		// Last Session Agent Sticky Logic
		String lastStickyLogic = environment.local()
				.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_STICKYSESSION)
				.asString(PMConstants.CHAT_SESSION_STICKY.NONE);
		String lastAgent = null;

		LOGGER.debug("CHAT_SESSION_STICKY : {}", lastStickyLogic);
		if (!PMConstants.CHAT_SESSION_STICKY.NONE.equals(lastStickyLogic)) {
			lastAgent = sessionStore.getLastAssignedAgent(params.contact());
			if (ArgUtil.is(lastAgent)) {
				LOGGER.debug("CHAT_SESSION_STICKY : lastAgent found {}", lastAgent);
				AgentSessionDoc agent = sessionStore.findById(lastAgent, AgentSessionDoc.class);
				if (ArgUtil.is(agent)) {
					LOGGER.debug("CHAT_SESSION_STICKY : has session {}", agent);
					if (PMConstants.CHAT_SESSION_STICKY.STRICT.equals(lastStickyLogic)) {
						LOGGER.debug("CHAT_SESSION_STICKY : because its strictly {}", agent);
						return agent;
					}
					if (PMConstants.CHAT_SESSION_STICKY.ONAVAILABLE.equals(lastStickyLogic)) {
						if (ArgUtil.nullAsFalse(agent.getIsOnline()) && ArgUtil.nullAsFalse(agent.getIsLoggedIn())
								&& (agent.getLastOnlineStamp() > timeThen)) {
							LOGGER.debug("CHAT_SESSION_STICKY : because its availanle {}", agent);
							return agent;
						}
					}
				}

			}
		}

		String assignmentRule = environment.local().keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_ASSIGNMENT)
				.asString(PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN);

		String assignedDept = ArgUtil.nonEmpty(params.getAssignToDeptCode(),
				environment.local().agent().getDefaultTeamCode(), DEFAULT.NO_DEPT);
		params.setAssignToDeptCode(assignedDept);

		LOGGER.debug("ASSIGNMENT_RULE : No Assignment {} {}", assignmentRule, assignedDept);

		if (PMConstants.ASSIGNMENT_RULE.STRICT_DEFAULT.equals(assignmentRule)) {
			String defAgentCode = environment.local().agent().defaultAgent(assignedDept);
			AgentSessionDoc agent = sessionStore.findById(defAgentCode, AgentSessionDoc.class);
			LOGGER.debug("ASSIGNMENT_RULE : Default {} : {}", defAgentCode, agent);
			return agent;
		}

		if (PMConstants.ASSIGNMENT_RULE.ROUND_ROBIN.equals(assignmentRule)) {

			Criteria onlineActiveAgents = Criteria.where("isOnline").is(true).and("isLoggedIn").is(true)
					.and("lastOnlineStamp").gt(timeThen).and("isEnabled").is(true);

			if (ArgUtil.is(params.getAssignToDeptCode())) {
				onlineActiveAgents.and("agentDept").is(assignedDept);
			}

			if (ArgUtil.is(params.getAssignToSkillCodes())) {

				List<Document> agg = MongoUtils.newAggregation(//
						match(Criteria.where("profile.quickskills.code").in(params.getAssignToSkillCodes())) //
						, project(bind("quickskills", "profile.quickskills.code").and("lastAssignStamp")
								.and("lastOnlineStamp").and("tags", "1"))//
						, unwind("quickskills")//
						, match(Criteria.where("quickskills").in(params.getAssignToSkillCodes())) //
						, group("_id").count().as("noOfMatches")//
								.first("lastAssignStamp").as("lastAssignStamp")//
								.first("lastOnlineStamp").as("lastOnlineStamp")//
						, sort(Direction.DESC, "noOfMatches").and(Direction.ASC, "lastAssignStamp")
				//
				);

				List<Document> luckyAgents = new ArrayList<Document>();
				sessionStore.collection("AGENT_SESSION").aggregate(agg).forEach(doc -> luckyAgents.add(doc));
				Document luckyAgent = CollectionUtil.getOne(luckyAgents);
				if (ArgUtil.is(luckyAgent)) {
					AgentSessionDoc avaialbleAgent = sessionStore.findByIdSafeCheck(luckyAgent.get("_id"),
							AgentSessionDoc.class);
					if (ArgUtil.is(avaialbleAgent)) {
						return avaialbleAgent;
					}
				}

			}

			{ // Default Team based Round Robin
				Query query = new Query();
				query.addCriteria(onlineActiveAgents).with(new Sort(Direction.ASC, "lastAssignStamp")).limit(1);
				List<AgentSessionDoc> agents = sessionStore.find(query, AgentSessionDoc.class);

				AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

				String defAgentCode = environment.local().agent().defaultAgent(params.getAssignToDeptCode());
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
			MongoQueryBuilder<AgentSessionDoc> builder = MongoQueryBuilder.collection(AgentSessionDoc.class)
					.whereId(agentCode);
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
			String agentCode = null;
			String agentDept = null;

			AgentSessionDoc avaialbleAgent = getAgentSessonAssignedAndActive(params);
			if (avaialbleAgent != null) {
				agentDept = avaialbleAgent.getAgentDept();
				agentCode = avaialbleAgent.getAgentCode();
			}

			if (ArgUtil.is(agentCode)) {
				assignToAgent(chatSessionDoc, agentDept, agentCode);
				logManager.event(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_AGENT, agentCode, agentDept);
				params.setAssignToAgentCode(agentCode);
				params.setAssignToDeptCode(agentDept);
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

		String agentCodeOld = chatSessionDoc.getAssignedToAgent();
		String agentDeptOld = chatSessionDoc.getAssignedToDept();

		if (ArgUtil.is(agentCodeOld) && !agentSession.isAdmin()) {
			boolean canPickAssigned = environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_PICK_ASSIGNED)
					.asBoolean(true);
			if (!canPickAssigned) {
				ApiResponseUtil.throwAccessDeniedException("Not Allowed, Contact Admin");
			}
		}

		if (!ArgUtil.areEqual(agentCodeOld, agentCode) || !ArgUtil.areEqual(agentDeptOld, agentDept)) {

			this.doAssign(chatSessionDoc, new PMArgs().contact(chatSessionDoc.contact()).assignToDeptCode(agentDept)
					.assignToAgentCode(agentCode));

			if (ArgUtil.is(agentCodeOld)) {
				ClientApp app = messageContext.clientApp(chatSessionDoc.getAssignedToQueue(), chatSessionDoc.contact());
				MapModel props = MapModel.from(app.props());
				MapPathEntry templ = props.keyEntry("agent_transfer");
				if (templ.exists()) {
					doSend(chatSessionDoc, new OutboxMessage().template(templ.asString()));
				}
			}

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
			messageDoc = doSend(chatSessionDoc, outboxMessage);
		}
		return chatArchive.getMessage(messageDoc, chatSessionDoc);
	}

	public ChatSessionDTO updateChatSessionStatus(String sessionId, PMConstants.CHAT_STATUS status) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		if (chatSessionService.updateSessionStatus(sessionDoc, status).exists()) {
			ChatSessionDTO dto = chatArchive.getChatSession(sessionDoc);
			stompTunnelService.sendToTag(sessionDoc.getAssignedToDept(), "/chat/session/update", dto);
			stompTunnelService.sendTo(StompQuery.toAll("/chat/session/delta").toSameOriginApp(),
					MapModel.createInstance().put("sessionId", sessionDoc.getSessionId()).put("event", "status_update")
							.toMap());
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
		stompTunnelService.sendTo(StompQuery.toAll("/chat/session/delta").toSameOriginApp(), MapModel.createInstance()
				.put("sessionId", messageDoc.getSessionId()).put("event", "new_message").toMap());
		if ("/exit_chat".equalsIgnoreCase(inboxMessage.toReplyEnum())) {
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
			PMConfigurationObject header = environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_HEADER);
			if (header.exists() && !ArgUtil.is(outboxMessage.getSubject())) {
				outboxMessage.setSubject(tmplClient.process(header.asString(), outboxMessage.session()));
			}
			sessionStore.updateResponseTime(sessionDoc);
			MessageDoc messageDoc = doSend(sessionDoc, outboxMessage);
			return chatArchive.getMessage(messageDoc, sessionDoc);
		}
		return new ChatMessageDTO();
	}

	private void beforeSend(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.is(outboxMessage.hsm().getCode())) {
			AgentResponseAuthDto p = ArgUtil.is(agentSession) ? agentSession.getProfile() : null;
			if (ArgUtil.is(p)
					&& ArgUtil.is(agentSession.getProfile().getAgent_code(), chatSessionDoc.getAssignedToAgent())) {
				OutboxMessage.AGENT_NAME.save(outboxMessage.model(), agentSession.getProfile().getAgent_name());
				OutboxMessage.AGENT_CODE.save(outboxMessage.model(), agentSession.getProfile().getAgent_code());
				OutboxMessage.TEAM_NAME.save(outboxMessage.model(), agentSession.getProfile().getDept().getDept_name());
				OutboxMessage.TEAM_CODE.save(outboxMessage.model(), agentSession.getProfile().getDept().getDept_code());
			} else {
				if (ArgUtil.is(chatSessionDoc.getAssignedToAgent())) {
					AgentDoc agent = agentStore.findByCode(chatSessionDoc.getAssignedToAgent());
					if (ArgUtil.is(agent)) {
						OutboxMessage.AGENT_NAME.save(outboxMessage.model(), agent.getAgent_name());
						OutboxMessage.AGENT_CODE.save(outboxMessage.model(), agent.getAgent_code());
					}
				}
				if (ArgUtil.is(chatSessionDoc.getAssignedToDept())) {
					DepartmentDoc deptDoc = agentStore.findDepartmentByCode(chatSessionDoc.getAssignedToDept());
					if (ArgUtil.is(deptDoc)) {
						OutboxMessage.TEAM_NAME.save(outboxMessage.model(), deptDoc.getDept_name());
						OutboxMessage.TEAM_CODE.save(outboxMessage.model(), deptDoc.getDept_code());
					}
				}
			}
		}
	}

	@Override
	public MessageDoc doSend(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		beforeSend(chatSessionDoc, outboxMessage);
		return chatService.send(chatSessionDoc, outboxMessage);
	}

	@Override
	public MessageDoc doReply(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) throws InterruptedException {
		beforeSend(chatSessionDoc, outboxMessage);
		return chatService.reply(chatSessionDoc, outboxMessage);
	}

	public ChatMessageDTO addStickyNote(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		MessageDoc messageDoc = logManager.note(chatSessionDoc, outboxMessage);
		ChatMessageDTO messageDto = chatArchive.getMessage(messageDoc, chatSessionDoc);
		stompTunnelService.sendToTag(chatSessionDoc.getAssignedToDept(), "/message/sent/new", messageDto);
		return messageDto;
	}

}
