package com.boot.jx.admin.api;

import java.util.List;
import java.util.Map;

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

import com.boot.jx.admin.manager.ChatParserAndImportor;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatArchive;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@RestController
public class AdminMsgController {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ChatArchive chatArchive;

	@Autowired
	private ChatParserAndImportor chatParseManager;

	@Autowired
	private SessionStore sessionStore;

	@RequestMapping(value = "/api/message/session", method = { RequestMethod.GET })
	public ApiResponse<ChatSessionDoc, Object> fetchSession(@RequestParam String startStamp,
			@RequestParam String endStamp,

			@RequestParam(required = false) String agentCode, @RequestParam(required = false) String contactType) {
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
		// System.out.println(query2.toString());
		List<ChatSessionDoc> messages = mongoTemplate.find(query2, ChatSessionDoc.class);
		return ApiResponse.buildResults(messages);
	}

	@RequestMapping(value = "/api/message/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
		chatSessionDto = chatArchive.withContact(chatSessionDto);
		chatSessionDto = chatArchive.withMessages(chatSessionDto);
		return ApiResponse.buildData(chatSessionDto);
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
}
