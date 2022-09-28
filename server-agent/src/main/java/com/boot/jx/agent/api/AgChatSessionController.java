package com.boot.jx.agent.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.api.ControllerRequestDTOs.ChatTagUpdateRequest;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@RestController
public class AgChatSessionController {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AgentChatHandlerImpl agentChatHandlerImpl;

	@Autowired
	private AgentService agentService;

	@Autowired
	private ChatArchiveService chatArchive;

	@Autowired
	private ChatArchiveBuilder chatArchiveBuilder;

	@Autowired
	private AgentSessionService agentSessionService;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionManager chatSessionManager;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private AgentStore agentStore;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {

		outboxMessage.route().setSendMode(CHAT_MODE.AGENT.toString());
		outboxMessage.route().setSenderCode(agentSession.getAgentCode());
		outboxMessage.route().setSenderApp(APP_TYPE.AGENT.name());
		outboxMessage.route().setSenderType(MESSAGE_SENDER_TYPE.AGENT);
		ChatSessionDoc sessionDoc = chatSessionFactory.linkSession(outboxMessage);

		// Session Stuff Logging <
		if (ArgUtil.isEmpty(sessionDoc.getAssignedToAgent())
				|| (environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_AGENT_CHAT_ONSEND_ASSIGNED).asBoolean()
						&& !ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode()))) {
			AgentSessionDoc agent = mongoTemplate.findById(agentSession.getAgentCode(), AgentSessionDoc.class);
			agentChatHandlerImpl.onAssign(agent, sessionDoc);
		}

		// Session Stuff Logging >
		if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
			outboxMessage.route().setQueueCode(sessionDoc.getAssignedToQueue());
			ChatMessageDTO messageDto = agentService.sendMessage(sessionDoc, outboxMessage);

			// Evaluate if required
			messageDto.setName(agentSession.getAgentCode());
			// messageDto.setType(outboxMessage.getType());
			messageDto.setText(outboxMessage.getMessage());
			messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());

			// messageDto.setMessageIdExt(outboxMessage.getMessageIdExt());
			// messageDto.setMessageId(outboxMessage.getMessageId());

			agentSessionService.refreshOnline();
			return new ApiResponse<ChatMessageDTO, Object>().result(messageDto)
					.meta(chatArchiveBuilder.sessionDTO().from(sessionDoc).get());
		} else {
			agentSessionService.refreshOnline();
			return new ApiResponse<ChatMessageDTO, Object>().message("Only assignee can respond to chat.");
		}

	}

	@Autowired
	AWSFileStore fileStore;

	@Autowired
	PMFileStoreClient pmFileStoreClient;

	@RequestMapping(value = { "/api/sessions/message/upload", "/api/session/message/upload" },
			method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> uploadSessionFile(@RequestParam String message,
			@RequestParam(required = false) String caption, @RequestParam(name = "file") MultipartFile file)
			throws InterruptedException {
		OutboxMessage outboxMessage = JsonUtil.parse(message, OutboxMessage.class);

		CommonFile f = pmFileStoreClient.uploadSessionFile(file, outboxMessage.getSessionId(),
				outboxMessage.getMessageIdRef());

		outboxMessage
				.attachment(new Attachment().mediaURL(f.getUrl()).mediaType(f.getFileType()).mediaCaption(caption));

		return sendSessionMessage(outboxMessage);
	}

	@RequestMapping(value = { "/api/sessions/note", "/api/session/note" }, method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> addStickyNote(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {
		ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

		// Session Stuff Logging >
		// if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(),
		// agentSession.getAgentCode()) || agentSession.isAdmin()) {
		if (ArgUtil.is(sessionDoc)) {
			outboxMessage.setAction(CHAT_SESSION_ACTIONS.ADD_STICKY_NOTE);
			ChatMessageDTO messageDto = agentService.sendMessage(sessionDoc, outboxMessage);
			// Evaluate if required
			messageDto.setName(agentSession.getAgentCode());
			// messageDto.setType(outboxMessage.getType());
			messageDto.setText(outboxMessage.getMessage());
			messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());
			agentSessionService.refreshOnline();
			return ApiResponse.buildResult(messageDto);
		} else {
			agentSessionService.refreshOnline();
			return new ApiResponse<ChatMessageDTO, Object>().message("Only Assignee/Admin can add StickyNote to chat.");
		}
	}

	@RequestMapping(value = { "/api/session/tag" }, method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> addSessionTags(@RequestBody ChatTagUpdateRequest updateRequest) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(updateRequest.sessionId);
		if (chatSessionService.updateSessionStatus(sessionDoc, updateRequest.status).exists()
				| chatSessionManager.updateSessionTags(sessionDoc, updateRequest.tags)) {
			documentUpdateListner.onChatSessionUpdate(sessionDoc);
		}
		return ApiResponse.buildData(ChatDTOUtil.getChatSessionDTO(sessionDoc));
	}

	@RequestMapping(value = { "/api/session/messages" }, method = { RequestMethod.GET })
	public ApiResponse<ChatMessageDTO, ChatSessionDTO> messageApi(@RequestParam String sessionId,
			@RequestParam(required = false) String messageId, @RequestParam(required = false) String messageIdExt) {
		ApiResponse<ChatMessageDTO, ChatSessionDTO> resp = ApiResponse.build();
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		if (ArgUtil.is(messageId)) {
			ChatSessionDTO chatSessionDto = chatArchive.getChatSession(sessionDoc);
			MessageDoc m = messageStore.findByMessageId(messageId, sessionDoc.contact().getContactType());
			return resp.result(chatArchive.createMessageDTO(m, chatSessionDto)).meta(chatSessionDto);
		} else if (ArgUtil.is(messageIdExt)) {
			ChatSessionDTO chatSessionDto = chatArchive.getChatSession(sessionDoc);
			MessageDoc m = messageStore.findOneByMessageIdExt(messageIdExt, sessionDoc.contact().getContactType());
			return resp.result(chatArchive.createMessageDTO(m, chatSessionDto)).meta(chatSessionDto);
		} else {
			if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentCode())) {
				sessionStore.update(new ChatSessionQuery(sessionDoc).read(agentSession.getAgentCode()));
			}
			ChatSessionDTO chatSessionDto = chatArchive.getChatSession(sessionDoc);
			chatSessionDto = chatArchive.withContact(chatSessionDto);
			return resp.results(chatArchive.getMessages(chatSessionDto)).meta(chatSessionDto);
		}
	}

	@RequestMapping(value = { "/api/session/agent", "/api/session/agent/assign" }, method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> assignAgent(@RequestParam String sessionId,
			@RequestParam(required = false) String agentId, @RequestParam(required = false) String agentCode,
			@RequestParam(required = false) String deptCode, @RequestParam(required = false) String deptId) {
		ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);

		if (ArgUtil.is(agentId)) {
			AgentDoc agent = agentStore.findById(agentId);
			agentChatHandlerImpl.onAssign(agent, chatSessionDoc);
		} else if (ArgUtil.is(agentCode)) {
			AgentDoc agent = agentStore.findByCode(agentCode);
			agentChatHandlerImpl.onAssign(agent, chatSessionDoc);
		} else if (ArgUtil.is(deptCode)) {
			agentChatHandlerImpl.onAssign(chatSessionDoc, deptCode, agentCode);
		}

		ChatSessionDTO chatSessionDto = chatArchiveBuilder.sessionDTO().from(chatSessionDoc).withContact()
				.isAssigned(agentSession.getAgentCode()).withMessages().get();
		return ApiResponse.buildResult(chatSessionDto);
	}

	@RequestMapping(value = { "/api/session/status" }, method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> updateSessionStatus(@RequestParam String sessionId,
			@RequestParam PMConstants.CHAT_STATUS status) {
		return ApiResponse.buildResult(agentChatHandlerImpl.updateChatSessionStatus(sessionId, status));
	}

}
