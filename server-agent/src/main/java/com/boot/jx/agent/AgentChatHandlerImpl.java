package com.boot.jx.agent;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class AgentChatHandlerImpl implements AgentChatHandler {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public boolean onAssignSupported(InboxMessage inboxMessage) {
		return true;
	}

	@Override
	public InboxMessage onAssign(InboxMessage inboxMessage) {

		Calendar today = Calendar.getInstance();
		today.add(Calendar.MINUTE, -10);

		Query query = new Query();
		query.addCriteria(Criteria.where("isOnline").is(true).and("isLoggedIn").is(true).and("lastOnlineStamp")
				.gt(today.getTimeInMillis())).with(new Sort(Direction.ASC, "lastOnlineStamp")).limit(1);

		List<AgentSessionDoc> agents = mongoTemplate.find(query, AgentSessionDoc.class);

		AgentSessionDoc avaialbleAgent = CollectionUtil.getOne(agents);

		if (ArgUtil.is(avaialbleAgent)) {
			ChatSessionDoc chatSessionDoc = mongoTemplate.findById(inboxMessage.getSessionId(), ChatSessionDoc.class);
			chatSessionDoc.setAssignedToAgent(avaialbleAgent.getAgentCode());
			chatSessionDoc.setAssignedToDept(inboxMessage.getAssignedToDept());
			mongoTemplate.save(chatSessionDoc);
			inboxMessage.setAssignedToAgent(avaialbleAgent.getAgentCode());
		}

		return inboxMessage;
	}

	@Autowired
	private StompTunnelService stompTunnelService;

	@Override
	public InboxMessage onMessage(InboxMessage inboxMessage) {
		stompTunnelService.sendTo(inboxMessage.getAssignedToAgent(), "/agent/onmessage", inboxMessage);
		return inboxMessage;
	}

}
