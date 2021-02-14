package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.bot.ChatContext;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.DefaultConnector;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Component
public class AgentService {

	@Autowired(required = false)
	private AgentChatHandler agentChatHandler;

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired(required = false)
	private DefaultConnector defaultConnector;

	@Autowired
	private ChatContext chatContext;

	public ApiResponse<InboxMessage, Object> assignToAgent(InboxMessage inboxMessage) {
		if (ArgUtil.is(agentChatHandler) && agentChatHandler.onAssignSupported(inboxMessage)) {
			return ApiResponse.buildResult(agentChatHandler.onAssign(inboxMessage));
		} else if (ArgUtil.is(chatClient.getAgentUrl())) {
			return chatClient.assignToAgent(inboxMessage);
		} else {
			ConnectorHandler connector = connectorHandlerFactory.get(inboxMessage.getContactType(),
					inboxMessage.getChannel());
			if (ArgUtil.is(connector)) {
				connector.assignToAgent(inboxMessage);
			} else if (ArgUtil.is(defaultConnector)) {
				chatContext.meta().setAgentEnabled(true);
				defaultConnector.assignToAgent(inboxMessage);
			}
			return ApiResponse.buildResult(inboxMessage);
		}
	}

	public ApiResponse<InboxMessage, Object> assignToAgent(String deptName) throws InterruptedException {
		InboxMessage inboxMessage = chatContext.getInboxMessage();
		if (ArgUtil.is(inboxMessage)) {
			inboxMessage.session().setAssignedToDept(deptName);
			return this.assignToAgent(inboxMessage);
		}
		return null;
	}

	public boolean onMessageSupported(InboxMessage inboxMessage) {
		return (ArgUtil.is(agentChatHandler) && agentChatHandler.onMessageSupported(inboxMessage));
	}

	public InboxMessage onMessage(InboxMessage inboxMessage) {
		return agentChatHandler.onMessageReceive(inboxMessage);
	}

	public OutboxMessage sendMessage(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		return agentChatHandler.onSend(sessionDoc, outboxMessage);
	}

}
