package com.boot.jx.agent;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;

public interface AgentChatHandler {

	public boolean onAssignSupported(InboxMessage inboxMessage);

	default public boolean onMessageSupported(InboxMessage inboxMessage) {
		return "AGENT".equalsIgnoreCase(inboxMessage.session().getMode());
	}

	public InboxMessage onAssign(InboxMessage inboxMessage);

	public InboxMessage onMessageReceive(InboxMessage inboxMessage);

	public OutboxMessage onSend(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage);
}
