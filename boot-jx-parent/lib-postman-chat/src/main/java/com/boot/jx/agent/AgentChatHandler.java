package com.boot.jx.agent;

import com.boot.jx.postman.model.InboxMessage;
import com.boot.utils.ArgUtil;

public interface AgentChatHandler {

	public boolean onAssignSupported(InboxMessage inboxMessage);

	default public boolean onMessageSupported(InboxMessage inboxMessage) {
		return ArgUtil.is(inboxMessage.getAssignedToAgent()) || ArgUtil.is(inboxMessage.getAssignedToDept());
	}

	public InboxMessage onAssign(InboxMessage inboxMessage);

	public InboxMessage onMessage(InboxMessage inboxMessage);
}
