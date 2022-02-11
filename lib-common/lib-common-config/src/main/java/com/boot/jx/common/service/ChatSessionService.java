package com.boot.jx.common.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class ChatSessionService {

    private static final Logger LOGGER = LoggerService.getLogger(CDNBuilder.class);

    @Autowired
    private PMEnvironment environment;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @Autowired
    private StompTunnelService stompTunnelService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatArchiveBuilder chatArchiveBuilder;

    @Autowired(required = false)
    private InBoundHandler inBoundHandler;

    public MessageDoc closeChatSession(ChatSessionDoc chatSessionDoc) {
	MessageDoc messageDoc = null;

	if (!chatSessionDoc.isResolved()) {
	    chatSessionManager.resolveSession(chatSessionDoc);
	    PMConfigurationObject resolvedReply = environment
		    .keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_RESOLVED);
	    if (resolvedReply.exists()) {
		messageDoc = chatService.send(chatSessionDoc, new OutboxMessage().templateId(resolvedReply.asString()));
	    }

	}

	chatSessionManager.closeSession(chatSessionDoc);
	stompTunnelService.sendToAll(PostManUtil.ON_DEPT_ASSIGN_TOPIC(chatSessionDoc.getAssignedToDept()),
		chatArchiveBuilder.buildChatSessionDTO().from(chatSessionDoc).withContact()
			.isAssigned(chatSessionDoc.getAssignedToAgent()).get());
	return messageDoc;
    }

    public InBoundEvent routeChatSession(String sessionId, String queue, Object params) {
	InBoundEvent event = chatSessionManager.assignToQueue(sessionId, queue);
	event.sessionRouted.params = params;
	if (ArgUtil.is(inBoundHandler)) {
	    inBoundHandler.handleAsync(event);
	}
	return event;
    }

}
