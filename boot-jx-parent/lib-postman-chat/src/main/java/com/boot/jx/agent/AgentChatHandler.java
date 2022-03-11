package com.boot.jx.agent;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

public interface AgentChatHandler {

    public boolean onAssignSupported(InboxMessage inboxMessage);

    @Deprecated
    default public boolean onMessageSupported(InboxMessage inboxMessage) {
	return "AGENT".equalsIgnoreCase(inboxMessage.session().getMode())
		&& ArgUtil.isEmptyValue(inboxMessage.session().isResolved());
    }

    public InboxMessage onMessageReceive(InboxMessage inboxMessage);

    public ChatMessageDTO onSend(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage);

}
