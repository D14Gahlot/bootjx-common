package com.boot.jx.agent;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatArchive;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClientConfig;
import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatDTOUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.PMStoreConstants;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.TimeUtils;

@Component
public class AgentChatHandlerImpl implements AgentChatHandler {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ChatClientConfig chatClientConfig;

	@Autowired
	private ChatService chatService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private ChatArchive chatArchive;

	@Autowired
	AgentStore agentStore;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@Override
	public boolean onAssignSupported(InboxMessage inboxMessage) {
		return true;
	}

	private AgentSessionDoc getAgentSessonAssigned(InboxMessage inboxMessage) {
		long timeThen = System.currentTimeMillis() - TimeUtils.toMillis(chatClientConfig.getAgentSessionTimeout());
		Query query = new Query();
		Criteria c = Criteria.where("isOnline").is(true).and("isLoggedIn").is(true).and("lastOnlineStamp").gt(timeThen);
		if (ArgUtil.is(inboxMessage.session().getDept())) {
			c.and("agentDept").is(inboxMessage.session().getDept());
		} else {
			inboxMessage.session().setDept(
					ArgUtil.nonEmpty(environment.config().agent().getDefaultTeamCode(), PMStoreConstants.NO_DEPT));
		}
		query.addCriteria(c).with(new Sort(Direction.ASC, "lastAssignStamp")).limit(1);

		List<AgentSessionDoc> agents = mongoTemplate.find(query, AgentSessionDoc.class);

		AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

		String defAgentCode = environment.config().agent().defaultAgent(inboxMessage.session().getDept());
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

	private void assignToAgent(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {
		sessionStore.assignToAgent(chatSessionDoc, agentDept, agentCode);
		if (ArgUtil.is(agentCode)) {
			CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentCode);
			builder.set("lastAssignStamp", System.currentTimeMillis());
			mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), AgentSessionDoc.class);
			documentUpdateListner.onAgentSessionUpdate(agentCode);
		}
	}

	@Override
	public InboxMessage onAssign(InboxMessage inboxMessage) {

		AgentSessionDoc avaialbleAgent = getAgentSessonAssigned(inboxMessage);

		// PUBLISH
		ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());

		if (ArgUtil.is(avaialbleAgent)) {

			assignToAgent(chatSessionDoc, avaialbleAgent.getAgentDept(), avaialbleAgent.getAgentCode());

			chatService.log(inboxMessage, MessageStore.EVENTS.ASGND_TO_AGENT, avaialbleAgent.getAgentCode(),
					avaialbleAgent.getAgentDept());

			inboxMessage.session().setAgent(avaialbleAgent.getAgentCode());
			inboxMessage.session().setDept(avaialbleAgent.getAgentDept());
		} else {
			assignToAgent(chatSessionDoc, inboxMessage.session().getDept(), null);
			chatService.log(inboxMessage, MessageStore.EVENTS.ASGND_TO_DEPT, inboxMessage.session().getDept());
		}

		stompTunnelService.sendToAll("/dept/onassign-" + inboxMessage.session().getDept(),
				chatArchive.getChatSessionDto(chatSessionDoc, inboxMessage.session().getAgent()));

		return inboxMessage;
	}

	public void onAssign(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {
		if (!ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)) {
			assignToAgent(chatSessionDoc, agentDept, agentCode);
			chatService.log(chatSessionDoc, agentSession.getAgentCode(), MessageStore.EVENTS.ASGND_TO_AGENT, agentCode,
					agentDept);
			stompTunnelService.sendToAll("/dept/onassign-" + agentDept,
					chatArchive.getChatSessionDto(chatSessionDoc, agentCode));
		}
	}

	public void onAssign(AgentSessionDoc avaialbleAgent, ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.is(avaialbleAgent)) {
			this.onAssign(chatSessionDoc, avaialbleAgent.getAgentDept(), avaialbleAgent.getAgentCode());
		}
	}

	public void onAssign(AgentDoc agentDoc, ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.is(agentDoc)) {
			String deptCode = agentStore.findDepartmentCodeById(agentDoc.getDept_id());
			this.onAssign(chatSessionDoc, deptCode, agentDoc.getAgent_code());
		}
	}

	public ChatMessageDTO exitAgentMode(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		MessageDoc messageDoc = null;

		if (!chatSessionDoc.isResolved()) {
			chatService.resolveSession(chatSessionDoc);
		}

		if (ArgUtil.is(outboxMessage)) {
			messageDoc = chatService.reply(chatSessionDoc, outboxMessage);
		}
		chatService.closeSession(chatSessionDoc);
		stompTunnelService.sendToAll("/dept/onassign-" + chatSessionDoc.getAssignedToDept(),
				chatArchive.getChatSessionDto(chatSessionDoc, chatSessionDoc.getAssignedToAgent()));

		return chatArchive.getMessage(messageDoc, chatSessionDoc);
	}

	public ChatSessionDTO updateChatSessionStatus(String sessionId, CHAT_STATUS status) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		if (chatService.updateSessionStatus(sessionDoc, status)) {
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
			chatService.log(inboxMessage, MessageStore.EVENTS.UNASGND);
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
			case "RESOLVE":
				return this.exitAgentMode(sessionDoc, outboxMessage);
			case "ADD_STICKY_NOTE":
				return this.addStickyNote(sessionDoc, outboxMessage);
			default:
				break;
			}
		} else {
			sessionStore.updateResponseTime(sessionDoc);
			MessageDoc messageDoc = chatService.reply(sessionDoc, outboxMessage);
			return chatArchive.getMessage(messageDoc, sessionDoc);
		}
		return new ChatMessageDTO();
	}

	public ChatMessageDTO addStickyNote(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		MessageDoc messageDoc = chatService.note(chatSessionDoc, outboxMessage);
		ChatMessageDTO messageDto = chatArchive.getMessage(messageDoc, chatSessionDoc);
		stompTunnelService.sendToTag(chatSessionDoc.getAssignedToDept(), "/message/sent/new", messageDto);
		return messageDto;
	}
}
