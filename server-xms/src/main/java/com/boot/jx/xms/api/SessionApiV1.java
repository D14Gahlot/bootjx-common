package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.jx.xms.dto.SessionQueueAssignment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Session Management", description = XmsConstants.SESSION_MNGMNT_DESCRIPTION)
@RestController
public class SessionApiV1 {

    @Autowired
    private ChatArchiveService chatArchive;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired(required = false)
    private InBoundHandler inBoundHandler;

    @ApiOperation(value = "Session Messages", notes = "${swagger.SessionApiV1.getSessionMessages.description}",
	    authorizations = @Authorization("X_API_KEY"))
    @XMSClientAuth
    @RequestMapping(value = "/api/v1/session/messages", method = { RequestMethod.GET })
    public ApiResponse<ChatMessageDTO, Object> getSessionMessages(@RequestParam String sessionId) {
	ChatSessionDTO session = new ChatSessionDTO();
	session.setSessionId(sessionId);
	session = chatArchive.withMessages(session);
	return ApiResponse.buildResults(session.getMessages());
    }

    @ApiOperation(value = "Session Routing", notes = "${swagger.SessionApiV1.sessionRouting.description}",
	    authorizations = @Authorization("X_API_KEY"))
    @XMSClientAuth
    @RequestMapping(value = "/api/v1/session/routing", method = { RequestMethod.POST })
    public ApiResponse<InBoundEvent, Object> sessionRouting(@RequestBody SessionQueueAssignment req) {
	InBoundEvent event = chatSessionService.routeChatSession(req.sessionId, req.queue, req.params);
	return ApiResponse.buildResults(event);
    }

}
