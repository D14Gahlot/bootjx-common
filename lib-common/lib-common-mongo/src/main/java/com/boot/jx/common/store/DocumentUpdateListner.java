package com.boot.jx.common.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;

@Component
public class DocumentUpdateListner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StompTunnelService stompTunnelService;

    @Autowired
    private ChatArchiveService chatArchive;

    public void onAgentSessionUpdate(String agentCode) {
	AgentSessionDoc agentSession = mongoTemplate.findById(agentCode, AgentSessionDoc.class);
	if (ArgUtil.is(agentSession)) {
	    stompTunnelService.sendToAll("/agent/session/update", agentSession);
	}
    }

    public void onAgentUpdate(String agentId) {
	AgentDoc agentDoc = mongoTemplate.findById(agentId, AgentDoc.class);

	if (ArgUtil.is(agentDoc)) {
	    CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentDoc.getAgent_code());
	    builder.set("isEnabled", agentDoc.getIsEnabled());
	    mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), AgentSessionDoc.class);
	    onAgentSessionUpdate(agentDoc.getAgent_code());
	}
    }

    public void onChatSessionUpdate(ChatSessionDoc sessionDoc) {
	if (ArgUtil.is(sessionDoc)) {
	    ChatSessionDTO dto = chatArchive.getChatSession(sessionDoc);
	    stompTunnelService.sendToTag(sessionDoc.getAssignedToDept(), "/chat/session/update", dto);
	}
    }

}
