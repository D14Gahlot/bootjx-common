package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.agent.AgentChatHandlerImpl;
import com.boot.jx.agent.AgentService;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.api.ControllerRequestDTOs.ChatTagUpdateRequest;
import com.boot.jx.agent.api.ControllerRequestDTOs.SessionSearchRequest;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.common.store.ChatArchiveBuilder;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_ASSIGN_GROUP;
import com.boot.jx.postman.PMConstants.CHAT_SESSION_ACTIONS;
import com.boot.jx.postman.PMConstants.CHAT_STATE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.MapBuilder;

@RestController
public class AgCProfileController {

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private AgentSessionBean agentSession;

	@Autowired
	private ChatArchiveService chatArchive;

	@Autowired
	private AgentSessionService agentSessionService;

	@Autowired
	private ChatSessionManager chatSessionManager;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@Autowired
	private AgentService agentService;

	@Autowired
	private ChatArchiveBuilder chatArchiveBuilder;

	@Autowired
	private AgentChatHandlerImpl agentChatHandlerImpl;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private ChatLogger logManager;

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

}
