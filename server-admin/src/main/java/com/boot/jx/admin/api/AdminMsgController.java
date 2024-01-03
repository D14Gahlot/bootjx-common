package com.boot.jx.admin.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;
import com.boot.jx.admin.dto.SessionSearchRequest;
import com.boot.jx.admin.manager.CSVHelper;
import com.boot.jx.admin.manager.ChatParserAndImportor;
import com.boot.jx.admin.service.BulkMessageService;
import com.boot.jx.admin.service.CSVService;
import com.boot.jx.admin.service.TestMessageService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.doc.GroupDoc;
import com.boot.jx.common.doc.ImportChatSessionDoc;
import com.boot.jx.common.dto.GroupSessionDto;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.manager.ChatSessionManager;
import com.boot.jx.postman.manager.StarterDocKit;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.task.JobTaskModel;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;
import com.google.i18n.phonenumbers.NumberParseException;

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
	private MessageStore messageStore;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	public StarterDocKit starterDocKit;

	@Autowired
	public CSVService fileService;

	@Autowired
	public ChatSessionManager chatSessionManager;

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
		;
		if ((status == null || status.isEmpty() || status.contains(null))) {
			LOGGER.info("status :" + status);
		} else {
			for (CHAT_STATUS chatSt : status) {
				statusLst.add(chatSt.toString());
			}
		}

