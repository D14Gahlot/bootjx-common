package com.boot.jx.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.agent.dto.ChatMessageDto;
import com.boot.jx.agent.dto.ChatSessionDto;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.PMStoreConstants;
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
	private SessionStore sessionStore;

	@Override
	public boolean onAssignSupported(InboxMessage inboxMessage) {
		return true;
	}

	@Override
	public InboxMessage onAssign(InboxMessage inboxMessage) {

		long timeThen = System.currentTimeMillis() - TimeUtils.toMillis(chatClient.getChatOnlholdTimeout());

		Query query = new Query();
		Criteria c = Criteria.where("isOnline").is(true).and("isLoggedIn").is(true).and("lastOnlineStamp").gt(timeThen);
		if (ArgUtil.is(inboxMessage.getAssignedToDept())) {
			c.and("agentDept").is(inboxMessage.getAssignedToDept());
		} else {
			inboxMessage.setAssignedToDept(PMStoreConstants.NO_DEPT);
		}
		query.addCriteria(c).with(new Sort(Direction.ASC, "lastOnlineStamp")).limit(1);

		List<AgentSessionDoc> agents = mongoTemplate.find(query, AgentSessionDoc.class);

		AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

		// PUBLISH
		ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
		chatSessionDoc.setAssignedToDept(inboxMessage.getAssignedToDept());

		if (ArgUtil.is(avaialbleAgent)) {
			chatSessionDoc.setAssignedToAgent(avaialbleAgent.getAgentCode());
			chatSessionDoc.setAssignedToDept(avaialbleAgent.getAgentDept());

			inboxMessage.setAssignedToAgent(avaialbleAgent.getAgentCode());
			inboxMessage.setAssignedToDept(avaialbleAgent.getAgentDept());
		}
		sessionStore.save(chatSessionDoc);

		stompTunnelService.sendToAll("/dept/onassign-" + inboxMessage.getAssignedToDept(),
				getChatSessionDto(chatSessionDoc, inboxMessage.getAssignedToAgent()));

		return inboxMessage;
	}

	public void onAssign(AgentSessionDoc avaialbleAgent, ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.is(avaialbleAgent)) {
			chatSessionDoc.setAssignedToAgent(avaialbleAgent.getAgentCode());
			stompTunnelService.sendToAll("/dept/onassign-" + avaialbleAgent.getAgentDept(),
					getChatSessionDto(chatSessionDoc, avaialbleAgent.getAgentDept()));
		}
	}

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	MessageStore messageStore;

	@Override
	public InboxMessage onMessage(InboxMessage inboxMessage) {

		MessageDoc messageDoc = messageStore.find(inboxMessage);
		ChatMessageDto messageDto = new ChatMessageDto();
		messageDto.setType(false);
		messageDto.setName(messageDoc.getContactId());
		messageDto.setText(ArgUtil.nonEmpty(messageDoc.getTemplate(), messageDoc.getMessage()));
		messageDto.setTimestamp(messageDoc.getTimestamp());
		messageDto.setSessionId(messageDoc.getSessionId());
		messageDto.setTags(messageDoc.getTags());
		stompTunnelService.sendTo(inboxMessage.getAssignedToAgent(), "/agent/onmessage", messageDto);

		if (inboxMessage.getMessage().equalsIgnoreCase("/exit_chat")) {
			ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
			chatSessionDoc.setAssignedToDept(null);
			chatSessionDoc.setAssignedToAgent(null);
			sessionStore.save(chatSessionDoc);
			stompTunnelService.sendToAll("/dept/onassign-" + inboxMessage.getAssignedToDept(),
					getChatSessionDto(chatSessionDoc, inboxMessage.getAssignedToAgent()));
			messageStore.log(inboxMessage, MessageStore.EVENTS.UNASGND);
		}

		return inboxMessage;
	}

	public ChatSessionDto getChatSessionDto(ChatSessionDoc chatSessionDoc, String agentCode) {
		ChatContactDoc contact = mongoTemplate.findById(chatSessionDoc.getContactId(), ChatContactDoc.class);

		// Populate
		ChatSessionDto chatSessionDto = new ChatSessionDto();
		chatSessionDto.setSessionId(contact.getSessionId());
		chatSessionDto.setContactType(contact.getContactType());
		chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
		chatSessionDto.setName(contact.getName());
		chatSessionDto.setProfilePic(contact.getProfilePic());
		chatSessionDto.setEmail(contact.getEmail());
		chatSessionDto.setPhone(contact.getPhone());
		chatSessionDto.setAssigned(ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode));
		chatSessionDto.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		chatSessionDto.setAssignedToDept(chatSessionDoc.getAssignedToDept());

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
			messageDto.setTags(messageDoc.getTags());
			messageDtos.add(messageDto);
		}
		chatSessionDto.setMessages(messageDtos);
		return chatSessionDto;
	}
}
