package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Controller
public class MsgController {

	@Autowired
	AppConfig appConfig;

	@Autowired
	SessionStore sessionStore;

	@Autowired
	AgentSessionBean agentSession;

	@Autowired
	MongoTemplate mongoTemplate;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDto, Object> getSessionsAssignedToMe() {
		List<ChatSessionDoc> sessions = sessionStore.findChatSessionDocByAgent(agentSession.getAgentCode());

		List<ChatSessionDto> chatSessionDtos = new ArrayList<ChatSessionDto>();
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatContactDoc contact = mongoTemplate.findById(chatSessionDoc.getContactId(), ChatContactDoc.class);

			// Populate
			ChatSessionDto chatSessionDto = new ChatSessionDto();
			chatSessionDto.setSessionId(contact.getContactId());
			chatSessionDto.setContactType(contact.getContactType());
			chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
			chatSessionDto.setName(contact.getContactId());

			Query query2 = new Query();
			query2.addCriteria(Criteria.where("sessionId").is(contact.getSessionId()));
			List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class);
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

}
