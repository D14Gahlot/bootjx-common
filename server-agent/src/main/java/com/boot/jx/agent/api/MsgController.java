package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Controller
public class MsgController {

	@Autowired
	AppConfig appConfig;

	@Autowired
	SessionStore sessionStore;

	@Autowired
	MessageStore messageStore;

	@Autowired
	AgentSessionBean agentSession;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private AgentChatHandlerImpl agentChatHandlerImpl;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDto, Object> getSessionsAssignedToMe() {
		List<ChatSessionDoc> sessions = sessionStore.findChatSessionDocByAgent(agentSession.getAgentCode());

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDto chatSessionDto = agentChatHandlerImpl.getChatSessionDto(chatSessionDoc);
			chatSessionDtos.add(chatSessionDto);
		}
		return ApiResponse.buildResults(chatSessionDtos);
	}

	@Autowired
	private ChatService chatService;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDto, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {
		ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());
		if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
			ChatMessageDto messageDto = new ChatMessageDto();
			messageDto.setType(true);
			messageDto.setName(agentSession.getAgentCode());
			sessionDoc.getContactId();
			chatService.send(sessionDoc, outboxMessage);
			return ApiResponse.buildResult(messageDto);
		}
		return null;
	}

}
