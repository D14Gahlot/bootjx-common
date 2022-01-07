package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.xms.XmsConstants.ApiClientParams;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Session APIs", description = "API's for Session Management")
@Controller
public class SessionApiV1 {

    @Autowired
    private ChatArchiveService chatArchive;

    @ApiOperation(value = "Read all messages for a session")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/session/messages", method = { RequestMethod.GET })
    public ApiResponse<ChatMessageDTO, Object> setWebhookUrl(@RequestParam String sessionId) {
	ChatSessionDTO session = new ChatSessionDTO();
	session.setSessionId(sessionId);
	session = chatArchive.withMessages(session);
	return ApiResponse.buildResults(session.getMessages());

    }

}
