package com.boot.jx.agent;

import com.boot.jx.postman.model.InboxMessage;

public interface AgentAssigner {

	public boolean isSupported(InboxMessage inboxMessage);

	public InboxMessage onAssign(InboxMessage inboxMessage);
}
