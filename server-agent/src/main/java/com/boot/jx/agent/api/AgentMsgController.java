package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatArchiveService;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Controller
public class AgentMsgController {

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private AgentSessionBean agentSession;

    @Autowired
    private ChatArchiveService chatArchive;

    @Autowired
    private AgentSessionService agentSessionService;

    @ResponseBody
    @RequestMapping(value = "/api/sessions/assignments", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, AgentSessionDoc> getSessionsAssignments(
	    @RequestParam(defaultValue = "false") boolean withMessage, @RequestParam(required = false) Boolean status) {

	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
	if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
	    List<ChatSessionDoc> sessions = sessionStore
		    .findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(), agentSession.getAgentDept());
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
	if (ArgUtil.is(status)) {
	    agentSessionService.setOnline(status.booleanValue());
	}
	return new ApiResponse<ChatSessionDTO, AgentSessionDoc>().results(chatSessionDtos)
		.details(agentSessionService.getAgentSessions());
    }

}
