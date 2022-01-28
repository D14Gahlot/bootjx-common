package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.SessionQueueAssignment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Session Management", description = XmsConstants.SESSION_MNGMNT_DESCRIPTION)
@Controller
public class SessionApiV1 {

    @Autowired
    private ChatArchiveService chatArchive;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @ApiOperation(value = "Fetch session messages")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/session/messages", method = { RequestMethod.GET })
    public ApiResponse<ChatMessageDTO, Object> getSessionMessages(@RequestParam String sessionId) {
	ChatSessionDTO session = new ChatSessionDTO();
	session.setSessionId(sessionId);
	session = chatArchive.withMessages(session);
	return ApiResponse.buildResults(session.getMessages());
    }

    @ApiOperation(value = "Assign session to different queue, all the inbound messages will be routed to this queue")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/session/queue", method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> assignQueue(@RequestBody SessionQueueAssignment req) {
	return ApiResponse
		.buildResults(chatArchive.getChatSession(chatSessionManager.assignToQueue(req.sessionId, req.queue)));
    }

}
