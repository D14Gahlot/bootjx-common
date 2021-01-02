package com.boot.jx.agent;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class AgentChatAssigner implements AgentAssigner {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public boolean isSupported(InboxMessage inboxMessage) {
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
			chatSessionDoc.setAssignedTo(avaialbleAgent.getAgentCode());
			mongoTemplate.save(chatSessionDoc);
			inboxMessage.setAssignedToAgent(avaialbleAgent.getAgentCode());
		}

		return inboxMessage;
	}

}
