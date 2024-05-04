package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.agent.AgentSessionService;
import com.boot.jx.agent.api.ControllerRequestDTOs.SessionSearchRequest;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.postman.PMConstants.CHAT_ASSIGN_GROUP;
import com.boot.jx.postman.PMConstants.CHAT_STATE;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;

@RestController
public class AgMainController {

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
	private ChatSessionFactory chatSessionFactory;

	private ApiResponse<ChatSessionDTO, AgentSessionDoc> getSessionAssignments(boolean withMessage, Boolean status,
			Boolean away, List<ChatSessionDTO> chatSessionDtos, SessionSearchQuery query) {
		if (agentSession.isLoggedIn() && ArgUtil.is(agentSession.getAgentDept())) {
			List<ChatSessionDoc> sessions = chatSessionManager.findChatSessionDocByAgentAndUnAssigned(query,
					agentSession.getAgentCode(), agentSession.getAgentDept());
			for (ChatSessionDoc chatSessionDoc : sessions) {
				ChatSessionDTO chatSessionDto = chatArchive.getChatSession(chatSessionDoc);
				// chatSessionDto = chatArchive.withContact(chatSessionDto);
				if (withMessage
						&& ArgUtil.isEqual(chatSessionDto.getAssignedToDept(), DEFAULT.NO_DEPT,
								agentSession.getAgentDept(), null, Constants.BLANK)
						&& ArgUtil.isEqual(chatSessionDto.getAssignedToAgent(), agentSession.getAgentCode(), null)) {
					chatSessionDto = chatArchive.withMessages(chatSessionDto);
				}else if(ArgUtil.isEqual(chatSessionDto.getAssignedToDept(), DEFAULT.NO_DEPT,
							agentSession.getAgentDept(), null, Constants.BLANK)
					&& ArgUtil.isEqual(chatSessionDto.getAssignedToAgent(), agentSession.getAgentCode(), null)) { //added by mru for test
				chatSessionDto = chatArchive.withMessages(chatSessionDto);
				}
				
				chatSessionDtos.add(chatSessionDto);
				
			}
		}
		if (away != null) {
			agentSessionService.setAway(away.booleanValue());
		}
		if (status != null) {
			agentSessionService.setOnline(status.booleanValue());
		}
		return new ApiResponse<ChatSessionDTO, AgentSessionDoc>().results(chatSessionDtos)
				.details(agentSessionService.getAgentSessions()).query(query);
	}

	@ApiRequest(type = RequestType.POLL)
	@RequestMapping(value = "/api/sessions/assignments", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDTO, AgentSessionDoc> getSessionsAssignments(
			@RequestParam(defaultValue = "false") boolean withMessage, @RequestParam(required = false) Boolean status,
			@RequestParam(required = false) Boolean away,
			@RequestParam(required = false, defaultValue = "HISTORY") String tab,
			@RequestParam(required = false) String search, @RequestParam(required = false) String searchStatus,
			@RequestParam(required = false, defaultValue = "0") int limit) {
		SessionSearchQuery query = new SessionSearchQuery();
		query.parse(search);
		query.limit = limit;
		query.add(ArgUtil.parseAsEnumT(tab, CHAT_ASSIGN_GROUP.class));
		query.add(ArgUtil.parseAsEnumT(searchStatus, CHAT_STATE.class));
		//System.out.println("query MRU SEARCH "+JsonUtil.toJsonPrettyPrint(query));
		return getSessionAssignments(withMessage, status, away, new ArrayList<ChatSessionDTO>(), query);
	}

	@ApiRequest(type = RequestType.POLL)
	@RequestMapping(value = "/api/sessions/assignments", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, AgentSessionDoc> getSessionsAssignments(@RequestBody SessionSearchQuery query,
			@RequestParam(defaultValue = "false") boolean withMessage, @RequestParam(required = false) Boolean status,
			@RequestParam(required = false) Boolean away, @RequestParam(required = false) String search) {
		List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
		query.parse(search);
		return getSessionAssignments(withMessage, status, away, new ArrayList<ChatSessionDTO>(), query);
	}

	@RequestMapping(value = { "/api/session/compose" }, method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDTO, ContactDTO> sessionCompose(@RequestParam String contactId) {
		ApiResponse<ChatSessionDTO, ContactDTO> resp = ApiResponse.build();
		ChatContactDoc chatContactDoc = sessionStore.getContact(contactId);
		Contactable contact = PostManUtil.getContactMeta(chatContactDoc);
		resp.meta(ChatDTOUtil.getContactDTO(chatContactDoc));
		if (PostManUtil.IS_SINGLE_THREAD(contact.getChannelType())) {
			ChatSessionDoc sessionDoc = chatSessionFactory.getChatSessionByContactId(contactId, null);
			if (ArgUtil.is(sessionDoc)) {
				ChatSessionDTO chatSessionDto = chatArchive.getChatSession(sessionDoc);
				resp.result(chatSessionDto);
			}
		}
		return resp;
	}

	@Deprecated
	@ResponseBody
	@RequestMapping(value = "/api/sessions/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
		chatSessionDto = chatArchive.getChatSession(chatSessionDto);
		chatSessionDto = chatArchive.withContact(chatSessionDto);
		return ApiResponse.buildResult(chatArchive.withMessages(chatSessionDto));
	}

	@RequestMapping(value = "/api/sessions/search", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> searchSessions(@RequestBody SessionSearchRequest query) {
		List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
		List<ChatSessionDoc> sessions = chatSessionManager.searchBy(query.status, query.tags, query.fromStamp,
				query.toStamp);
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDTO chatSessionDto = chatArchive.withContact(chatSessionDoc);
			chatSessionDtos.add(chatSessionDto);
		}
		/**
		 * remove duplicate /multiple Session for each contact we can filter based on
		 * name , phone number on any field
		 **/
		if (chatSessionDtos != null && !chatSessionDtos.isEmpty()) {
			Set<String> chatSessionSet = new HashSet<>();
			chatSessionDtos = chatSessionDtos.stream().filter(e -> chatSessionSet.add(e.getPhone()))
					.collect(Collectors.toList());
		}
		return ApiResponse.buildResults(chatSessionDtos);
	}

	@RequestMapping(value = "/api/sessions/primary", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> searchPrimarySessions(@RequestBody SessionSearchRequest query) {
		List<ChatSessionDTO> chatSessionDtos = new ArrayList<ChatSessionDTO>();
		List<ChatSessionDoc> sessions = chatSessionManager.searchPrimary(query.text);
		for (ChatSessionDoc chatSessionDoc : sessions) {
			ChatSessionDTO chatSessionDto = chatArchive.getChatSession(chatSessionDoc);
			chatSessionDtos.add(chatSessionDto);
		}
		return ApiResponse.buildResults(chatSessionDtos);
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

	/**
	 * 
	 * @deprecated - path - /api/sessions/contact
	 * 
	 * @param contactId
	 * @param fromStamp
	 * @param toStamp
	 * @return
	 */
	@RequestMapping(value = { "/api/sessions/by/contact", "/api/sessions/contact" }, method = { RequestMethod.GET })
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

}
