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

import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.MapBuilder;

@Controller
public class MsgController {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AgentChatHandlerImpl agentChatHandlerImpl;

	@Autowired
	private AgentService agentService;

	@Autowired
	private AgentSessionService agentSessionService;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDto, Object> getSessionsAssignedToMe() {

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();

		if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
			List<ChatSessionDoc> sessions = sessionStore
					.findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(), agentSession.getAgentDept());

			for (ChatSessionDoc chatSessionDoc : sessions) {
				ChatSessionDto chatSessionDto = agentChatHandlerImpl.getChatSessionDto(chatSessionDoc,
						agentSession.getAgentCode());
				chatSessionDtos.add(chatSessionDto);
			}
		}

		agentSessionService.refreshOnline();

		return ApiResponse.buildResults(chatSessionDtos,
				MapBuilder.map().put("isOnline", agentSession.isOnline()).build());
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDto, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {
		ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

		// Session Stuff Logging <
		if (ArgUtil.isEmpty(sessionDoc.getAssignedToAgent())) {
			AgentSessionDoc agent = mongoTemplate.findById(agentSession.getAgentCode(), AgentSessionDoc.class);
			agentChatHandlerImpl.onAssign(agent, sessionDoc, outboxMessage);
		}
		if (ArgUtil.isNone(sessionDoc.getFistResponseStamp())) {
			sessionDoc.setFistResponseStamp(System.currentTimeMillis());
		}
		sessionDoc.setLastResponseStamp(System.currentTimeMillis());
		mongoTemplate.save(sessionDoc);
		// Session Stuff Logging >

		if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
			ChatMessageDto messageDto = new ChatMessageDto();
			messageDto.setType(true);
			messageDto.setName(agentSession.getAgentCode());
			agentService.sendMessage(sessionDoc, outboxMessage);
			messageDto.setMessageId(outboxMessage.getMessageId());
			messageDto.setMessageIdExt(outboxMessage.getMessageIdExt());
			messageDto.setText(outboxMessage.getMessage());
			messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());
			return ApiResponse.buildResult(messageDto);
		}
		agentSessionService.refreshOnline();
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<QuickReply> listSmartReply(@RequestParam(value = "value", required = false) List<String> categories) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("category").in(categories.stream().toArray(String[]::new)));
		return mongoTemplate.find(query2, QuickReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.GET })
	public List<TemplateReply> listMediaReply() {
		return mongoTemplate.findAll(TemplateReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/contact", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDto, Object> getSessionsForContact(@RequestParam String contactId) {

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();

		List<ChatSessionDoc> sessions = sessionStore.findChatSessionContactId(contactId);
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDto chatSessionDto = agentChatHandlerImpl.toChatSessionDto(chatSessionDoc);
			chatSessionDtos.add(chatSessionDto);
		}
		return ApiResponse.buildResults(chatSessionDtos,
				MapBuilder.map().put("isOnline", agentSession.isOnline()).build());
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDto, Object> getMessagesForSession(@RequestBody ChatSessionDto chatSessionDto) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(chatSessionDto.getSessionId());
		return ApiResponse.buildResults(
				agentChatHandlerImpl.getChatSessionDto(sessionDoc, agentSession.getAgentCode()).getMessages());
	}
}
