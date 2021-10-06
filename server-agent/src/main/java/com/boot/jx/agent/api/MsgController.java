package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.chat.ChatArchive;
import com.boot.jx.chat.ChatService;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.PMStoreConstants;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;

@Controller
public class MsgController {

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private AgentSessionBean agentSession;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AgentChatHandlerImpl agentChatHandlerImpl;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatArchive chatArchive;

    @Autowired
    private AgentSessionService agentSessionService;

    @ResponseBody
    @RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, Object> getSessionsAssignedToMe(
	    @RequestParam(defaultValue = "true") boolean withMessage) {

	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();

	if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
	    List<ChatSessionDoc> sessions = sessionStore
		    .findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(), agentSession.getAgentDept());
	    for (ChatSessionDoc chatSessionDoc : sessions) {
		ChatSessionDTO chatSessionDto = chatArchive.getChatSession(chatSessionDoc);
		chatSessionDto = chatArchive.withContact(chatSessionDto);

		if (ArgUtil.isEqual(chatSessionDto.getAssignedToDept(), PMStoreConstants.NO_DEPT,
			agentSession.getAgentDept(), null, Constants.BLANK)
			&& ArgUtil.isEqual(chatSessionDto.getAssignedToAgent(), agentSession.getAgentCode(), null)
			&& withMessage) {
		    chatSessionDto = chatArchive.withMessages(chatSessionDto);
		}
		chatSessionDtos.add(chatSessionDto);
	    }
	}

	agentSessionService.refreshOnline();

	return ApiResponse.buildResults(chatSessionDtos, MapBuilder.map().put("isOnline", agentSession.isOnline())
		.put("profile", agentSession.getProfile()).build());
    }

    @ResponseBody
    @RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
    public ApiResponse<ChatMessageDTO, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
	    throws InterruptedException {

	ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

	// Session Stuff Logging <
	if (ArgUtil.isEmpty(sessionDoc.getAssignedToAgent())) {
	    AgentSessionDoc agent = mongoTemplate.findById(agentSession.getAgentCode(), AgentSessionDoc.class);
	    agentChatHandlerImpl.onAssign(agent, sessionDoc);
	}

	// Session Stuff Logging >
	if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
	    ChatMessageDTO messageDto = agentService.sendMessage(sessionDoc, outboxMessage);

	    // Evaluate if required
	    messageDto.setName(agentSession.getAgentCode());
	    // messageDto.setType(outboxMessage.getType());
	    messageDto.setText(outboxMessage.getMessage());
	    messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());

	    // messageDto.setMessageIdExt(outboxMessage.getMessageIdExt());
	    // messageDto.setMessageId(outboxMessage.getMessageId());

	    agentSessionService.refreshOnline();
	    return ApiResponse.buildResult(messageDto);
	} else {
	    agentSessionService.refreshOnline();
	    return new ApiResponse<ChatMessageDTO, Object>().message("Only assignee can respond to chat.");
	}

    }

    @Autowired
    AWSFileStore fileStore;

    @Autowired
    PMFileStoreClient pmFileStoreClient;

    @ResponseBody
    @RequestMapping(value = "/api/sessions/message/upload", method = { RequestMethod.POST })
    public ApiResponse<ChatMessageDTO, Object> uploadSessionFile(@RequestParam String message,
	    @RequestParam(name = "file") MultipartFile file) throws InterruptedException {
	OutboxMessage outboxMessage = JsonUtil.parse(message, OutboxMessage.class);

	CommonFile f = pmFileStoreClient.uploadSessionFile(file, outboxMessage.getSessionId(),
		outboxMessage.getMessageIdRef());

	outboxMessage.attachment(new Attachment().mediaURL(f.getUrl()).mediaType(f.getFileType())
		.mediaCaption(ArgUtil.nonEmpty(outboxMessage.getSubject(), file.getOriginalFilename())));

	return sendSessionMessage(outboxMessage);
    }

    @ResponseBody
    @RequestMapping(value = "/api/sessions/contact", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, Object> getSessionsForContact(@RequestParam String contactId) {

	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();

	List<ChatSessionDoc> sessions = sessionStore.findSimilarChatSessionForContactId(contactId);
	for (ChatSessionDoc chatSessionDoc : sessions) {
	    ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
	    chatSessionDtos.add(chatSessionDto);
	}
	return ApiResponse.buildResults(chatSessionDtos,
		MapBuilder.map().put("isOnline", agentSession.isOnline()).build());
    }

    @ResponseBody
    @RequestMapping(value = "/api/sessions/contact/active", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, Object> getActiveSessionsForContact(@RequestParam String contactId) {
	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
	List<ChatSessionDoc> sessions = sessionStore.findActiveChatSessionForContactId(contactId);
	for (ChatSessionDoc chatSessionDoc : sessions) {
	    if (sessionStore.isSessionValid(chatSessionDoc)) {
		ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
		chatSessionDtos.add(chatSessionDto);
	    }
	}

	return ApiResponse.buildResults(chatSessionDtos);
    }

    @ResponseBody
    @RequestMapping(value = "/api/sessions/messages", method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
	chatSessionDto = chatArchive.getChatSession(chatSessionDto);
	chatSessionDto = chatArchive.withContact(chatSessionDto);
	return ApiResponse.buildResult(chatArchive.withMessages(chatSessionDto));
    }

    @Autowired
    AgentStore agentStore;

    @ResponseBody
    @RequestMapping(value = { "/api/session/agent", "/api/session/agent/assign" }, method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> assignAgent(@RequestParam String sessionId,
	    @RequestParam String agentId) {
	ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);
	AgentDoc agent = agentStore.findById(agentId);
	agentChatHandlerImpl.onAssign(agent, chatSessionDoc);
	ChatSessionDTO chatSessionDto = chatArchive.getChatSessionDto(chatSessionDoc, agentSession.getAgentCode());
	return ApiResponse.buildResult(chatSessionDto);
    }

    @ResponseBody
    @RequestMapping(value = { "/api/contact/label" }, method = { RequestMethod.POST })
    public ApiResponse<ContactDTO, Object> addContactLabel(@RequestParam String sessionId,
	    @RequestBody ListRequestModel<QuickLabel> labels) {
	ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
	ChatContactDoc contact = sessionStore.getContact(sessionDoc.getContactId());

	List<String> oldList = contact.labelId();
	List<String> newList = new ArrayList<String>();
	for (QuickLabel tag : labels.getValues()) {
	    newList.add(tag.getId());
	}
	newList = CollectionUtil.distinct(newList);
	contact.setLabelId(CollectionUtil.distinct(newList));
	sessionStore.save(contact);

	// LOGS
	List<String> removedItems = new ArrayList<String>(oldList);
	removedItems.removeAll(newList);
	if (ArgUtil.is(removedItems)) {
	    chatService.log(sessionDoc, EVENTS.LABEL_REMOVED, removedItems.toArray(new String[0]));
	}

	List<String> addedItems = new ArrayList<String>(newList);
	addedItems.removeAll(oldList);
	if (ArgUtil.is(addedItems)) {
	    chatService.log(sessionDoc, EVENTS.LABEL_ADDED, addedItems.toArray(new String[0]));
	}
	return ApiResponse.buildData(ChatDTOUtil.getContactDTO(contact));
    }

    @ResponseBody
    @RequestMapping(value = { "/api/session/status" }, method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> updateSessionStatus(@RequestParam String sessionId,
	    @RequestParam CHAT_STATUS status) {
	return ApiResponse.buildResult(agentChatHandlerImpl.updateChatSessionStatus(sessionId, status));
    }
}
