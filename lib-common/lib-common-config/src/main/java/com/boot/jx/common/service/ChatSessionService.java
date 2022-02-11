package com.boot.jx.common.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.OutboxMessage;
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

}
