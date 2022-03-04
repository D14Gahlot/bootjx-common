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
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.manager.LogManager;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;

@Controller
public class MsgController {

    @Autowired
    private SessionStore sessionStore;
    
    @Autowired
    ChatSessionManager chatSessionManager;

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
    private LogManager logManager;

    @Autowired
    private AgentSessionService agentSessionService;

    @ResponseBody
    @RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
    public ApiResponse<ChatMessageDTO, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
	    throws InterruptedException {

	ChatSessionDoc sessionDoc = sessionStore.createSession(outboxMessage);
	
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
    public ApiResponse<ChatSessionDTO, Object> getSessionsForContact(@RequestParam String contactId,
	    @RequestParam(defaultValue = "0", required = false) Long fromStamp,
	    @RequestParam(defaultValue = "0", required = false) Long toStamp) {

	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();

	List<ChatSessionDoc> sessions = sessionStore.findSimilarChatSessionForContactId(contactId, fromStamp, toStamp);
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
    private AgentStore agentStore;

    @ResponseBody
    @RequestMapping(value = { "/api/session/agent", "/api/session/agent/assign" }, method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> assignAgent(@RequestParam String sessionId,
	    @RequestParam String agentId) {
	ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);
	AgentDoc agent = agentStore.findById(agentId);
	agentChatHandlerImpl.onAssign(agent, chatSessionDoc);
	ChatSessionDTO chatSessionDto = chatArchiveBuilder.sessionDTO().from(chatSessionDoc).withContact()
		.isAssigned(agentSession.getAgentCode()).withMessages().get();
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
	    logManager.event(sessionDoc, EVENTS.LABEL_REMOVED, removedItems.toArray(new String[0]));
	}

	List<String> addedItems = new ArrayList<String>(newList);
	addedItems.removeAll(oldList);
	if (ArgUtil.is(addedItems)) {
	    logManager.event(sessionDoc, EVENTS.LABEL_ADDED, addedItems.toArray(new String[0]));
	}
	return ApiResponse.buildData(ChatDTOUtil.getContactDTO(contact));
    }

    @ResponseBody
    @RequestMapping(value = { "/api/session/status" }, method = { RequestMethod.POST })
    public ApiResponse<ChatSessionDTO, Object> updateSessionStatus(@RequestParam String sessionId,
	    @RequestParam PMConstants.CHAT_STATUS status) {
	return ApiResponse.buildResult(agentChatHandlerImpl.updateChatSessionStatus(sessionId, status));
    }

    /*
     * search by status
     * 
     * @ResponseBody
     * 
     * @RequestMapping(value = "/api/sessions/searchby/status", method = {
     * RequestMethod.GET }) public ApiResponse<ChatSessionDTO, Object>
     * getByStatus(@RequestParam(required = false,defaultValue ="OPEN") CHAT_STATUS
     * status) { List<ChatSessionDTO> chatSessionDtos = new
     * ArrayList<ChatSessionDTO>(); List<ChatSessionDoc> sessions =
     * sessionStore.findByStatus(status); for (ChatSessionDoc chatSessionDoc :
     * sessions) { ChatSessionDTO chatSessionDto =
     * chatArchive.withContact(chatSessionDoc); chatSessionDtos.add(chatSessionDto);
     * 
     * }
     * 
     * return ApiResponse.buildResults(chatSessionDtos); } // search by tagCategory
     * 
     * @ResponseBody
     * 
     * @RequestMapping(value = "/api/sessions/searchby/category", method = {
     * RequestMethod.GET }) public ApiResponse<ChatSessionDTO, Object>
     * getByTagCategory(@RequestParam(required= false) String tagCategory) {
     * List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
     * List<ChatSessionDoc> sessions = sessionStore.findByTagCategory(tagCategory);
     * for (ChatSessionDoc chatSessionDoc : sessions) { ChatSessionDTO
     * chatSessionDto = chatArchive.withContact(chatSessionDoc);
     * chatSessionDtos.add(chatSessionDto);
     * 
     * } return ApiResponse.buildResults(chatSessionDtos); }
     */
    /** search by status or tagCategory **/
    @ResponseBody
    @Deprecated
    @RequestMapping(value = "/api/sessions/searchby/statusorcategory", method = { RequestMethod.GET })
    public ApiResponse<ChatSessionDTO, Object> getByStatusOrCategory(
	    @RequestParam(required = false) List<CHAT_STATUS> status,
	    @RequestParam(required = false) List<String> tagCategory, @RequestParam(required = false) long dateRange1,
	    @RequestParam(required = false) long dateRange2) {
	List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
	List<ChatSessionDoc> sessions = sessionStore.findByStatusOrQuickTag(status, tagCategory, dateRange1,
		dateRange2);
	for (ChatSessionDoc chatSessionDoc : sessions) {
	    ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
	    chatSessionDtos.add(chatSessionDto);
	}
	return ApiResponse.buildResults(chatSessionDtos);
    }

}
