package com.boot.jx.inbound;

import com.boot.jx.postman.model.InboxMessage;

public interface InBoundHandler {

	public InboxMessage onMessage(InboxMessage inboxMessage);
}
