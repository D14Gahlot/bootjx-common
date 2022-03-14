package com.boot.jx.agent;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.LogManager;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class AgentChatHandlerImpl implements AgentChatHandler {

    public static final Logger LOGGER = LoggerService.getLogger(AgentChatHandlerImpl.class);

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private LogManager logManager;

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

    @Override
    public boolean onAssignSupported(InboxMessage inboxMessage) {
	return true;
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
    private void onAssign(ChatSessionDoc chatSessionDoc, String agentDept, String agentCode) {
	if (!ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)) {
	    chatSessionService.assignSessionToAgent(chatSessionDoc, agentDept, agentCode);
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
	chatSessionService.closeSession(chatSessionDoc);
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
