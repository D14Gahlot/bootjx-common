package com.boot.jx.admin.api;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.service.TestMessageService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.GroupDoc;
import com.boot.jx.common.dto.CsvDto;
import com.boot.jx.common.dto.GroupSessionDto;
import com.boot.jx.common.dto.ProfileSearchCriteria;
import com.boot.jx.common.dto.ProfileSearchQuery;
import com.boot.jx.common.helper.CSVHelper;
import com.boot.jx.common.service.CSVService;
import com.boot.jx.common.service.CustomerProfileService;
import com.boot.jx.common.store.ChatArchiveService;
import com.boot.jx.common.tasks.BulkMessageService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoStore.PaginatedQuery;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.ProfileFilterMasterDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.ChronoScheduler;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.model.MapModel;
import com.boot.model.UtilityModels.PublicJsonProperty;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.i18n.phonenumbers.NumberParseException;

@RestController
public class AdminMsgBulkController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminMsgBulkController.class);

	@Autowired
	private ChatArchiveService chatArchive;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private CSVService fileService;

	@Autowired
	private CustomerProfileService cusProfileService;

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private TestMessageService testMessageService;

	@Autowired
	private BulkMessageService bulkMessageService;

	@RequestMapping(value = "/api/message/test/push/send", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> sendTestMessage(@RequestBody OutboxMessage bulkMessage)
			throws NumberParseException {
		if (ArgUtil.is(bulkMessage.getReferenceKey())) {
			List<OutboxMessage> lstOutBoxMsg = getCsvData(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendMultiple(lstOutBoxMsg, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		} else if (ArgUtil.is(bulkMessage.getGroupId()) || ArgUtil.is(bulkMessage.getGroups())) {
			List<OutboxMessage> lstOutBoxMsg = getGroupDetails(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendToGroup(lstOutBoxMsg, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		} else {
			return ApiResponse.buildResult(testMessageService.send(bulkMessage)).message("Bulk Message Job Created");
		}
	}

	@RequestMapping(value = "/api/message/bulk/push/send", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> sendBulkMessage(@RequestBody OutboxMessage bulkMessage)
			throws Exception {
		if (ArgUtil.is(bulkMessage.getReferenceKey())) {
			List<OutboxMessage> lstOutBoxMsg = getCsvData(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendMultiple(lstOutBoxMsg, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}

		} else if (ArgUtil.is(bulkMessage.getGroupId()) || ArgUtil.is(bulkMessage.getGroups())) {
			List<OutboxMessage> lstOutBoxMsg = getGroupDetailsV1(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendToGroup(lstOutBoxMsg, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}

		} else if (ArgUtil.is(bulkMessage.getFilters())) {
			List<OutboxMessage> lstOutBoxMsg = getFilterDetails(bulkMessage);
			BulkSessionDoc bulkDoc = bulkMessageService.sendToFilterGroup(lstOutBoxMsg, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}

		} else {
			BulkSessionDoc bulkDoc = bulkMessageService.send(bulkMessage, bulkMessage.getScheduler());
			if (ArgUtil.is(bulkDoc)) {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Created");
			} else {
				return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");
			}
		}
	}

	/** reschedule action : use /resend with a flag 'cancelExisting' **/
	/** resend action : /resend > same req as /send along with bulkSessionId **/
	@RequestMapping(value = "/api/message/bulk/push/re-send", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> reSendBulkMessage(@RequestBody OutboxMessage bulkMessage)
			throws Exception {
		BulkSessionDoc bulkDoc = null;
		if (ArgUtil.is(bulkMessage) && ArgUtil.is(bulkMessage.getBulkSessionId())) {
			String bulkSessionId = bulkMessage.getBulkSessionId();
			bulkDoc = CollectionUtil.getOne(mongoTemplate
					.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));
			if (ArgUtil.is(bulkDoc) && ArgUtil.is(bulkDoc.getScheduler()) && bulkMessage.cancelExisting == true) {

				bulkMessageService.cancelScheduleJob(bulkDoc);
				bulkMessage.setScheduler(bulkMessage.getScheduler());
				bulkMessageService.reSend(bulkMessage, bulkDoc);
				// bulkMessageService.reSchedule(bulkDoc, bulkMessage.getScheduler());
				return ApiResponse.buildResult(bulkDoc).message("The bulk message job has been rescheduled");
			} else {
				if (ArgUtil.is(bulkMessage.getScheduler())) {
					bulkMessage.templateId(bulkDoc.getTemplateId());
					bulkMessage.message(bulkDoc.getMessage());
					bulkMessage.contact().setLane(bulkDoc.getLane());
					bulkMessage.contact().setContactId(bulkDoc.getChannelId());
					bulkMessage.contact().setContactType(bulkDoc.getContactType());
					bulkMessage.setCampaignTitle(bulkDoc.getCampaignTitle());

					if (ArgUtil.is(bulkDoc.getGroupId()) || ArgUtil.is(bulkDoc.getGroups())) {
						bulkMessage.setGroupId(bulkDoc.getGroupId());
						if (ArgUtil.isEmpty(bulkDoc.getGroups())) {
							bulkMessage.setGroups(Arrays.asList(bulkDoc.getGroupId()));
						}
						List<OutboxMessage> lstOutBoxMsg = getGroupDetailsV1(bulkMessage);
						bulkMessageService.sendToGroup(lstOutBoxMsg, bulkMessage.getScheduler());
					} else if (ArgUtil.is(bulkDoc.getFilters())) {
						bulkMessage.setFilters(bulkDoc.getFilters());
						List<OutboxMessage> lstOutBoxMsg = getFilterDetails(bulkMessage);
						bulkMessageService.sendToFilterGroup(lstOutBoxMsg, bulkMessage.getScheduler());
					} else {
						Query queryAll = new Query();
						queryAll.addCriteria(Criteria.where("type").in("O"));
						queryAll.addCriteria(Criteria.where("bulkSessionId").is(bulkDoc.getBulkSessionId()));
						queryAll.fields().include("contact.phone").include("messageId");
						List<MessageDoc> msgDoc = mongoTemplate.find(queryAll, MessageDoc.class,
								MessageDoc.COLLECTION_NAME + "_" + bulkDoc.getContactType().toString());
						List<String> to = new ArrayList<>();
						if (ArgUtil.is(msgDoc)) {
							msgDoc.forEach(doc -> to.add(doc.getContact().getPhone()));
							bulkMessage.setTo(to);
						}
						bulkMessageService.send(bulkMessage, bulkMessage.getScheduler());
					}

				}

				return ApiResponse.buildResult(bulkDoc).message("The bulk message job has been re-send");
			}
		}
		return ApiResponse.buildResult(bulkDoc).message("Bulk Message Job Failed");

	}

	@RequestMapping(value = "/api/message/bulk/push/cancel", method = { RequestMethod.POST })
	public ApiResponse<BulkSessionDoc, Object> cancelBulkMessage(@RequestParam String bulkSessionId) throws Exception {
		BulkSessionDoc bulkDoc = CollectionUtil.getOne(mongoTemplate
				.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));

		if (ArgUtil.is(bulkDoc) && ArgUtil.is(bulkDoc.getScheduler())) {
			boolean isSchValid = isScheduleTimeValid(bulkDoc);
			// Parse the interval to ZonedDateTime
			ChronoScheduler cSch = bulkDoc.getScheduler();
			// if (intervalTime.isAfter(currentTime)) {
			if (isSchValid == true) {
				bulkDoc = bulkMessageService.cancelScheduleJob(bulkDoc);
				return ApiResponse.buildResult(bulkDoc).message("The bulk message job has been canceled");
			}
			return ApiResponse.buildResult(bulkDoc).message("Scheduled time for bulk messages has passed");

		}
		return ApiResponse.buildResult(bulkDoc).message("The bulk message cancel job could not be found");
	}

	@RequestMapping(value = "/api/message/bulk/push/retry", method = { RequestMethod.POST })
	public ApiResponse<Object, Object> sendBulkMessage(@RequestParam String jobId, @RequestParam String action)
			throws NumberParseException {

		if (ArgUtil.is(action, "refresh")) {
			bulkMessageService.refreshJob(jobId);
		} else if (ArgUtil.is(action, "reset")) {
			bulkMessageService.resetJob(jobId);
		} else if (ArgUtil.is(action, "restart")) {
			bulkMessageService.resetJob(jobId);
		} else if (ArgUtil.is(action, "stop")) {
			bulkMessageService.stopJobV1(jobId);
		} else if (ArgUtil.is(action, "tally")) {
			BulkSessionDoc session = mongoTemplate.findById(jobId, BulkSessionDoc.class);
			BatchJob job = session.getJob();
			if (ArgUtil.not(job)) {
				job = bulkMessageService.job(jobId, MapModel.createInstance()
						// Contact Type for each message
						.put("contactType", session.getContactType())
						// Channel for each message
						.put("channelType", session.getChannelId())
						// Lane for each message
						.put("lane", session.getLane()));
			}
			bulkMessageService.tally(job);
		}
		return ApiResponse.build().message("Bulk Message Job [" + action + "]");
	}

	@Deprecated
	@RequestMapping(value = "/api/message/bulk/push/logs", method = { RequestMethod.GET })
	public ApiResponse<BulkSessionDoc, Object> getBulkSession(@RequestParam String startStamp,
			@RequestParam String endStamp, @RequestParam(required = false) String bulkSessionId)
			throws NumberParseException {

		Long startStampLong = ArgUtil.parseAsLong(startStamp);
		Long endStampLong = ArgUtil.parseAsLong(endStamp);
		Query query = new Query();
		List<BulkSessionDoc> lst = new ArrayList<>();
		if (ArgUtil.is(bulkSessionId)) {
			query.addCriteria(QueryCriteria.whereId(bulkSessionId));
			query.addCriteria(Criteria.where("createdStamp").gt(startStampLong).lt(endStampLong));
			query.with(new Sort(Sort.Direction.DESC, "createdStamp"));
			lst = mongoTemplate.find(query, BulkSessionDoc.class);
			lst = checkNull(lst);
			return ApiResponse.buildResults(lst);
		}
		query.addCriteria(Criteria.where("createdStamp").gt(startStampLong).lt(endStampLong));
		query.with(new Sort(Sort.Direction.DESC, "createdStamp"));
		lst = mongoTemplate.find(query, BulkSessionDoc.class);
		lst = checkNull(lst);
		return ApiResponse.buildResults(lst);

	}

	@RequestMapping(value = "/api/message/bulk/v2/logs", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<ChatSessionDoc, Object> getSession(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String type) {
		return ApiResponse
				.buildResults(
						mongoTemplate
								.getPages(PaginatedQuery.select(ChatSessionDoc.class, "CHAT_SESSION").pageNo(pageNo)
										.pageSize(pageSize).pageSize(pageSize).sortBy(sortBy).sortDir(sortDir))
								.getResults());
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

	@RequestMapping(value = "/api/message/bulk/push/messages", method = { RequestMethod.GET })
	public ApiResponse<ChatMessageDTO, BulkSessionDoc> getBulkMessagesGet(@RequestParam String bulkSessionId)
			throws NumberParseException {
		ApiResponse<ChatMessageDTO, BulkSessionDoc> resp = ApiResponse.instance(ChatMessageDTO.class,
				BulkSessionDoc.class);
		BulkSessionDoc session = CollectionUtil.getOne(mongoTemplate
				.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));
		resp.setMeta(session);
		if (ArgUtil.is(session)) {
			List<MessageDoc> msgs = messageStore.findByBulkSessionIdPaged(session.getBulkSessionId(),
					session.contactType());
			resp.results(ChatDTOUtil.getChatMessageDTO(msgs, null, session.getCreatedBy()));
		}
		return resp;
	}

	@RequestMapping(value = "/api/message/bulk/push/messages/replay/count", method = { RequestMethod.POST })
	public ApiResponse<ChatMessageDTO, BulkSessionDoc> getBulkMessagesReplayCount(@RequestParam String bulkSessionId)
			throws NumberParseException {
		ApiResponse<ChatMessageDTO, BulkSessionDoc> resp = ApiResponse.instance(ChatMessageDTO.class,
				BulkSessionDoc.class);

		BulkSessionDoc session = CollectionUtil.getOne(mongoTemplate
				.find(new Query().addCriteria(QueryCriteria.whereId(bulkSessionId)), BulkSessionDoc.class));
		resp.setMeta(session);

		if (ArgUtil.is(session)) {
			List<MessageDoc> msgs = messageStore.findByBulkSessionIdWithRplyCount(session.getBulkSessionId(),
					session.contactType());
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
			String hsmTemplateCode = null;
			String groupTitle = outboxMessage.getCampaignTitle();
			CsvDto csvDoc = mongoTemplate.findById(csvRefKeyId, CsvDto.class);
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}
			if (ArgUtil.is(csvDoc)) {
				List<Map<Object, Object>> lstMap = csvDoc.getLstMap();
				for (Map<Object, Object> map : lstMap) {
					OutboxMessage outboxMsg = new OutboxMessage();
					CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
					hsmTemp.setId(hsmId);
					hsmTemp.setCode(hsmTemplateCode);
					outboxMsg.setMessage(otBoxMsg.getMessage());
					outboxMsg.setAttachments(otBoxMsg.getAttachments());
					outboxMsg.setContact(otBoxMsg.getContact());
					outboxMsg.setCampaignTitle(groupTitle);
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
			String groupTitle = outboxMessage.getCampaignTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			String hsmTemplateCode = null;
			String groupName = null;
			GroupDoc groupDoc = mongoTemplate.findById(groupId, GroupDoc.class);
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}
			if (ArgUtil.is(groupDoc)) {
				groupName = groupDoc.getGroupName();
				List<GroupSessionDto> lstDto = groupDoc.getSessions();
				for (GroupSessionDto dto : lstDto) {
					OutboxMessage outboxMsg = new OutboxMessage();
					CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
					hsmTemp.setId(hsmId);
					hsmTemp.setCode(hsmTemplateCode);
					hsmTemp.setData(otBoxMsg.getHsm().data());

					outboxMsg.setGroupId(groupId);
					outboxMsg.setCampaignTitle(groupTitle);
					outboxMsg.setMessage(otBoxMsg.getMessage());

					outboxMsg.setAttachments(otBoxMsg.getAttachments());
					outboxMsg.setContact(otBoxMsg.getContact());
					outboxMsg.setTo(Arrays.asList(dto.getPhone()));
					outboxMsg.setHsm(hsmTemp);
					outboxMsg.setGroupName(groupName);
					listOfOutboxMsg.add(outboxMsg);
				} // end of listOfOutboxMsgs

			}

		}
		return listOfOutboxMsg;
	}

	public List<OutboxMessage> getGroupDetailsV1(OutboxMessage outboxMessage) {

		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();

		if (outboxMessage != null) {
			List<String> groups = outboxMessage.getGroups();
			if (ArgUtil.isEmpty(groups)) {
				groups = new ArrayList<>();
				groups.add(outboxMessage.getGroupId());
			}
			String groupTitle = outboxMessage.getCampaignTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			String hsmTemplateCode = null;
			StringBuilder concatGroupNames = new StringBuilder();
			Set<String> uniquePhoneNumbers = new HashSet<>();
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}
			if (ArgUtil.is(groups)) {
				for (String groupId : groups) {
					GroupDoc groupDoc = mongoTemplate.findById(groupId, GroupDoc.class);

					if (ArgUtil.is(groupDoc)) {
						OutboxMessage outboxMsg = new OutboxMessage();
						if (concatGroupNames.length() > 0) {
							concatGroupNames.append(" , "); // Add a comma separator
						}
						concatGroupNames.append(groupDoc.getGroupName());

						List<GroupSessionDto> lstDto = groupDoc.getSessions();
						CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
						hsmTemp.setId(hsmId);
						hsmTemp.setCode(hsmTemplateCode);
						hsmTemp.setData(otBoxMsg.getHsm().data());

						outboxMsg.setGroupId(groupId);
						outboxMsg.setCampaignTitle(groupTitle);
						outboxMsg.setMessage(otBoxMsg.getMessage());

						outboxMsg.setAttachments(otBoxMsg.getAttachments());
						outboxMsg.setContact(otBoxMsg.getContact());
						outboxMsg.setHsm(hsmTemp);
						outboxMsg.setGroupName(concatGroupNames.toString());

						for (GroupSessionDto dto : lstDto) {
							outboxMsg.setTo(Arrays.asList(dto.getPhone()));
							uniquePhoneNumbers.add(dto.getPhone());
						}
						List<String> toLst = new ArrayList<>(uniquePhoneNumbers);
						outboxMsg.setTo(toLst);
						outboxMsg.setGroups(otBoxMsg.getGroups());
						listOfOutboxMsg.add(outboxMsg);
					}

				}

			}

		}
		return listOfOutboxMsg;
	}

	private List<BulkSessionDoc> checkNull(List<BulkSessionDoc> lstofSession) {
		List<BulkSessionDoc> lst = new ArrayList<>();
		for (BulkSessionDoc doc : lstofSession) {
			BulkSessionDoc sDoc = new BulkSessionDoc();
			sDoc.setBulkSessionId(ArgUtil.parseAsString(doc.getBulkSessionId(), Constants.BLANK));
			sDoc.setTemplate(ArgUtil.parseAsString(doc.getTemplate(), Constants.BLANK));
			sDoc.setTemplateId(ArgUtil.parseAsString(doc.getTemplateId(), Constants.BLANK));
			sDoc.setMessage(ArgUtil.parseAsString(doc.getMessage(), Constants.BLANK));
			sDoc.setCampaignTitle(ArgUtil.parseAsString(doc.getCampaignTitle(), Constants.BLANK));
			sDoc.setCreatedBy(ArgUtil.parseAsString(doc.getCreatedBy(), Constants.BLANK));
			sDoc.setCreatedStamp(doc.getCreatedStamp());
			sDoc.setContactType(ArgUtil.parseAsString(doc.getContactType(), Constants.BLANK));
			sDoc.setChannelId(ArgUtil.parseAsString(doc.getChannelId(), Constants.BLANK));
			sDoc.setLane(ArgUtil.parseAsString(doc.getLane(), Constants.BLANK));
			sDoc.setMessageCount(doc.getMessageCount() == null ? Constants.DEFAULT_INTEGER : doc.getMessageCount());
			sDoc.setMessageFailedCount(
					doc.getMessageFailedCount() == null ? Constants.DEFAULT_INTEGER : doc.getMessageFailedCount());
			sDoc.setStats(doc.getStats());
			sDoc.setJob(doc.getJob());
			sDoc.setCompletedStamp(
					doc.getCompletedStamp() == null ? Constants.DEFAULT_INTEGER : doc.getCompletedStamp());
			sDoc.setGroupId(ArgUtil.parseAsString(doc.getGroupId(), Constants.BLANK));
			sDoc.setGroupName(ArgUtil.parseAsString(doc.getGroupName(), Constants.BLANK));
			sDoc.setStatus(doc.getStatus());
			sDoc.setScheduler(doc.getScheduler());

			lst.add(sDoc);
		}
		return lst;
	}

	public boolean isScheduleTimeValid(BulkSessionDoc bulkDoc) {
		boolean isScheduleValid = false;
		// Parse the interval to ZonedDateTime
		ChronoScheduler cSch = bulkDoc.getScheduler();
		ZonedDateTime intervalTime = ZonedDateTime.parse(cSch.getInterval(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
		// Get the current time
		ZonedDateTime currentTime = ZonedDateTime.now();
		// Check if the interval is in the future
		if (intervalTime.isAfter(currentTime)) {
			isScheduleValid = true;
			return isScheduleValid;
		}
		return isScheduleValid;
	}

	private List<OutboxMessage> getFilterDetails(OutboxMessage outboxMessage) {

		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();

		if (outboxMessage != null && ArgUtil.is(outboxMessage.getFilters())) {
			List<String> filters = outboxMessage.getFilters();
			String campTitle = outboxMessage.getCampaignTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			String hsmTemplateCode = null;

			StringBuilder concatFilterpNames = new StringBuilder();
			Set<String> uniquePhoneNumbers = new HashSet<>();
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}

			for (String filterId : filters) {
				ProfileFilterMasterDoc profileFilter = mongoTemplate.findById(filterId, ProfileFilterMasterDoc.class);

				if (ArgUtil.is(profileFilter) && ArgUtil.is(profileFilter.get_filterCriteria())) {
					OutboxMessage outboxMsg = new OutboxMessage();
					List<List<Object>> filterCri = profileFilter.get_filterCriteria();

					List<List<ProfileSearchCriteria>> searCri = bulkMessageService.getSearchCriteria(filterCri);
					ProfileSearchQuery profSerarch = new ProfileSearchQuery();
					profSerarch.setSearchCriterias(searCri);

					List<CustomerProfileDoc> docs = null;
					if (ArgUtil.is(searCri)) {
						docs = cusProfileService.getProfileSearch(profSerarch);
					}
					if (ArgUtil.is(docs)) {

						if (concatFilterpNames.length() > 0) {
							concatFilterpNames.append(" , "); // Add a comma separator
						}
						concatFilterpNames.append(profileFilter.getFilterName());
						for (CustomerProfileDoc profielDoc : docs) {
							Set<PBPhone> lstDto = profielDoc.getPhones();
							CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
							hsmTemp.setId(hsmId);
							hsmTemp.setCode(hsmTemplateCode);
							hsmTemp.setData(otBoxMsg.getHsm().data());

							outboxMsg.setAttachments(otBoxMsg.getAttachments());
							outboxMsg.setContact(otBoxMsg.getContact());
							outboxMsg.setHsm(hsmTemp);
							outboxMsg.setGroupName(concatFilterpNames.toString());
							outboxMsg.setCampaignTitle(campTitle);
							for (PBPhone dto : lstDto) {
								outboxMsg.setTo(Arrays.asList(dto.getPhone()));
								uniquePhoneNumbers.add(dto.getPhone());
							}
							List<String> toLst = new ArrayList<>(uniquePhoneNumbers);
							outboxMsg.setTo(toLst);
							outboxMsg.setFilters(filters);
							listOfOutboxMsg.add(outboxMsg);
						}
					}
				}
			}

		}
		return listOfOutboxMsg;
	}

}