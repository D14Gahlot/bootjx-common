package com.boot.jx.agent;

import com.boot.jx.chat.ChatAssigner;
import com.boot.jx.postman.model.InboxMessage;

public class AgentChatAssigner implements ChatAssigner {

	@Override
	public boolean isSupported(InboxMessage inboxMessage) {
		return false;
	}

	@Override
	public InboxMessage onAssign(InboxMessage inboxMessage) {
		return null;
	}

}
