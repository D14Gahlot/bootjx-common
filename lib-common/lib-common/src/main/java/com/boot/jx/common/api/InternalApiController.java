package com.boot.jx.common.api;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.dto.ControllerRequestDTOs.ChatTagUpdateRequest;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.SessionStore;

@RestController
public class InternalApiController {

	private static final Logger LOGGER = LoggerService.getLogger(InternalApiController.class);

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionManager chatSessionManager;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@RequestMapping(value = { "/int/session/tag" }, method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> addSessionTags(@RequestBody ChatTagUpdateRequest updateRequest) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(updateRequest.sessionId);
		if (chatSessionService.updateSessionStatus(sessionDoc, updateRequest.status).exists()
				| chatSessionManager.updateSessionTags(sessionDoc, updateRequest.tags)) {
			documentUpdateListner.onChatSessionUpdate(sessionDoc);
		}
		return ApiResponse.buildData(ChatDTOUtil.getChatSessionDTO(sessionDoc));
	}

}
