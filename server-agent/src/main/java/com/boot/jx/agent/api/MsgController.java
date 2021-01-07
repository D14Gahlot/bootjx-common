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
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompTunnelService;
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
	private StompTunnelService stompTunnelService;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDto, Object> getSessionsAssignedToMe() {
		List<ChatSessionDoc> sessions = sessionStore.findChatSessionDocByAgent(agentSession.getAgentCode());

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatContactDoc contact = mongoTemplate.findById(chatSessionDoc.getContactId(), ChatContactDoc.class);

			// Populate
			ChatSessionDto chatSessionDto = new ChatSessionDto();
			chatSessionDto.setSessionId(contact.getSessionId());
			chatSessionDto.setContactType(contact.getContactType());
			chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
			chatSessionDto.setName(contact.getContactId());

			List<MessageDoc> messages = messageStore.findBySessionId(contact.getSessionId(), contact.getContactType());
			List<ChatMessageDto> messageDtos = new ArrayList<ChatMessageDto>();
			for (MessageDoc messageDoc : messages) {
				ChatMessageDto messageDto = new ChatMessageDto();
				if (ArgUtil.areEqual(messageDoc.getType(), "I")) {
					messageDto.setType(false);
					messageDto.setName(chatSessionDto.getName());
				} else {
					messageDto.setType(true);
					messageDto.setName(messageDoc.getAgent());
				}
				messageDto.setText(ArgUtil.nonEmpty(messageDoc.getTemplate(), messageDoc.getMessage()));
				messageDto.setTimestamp(messageDoc.getTimestamp());
				messageDtos.add(messageDto);
			}
			chatSessionDto.setMessages(messageDtos);
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
			chatService.send(outboxMessage);
			return ApiResponse.buildResult(messageDto);
		}
		return null;
	}

}
