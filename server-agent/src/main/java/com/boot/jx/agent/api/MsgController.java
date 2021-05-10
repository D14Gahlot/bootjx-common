package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.doc.AgentSessionDoc;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.chat.ChatArchive;
import com.boot.jx.chat.ChatDTOUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
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
	public ApiResponse<ChatSessionDTO, Object> getSessionsAssignedToMe() {

		List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();

		if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
			List<ChatSessionDoc> sessions = sessionStore
					.findChatSessionDocByAgentAndUnAssigned(agentSession.getAgentCode(), agentSession.getAgentDept());

			for (ChatSessionDoc chatSessionDoc : sessions) {
				ChatSessionDTO chatSessionDto = chatArchive.getChatSessionDto(chatSessionDoc,
						agentSession.getAgentCode());
				chatSessionDtos.add(chatSessionDto);
			}
		}

		agentSessionService.refreshOnline();

		return ApiResponse.buildResults(chatSessionDtos,
				MapBuilder.map().put("isOnline", agentSession.isOnline()).build());
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/send", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> sendSessionMessage(@RequestBody OutboxMessage outboxMessage)
			throws InterruptedException {
		ChatSessionDoc sessionDoc = sessionStore.getSession(outboxMessage.getSessionId());

		// Session Stuff Logging <
		if (ArgUtil.isEmpty(sessionDoc.getAssignedToAgent())) {
			AgentSessionDoc agent = mongoTemplate.findById(agentSession.getAgentCode(), AgentSessionDoc.class);
			agentChatHandlerImpl.onAssign(agent, sessionDoc, outboxMessage);
		}

		if (ArgUtil.isNone(sessionDoc.getFistResponseStamp())) {
			sessionDoc.setFistResponseStamp(System.currentTimeMillis());
		}

		sessionDoc.setLastResponseStamp(System.currentTimeMillis());
		mongoTemplate.save(sessionDoc);
		// Session Stuff Logging >

		if (ArgUtil.areEqual(sessionDoc.getAssignedToAgent(), agentSession.getAgentCode())) {
			ChatMessageDTO messageDto = new ChatMessageDTO();
			messageDto.setName(agentSession.getAgentCode());
			agentService.sendMessage(sessionDoc, outboxMessage);
			messageDto.setType(outboxMessage.getType());
			messageDto.setMessageId(outboxMessage.getMessageId());
			messageDto.setMessageIdExt(outboxMessage.getMessageIdExt());
			messageDto.setText(outboxMessage.getMessage());
			messageDto.setMessageIdRef(outboxMessage.getMessageIdRef());
			return ApiResponse.buildResult(messageDto);
		}

		agentSessionService.refreshOnline();
		return null;
	}

	@Autowired
	AWSFileStore fileStore;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/message/upload", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> uploadSessionFile(@RequestParam String message,
			@RequestParam(name = "file") MultipartFile file) throws InterruptedException {
		OutboxMessage outboxMessage = JsonUtil.parse(message, OutboxMessage.class);
		CommonFile f = fileStore.upload2(file,
				String.format("%s/session/%s", AppContextUtil.getTenant(), outboxMessage.getSessionId()),
				String.format("%s_%s", outboxMessage.getMessageIdRef(), file.getOriginalFilename()));
		outboxMessage.attachment(new Attachment().mediaURL(f.getUrl()).mediaType(f.getFileType()));
		return sendSessionMessage(outboxMessage);
	}

	@ResponseBody
	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<QuickReply> listSmartReply(@RequestParam(value = "value", required = false) List<String> categories) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("category").in(categories.stream().toArray(String[]::new)));
		return mongoTemplate.find(query2, QuickReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.GET })
	public List<TemplateReply> listMediaReply() {
		return mongoTemplate.findAll(TemplateReply.class);
	}

	@ResponseBody
	@RequestMapping(value = "/gallery/map/quick_actions", method = { RequestMethod.GET })
	public List<QuickAction> listQuickActions() {
		return mongoTemplate.findAll(QuickAction.class);
	}

	@ResponseBody
	@RequestMapping(value = { "/gallery/map/quick_labels" }, method = { RequestMethod.GET })
	public List<QuickLabel> listQuickTags() {
		return mongoTemplate.findAll(QuickLabel.class);
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/contact", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDTO, Object> getSessionsForContact(@RequestParam String contactId) {

		List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();

		List<ChatSessionDoc> sessions = sessionStore.findChatSessionContactId(contactId);
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
			chatSessionDtos.add(chatSessionDto);
		}
		return ApiResponse.buildResults(chatSessionDtos,
				MapBuilder.map().put("isOnline", agentSession.isOnline()).build());
	}

	@ResponseBody
	@RequestMapping(value = "/api/sessions/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
		return ApiResponse.buildResults(chatArchive.getMessages(chatSessionDto));
	}

	@ResponseBody
	@RequestMapping(value = { "/api/contact/label" }, method = { RequestMethod.POST })
	public ApiResponse<ContactDTO, Object> addContactTag(@RequestParam String sessionId,
			@RequestBody ListRequestModel<QuickLabel> tags) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		ChatContactDoc contact = sessionStore.getContact(sessionDoc.getContactId());

		List<String> oldList = contact.labelId();
		List<String> newList = new ArrayList<String>();
		for (QuickLabel tag : tags.getValues()) {
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
}
