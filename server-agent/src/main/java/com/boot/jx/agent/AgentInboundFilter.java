package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.inbound.InBoundFilter;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.MessageStore;

@Component
public class AgentInboundFilter implements InBoundFilter {

	@Override
	public boolean onFilter(InboxMessage inboxMessage) {

		return true;
	}

}
