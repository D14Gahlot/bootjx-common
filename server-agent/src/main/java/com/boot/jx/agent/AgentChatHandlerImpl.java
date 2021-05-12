package com.boot.jx.agent;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.chat.ChatArchive;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatCommands;
import com.boot.jx.chat.ChatDTOUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
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
	private ChatService chatService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private ChatArchive chatArchive;

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
				chatArchive.getChatSessionDto(chatSessionDoc, inboxMessage.session().getAgent()));

		return inboxMessage;
	}

	public void onAssign(AgentSessionDoc avaialbleAgent, ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		if (ArgUtil.is(avaialbleAgent)) {
			sessionStore.setAssignedToAgent(chatSessionDoc, avaialbleAgent.getAgentCode());
			chatService.log(chatSessionDoc, MessageStore.EVENTS.ASGND_TO_AGENT, avaialbleAgent.getAgentCode(),
					avaialbleAgent.getAgentDept());
			stompTunnelService.sendToAll("/dept/onassign-" + avaialbleAgent.getAgentDept(),
					chatArchive.getChatSessionDto(chatSessionDoc, avaialbleAgent.getAgentCode()));
		}
	}

	public void exitAgentMode(ChatSessionDoc chatSessionDoc, OutboxMessage outboxMessage) {
		chatService.resolveSession(chatSessionDoc);
		if (ArgUtil.is(outboxMessage)) {
			chatService.reply(chatSessionDoc, outboxMessage);
		}
		chatService.closeSession(chatSessionDoc);
		stompTunnelService.sendToAll("/dept/onassign-" + chatSessionDoc.getAssignedToDept(),
				chatArchive.getChatSessionDto(chatSessionDoc, chatSessionDoc.getAssignedToAgent()));
	}

	@Override
	public InboxMessage onMessageReceive(InboxMessage inboxMessage) {
		MessageDoc messageDoc = messageStore.find(inboxMessage);
		ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
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
			ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
			messageDto.setName(messageDoc.getAgent());
			stompTunnelService.sendTo(outboxMessage.session().getAgent(), "/agent/onmessage", messageDto);
		}
		return outboxMessage;
	}

}