//		if (statusLst != null && !statusLst.isEmpty()) {
//			query2.addCriteria(Criteria.where("status").in(statusLst));
//		}
//		if (tagCategory != null && !tagCategory.isEmpty() && !tagCategory.contains(null) && !tagCategory.contains("")) {
//			query2.addCriteria(Criteria.where("tagId").in(tagCategory));
//		}

		query2 = query2.addCriteria(criteria).with(new Sort(Sort.Direction.DESC, "startSessionStamp"));
		sessions = mongoTemplate.find(query2, ChatSessionDoc.class);

		List<ChatSessionDoc> statusDocLst = new ArrayList<>();
		List<ChatSessionDoc> tagLst = new ArrayList<>();

		if (ArgUtil.is(statusLst)) {
			for (ChatSessionDoc doc : sessions) {
				for (String sts : statusLst) {
					if (doc.getStatus() != null && doc.getStatus().equalsIgnoreCase(sts)) {
						statusDocLst.add(doc);
					}
				}
			}
		}

		if (ArgUtil.is(tagCategory)) {
			for (ChatSessionDoc doc : sessions) {

				if (ArgUtil.is(doc.getTagId())) {
					List<String> docTagIdList = doc.getTagId();
					Collections.sort(docTagIdList);
					boolean booTag = tagCategory.stream().filter(element -> docTagIdList.contains(element)).findFirst()
							.isPresent();
					if (booTag) {
						if (statusDocLst != null && !statusDocLst.contains(doc)) {
							tagLst.add(doc);
						}

					}
				}
			}
		}
		if (statusDocLst != null && !statusDocLst.isEmpty()) {
			messageSessnDocs.addAll(statusDocLst);
		}

		if (tagLst != null && !tagLst.isEmpty()) {
			messageSessnDocs.addAll(tagLst);
		}

		if (messageSessnDocs == null || messageSessnDocs.isEmpty()) {
			messageSessnDocs.addAll(sessions);
		}
		/** end **/

		return ApiResponse.buildResults(messageSessnDocs);
	}

	@RequestMapping(value = "/api/message/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatSessionDTO, Object> getMessagesForSession(@RequestBody ChatSessionDTO chatSessionDto) {
		chatSessionDto = chatArchive.getChatSession(chatSessionDto);
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

	@Autowired
	private TestMessageService testMessageService;

	@Autowired
	private BulkMessageService bulkMessageService;

	@RequestMapping(value = "/api/message/test/push/send", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> sendTestMessage(@RequestBody OutboxMessage bulkMessage)
			throws NumberParseException {
		if (ArgUtil.is(bulkMessage.getReferenceKey())) {
			List<OutboxMessage> lstOutBoxMsg = getCsvData(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendMultiple(lstOutBoxMsg);
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		}else if(ArgUtil.is(bulkMessage.getGroupId())) {
			List<OutboxMessage> lstOutBoxMsg = getGroupDetails(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendMultiple(lstOutBoxMsg);
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		}else {
			return ApiResponse.buildResult(testMessageService.send(bulkMessage)).message("Bulk Message Job Created");
		}
	}

	@RequestMapping(value = "/api/message/bulk/push/send", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> sendBulkMessage(@RequestBody OutboxMessage bulkMessage)
			throws NumberParseException {
		if (ArgUtil.is(bulkMessage.getReferenceKey())) {
			List<OutboxMessage> lstOutBoxMsg = getCsvData(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendMultiple(lstOutBoxMsg);
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		} else {
			return ApiResponse.buildResult(bulkMessageService.send(bulkMessage)).message("Bulk Message Job Created");
		}
	}

	@RequestMapping(value = "/api/message/bulk/push/retry", method = { RequestMethod.POST })
	public ApiResponse<Object, Object> sendBulkMessage(@RequestParam String jobId, @RequestParam String action)
			throws NumberParseException {

		if (ArgUtil.is(action, "refresh")) {
			bulkMessageService.refreshJob(jobId);
		} else if (ArgUtil.is(action, "reset")) {
			bulkMessageService.resetJob(jobId);
		} else if (ArgUtil.is(action, "stop")) {
			bulkMessageService.stopJob(jobId);
		} else if (ArgUtil.is(action, "tally")) {
			BulkSessionDoc session = mongoTemplate.findById(jobId, BulkSessionDoc.class);
			BatchJob job = session.getJob();
			if (ArgUtil.not(job)) {
				job = JobTaskModel.newBatchJob()
						// Set Unique Job Id
						.jobId(session.getBulkSessionId())
						// Contact Type for each message
						.data("contactType", session.getContactType())
						// Channel for each message
						.data("channelType", session.getChannelId())
						// Lane for each message
						.data("lane", session.getLane());
			}
			bulkMessageService.tally(job);
		}
		return ApiResponse.build().message("Bulk Message Job [" + action + "]");
	}

	@RequestMapping(value = "/api/message/bulk/push/logs", method = { RequestMethod.GET })
	public ApiResponse<BulkSessionDoc, Object> getBulkSession(@RequestParam String startStamp,
			@RequestParam String endStamp, @RequestParam(required = false) String bulkSessionId)
			throws NumberParseException {
		if (ArgUtil.is(bulkSessionId)) {
			return ApiResponse.buildResults(mongoTemplate
					.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));
		}
		return ApiResponse.buildResults(mongoTemplate
				.find(new Query().with(new Sort(Sort.Direction.DESC, "createdStamp")), BulkSessionDoc.class));
	}

	@RequestMapping(value = "/api/message/bulk/push/messages", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, BulkSessionDoc> getBulkMessages(@RequestParam String bulkSessionId)
			throws NumberParseException {
		ApiResponse<ChatMessageDTO, BulkSessionDoc> resp = ApiResponse.instance(ChatMessageDTO.class,
				BulkSessionDoc.class);

		BulkSessionDoc session = CollectionUtil.getOne(mongoTemplate
				.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));
		resp.setMeta(session);

		if (ArgUtil.is(session)) {
			List<MessageDoc> msgs = messageStore.findByBulkSessionId(session.getBulkSessionId(), session.contactType());
			resp.results(ChatDTOUtil.getChatMessageDTO(msgs, null, session.getCreatedBy()));
		}
		return resp;
	}

	/** search by status or tagCategory **/
	@ResponseBody
	@RequestMapping(value = "/api/message/sessions/searchby/statusorcategory", method = { RequestMethod.GET })
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

	/** upload csv file **/
	@RequestMapping(value = "/pub/message/bulk/push/csv/read", method = { RequestMethod.POST })
	public ApiResponse<CsvDto, Object> sendBulkCsvMessage(@RequestParam(required = true) String templateId,
			@RequestParam("file") MultipartFile file) throws NumberParseException {
		String message = "";
		CsvDto lst = null;
		if (CSVHelper.hasCSVFormat(file)) {
			try {
				lst = fileService.save(templateId, file);
				message = "Uploaded the file successfully: " + file.getOriginalFilename();
				OutboxMessage outboxMessage = new OutboxMessage();
				outboxMessage.setReferenceKey(lst.getReferenceKey());
				// getCsvData(outboxMessage);
				return ApiResponse.buildResult(lst).message(message);
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ApiResponse.buildResult(lst).message(message);
			}
		} else if (CSVHelper.hasExcelFormat(file)) {
			try {
				lst = fileService.readExcel(templateId, file);
				message = "Uploaded the file successfully: " + file.getOriginalFilename();
				OutboxMessage outboxMessage = new OutboxMessage();
				outboxMessage.setReferenceKey(lst.getReferenceKey());
				return ApiResponse.buildResult(lst).message(message);
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ApiResponse.buildResult(lst).message(message);
			}

		} else if (CSVHelper.hasExcelSXFormat(file)) {

			try {
				lst = fileService.readExcelXS(templateId, file);
				message = "Uploaded the file successfully: " + file.getOriginalFilename();
				OutboxMessage outboxMessage = new OutboxMessage();
				outboxMessage.setReferenceKey(lst.getReferenceKey());
				return ApiResponse.buildResult(lst).message(message);
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ApiResponse.buildResult(lst).message(message);
			}
		} else {
			message = "Please upload a csv or excel file!";
			return ApiResponse.buildResult(lst).message(message);
		}
	}

	public List<OutboxMessage> getCsvData(OutboxMessage outboxMessage) {
		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();
		if (outboxMessage != null) {
			String csvRefKeyId = outboxMessage.getReferenceKey();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			CsvDto csvDoc = mongoTemplate.findById(csvRefKeyId, CsvDto.class);
			if (ArgUtil.is(csvDoc)) {
				List<Map<Object, Object>> lstMap = csvDoc.getLstMap();
				for (Map<Object, Object> map : lstMap) {
					OutboxMessage outboxMsg = new OutboxMessage();
					CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
					hsmTemp.setId(hsmId);
					outboxMsg.setMessage(otBoxMsg.getMessage());
					outboxMsg.setAttachments(otBoxMsg.getAttachments());
					outboxMsg.setContact(otBoxMsg.getContact());
					Map<String, Object> data = new HashMap<>();
					for (Map.Entry<Object, Object> entry : map.entrySet()) {
						String k = ArgUtil.parseAsString(entry.getKey());
						String v = ArgUtil.parseAsString(entry.getValue());
						if (ArgUtil.parseAsString(k).equalsIgnoreCase("contacts")) {
							outboxMsg.setTo(Arrays.asList(v.toString()));
						} else if (ArgUtil.is(k)) {
							data.put(ArgUtil.parseAsString(k), v);
						}
					}
					if (data != null && !data.isEmpty()) {
						hsmTemp.setData(data);
					}
					outboxMsg.setHsm(hsmTemp);

					listOfOutboxMsg.add(outboxMsg);
				} // end of listOfOutboxMsgs

			}

		}
		return listOfOutboxMsg;
	}
	
	/** fetch group details **/
	public List<OutboxMessage> getGroupDetails(OutboxMessage outboxMessage) {
		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();
		if (outboxMessage != null) {
			String groupId = outboxMessage.getGroupId();
			String groupTitle=outboxMessage.getGroupTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			GroupDoc groupDoc = mongoTemplate.findById(groupId, GroupDoc.class);
			if (ArgUtil.is(groupDoc)) {
				List<GroupSessionDto> lstDto = groupDoc.getSessions();
				for (GroupSessionDto dto : lstDto) {
					OutboxMessage outboxMsg = new OutboxMessage();
					CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
					hsmTemp.setId(hsmId);
					outboxMsg.setGroupId(groupId);
					outboxMsg.setGroupTitle(groupTitle);
					outboxMsg.setMessage(otBoxMsg.getMessage());
					outboxMsg.setAttachments(otBoxMsg.getAttachments());
					outboxMsg.setContact(otBoxMsg.getContact());
					outboxMsg.setTo(Arrays.asList(dto.getPhone()));
					outboxMsg.setHsm(hsmTemp);
					listOfOutboxMsg.add(outboxMsg);
				} // end of listOfOutboxMsgs

			}

		}
		return listOfOutboxMsg;
	}

}
