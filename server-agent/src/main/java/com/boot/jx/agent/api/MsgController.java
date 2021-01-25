package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.expression.Arrays;

import com.boot.jx.AppConfig;
import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.SmartReply;
import com.boot.jx.postman.model.InboxMessage;
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
		List<ChatSessionDoc> sessions = sessionStore
				.findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(),agentSession.getAgentDept());

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDto chatSessionDto = agentChatHandlerImpl.getChatSessionDto(chatSessionDoc,
					agentSession.getAgentCode());
			chatSessionDtos.add(chatSessionDto);
		}
		agentSession.refreshOnline();
		return ApiResponse.buildResults(chatSessionDtos);
	}

	@Autowired
	private ChatService chatService;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDto, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {
		ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

		outboxMessage.setAgent(agentSession.getAgentCode());

		if (ArgUtil.isEmpty(sessionDoc.getAssignedToAgent())) {
			AgentSessionDoc agent = mongoTemplate.findById(agentSession.getAgentCode(), AgentSessionDoc.class);
			agentChatHandlerImpl.onAssign(agent, sessionDoc, outboxMessage);
			mongoTemplate.save(sessionDoc);
		}

		if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
			ChatMessageDto messageDto = new ChatMessageDto();
			messageDto.setType(true);
			messageDto.setName(agentSession.getAgentCode());
			sessionDoc.getContactId();
			chatService.send(sessionDoc, outboxMessage);
			return ApiResponse.buildResult(messageDto);
		}
		agentSession.refreshOnline();
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<SmartReply> listSmartReply(@RequestParam(value = "value", required = false) List<String> categories) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("_id.category").in(categories.stream().toArray(String[]::new)));
		return mongoTemplate.find(query2, SmartReply.class);
	}

}
