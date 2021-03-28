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
import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatDTOUtil;
import com.boot.jx.chat.ChatService;
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
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.TimeUtils;

@Component
public class AgentChatHandlerImpl implements AgentChatHandler {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ChatService chatService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	MessageStore messageStore;

	@Override
	public boolean onAssignSupported(InboxMessage inboxMessage) {
		return true;
	}

	@Override
	public InboxMessage onAssign(InboxMessage inboxMessage) {

		long timeThen = System.currentTimeMillis() - TimeUtils.toMillis(chatClient.getChatOnlholdTimeout());

		Query query = new Query();
		Criteria c = Criteria.where("isOnline").is(true).and("isLoggedIn").is(true).and("lastOnlineStamp").gt(timeThen);
		if (ArgUtil.is(inboxMessage.session().getDept())) {
			c.and("agentDept").is(inboxMessage.session().getDept());
		} else {
			inboxMessage.session().setDept(PMStoreConstants.NO_DEPT);
		}
		query.addCriteria(c).with(new Sort(Direction.ASC, "lastOnlineStamp")).limit(1);

		List<AgentSessionDoc> agents = mongoTemplate.find(query, AgentSessionDoc.class);

		AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

		// PUBLISH
		ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
		chatSessionDoc.setAssignedToDept(inboxMessage.session().getDept());
		chatSessionDoc.setAssignedDeptStamp(System.currentTimeMillis());
		chatSessionDoc.setMode("AGENT");
		chatSessionDoc.setAssignedToAgent(null);

		if (ArgUtil.is(avaialbleAgent)) {
			chatSessionDoc.setAssignedToAgent(avaialbleAgent.getAgentCode());
			chatSessionDoc.setAssignedToDept(avaialbleAgent.getAgentDept());
			chatSessionDoc.setAssignedAgentStamp(System.currentTimeMillis());

			messageStore.log(inboxMessage, MessageStore.EVENTS.ASGND_TO_AGENT, avaialbleAgent.getAgentCode(),
					avaialbleAgent.getAgentDept());

			inboxMessage.session().setAgent(avaialbleAgent.getAgentCode());
			inboxMessage.session().setDept(avaialbleAgent.getAgentDept());
		} else {
			messageStore.log(inboxMessage, MessageStore.EVENTS.ASGND_TO_DEPT, inboxMessage.session().getDept());
		}
		sessionStore.save(chatSessionDoc);

		stompTunnelService.sendToAll("/dept/onassign-" + inboxMessage.session().getDept(),
				getChatSessionDto(chatSessionDoc, inboxMessage.session().getAgent()));

		return inboxMessage;
	}

