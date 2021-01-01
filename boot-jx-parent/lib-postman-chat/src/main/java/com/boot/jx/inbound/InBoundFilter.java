package com.boot.jx.inbound;

import com.boot.jx.postman.model.InboxMessage;

public interface InBoundFilter {

	public boolean onFilter(InboxMessage inboxMessage);
}
