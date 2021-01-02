package com.boot.jx.chat;

import com.boot.jx.postman.model.InboxMessage;

public interface ChatAssigner {

	public boolean isSupported(InboxMessage inboxMessage);

	public InboxMessage onAssign(InboxMessage inboxMessage);
}
