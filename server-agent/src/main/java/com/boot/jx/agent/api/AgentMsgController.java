package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.api.ControllerRequestDTOs.ChatTagUpdateRequest;
import com.boot.jx.agent.api.ControllerRequestDTOs.SessionSearchRequest;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.ConfigConstants.KEY;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@RestController
public class AgentMsgController {

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private AgentSessionBean agentSession;

    @Autowired
    private ChatArchiveService chatArchive;

    @Autowired
    private AgentSessionService agentSessionService;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @Autowired
    private DocumentUpdateListner documentUpdateListner;

    @Autowired
    private AgentService agentService;

    @Autowired
    private PMEnvironment environment;

    @ApiRequest(type = RequestType.POLL)
    @RequestMapping(value = "/api/sessions/assignments", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, AgentSessionDoc> getSessionsAssignments(
	    @RequestParam(defaultValue = "false") boolean withMessage, @RequestParam(required = false) Boolean status,
	    @RequestParam(required = false) Boolean away,
	    @RequestParam(required = false, defaultValue = "HISTORY") String tab,
	    @RequestParam(required = false) String search) {

	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
	if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
	    List<ChatSessionDoc> sessions = null;
	    long historyPeriod = environment.keyEntry(KEY.POSTMAN_AGENT_TAB_HISTORY_PERIOD).asLong(0L);
	    if (historyPeriod > 0L && "HISTORY".equals(tab)) {
		sessions = chatSessionManager.findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(),
			agentSession.getAgentDept(), search,
			PMConstants.DEFAULT_VALUES.POSTMAN_AGENT_TAB_HISTORY_PERIOD + historyPeriod);
	    } else {
		ApiResponseUtil.addLog("Only Active Chats");
		sessions = chatSessionManager.findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(),
			agentSession.getAgentDept(), search);
	    }

	    for (ChatSessionDoc chatSessionDoc : sessions) {
		ChatSessionDTO chatSessionDto = chatArchive.getChatSession(chatSessionDoc);
		chatSessionDto = chatArchive.withContact(chatSessionDto);
		if (withMessage
			&& ArgUtil.isEqual(chatSessionDto.getAssignedToDept(), DEFAULT.NO_DEPT,
				agentSession.getAgentDept(), null, Constants.BLANK)
			&& ArgUtil.isEqual(chatSessionDto.getAssignedToAgent(), agentSession.getAgentCode(), null)) {
		    chatSessionDto = chatArchive.withMessages(chatSessionDto);
		}
		chatSessionDtos.add(chatSessionDto);
	    }
	}
	if (away != null) {
	    agentSessionService.setAway(away.booleanValue());
	}
	if (status != null) {
	    agentSessionService.setOnline(status.booleanValue());
	}
	return new ApiResponse<ChatSessionDTO, AgentSessionDoc>().results(chatSessionDtos)
		.details(agentSessionService.getAgentSessions());
    }

    @RequestMapping(value = { "/api/session/tag" }, method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> addSessionTags(@RequestBody ChatTagUpdateRequest updateRequest) {
	ChatSessionDoc sessionDoc = sessionStore.getSession(updateRequest.sessionId);
	if (chatSessionManager.updateSessionStatus(sessionDoc, updateRequest.status)
		| chatSessionManager.updateSessionTags(sessionDoc, updateRequest.tags)) {
	    documentUpdateListner.onChatSessionUpdate(sessionDoc);
	}
	return ApiResponse.buildData(ChatDTOUtil.getChatSessionDTO(sessionDoc));
    }

    @RequestMapping(value = "/api/sessions/note", method = { RequestMethod.POST })
    public ApiResponse<ChatMessageDTO, Object> addStickyNote(@RequestBody OutboxMessage outboxMessage)
	    throws InterruptedException {
	ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

	// Session Stuff Logging >
	// if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(),
	// agentSession.getAgentCode()) || agentSession.isAdmin()) {
	if (ArgUtil.is(sessionDoc)) {
	    outboxMessage.setAction(CHAT_SESSION_ACTIONS.ADD_STICKY_NOTE);
	    ChatMessageDTO messageDto = agentService.sendMessage(sessionDoc, outboxMessage);
	    // Evaluate if required
	    messageDto.setName(agentSession.getAgentCode());
	    // messageDto.setType(outboxMessage.getType());
	    messageDto.setText(outboxMessage.getMessage());
	    messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());
	    agentSessionService.refreshOnline();
	    return ApiResponse.buildResult(messageDto);
	} else {
	    agentSessionService.refreshOnline();
	    return new ApiResponse<ChatMessageDTO, Object>().message("Only Assignee/Admin can add StickyNote to chat.");
	}
    }

    @RequestMapping(value = "/api/sessions/search", method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> searchSessions(@RequestBody SessionSearchRequest query) {
	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();	
	List<ChatSessionDoc> sessions = chatSessionManager.searchBy(query.status, query.tags, query.fromStamp,
		query.toStamp);
	for (ChatSessionDoc chatSessionDoc : sessions) {
		ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
	    chatSessionDtos.add(chatSessionDto);
	}
	/**remove duplicate /multiple Session for each contact  we can filter based on name , phone number on any field **/
	if(chatSessionDtos!=null &&  !chatSessionDtos.isEmpty()) {
		Set<String> chatSessionSet = new HashSet<>();
		chatSessionDtos=chatSessionDtos.stream().filter(e->chatSessionSet.add(e.getPhone())).collect(Collectors.toList());
	}
	return ApiResponse.buildResults(chatSessionDtos);
    }

    @RequestMapping(value = "/api/sessions/primary", method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> searchPrimarySessions(@RequestBody SessionSearchRequest query) {
	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
	List<ChatSessionDoc> sessions = chatSessionManager.searchPrimary(query.text);
	for (ChatSessionDoc chatSessionDoc : sessions) {
	    ChatSessionDTO chatSessionDto = chatArchive.getChatSession(chatSessionDoc);
	    chatSessionDtos.add(chatSessionDto);
	}
	return ApiResponse.buildResults(chatSessionDtos);
    }
}
