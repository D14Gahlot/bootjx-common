package com.boot.jx.agent;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;

public interface AgentChatHandler {

    public InboxMessage onMessageReceive(InboxMessage inboxMessage);

    public ChatMessageDTO onSend(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage);

    public PMArgs doAssign(ChatSessionDoc session, PMArgs params);

}