	public void onAssign(AgentSessionDoc avaialbleAgent, ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.is(avaialbleAgent)) {
			chatSessionDoc.setAssignedToAgent(avaialbleAgent.getAgentCode());
			chatSessionDoc.setAssignedAgentStamp(System.currentTimeMillis());

			messageStore.log(outboxMessage, MessageStore.EVENTS.ASGND_TO_AGENT, avaialbleAgent.getAgentCode(),
					avaialbleAgent.getAgentDept());

			stompTunnelService.sendToAll("/dept/onassign-" + avaialbleAgent.getAgentDept(),
					getChatSessionDto(chatSessionDoc, avaialbleAgent.getAgentCode()));
		}
	}

	public void exitAgentMode(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		chatService.resolveSession(chatSessionDoc);
		if (ArgUtil.is(outboxMessage)) {
			chatService.reply(chatSessionDoc, outboxMessage);
		}
		chatService.closeSession(chatSessionDoc);
		stompTunnelService.sendToAll("/dept/onassign-" + chatSessionDoc.getAssignedToDept(),
				getChatSessionDto(chatSessionDoc, chatSessionDoc.getAssignedToAgent()));
	}

	@Override
	public InboxMessage onMessageReceive(InboxMessage inboxMessage) {
		MessageDoc messageDoc = messageStore.find(inboxMessage);
		ChatMessageDto messageDto = entityToDto(messageDoc);
		messageDto.setName(inboxMessage.getFromName());
		stompTunnelService.sendTo(inboxMessage.session().getAgent(), "/agent/onmessage", messageDto);
		if (inboxMessage.getMessage().equalsIgnoreCase("/exit_chat")) {
			ChatSessionDoc chatSessionDoc = sessionStore.getSession(inboxMessage.getSessionId());
			exitAgentMode(chatSessionDoc, null);
			messageStore.log(inboxMessage, MessageStore.EVENTS.UNASGND);
		}
		return inboxMessage;
	}

	public OutboxMessage onSend(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		String action = ChatCommands.getCommand(outboxMessage);
		if (ArgUtil.is(action)) {
			outboxMessage.setAction(action);
			switch (action) {
			case "RESOLVE":
				this.exitAgentMode(sessionDoc, outboxMessage);
				break;
			default:
				break;
			}
		} else {
			chatService.reply(sessionDoc, outboxMessage);
			MessageDoc messageDoc = messageStore.find(outboxMessage);
			ChatMessageDto messageDto = entityToDto(messageDoc);
			messageDto.setName(messageDoc.getAgent());
			stompTunnelService.sendTo(outboxMessage.session().getAgent(), "/agent/onmessage", messageDto);
		}
		return outboxMessage;
	}

	private ChatMessageDto entityToDto(MessageDoc messageDoc) {
		ChatMessageDto messageDto = new ChatMessageDto();
		messageDto.setType(messageDoc.getType());
		messageDto.setText(messageDoc.getMessage());
		messageDto.setTemplate(messageDoc.getTemplate());
		messageDto.setTimestamp(messageDoc.getTimestamp());
		messageDto.setSessionId(messageDoc.getSessionId());
		messageDto.setMessageId(messageDoc.getMessageId());
		messageDto.setMessageIdExt(messageDoc.getMessageIdExt());
		messageDto.setMessageIdRef(messageDoc.getMessageIdRef());
		messageDto.setTags(messageDoc.getTags());
		messageDto.setAttachments(messageDoc.getAttachments());
		messageDto.setSender(messageDoc.getAgent());
		messageDto.setLogs(messageDoc.getLogs());
		messageDto.setAction(messageDoc.getAction());
		return messageDto;
	}

	public ChatSessionDto getChatSessionDto(ChatSessionDoc chatSessionDoc, String agentCode) {
		ChatSessionDto chatSessionDto = toChatSessionDto(chatSessionDoc);
		chatSessionDto.setAssigned(ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)
				&& ArgUtil.isNone(chatSessionDoc.getResolveSessionStamp()));
		List<ChatMessageDto> messageDtos = getMessages(chatSessionDto);
		chatSessionDto.setMessages(messageDtos);
		return chatSessionDto;
	}

	public List<ChatMessageDto> getMessages(ChatSessionDto chatSessionDto) {
		List<MessageDoc> messages = messageStore.findBySessionId(chatSessionDto.getSessionId(),
				chatSessionDto.getContactType());
		List<ChatMessageDto> messageDtos = new ArrayList<ChatMessageDto>();
		for (MessageDoc messageDoc : messages) {
			ChatMessageDto messageDto = entityToDto(messageDoc);
			if (ArgUtil.areEqual(messageDoc.getType(), "I")) {
				messageDto.setName(chatSessionDto.getName());
			} else {
				messageDto.setName(messageDoc.getAgent());
			}
			messageDtos.add(messageDto);
		}
		return messageDtos;
	}

	public ChatSessionDto toChatSessionDto(ChatSessionDoc chatSessionDoc) {
		ChatContactDoc contact = mongoTemplate.findById(chatSessionDoc.getContactId(), ChatContactDoc.class);
		// Populate
		ChatSessionDto chatSessionDto = EntityDtoUtil.entityToDto(chatSessionDoc, new ChatSessionDto());

		chatSessionDto.setSessionId(chatSessionDto.getSessionId());
		chatSessionDto.setContactType(contact.getContactType());
		chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
		chatSessionDto.setName(contact.getName());
		chatSessionDto.setProfilePic(contact.getProfilePic());
		chatSessionDto.setEmail(contact.getEmail());
		chatSessionDto.setPhone(contact.getPhone());
		chatSessionDto.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		chatSessionDto.setAssignedToDept(chatSessionDoc.getAssignedToDept());
		chatSessionDto.setContactId(contact.getContactId());
		chatSessionDto.setActive(chatSessionDoc.isActive());
		chatSessionDto.setContact(ChatDTOUtil.getContactDTO(contact));

		return chatSessionDto;
	}

}
