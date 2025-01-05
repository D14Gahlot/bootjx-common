package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.AmxResponseSchemes.ApiResultsMetaCompactResponse;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.jx.xms.dto.SessionRequestObjects.SessionQueueAssignment;
import com.boot.jx.xms.dto.SessionRequestObjects.SessionStatusClose;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.CollectionUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Session Management", description = XmsConstants.SESSION_MNGMNT_DESCRIPTION)
@RestController
public class SessionApiV1 {

	@Autowired
	private ChatArchiveBuilder chatArchiveBuilder;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired(required = false)
	private InBoundHandler inBoundHandler;

	@ApiOperation(value = "Session Messages", notes = "${swagger.SessionApiV1.getSessionMessages.description}",
			hidden = true, authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/session/messages", method = { RequestMethod.GET })
	public ApiResultsMetaCompactResponse<ChatMessageDTO, Object> getSessionMessages(@RequestParam String sessionId) {
		ChatSessionDTO session = chatArchiveBuilder.sessionDTO().from(sessionId).withMessages().get();
		return ApiResponse.buildResults(session.getMessages());
	}

	@ApiOperation(value = "Session Messages", notes = "${swagger.SessionApiV1.getSessionMessages.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/session/messages", method = { RequestMethod.POST })
	public ApiResultsMetaCompactResponse<ChatMessageDTO, Object> getSessionMessagesPost(
			@RequestBody SessionStatusClose req) {
		return getSessionMessages(req.sessionId);
	}

	@ApiOperation(value = "Session Routing", notes = "${swagger.SessionApiV1.sessionRouting.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/session/routing", method = { RequestMethod.POST })
	public ApiResultsMetaCompactResponse<InBoundEvent, Object> sessionRouting(@RequestBody SessionQueueAssignment req) {
		InBoundEvent event = chatSessionService.routeSession(req.sessionId,
				new PMArgs().assignToQueueCode(req.queue).assignToAgentCode(req.agent).assignToDeptCode(req.team)
						.assignToSkillCode(CollectionUtil.asArray(req.skills)).note(req.note).params(req.params));
		return ApiResponse.buildResults(event);
	}

	@ApiOperation(value = "Session Close", notes = "${swagger.SessionApiV1.sessionClose.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/session/close", method = { RequestMethod.POST })
	public ApiResultsMetaCompactResponse<InBoundEvent, Object> sessionClose(@RequestBody SessionStatusClose req) {
		NodeEntry<InBoundEvent> eventEntry = chatSessionService.closeSession(req.sessionId);
		if (eventEntry.exists()) {
			return ApiResponse.buildResults(eventEntry.value());
		}
		return ApiResponse.instance(InBoundEvent.class);
	}

	@ApiOperation(value = "Session Resolve", notes = "${swagger.SessionApiV1.sessionResolve.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/session/resolve", method = { RequestMethod.POST })
	public ApiResultsMetaCompactResponse<InBoundEvent, Object> sessionResolve(@RequestBody SessionStatusClose req) {
		NodeEntry<InBoundEvent> eventEntry = chatSessionService.resolveSession(req.sessionId);
		if (eventEntry.exists()) {
			return ApiResponse.buildResults(eventEntry.value());
		}
		return ApiResponse.instance(InBoundEvent.class);
	}

}
