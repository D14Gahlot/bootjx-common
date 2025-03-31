package com.boot.jx.admin.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.SessionSearchRequest;
import com.boot.jx.admin.manager.ChatParserAndImportor;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.doc.ImportChatSessionDoc;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.PMEnvironment.SummaryView;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AdminMsgController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminMsgController.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatArchiveService chatArchive;

	@Autowired
	private ChatParserAndImportor chatParseManager;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionManager chatSessionManager;

	@RequestMapping(value = "/api/message/session", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDoc, Object> fetchSession(@RequestParam String startStamp,
			@RequestParam String endStamp, @RequestParam(required = false) String agentCode,
			@RequestParam(required = false) String contactType) {
		Query query2 = new Query();

		Criteria criteria = new Criteria();
		Long startStampLong = ArgUtil.parseAsLong(startStamp);
		Long endStampLong = ArgUtil.parseAsLong(endStamp);
		Criteria dateCriteria = new Criteria().orOperator(
				new Criteria().andOperator(Criteria.where("startSessionStamp").gt(startStampLong),
						Criteria.where("startSessionStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("closeSessionStamp").gt(startStampLong),
						Criteria.where("closeSessionStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("assignedDeptStamp").gt(startStampLong),
						Criteria.where("assignedDeptStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("assignedAgentStamp").gt(startStampLong),
						Criteria.where("assignedAgentStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("fistResponseStamp").gt(startStampLong),
						Criteria.where("fistResponseStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("lastResponseStamp").gt(startStampLong),
						Criteria.where("lastResponseStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("lastInComingStamp").gt(startStampLong),
						Criteria.where("lastInComingStamp").lt(endStampLong)));

		criteria.andOperator(dateCriteria);

		if (ArgUtil.is(agentCode)) {
			criteria.and("assignedToAgent").is(agentCode);
		}
		query2 = query2.addCriteria(criteria).with(new Sort(Sort.Direction.DESC, "startSessionStamp"));
		List<ChatSessionDoc> messages = mongoTemplate.find(query2, ChatSessionDoc.class);

		return ApiResponse.buildResults(messages);
	}

	@JsonView(SummaryView.class)
	@RequestMapping(value = "/api/message/v2/session", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDoc, Object> fetchSessionV2(@RequestBody SessionSearchQuery query) {

		long startStampLong = query.fromStamp;
		long endStampLong = query.toStamp;

		Criteria dateCriteria = new Criteria().orOperator(
				new Criteria().andOperator(Criteria.where("startSessionStamp").gt(startStampLong),
						Criteria.where("startSessionStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("closeSessionStamp").gt(startStampLong),
						Criteria.where("closeSessionStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("assignedDeptStamp").gt(startStampLong),
						Criteria.where("assignedDeptStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("assignedAgentStamp").gt(startStampLong),
						Criteria.where("assignedAgentStamp").lt(endStampLong)),
				
				new Criteria().andOperator(Criteria.where("agentSessionStamp").gt(startStampLong),
						Criteria.where("agentSessionStamp").lt(endStampLong)),
				
				new Criteria().andOperator(Criteria.where("fistResponseStamp").gt(startStampLong),
						Criteria.where("fistResponseStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("lastResponseStamp").gt(startStampLong),
						Criteria.where("lastResponseStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("lastInComingStamp").gt(startStampLong),
						Criteria.where("lastInComingStamp").lt(endStampLong)));
		
		

		if (ArgUtil.is(query.text)) {
			dateCriteria.orOperator(
					// Check all fields
					Criteria.where("contactId").regex("" + query.text + "", "i"),
					Criteria.where("contactName").regex("" + query.text + "", "i"), // @Deprecated
					Criteria.where("contact.name").regex("" + query.text + "", "i"),
					Criteria.where("contact.phone").regex("" + query.text + "", "i"),
					Criteria.where("contact.email").regex("" + query.text + "", "i"));
		}

		List<ChatSessionDoc> sessions = chatSessionManager.findChatSessionsByQuery(query, dateCriteria);
		return ApiResponse.buildResults(sessions);
	}

	@RequestMapping(value = "/api/message/v1/session", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDoc, Object> fetchSessionV1(@RequestBody SessionSearchRequest query) {
		LOGGER.info("fetchSessionV1 :" + JsonUtil.toJson(query));
		List<ChatSessionDoc> messageSessnDocs = new ArrayList<ChatSessionDoc>();
		List<ChatSessionDoc> sessions = null;
		// sessions = chatSessionManager.searchByV1(query.status, query.tags,
		// query.fromStamp,query.toStamp);
		sessions = new ArrayList<ChatSessionDoc>();

		Long startStampLong = query.fromStamp;
		Long endStampLong = query.toStamp;

		Criteria criteria = new Criteria();

		Query query2 = new Query();

		Criteria dateCriteria = new Criteria().orOperator(
				new Criteria().andOperator(Criteria.where("startSessionStamp").gt(startStampLong),
						Criteria.where("startSessionStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("closeSessionStamp").gt(startStampLong),
						Criteria.where("closeSessionStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("assignedDeptStamp").gt(startStampLong),
						Criteria.where("assignedDeptStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("assignedAgentStamp").gt(startStampLong),
						Criteria.where("assignedAgentStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("fistResponseStamp").gt(startStampLong),
						Criteria.where("fistResponseStamp").lt(endStampLong)),
				new Criteria().andOperator(Criteria.where("lastResponseStamp").gt(startStampLong),
						Criteria.where("lastResponseStamp").lt(endStampLong)),

				new Criteria().andOperator(Criteria.where("lastInComingStamp").gt(startStampLong),
						Criteria.where("lastInComingStamp").lt(endStampLong)));

		criteria.andOperator(dateCriteria);

		if (ArgUtil.is(query.agantCode)) {
			criteria.and("assignedToAgent").is(query.agantCode);
		}

		/** start **/

		List<String> tagCategory = new ArrayList<String>();
		for (QuickTag tag : query.tags) {
			tagCategory.add(tag.getId());
		}
		Collections.sort(tagCategory);
		List<CHAT_STATUS> status = query.status;
		List<String> statusLst = new ArrayList<>();
		if ((status == null || status.isEmpty() || status.contains(null))) {
			LOGGER.info("status :" + status);
		} else {
			for (CHAT_STATUS chatSt : status) {
				statusLst.add(chatSt.toString());
			}
		}

		if (statusLst != null && !statusLst.isEmpty()) {
			query2.addCriteria(Criteria.where("status").in(statusLst));
		}

		if (tagCategory != null && !tagCategory.isEmpty() && !tagCategory.contains(null) && !tagCategory.contains("")) {
			query2.addCriteria(Criteria.where("tagId").in(tagCategory));
			// query2.addCriteria(Criteria.where("tagId").is(tagCategory));
		}

		query2 = query2.addCriteria(criteria).with(new Sort(Sort.Direction.DESC, "startSessionStamp"));
		sessions = mongoTemplate.find(query2, ChatSessionDoc.class);
		return ApiResponse.buildResults(sessions);
	}

	@RequestMapping(value = "/api/message/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
		chatSessionDto = chatArchive.getChatSession(chatSessionDto);
		if (ArgUtil.is(chatSessionDto) && chatSessionDto.getTagId() != null && !chatSessionDto.getTagId().isEmpty()) {
			List<String> tagCodeList = chatArchive.getTagCodeFromId(chatSessionDto.getTagId());
			chatSessionDto.setTagId(tagCodeList);
		}
		chatSessionDto = chatArchive.withContact(chatSessionDto);
		chatSessionDto = chatArchive.withMessages(chatSessionDto);
		return ApiResponse.buildData(chatSessionDto);
	}

	@RequestMapping(value = "/api/message/session/close", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDoc, Object> closeChatSesson(@RequestParam String sessionId) {
		ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);
		chatSessionService.closeSession(chatSessionDoc);
		return ApiResponse.buildData(chatSessionDoc);
	}

	@RequestMapping(value = "/api/message/session/route", method = { RequestMethod.POST })
	public ApiResponse<InBoundEvent, Object> routeChatSesson(@RequestParam String sessionId,
			@RequestParam(required = false, defaultValue = "") String queue) {
		InBoundEvent event = chatSessionService.routeSession(sessionId, new PMArgs().assignToQueueCode(queue));
		return ApiResponse.buildData(event);
	}

	@RequestMapping(value = "/api/message/session/remove", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDoc, Object> getChatDetails(@RequestBody ChatSessionDoc chatSessionDoc) {
		sessionStore.deleteSession(chatSessionDoc);
		return ApiResponse.buildData(chatSessionDoc);
	}

	@RequestMapping(value = "/api/message/session/parse", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Map<String, Object>> getChatDetails(
			@RequestParam(name = "file") MultipartFile file, @RequestParam String clientDate,
			@RequestParam(required = false) String clientDateFormat, @RequestParam ContactType contactType) {
		return chatParseManager.getChats(file, contactType, clientDate, clientDateFormat);
	}

	@RequestMapping(value = "/api/message/session/import", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Map<String, Object>> importChat(
			@RequestBody ApiResponse<ChatSessionDTO, Map<String, Object>> requestBody) {
		return chatParseManager.importChat(requestBody);
	}

	@RequestMapping(value = "/api/message/session/import/logs", method = { RequestMethod.GET })
	public ApiResponse<ImportChatSessionDoc, Object> importChatLogs() {
		return ApiResponse.buildResults(mongoTemplate
				.find(new Query().with(new Sort(Sort.Direction.DESC, "createdStamp")), ImportChatSessionDoc.class));
	}

	@RequestMapping(value = "/api/message/session/import/trash", method = { RequestMethod.POST })
	public ApiResponse<ImportChatSessionDoc, Object> importChatLogsDelete(@RequestBody ImportChatSessionDoc doc) {
		return chatParseManager.trashChat(doc);
	}

}