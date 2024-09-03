package com.boot.jx.admin.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.LeadMessanger;
import com.boot.jx.admin.dto.PeakLoadDto;
import com.boot.jx.admin.dto.UniqueContactDto;
import com.boot.jx.async.ContextAwareCollection;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageMetaWrapper;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
public class AgentAnalyticsManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2207003486527490122L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentAnalyticsManager.class);
	public static final String CHAT_SESSION = "CHAT_SESSION";

	public static final String DEFAULT_AGENT = "TEAM";
	public static final int OPEN_CONV_HR_LMT = 5;

	public static final int OPEN_CONV_HR = 1;

	public static final String MY_BOT = "BOT";

	@Autowired
	CommonMongoTemplate mongoTemplate;

	@Autowired
	AdminDashBoardManager adminDbMgr;

	public List<DashBoardResponseDto> getAgentWiseAnalytics(DashBoardRequestDto req) {
		LOGGER.info("getAgentWiseAnalytics {} :" + JsonUtil.toJson(req));
		List<DashBoardResponseDto> lstDto = new ArrayList<>();
		DashBoardResponseDto dto = null;
		List<String> allAgent = null;
		long date1 = 0;
		long date2 = 0;
		if (ArgUtil.is(req.getDateRange1()) && req.getDateRange1() > 0) {
			date1 = req.getDateRange1();
		} else {
			date1 = todayStartTime();
		}
		if (ArgUtil.is(req.getDateReange2()) && req.getDateReange2() > 0) {
			date2 = req.getDateReange2();
		} else {
			date2 = todayEndTime();
		}

		if (req != null && (ArgUtil.isEmptyString(req.getAgent()) || req.getAgent().equalsIgnoreCase(DEFAULT_AGENT))) {
			allAgent = getAgentList(date1, date2);
		}

		if (allAgent != null && !allAgent.isEmpty()) {
			boolean parellel = true;
			if (parellel) {
				long date1Final = date1;
				long date2Final = date2;

				lstDto = new ContextAwareCollection<String>(allAgent).ayncStream().map(agent -> {
					if (ArgUtil.is(agent)) {
						return getAgentAnalytics(agent, date1Final, date2Final, req.getContactType());
					}
					return null;
				}).filter(Objects::nonNull).collect(Collectors.toList());
			} else {
				for (String agent : allAgent) {
					dto = new DashBoardResponseDto();
					if (!StringUtils.isBlank(agent)) {
						dto = getAgentAnalytics(agent, date1, date2, req.getContactType());
						lstDto.add(dto);
					}

				}

			}

		} else {
			dto = getAgentAnalytics(req.getAgent(), date1, date2, req.getContactType());
			lstDto.add(dto);
		}

		/** "mode" : "BOT", "assignedToAgent" : null, **/
		if ((!ArgUtil.isEmptyString(req.getAgent()) && req.getAgent().equalsIgnoreCase(DEFAULT_AGENT))) {
			dto = getAgentAnalytics(null, date1, date2, req.getContactType());
			dto.setAgentName(MY_BOT);
			lstDto.add(dto);
		}

		return lstDto;
	}

	public DashBoardResponseDto getSummery(List<DashBoardResponseDto> dtoLst) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		dto.setAgentName(DEFAULT_AGENT);
		long totalInMsg = 0;
		long totalOutMsg = 0;
		long totalMsg = 0;
		long totalTemplateMsgSent = 0;
		long totalTemplateMsgDelivered = 0;
		long totalUniqCon = 0;
		long totalOpenMsg = 0;
		long totalResolvedMsg = 0;
		long convDuration = 0;
		long botScore = 0;
		double botClosure = 0.0d;
		double totalStartLag = 0.0d;
		long totSatisScore = 0;
		long totSatisFeedback = 0;
		Map<Object, Object> graphApiMap = new HashMap<Object, Object>();
		Map<Object, Object> graphApiMapV1 = new HashMap<Object, Object>();

		dto.setPeakLoad(new PeakLoadDto());
		for (DashBoardResponseDto dt : dtoLst) {
			// LOGGER.info("get Agent/channel wise { ==== }:" + JsonUtil.toJson(dt));
			totalInMsg += dt.getTotalInMsgExchanged();
			totalOutMsg += dt.getTotalOutMsgExchanged();
			totalMsg += dt.getTotalMsgExchanged();
			totalTemplateMsgSent += dt.getTotalTemplateMsgSent();
			totalTemplateMsgDelivered += dt.getTotalTemplateMsgDelivered();
			totalOpenMsg += dt.getOpenConversation();
			totalResolvedMsg += dt.getResolvedConversation();
			convDuration += dt.getConverDuration();
			totalUniqCon += dt.getUniqueConversation();
			if (dt.getStartLag() > 0) {
				totalStartLag += dt.getStartLag();
			}
			botScore = dt.getBotScore();
			botClosure = dt.getBotClosure();
			if (dt.getLeadMessanger() != null) {
				dto.setLeadMessanger(dt.getLeadMessanger());
			}
			if (dt.getGraphApiDetails() != null && !dt.getGraphApiDetails().isEmpty()) {
				graphApiMap = mergerMapKyAndValue(graphApiMap, dt.getGraphApiDetails());
			}
			if (dt.getGraphApiDetailsV1() != null && !dt.getGraphApiDetailsV1().isEmpty()) {
				graphApiMapV1 = mergerMapKyAndValue(graphApiMapV1, dt.getGraphApiDetailsV1());
			}
			if (ArgUtil.is(dt.getPeakLoad()) && dt.getPeakLoad().getTotal() > dto.getPeakLoad().getTotal()) {
				dto.setPeakLoad(dt.getPeakLoad());
			}

			if (dt.getSatisfactionScore() > 0) {
				totSatisFeedback += 1;
				totSatisScore += dt.getSatisfactionScore();
			}

		}
		dto.setTotalInMsgExchanged(totalInMsg);
		dto.setTotalOutMsgExchanged(totalOutMsg);
		dto.setTotalMsgExchanged(totalMsg);
		dto.setTotalTemplateMsgSent(totalTemplateMsgSent);
		dto.setTotalTemplateMsgDelivered(totalTemplateMsgDelivered);
		dto.setOpenConversation(totalOpenMsg);
		dto.setResolvedConversation(totalResolvedMsg);
		dto.setUniqueConversation(totalUniqCon);
		dto.setBotScore(botScore);
		dto.setBotClosure(botClosure);
		if (totalMsg != 0) {
			dto.setConverDuration(convDuration / totalMsg);
		}
		dto.setStartLag(totalStartLag);
		if (graphApiMap != null && !graphApiMap.isEmpty()) {
			dto.setGraphApiDetails(graphApiMap);
		}
		if (graphApiMapV1 != null && !graphApiMapV1.isEmpty()) {
			dto.setGraphApiDetailsV1(graphApiMapV1);
		}
		if (totSatisScore > 0) {
			dto.setSatisfactionScore(totSatisScore / totSatisFeedback);
		}

		LOGGER.debug("\n\n get Summary ========:" + JsonUtil.toJson(dto));
		return dto;
	}

	@SuppressWarnings("unchecked")
	public DashBoardResponseDto getAgentAnalytics(String agent, long dateRange1, long dateRange2, Object contact) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LOGGER.debug(dtf.format(LocalDateTime.now()) + " Get Analytics for  :" + agent);
		DashBoardResponseDto dto = new DashBoardResponseDto();
		dto.setAgentName(agent == null ? MY_BOT : agent);
		/** Unique agent list **/
		List<UniqueContactDto> distinctContactLst = getUniqueAgentWiseContactListV1(agent, dateRange1, dateRange2);

		if (ArgUtil.is(distinctContactLst)) {
			dto.setUniqueConversation(distinctContactLst.size());
		}

		/** Total Agent-contact wise msg **/
		if (distinctContactLst != null && !distinctContactLst.isEmpty()) {

			List<MessageDoc> totalAgConMsgExchanged = getTotalMessageAgentAndContactWiseV1(distinctContactLst,
					dateRange1, dateRange2, contact, agent);
			long totalAgConMsgExchangedCnt = getTotalMessageAgentAndContactWiseV2(distinctContactLst, dateRange1,
					dateRange2, contact, agent);

			if (ArgUtil.is(totalAgConMsgExchanged)) {
				dto.setTotalMsgExchanged(totalAgConMsgExchangedCnt);

				List<MessageDoc> totalTemplateMsgExchanged = new ArrayList<>();
				List<MessageDoc> totalTemplateMsgDelivered = new ArrayList<>();
				for (MessageDoc messageDoc : totalAgConMsgExchanged) {
					if (messageDoc != null) {
						MessageMetaWrapper metaWrapper = new MessageMetaWrapper(messageDoc.getMeta());
						if (metaWrapper != null) {
							if (metaWrapper.sendType() != null && metaWrapper.sendType().equalsIgnoreCase("PM")) {
								totalTemplateMsgExchanged.add(messageDoc);
								if (messageDoc.getStamps().get(Message.Status.DLVRD.name()) != null) {
									totalTemplateMsgDelivered.add(messageDoc);
								}
							}
						}
					}
				}
				if (ArgUtil.is(totalTemplateMsgDelivered)) {
					dto.setTotalTemplateMsgDelivered(totalTemplateMsgDelivered.size());
				}
				if (ArgUtil.is(totalTemplateMsgExchanged)) {
					dto.setTotalTemplateMsgSent(totalTemplateMsgExchanged.size());
				}
			}

			/** Open conversation **/
			long openConvesCnt = getAgentWiseOpenConversationV1(agent, dateRange1, dateRange2);
			if (ArgUtil.is(openConvesCnt)) {
				dto.setOpenConversation(openConvesCnt);

			}

			/** Resolved Conversation **/
			long resConvCnt = getAgentWiseResolvedConversationV1(agent, dateRange1, dateRange2);
			if (ArgUtil.is(resConvCnt)) {
				dto.setResolvedConversation(resConvCnt);
			}
			/** Peak Load **/

			PeakLoadDto peakLoadResult = adminDbMgr.getPeakLoadMsgCount(totalAgConMsgExchanged);// getAgentPeakLoadMsgCount(totalMsgExchanged);
			dto.setPeakLoad(peakLoadResult);

			/** lead Messanger **/

			LeadMessanger leadMsg = getLeadMessenger(agent, dateRange1, dateRange2, contact);
			dto.setLeadMessanger(leadMsg);

			/** Converation duration **/
			long conVerDuration = getConversationDurationV1(agent, dateRange1, dateRange2, distinctContactLst);

			if (ArgUtil.is(conVerDuration)) {
				dto.setConverDuration(conVerDuration);
			}

			/** startLag **/
			double startLag = getStartLagV1(agent, dateRange1, dateRange2, distinctContactLst);
			dto.setStartLag(startLag);

			/** bot score **/

			long botScore = getBotScoreV1(dateRange1, dateRange2);
			dto.setBotScore(botScore);

			/** bot closure **/

			double botClosure = getBotClosureV1(dateRange1, dateRange2, dto.getTotalMsgExchanged());
			dto.setBotClosure(botClosure);

			/** Satisfaction score **/
			double satisfactionScore = getSatisfactionScoreV1(dateRange1, dateRange2, agent);
			dto.setSatisfactionScore(satisfactionScore);

			/** find the date diff between two dates **/

			Map<String, Integer> dateDiffMAp = getDateDiff(dateRange1, dateRange2);
			int hour = 0;
			int days = 0;
			if (ArgUtil.is(dateDiffMAp)) {
				hour = dateDiffMAp.get("HOUR");
				days = dateDiffMAp.get("DAYS");
			}
			LOGGER.debug("mru hour {===}:" + hour + "\t dateDiffMAp :" + dateDiffMAp);

			if (hour <= 24) {
				Map<Object, Object> dayMapLst = getHourWiseCountV2(distinctContactLst, agent, dateRange1, dateRange2,
						contact);
				Map<Object, Object> hourWiseCount = (Map<Object, Object>) dayMapLst.get("HR");
				Map<Object, Object> hourWiseCountV1 = (Map<Object, Object>) dayMapLst.get("HR_V1");

				dto.setGraphApiDetails(hourWiseCount);
				dto.setGraphApiDetailsV1(hourWiseCountV1);
			} else if (hour > 24 && days <= 31) {
				Map<Object, Object> dayMapLst = getDateWiseCountV3(distinctContactLst, agent, dateRange1, dateRange2,
						contact);
				Map<Object, Object> dateWiseCount = (Map<Object, Object>) dayMapLst.get("DAY");
				Map<Object, Object> timeStampWiseCount = (Map<Object, Object>) dayMapLst.get("DAY_V1");

				dto.setGraphApiDetails(dateWiseCount);
				dto.setGraphApiDetailsV1(timeStampWiseCount);
			} else {
				Map<Object, Object> weekMapLst = getWeekWiseCountV2(distinctContactLst, agent, dateRange1, dateRange2,
						contact);
				Map<Object, Object> dweekWiseCount = (Map<Object, Object>) weekMapLst.get("WEEK");
				Map<Object, Object> timeStampWiseCount = (Map<Object, Object>) weekMapLst.get("WEEK_V1");

				dto.setGraphApiDetails(dweekWiseCount);
				dto.setGraphApiDetailsV1(timeStampWiseCount);

			}
		}

		return dto;
	}

	public List<String> getAgentList() {
		List<String> distinceAgentList = mongoTemplate.distinctValues("CHAT_SESSION", "assignedToAgent", String.class);
		return distinceAgentList;
	}

	public List<String> getAgentList(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		List<String> distinceAgentList = mongoTemplate.distinctValues("CHAT_SESSION", "assignedToAgent", String.class);

		if (distinceAgentList == null || distinceAgentList.isEmpty()) {
			distinceAgentList = getDefaultAgent(dateRange1, dateRange2);
		}
		return distinceAgentList;
	}

	public List<String> getDefaultAgent(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));

		List<String> distinceAgentList = mongoTemplate.distinctValues("CHAT_SESSION", "mode", String.class);
		return distinceAgentList;
	}

	public List<String> getDefaultDistinctContact(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		query.addCriteria(Criteria.where("assignedToAgent").exists(false));
		// removeChatSessField(query);
		query.fields().include("assignedAgentStamp").include("contactId");
		// List<String> distinceAgentList = mongoTemplate.distinctValues("CHAT_SESSION",
		// "contactId", String.class);

		List<ChatSessionDoc> chatSessDocLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		List<String> distinceContactList = getDistinct(chatSessDocLst);

		return distinceContactList;
	}

	public List<UniqueContactDto> getDefaultDistinctContactV1(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		query.addCriteria(Criteria.where("assignedToAgent").exists(false));
		query.fields().include("assignedAgentStamp").include("contactId").include("contact");

		// Fetch documents from MongoDB
		List<ChatSessionDoc> chatSessDocLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

		// Optimize distinct contact list processing
		return getDistinctV1(chatSessDocLst);
	}

	public List<String> getUniqueAgentWiseContactList(String agent, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId");
		// System.out.println("QRY :"+agent+"\t "+JsonUtil.toJsonPrettyPrint(query));
		// removeChatSessField(query);
		List<String> distinctIdList = new ArrayList<>();

		List<ChatSessionDoc> chatSessDocLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

		distinctIdList = getDistinct(chatSessDocLst);

		if (agent == null) {
			distinctIdList = getDefaultDistinctContact(dateRange1, dateRange2);
		}

		return distinctIdList;
	}

	public List<UniqueContactDto> getUniqueAgentWiseContactListV1(String agent, long dateRange1, long dateRange2) {

		// Early return if agent is null to avoid unnecessary query execution
		if (agent == null) {
			return getDefaultDistinctContactV1(dateRange1, dateRange2);
		}

		// Construct the query
		Query query = new Query(
				Criteria.where("assignedToAgent").is(agent).and("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId").include("contact");

		// Execute the query and fetch results
		long st = System.currentTimeMillis();
		List<ChatSessionDoc> chatSessDocLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		long et = System.currentTimeMillis();
		// Process the results
		List<UniqueContactDto> distinctIdList = new ArrayList<>();
		if (ArgUtil.is(chatSessDocLst)) {
			distinctIdList = getDistinctV1(chatSessDocLst);
		}

		return distinctIdList;
	}

	public List<String> getDistinct(List<ChatSessionDoc> chatSessDocLst) {
		List<String> distinctIdList = new ArrayList<>();
		for (ChatSessionDoc doc : chatSessDocLst) {
			if (ArgUtil.is(doc.getContactId())) {
				distinctIdList.add(doc.getContactId());
			}
		}
		if (distinctIdList != null && !distinctIdList.isEmpty()) {
			distinctIdList = distinctIdList.stream().distinct().collect(Collectors.toList());
		}

		return distinctIdList;
	}

	public List<UniqueContactDto> getDistinctV1(List<ChatSessionDoc> chatSessDocLst) {
		List<UniqueContactDto> distinctIdList = new ArrayList<>();
		for (ChatSessionDoc doc : chatSessDocLst) {
			if (ArgUtil.is(doc.getContactId())) {
				UniqueContactDto dto = new UniqueContactDto();
				dto.setContactId(doc.getContactId());
				dto.setContactType(doc.getContact().getContactType());
				distinctIdList.add(dto);
			}
		}
		// Fetch unique combinations of contactId and contactType
		// Fetch unique combinations of contactId and contactType
		List<UniqueContactDto> uniqueContacts = distinctIdList.stream()
				.collect(Collectors.toMap(contact -> contact.getContactId() + "-" + contact.getContactType(), // Key:
																												// combination
																												// of
																												// contactId
																												// and
																												// contactType
						contact -> contact, // Value: the UniqueContactDto object itself
						(contact1, contact2) -> contact1 // In case of duplicates, keep the first one
				)).values().stream().collect(Collectors.toList());

		return uniqueContacts;
	}

	public List<ChatSessionDoc> getAgentWiseTotalMsgExchanged(String agent, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("assignedAgentStamp").gte(dateRange1).lt(dateRange2));
		query.with(new Sort(new Order(Direction.ASC, "timestamp")));

		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		return totalMsgDoc;
	}

	public List<ChatSessionDoc> getAgentWiseOpenConversation(String agent, long dateRange1, long dateRange2) {
		List<ChatSessionDoc> totalOpenMsgDoc = new ArrayList<ChatSessionDoc>();
		long currentTimeStamp = System.currentTimeMillis();

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("active").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId").include("contact");
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

		for (ChatSessionDoc chatDoc : totalMsgDoc) {
			long assignToAgent = chatDoc.getAssignedAgentStamp();
			long diffInMilliSeconds = currentTimeStamp - assignToAgent;
			int diffInHours = (int) (diffInMilliSeconds / (60 * 60 * 1000));
			totalOpenMsgDoc.add(chatDoc);
		}
		return totalOpenMsgDoc;
	}

	public long getAgentWiseOpenConversationV1(String agent, long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("active").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId").include("contact");
		long count = mongoTemplate.count(query, CHAT_SESSION);
		return count;
	}

	public List<ChatSessionDoc> getAgentWiseResolvedConversation(String agent, long dateRange1, long dateRange2) {
		List<ChatSessionDoc> totalResolvedMsgDoc = new ArrayList<ChatSessionDoc>();
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("resolved").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId").include("contact");
		// long st =System.currentTimeMillis();
		// System.out.println("getAgentWiseResolvedConversation ST :"+st);
		// System.out.println("getAgentWiseResolvedConversation query
		// :"+JsonUtil.toJson(query));
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		// long et1 =System.currentTimeMillis();
		// System.out.println("getAgentWiseResolvedConversation END TIME QUERY EXE
		// :"+et1 +"\t difference "+(et1-st));
		for (ChatSessionDoc chatDoc : totalMsgDoc) {
			totalResolvedMsgDoc.add(chatDoc);
		}
		// long et2 =System.currentTimeMillis();
		// System.out.println("getAgentWiseResolvedConversation END TIME
		// totalResolvedMsgDoc :"+et2+"\t difference "+(et2-st));
		return totalResolvedMsgDoc;
	}

	public long getAgentWiseResolvedConversationV1(String agent, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("resolved").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.fields().include("assignedToAgent").include("assignedAgentStamp").include("contactId").include("contact")
				.include("resolved");
		long count = mongoTemplate.count(query, CHAT_SESSION);

		return count;
	}

	public long getConversationDuration(String agent, long startTime, long endTime,List<String> uniquContactIdLst) {
		Map<String, Long> conVerMsgLst = new HashMap<String, Long>();
		Long maxEntryKeyValue = new Long(0);
		for (Object chatSession : uniquContactIdLst) {
			String conId = (String) chatSession;
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			if (chatSessionCon != null) {
				long fistResponseStamp = chatSessionCon.getFistResponseStamp();
				long lastResponseStamp = chatSessionCon.getLastResponseStamp();
				Long converDuration = lastResponseStamp - fistResponseStamp;
				conVerMsgLst.put(conId, converDuration);
			}
		}

		if (conVerMsgLst != null && !conVerMsgLst.isEmpty() && ArgUtil.is(conVerMsgLst)) {
			Object maxEntryKey = Collections.max(conVerMsgLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			maxEntryKeyValue = (Long) conVerMsgLst.get(maxEntryKey);
		}

		return maxEntryKeyValue;
	}
	
	public long getConversationDurationV1(String agent, long startTime, long endTime,List<UniqueContactDto> uniquContactIdLst) {
		Map<String, Long> conVerMsgLst = new HashMap<String, Long>();
		Long maxEntryKeyValue = new Long(0);
		for (UniqueContactDto chatSession : uniquContactIdLst) {
			String conId = chatSession.getContactId();
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			if (chatSessionCon != null) {
				long fistResponseStamp = chatSessionCon.getFistResponseStamp();
				long lastResponseStamp = chatSessionCon.getLastResponseStamp();
				Long converDuration = lastResponseStamp - fistResponseStamp;
				conVerMsgLst.put(conId, converDuration);
			}
		}

		if (conVerMsgLst != null && !conVerMsgLst.isEmpty() && ArgUtil.is(conVerMsgLst)) {
			Object maxEntryKey = Collections.max(conVerMsgLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			maxEntryKeyValue = (Long) conVerMsgLst.get(maxEntryKey);
		}

		return maxEntryKeyValue;
	}
	

	public double getStartLag(String agent, long dateRange1, long dateRange2) {
		Map<String, Double> startLagMapLst = new HashMap<String, Double>();
		double startLag = 0.0d;
		double percentageWithDecimal = 0.0d;

		List<String> uniquContactIdLst = getUniqueAgentWiseContactList(agent, dateRange1, dateRange2);
		for (Object chatSession : uniquContactIdLst) {
			String conId = (String) chatSession;
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			if (chatSessionCon != null) {
				long fistResponseStamp = chatSessionCon.getFistResponseStamp();
				long assignedDeptStamp = chatSessionCon.getAssignedDeptStamp();
				double converDuration = fistResponseStamp - assignedDeptStamp;
				Double diffInMin = (double) (converDuration / (60 * 1000));
				if (diffInMin > 0) {
					startLagMapLst.put(conId, diffInMin);
				}
			}
		}

		if (startLagMapLst != null && !startLagMapLst.isEmpty() && ArgUtil.is(startLagMapLst)) {
			Object maxEntryKey = Collections.max(startLagMapLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			Double maxEntryKeyValue = startLagMapLst.get(maxEntryKey);
			startLag = maxEntryKeyValue;
		}

		return startLag;
	}

	public double getStartLagV1(String agent, long dateRange1, long dateRange2,
			List<UniqueContactDto> uniquContactIdLst) {
		Map<String, Double> startLagMapLst = new HashMap<String, Double>();
		double startLag = 0.0d;
		// List<String> uniquContactIdLst = getUniqueAgentWiseContactList(agent,
		// dateRange1, dateRange2);
		for (UniqueContactDto chatSession : uniquContactIdLst) {
			String conId = chatSession.getContactId();
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			if (chatSessionCon != null) {
				long fistResponseStamp = chatSessionCon.getFistResponseStamp();
				long assignedDeptStamp = chatSessionCon.getAssignedDeptStamp();
				double converDuration = fistResponseStamp - assignedDeptStamp;
				Double diffInMin = (double) (converDuration / (60 * 1000));
				if (diffInMin > 0) {
					startLagMapLst.put(conId, diffInMin);
				}
			}
		}
//System.out.println("getStartLagV1 :"+JsonUtil.toJson(startLagMapLst));
		if (startLagMapLst != null && !startLagMapLst.isEmpty() && ArgUtil.is(startLagMapLst)) {
			Object maxEntryKey = Collections.max(startLagMapLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			Double maxEntryKeyValue = startLagMapLst.get(maxEntryKey);
			startLag = maxEntryKeyValue;
		}

		// Return the maximum start lag value if available
		double startLag1 = startLagMapLst.values().stream().max(Double::compareTo).orElse(0.0);
		// System.out.println("startLag1 :"+startLag1+"\t startLag :"+startLag);

		return startLag;
	}

	public PeakLoadDto getAgentPeakLoadMsgCountOld(String agent, long startTime, long endTime) {
		Aggregation agg = newAggregation(match(Criteria.where("assignedAgentStamp").gt(startTime).lt(endTime)),
				group("assignedAgentStamp").count().as("total"),
				project("total").and("assignedAgentStamp").previousOperation(),
				sort(Sort.Direction.DESC, "total", "assignedAgentStamp"));
		// Convert the aggregation result into a List
		AggregationResults<PeakLoadDto> groupResults = mongoTemplate.aggregate(agg, CHAT_SESSION, PeakLoadDto.class);
		PeakLoadDto peakLoadResult = null;
		if (groupResults != null && !groupResults.getMappedResults().isEmpty()) {
			peakLoadResult = groupResults.getMappedResults().get(0);
		}
		return peakLoadResult;
	}

	public PeakLoadDto getAgentPeakLoadMsgCount(List<ChatSessionDoc> msgLst) {
		PeakLoadDto peakLoadResult = new PeakLoadDto();
		List<Integer> hourList = new ArrayList<Integer>();
		List<String> dateWithTimeList = new ArrayList<String>();
		Map<Object, Integer> mapLst = new HashMap<Object, Integer>();
		for (ChatSessionDoc msg : msgLst) {
			long timeStamp = msg.getAssignedAgentStamp();
			Date date = new Date(timeStamp);
			String dateWithTime = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(date);
			String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			SimpleDateFormat sdfH = new SimpleDateFormat("HH");
			String formattedDateH = sdfH.format(date);
			dateWithTimeList.add(dateWithTime);
			hourList.add(Integer.parseInt(formattedDateH));

		}
		Collections.sort(dateWithTimeList);

		Set<Object> dateWithTimeWiseCount = new HashSet<Object>(dateWithTimeList);
		for (Object key : dateWithTimeWiseCount) {
			mapLst.put(key, Collections.frequency(dateWithTimeList, key));

		}
		if (mapLst != null && ArgUtil.is(mapLst) && !mapLst.isEmpty()) {
			Object maxEntryKey = Collections.max(mapLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			Integer maxEntryKeyValue = mapLst.get(maxEntryKey);
			peakLoadResult.setTimestamp(maxEntryKey);
			peakLoadResult.setTotal(maxEntryKeyValue.longValue());

		}
		return peakLoadResult;
	}

	/** fetch lead mesenger **/

	public LeadMessanger getLeadMessenger(Object contactype, long startTime, long endTime,Object contact) {
		LeadMessanger leadMessanger = new LeadMessanger();
		double percentageWithDecimal = 0.0;
		//List<String> lst = adminDbMgr.getListOfContactType();
		
		List<String> lst =getContactType(contact);
		
		Map<String, Integer> leasMsgLst = new HashMap<String, Integer>();
		for (String contactType : lst) {
			List<MessageDoc> msgDocLst = adminDbMgr.getTotalMsgCount(contactType, startTime, endTime);
			leasMsgLst.put(contactType, msgDocLst.size());
		}
		//LOGGER.debug("lead Msg  :" + leasMsgLst.toString());

		if (leasMsgLst != null && ArgUtil.is(leasMsgLst)) {
			Object maxEntryKey = Collections.max(leasMsgLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			Integer maxEntryKeyValue = leasMsgLst.get(maxEntryKey);
			Integer sumOfAllContactMsg = leasMsgLst.values().stream().mapToInt(i -> i).sum();
			if (maxEntryKeyValue > 0 && sumOfAllContactMsg > 0) {
				double percentage = ((maxEntryKeyValue.doubleValue() / sumOfAllContactMsg.doubleValue()) * 100);
				BigDecimal bd = new BigDecimal(percentage).setScale(2, RoundingMode.HALF_UP);
				percentageWithDecimal = bd.doubleValue();
			}
			leadMessanger.setContactType(maxEntryKey);
			leadMessanger.setNoOfMessage(maxEntryKeyValue);
			leadMessanger.setTotalContactMessage(sumOfAllContactMsg);
			leadMessanger.setPercentage(percentageWithDecimal);
		}
		return leadMessanger;
	}
	/** Fetch lead messenger **/
    public LeadMessanger getLeadMessengerV1(Object contacttype, long startTime, long endTime, Object contact) {
        LeadMessanger leadMessanger = new LeadMessanger();

        // Fetch contact types list
        List<String> contactTypeList = getContactType(contact);

        // Map to hold the message count for each contact type
        Map<String, Integer> messageCountMap = new HashMap<>();

        // Populate the map with message counts
        for (String type : contactTypeList) {
        	Integer messageCount = adminDbMgr.getTotalMsgCountV1(type, startTime, endTime);
            messageCountMap.put(type, messageCount);
        }
        LOGGER.debug("Lead Messenger message counts: " + messageCountMap);

        // Check if the map is not empty
        if (!messageCountMap.isEmpty()) {
            // Get the contact type with the maximum message count
            Map.Entry<String, Integer> maxEntry = Collections.max(messageCountMap.entrySet(), Map.Entry.comparingByValue());

            Integer maxMessageCount = maxEntry.getValue();
            Integer totalMessages = messageCountMap.values().stream().mapToInt(Integer::intValue).sum();

            // Calculate percentage of maxMessageCount relative to totalMessages
            if (totalMessages > 0) {
                double percentage = (double) maxMessageCount / totalMessages * 100;
                double percentageWithDecimal = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue();

                leadMessanger.setContactType(maxEntry.getKey());
                leadMessanger.setNoOfMessage(maxMessageCount);
                leadMessanger.setTotalContactMessage(totalMessages);
                leadMessanger.setPercentage(percentageWithDecimal);
            }
        }
        //System.out.println("LEAD MSG v1:"+JsonUtil.toJson(leadMessanger));
        return leadMessanger;
    }
    

	/** Timestamp **/

	public Map<Object, Object> getHourWiseCount(List<ChatSessionDoc> msgLst) {
		List<Integer> hourList = new ArrayList<Integer>();
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (ChatSessionDoc msg : msgLst) {
			long timeStamp = msg.getAssignedAgentStamp();
			Date date = new Date(timeStamp);
			String dateWithTime = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(date);
			String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			SimpleDateFormat sdfH = new SimpleDateFormat("HH");
			String formattedDateH = sdfH.format(date);
			dateWiseList.add(ddMMyyyyFormat);
			hourList.add(Integer.parseInt(formattedDateH));

		}
		Collections.sort(hourList);

		Set<Object> hourWiseCount = new HashSet<Object>(hourList);
		for (Object key : hourWiseCount) {
			mapLst.put(key, Collections.frequency(hourList, key));
			LOGGER.debug(key + ": " + Collections.frequency(hourList, key));
			LOGGER.info(key + ": " + Collections.frequency(hourList, key));
		}

		return mapLst;
	}

	/** date wise count **/
	public Map<Object, Object> getDateWiseCount(List<ChatSessionDoc> msgLst) {
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (ChatSessionDoc msg : msgLst) {
			long timeStamp = msg.getAssignedAgentStamp();
			Date date = new Date(timeStamp);
			String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			dateWiseList.add(ddMMyyyyFormat);
		}

		// Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(dateWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(dateWiseList, key));
			LOGGER.debug(key + ": " + Collections.frequency(dateWiseList, key));
			LOGGER.info(key + ": " + Collections.frequency(dateWiseList, key));
		}

		return mapLst;
	}

	/** week wise count **/
	public Map<Object, Object> getWeekWiseCount(List<ChatSessionDoc> msgLst) {
		List<Object> weekWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		Calendar cal = Calendar.getInstance();
		for (ChatSessionDoc msg : msgLst) {
			long timeStamp = msg.getAssignedAgentStamp();
			Date date = new Date(timeStamp);
			String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			cal.setTime(date);
			String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).toUpperCase();
			int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
			String str = month + " (WEEK) " + weekOfMonth;
			weekWiseList.add(str);
		}

		// Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(weekWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(weekWiseList, key));
		}

		return mapLst;
	}

	public long todayStartTime() {
		ZonedDateTime todayStartTime = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		long longTodayStartTime = todayStartTime.toInstant().toEpochMilli();
		return longTodayStartTime;
	}

	public long todayEndTime() {
		ZonedDateTime todayStartTime = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		ZonedDateTime endToday = todayStartTime.with(LocalTime.MAX);
		long longTodayendTime = endToday.toInstant().toEpochMilli();
		return longTodayendTime;
	}

	public Map<String, Integer> getDateDiff(long date1, long date2) {

		Map<String, Integer> dateDiffMap = new HashMap<String, Integer>();
		// For thousand separator
		DecimalFormat decimalFormatter = new DecimalFormat("###,###");
		long diffInMilliSeconds = date2 - date1;

		int diffInMin = (int) (diffInMilliSeconds / (60 * 1000));

		int diffInHours = (int) (diffInMilliSeconds / (60 * 60 * 1000));

		int diffInDays = (int) (diffInMilliSeconds / (24 * 60 * 60 * 1000));

		dateDiffMap.put("MINUTE", diffInMin);
		dateDiffMap.put("HOUR", diffInHours);
		dateDiffMap.put("DAYS", diffInDays);

		return dateDiffMap;
	}

	public Map<Object, Object> mergerMapKyAndValue(Map<Object, Object> mergeMap, Map<Object, Object> map2) {
		Map<Object, Object> mergeValue = mergeMap;
		// Merge maps
		map2.forEach((key, value) -> mergeValue.merge(key, value, (v1, v2) -> v1 == v2 ? v1 : (Long) v1 + (Long) v2));
		return mergeValue;
	}

	// Get Total Msg from

	public List<MessageDoc> getTotalMessageAgentAndContactWise(List<String> contactIds, long dateRange1,
			long dateRange2, Object contact, String agent) {
		List<MessageDoc> totalMsgDocLst = new ArrayList<MessageDoc>();
		for (String contactId : contactIds) {
			List<MessageDoc> msgDocLst = getMsgCountAgentContactWise(contactId, dateRange1, dateRange2, contact, agent);
			totalMsgDocLst.addAll(msgDocLst);
		}
		return totalMsgDocLst;
	}

	public List<MessageDoc> getTotalMessageAgentAndContactWiseV1(List<UniqueContactDto> contactIds, long dateRange1,
			long dateRange2, Object contact, String agent) {
		List<MessageDoc> totalMsgDocLst = new ArrayList<MessageDoc>();
		List<String> contactList = new ArrayList<>();
		for (UniqueContactDto contactId : contactIds) {
			if (ArgUtil.isEmptyValue(contact)) {
				contact = "MESSAGE_" + contactId.getContactType();
				contactList.add(contact.toString());
			}
			long st = System.currentTimeMillis();
			// System.out.println("getTotalMessageAgentAndContactWiseV1 start Time:"+st);
			List<MessageDoc> msgDocLst = getMsgCountAgentContactWise(contactId.getContactId(), dateRange1, dateRange2,
					contact, agent);
			long et = System.currentTimeMillis();
			// System.out.println("getTotalMessageAgentAndContactWiseV1 End Time:"+et+"\t
			// difference :"+(et-st));
			if (msgDocLst != null && !msgDocLst.isEmpty()) {
				totalMsgDocLst.addAll(msgDocLst);
			}
		}
		for (Object con : contactList) {
			// System.out.println("CONTACT LIST :"+con);
		}

		return totalMsgDocLst;
	}

	public long getTotalMessageAgentAndContactWiseV2(List<UniqueContactDto> contactIds, long dateRange1,
			long dateRange2, Object contact, String agent) {
		long totalMsgCount = 0;
		for (UniqueContactDto contactId : contactIds) {
			if (ArgUtil.isEmptyValue(contact)) {
				contact = "MESSAGE_" + contactId.getContactType();
			}
			long st = System.currentTimeMillis();
			// System.out.println("getTotalMessageAgentAndContactWiseV2 start Time:"+st);
			long count = getMsgCountAgentContactWiseV2(contactId.getContactId(), dateRange1, dateRange2, contact,
					agent);
			long et = System.currentTimeMillis();
			// System.out.println("getTotalMessageAgentAndContactWiseV2 End Time:"+et+"\t
			// difference :"+(et-st));
			if (ArgUtil.is(count)) {
				totalMsgCount += count;
			}
		}
		return totalMsgCount;
	}

	// To fetch all the records from a collection
	public List<MessageDoc> getMsgCountAgentContactWise(String contactId, long dateRange1, long dateRange2,
			Object contact, String agent) {

		List<MessageDoc> totalMsgDoc = new ArrayList<MessageDoc>();

		List<String> lst = getContactType(contact);
		for (String contactType : lst) {

			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(contactId));
			if (ArgUtil.is(agent)) {
				query.addCriteria(Criteria.where("agent").is(agent));
			}
			query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));

			query.with(Sort.by(Direction.ASC, "timestamp"));
			removeMsgFields(query);
			List<MessageDoc> totalMsg = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			// System.out.println("getMsgCountAgentContactWise query { ===}
			// "+JsonUtil.toJson(query));

			if (totalMsg != null && !totalMsg.isEmpty()) {
				totalMsgDoc.addAll(totalMsg);
			}
		}
		return totalMsgDoc;
	}

	// To fetch all the records from a collection
	public long getMsgCountAgentContactWiseV2(String contactId, long dateRange1, long dateRange2, Object contact,
			String agent) {
		long totalMsg = 0;

		List<String> lst = getContactType(contact);
		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(contactId));
			query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));
			if (ArgUtil.is(agent)) {
				query.addCriteria(Criteria.where("agent").is(agent));
			}
			query.with(Sort.by(Direction.ASC, "timestamp"));
			removeMsgFields(query);
			long count = mongoTemplate.count(query, contactType.toString());

			if (count > 0) {
				totalMsg += count;
			}
		}
		return totalMsg;
	}

	/** get Bot Score **/

	
	public long getBotScore(long dateRange1, long dateRange2) {

		long totalBotScore = 0;
		long averageBotScore = 0;
		Query query = new Query();
		query.addCriteria(Criteria.where("mode").is("BOT"));
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessField(query);
		List<ChatSessionDoc> botScoreLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		for (ChatSessionDoc chat : botScoreLst) {
			LOGGER.debug("Chat doc :" + chat.getBotScore());
			totalBotScore += chat.getBotScore() == null ? 0 : chat.getBotScore();
		}
		if (botScoreLst.size() > 0 && totalBotScore != 0) {
			averageBotScore = totalBotScore / botScoreLst.size();
		}
		return averageBotScore;
	}
	
	
	 public long getBotScoreV1(long dateRange1, long dateRange2) {
		    long averageBotScore = 0;
	        Query query = new Query();
	        query.addCriteria(Criteria.where("mode").is("BOT"));
	        query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
	        removeChatSessField(query);
	        // Fetch the list of ChatSessionDoc objects
	        List<ChatSessionDoc> botScoreLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

	        // Calculate total bot score using streams
	        long totalBotScore = botScoreLst.stream()
	                .mapToLong(chat -> chat.getBotScore() == null ? 0 : chat.getBotScore())
	                .sum();

	      //  LOGGER.debug("Total Bot Score: " + totalBotScore);
	        // Calculate the average bot score if the list is not empty
	        averageBotScore = botScoreLst.isEmpty() ? 0 : totalBotScore / botScoreLst.size();
	      
	        return averageBotScore;
	    }


	/** get Bot Score **/
	
	public double getBotClosure(long dateRange1, long dateRange2, long totalMsg) {
		long botSize = 0;
		double botClosure = 0;
		Query query = new Query();
		query.addCriteria(Criteria.where("mode").is("BOT"));
		query.addCriteria(Criteria.where("active").is(false));
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessField(query);
		List<ChatSessionDoc> botLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		if (botLst != null && !botLst.isEmpty()) {
			botSize = botLst.size();
		}
		if (ArgUtil.is(botSize) && ArgUtil.is(totalMsg) && totalMsg > 0) {
			botClosure = (botSize / totalMsg) * 100;
			BigDecimal bd = new BigDecimal(botClosure).setScale(2, RoundingMode.HALF_UP);
			botClosure = bd.doubleValue();
		}
		return botClosure;
	}
	
	
	public double getBotClosureV1(long dateRange1, long dateRange2, long totalMsg) {
        if (totalMsg <= 0) {
            return 0.0;
        }
        double botClosure =0.0;
        // Construct the query
        Query query = new Query();
        query.addCriteria(Criteria.where("mode").is("BOT"))
             .addCriteria(Criteria.where("active").is(false))
             .addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
        removeChatSessField(query);

        // Fetch the list of inactive BOT sessions within the date range
        long botSize = mongoTemplate.count(query, ChatSessionDoc.class, CHAT_SESSION);

        // Calculate bot closure percentage
        botClosure = ((double) botSize / totalMsg) * 100;
        botClosure =BigDecimal.valueOf(botClosure).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return botClosure;
    }

	/** get Satisfaction Score **/
	public double getSatisfactionScore(long dateRange1, long dateRange2, String agent) {

		double satisfactionScore = 0;
		double totSatisScore = 0;
		Query query = new Query();
		query.addCriteria(Criteria.where("mode").is("BOT"));
		// query.addCriteria(Criteria.where("assignedToQueue").is("feedback"));
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("feedback").exists(true));
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessFieldForSatisScore(query);
		List<ChatSessionDoc> botLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		if (botLst != null && !botLst.isEmpty()) {
			LOGGER.info("getSatisfactionScore :" + botLst.size());
			for (ChatSessionDoc doc : botLst) {
				if (doc.getFeedback() != null) {
					totSatisScore += doc.getFeedback().get("score") == null ? 0
							: (double) doc.getFeedback().get("score");
				}
			}
			LOGGER.info("totSatisScore :" + totSatisScore);
			if (totSatisScore > 0) {
				satisfactionScore = totSatisScore / botLst.size();
			}
		}
		// getSatisfactionScoreV1(dateRange1,dateRange2,agent);
		LOGGER.info("satisfactionScore OLD: " + satisfactionScore);
		return satisfactionScore;
	}

	public double getSatisfactionScoreV1(long dateRange1, long dateRange2, String agent) {
		Query query = new Query();
		query.addCriteria(Criteria.where("mode").is("BOT")).addCriteria(Criteria.where("assignedToAgent").is(agent))
				.addCriteria(Criteria.where("feedback").exists(true))
				.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessFieldForSatisScore(query);

		// Fetch the list of sessions that match the query
		List<ChatSessionDoc> botLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

		// Calculate the total satisfaction score
		double totalSatisfactionScore = botLst.stream()
				.filter(doc -> doc.getFeedback() != null && doc.getFeedback().get("score") != null)
				.mapToDouble(doc -> (double) doc.getFeedback().get("score")).sum();

		// Log information
		double satisfactionScore = botLst.isEmpty() ? 0.0 : totalSatisfactionScore / botLst.size();
		// Calculate the average satisfaction score

		LOGGER.info("satisfactionScore V1: " + satisfactionScore);
		return satisfactionScore;
	}

	public void removeMsgFields(Query query2) {
		query2.fields().exclude("model").exclude("meta").exclude("stamps").exclude("contact").exclude("tags")
				.exclude("attachments");
	}

	private void removeChatSessField(Query query2) {
		query2.fields().exclude("updated").exclude("lastInBoundMsg").exclude("lastMsg").exclude("lastBotReply");
	}

	private void removeChatSessFieldForSatisScore(Query query2) {
		query2.fields().exclude("updated").exclude("lastInBoundMsg").exclude("lastMsg").exclude("lastBotReply")
				.exclude("msg").exclude("stamps").exclude("contact");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<String> getContactType(Object contact) {
		List<String> lst = null;
		if (ArgUtil.is(contact) && contact != null) {
			lst = new ArrayList<String>();
			if (contact instanceof ArrayList) {
				lst = (ArrayList) contact;
			} else {
				lst.add(contact.toString());
			}

		} else {
			lst = adminDbMgr.getListOfContactType();
		}
		return lst;
	}

	/** Fetch data asynchronously **/
	public List<DashBoardResponseDto> fetchDataAsynchronously(List<String> allAgent, long date1, long date2,
			Object contactType) {
		Long st = Instant.now().getEpochSecond();

		// Define the ExecutorService
		ExecutorService executorService = Executors.newFixedThreadPool(allAgent.size());
		// Fetch data asynchronously
		List<CompletableFuture<DashBoardResponseDto>> futures = allAgent.stream()
				.map(agent -> fetchAgentAnalyticsAsync(agent, date1, date2, contactType, executorService))
				.collect(Collectors.toList());

		// Wait for all futures to complete and collect results
		List<DashBoardResponseDto> results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

		// Shutdown the executor service
		executorService.shutdown();
		Long et = Instant.now().getEpochSecond();
		System.out.println("fetchDataAsynchronously tTime {} " + (et - st));

		return results;
	}

	/** Asynchronous method to fetch agent analytics **/
	private CompletableFuture<DashBoardResponseDto> fetchAgentAnalyticsAsync(String agent, long date1, long date2,
			Object contactType, ExecutorService executorService) {

		return CompletableFuture.supplyAsync(() -> {
			try {
				DashBoardResponseDto dto = new DashBoardResponseDto();
				dto = getAgentAnalytics(agent, date1, date2, contactType);
				System.out.println("agent :" + agent + "\t dto :" + JsonUtil.toJson(dto));
				return dto;

			} catch (Exception e) {
				System.err.println("Error fetching data for agent " + agent + ": " + e.getMessage());
				return new DashBoardResponseDto(); // Return an empty DTO or handle appropriately
			}
		}, executorService);
	}

	public Map<Object, Object> getHourWiseCountV2(List<UniqueContactDto> contactIds, String agent, long dateRange1,
			long dateRange2, Object contact) {
		Map<Object, Object> mapLst = new HashMap<>(); // Store date (day) as key and count as value
		Map<String, Long> hourCountMap = new HashMap<>();
		Map<Long, Long> hourCountMapV1 = new HashMap<>();

		if (ArgUtil.isEmptyValue(contact)) {
			for (UniqueContactDto contactId : contactIds) {
				System.out.println("Agent :" + agent + "\t contactId :" + JsonUtil.toJson(contactId));
				String contactType = "MESSAGE_" + contactId.getContactType();
				Query query = new Query();
				query.addCriteria(Criteria.where("contactId").is(contactId.getContactId()));
				if (ArgUtil.is(agent)) {
					query.addCriteria(Criteria.where("agent").is(agent));
				}
				query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));
				query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
				query.fields().include("_id").include("timestamp").include("time");
				List<MessageDoc> msgLst = mongoTemplate.find(query, MessageDoc.class, contactType);
				for (MessageDoc msg : msgLst) {
					long timeStamp = msg.getTimestamp();
					Date date = new Date(timeStamp);
					SimpleDateFormat sdfH = new SimpleDateFormat("hh aa");
					String formattedDateH = sdfH.format(date);
					timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60)));
					// Increment the count for this day in the map
					hourCountMap.put(formattedDateH, hourCountMap.getOrDefault(formattedDateH, 0L) + 1);
					hourCountMapV1.put(timeStamp, hourCountMapV1.getOrDefault(timeStamp, 0L) + 1);
				}
			}

		}

		mapLst.put("HR", hourCountMap);
		mapLst.put("HR_V1", hourCountMapV1);
		return mapLst;
	}

	public void getHourWiseCountV3(List<UniqueContactDto> contactIds, String agent, long dateRange1, long dateRange2,
			Object contact) {
		if (ArgUtil.isEmptyValue(contact)) {
			for (UniqueContactDto contactId : contactIds) {
				System.out.println("Agent: " + agent + "\t contactId: " + JsonUtil.toJson(contactId));
				String contactType = "MESSAGE_" + contactId.getContactType();

				// Create Aggregation Pipeline
				Aggregation aggregation = Aggregation.newAggregation(
						// Match Stage
						Aggregation.match(Criteria.where("contactId").is(contactId.getContactId()).and("agent")
								.is(agent).and("timestamp").gte(new Date(dateRange1)).lte(new Date(dateRange2))),

						// Project Stage
						Aggregation.project("timestamp").andExpression("year(timestamp)").as("year")
								.andExpression("month(timestamp)").as("month").andExpression("dayOfMonth(timestamp)")
								.as("day").andExpression("hour(timestamp)").as("hour")
								.andExpression("minute(timestamp)").as("minute").andExpression("second(timestamp)")
								.as("second").andExpression("dateToMillis(timestamp)").as("hourStart")
								.andExpression("concat("
										+ "toString( cond([eq([mod([hour(timestamp), 12]), 0]), 12, mod([hour(timestamp), 12]) ])), "
										+ "cond([gte([hour(timestamp), 12]), 'PM', 'AM'])" + ")")
								.as("hour12Format"),

						// Group Stage
						Aggregation.group("hour", "hour12Format", "hourStart").count().as("count"),

						// Project Stage
						Aggregation.project("count").and("_id.hour").as("hour").and("_id.hour12Format")
								.as("hour12Format").and("_id.hourStart").as("hourStamp"),

						// Sort Stage
						Aggregation.sort(Sort.by(Sort.Order.asc("hour"))));

				// Execute Aggregation
				AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, contactType, Map.class);

				// Process Results
				List<Map> resultList = results.getMappedResults();
				Map<String, Integer> resultMap = new HashMap<>();
				for (Map res : resultList) {
					String key = res.get("hour") + " " + res.get("hour12Format");
					Integer count = (Integer) res.get("count");
					resultMap.put(key, count);
				}
				System.out.println("ResultMap: " + agent + " ====" + JsonUtil.toJson(resultMap));
			}
		}
	}

	public Map<Object, Object> getDateWiseCountV2(List<UniqueContactDto> contactIds, String agent, long dateRange1,
			long dateRange2, Object contact) {
		Map<Object, Object> mapLst = new HashMap<>(); // Store date (day) as key and count as value
		Map<Long, Long> dayMapLst = new HashMap<>();
		Map<Long, Long> dayMapLstV1 = new HashMap<>();
		if (ArgUtil.isEmptyValue(contact)) {
			for (UniqueContactDto contactId : contactIds) {
				String contactType = "MESSAGE_" + contactId.getContactType();
				Query query = new Query();
				query.addCriteria(Criteria.where("contactId").is(contactId.getContactId()));
				if (ArgUtil.is(agent)) {
					query.addCriteria(Criteria.where("agent").is(agent));
				}
				query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));

				query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
				query.fields().include("_id").include("timestamp").include("time");
				List<MessageDoc> msgLst = mongoTemplate.find(query, MessageDoc.class, contactType);

				for (MessageDoc msg : msgLst) {
					long timeStamp = msg.getTimestamp();
					Date date = new Date(timeStamp);
					long day = Long.parseLong(new SimpleDateFormat("d").format(date));
					timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60 * 24)));
					// Increment the count for this day in the map
					dayMapLst.put(day, dayMapLst.getOrDefault(day, 0L) + 1);
					dayMapLstV1.put(timeStamp, dayMapLstV1.getOrDefault(timeStamp, 0L) + 1);
				}
			}
		}
		mapLst.put("DAY", dayMapLst);
		mapLst.put("DAY_V1", dayMapLstV1);

		return mapLst;
	}

	  public Map<Object, Object> getDateWiseCountV3(List<UniqueContactDto> contactIds, String agent, long dateRange1, long dateRange2, Object contact) {
	        Map<Object, Object> mapLst = new HashMap<>();
	        Map<Long, Long> dayMapLst = new HashMap<>();
	   	   Map<Long, Long> dayMapLstV1 = new HashMap<>();
	        try {

	        if (ArgUtil.isEmptyValue(contact)) {
	            for (UniqueContactDto contactId : contactIds) {
	                String contactType = "MESSAGE_" + contactId.getContactType();

	                // Create match operation to filter by contactId, agent, and date range
	                MatchOperation matchOperation = Aggregation.match(Criteria
	                        .where("contactId").is(contactId.getContactId())
	                        .and("timestamp").gte(dateRange1).lt(dateRange2)
	                        .and(Optional.ofNullable(agent).isPresent() ? "agent" : null).is(Optional.ofNullable(agent).orElse(null)));

	                // Convert timestamp (long) to BSON Date and format to day string
	                AggregationOperation projectToDay = context -> new Document("$project",
	                        new Document("day", new Document("$dateToString", 
	                                new Document("format", "%Y-%m-%d")
	                                .append("date", new Document("$add", Arrays.asList(new Date(0), "$timestamp")))))
	                        .append("time", "$time"));

	                // Group by the formatted date string
	                GroupOperation groupOperation = Aggregation.group("day").count().as("count");

	                // Sort by the day
	                SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"));

	                // Create the aggregation pipeline
	                Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectToDay, groupOperation, sortOperation);

	                // Execute the aggregation
	                AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, contactType, Document.class);

	       
	                for (Document result : results.getMappedResults()) {
	                    String dayStr = result.getString("_id"); // Group key
	                    Long count = Long.valueOf(result.getInteger("count").longValue()); 
	                    
	                    SimpleDateFormat sdf = new SimpleDateFormat("d");
	                    long day = Long.parseLong(sdf.format(new SimpleDateFormat("yyyy-MM-dd").parse(dayStr)));

	                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd");
	                    long timestamp = timestampFormat.parse(dayStr).getTime();

	                    dayMapLst.put(day, dayMapLst.getOrDefault(day, 0L) + count);
		                dayMapLstV1.put(timestamp, dayMapLstV1.getOrDefault(timestamp, 0L) + count);
		
	                }
	            }
	        }
	        
	        mapLst.put("DAY", dayMapLst);
		    mapLst.put("DAY_V1", dayMapLstV1);
	        }catch(Exception e) {
	        	e.printStackTrace();
	        }

	        return mapLst;
	    }

	
	public Map<Object, Object> getWeekWiseCountV2(List<UniqueContactDto> contactIds, String agent, long dateRange1,
			long dateRange2, Object contact) {
		Map<Object, Object> mapLst = new HashMap<>(); // Store date (day) as key and count as value
		Map<String, Long> weekCountMap = new HashMap<>();
		Map<Long, Long> weekCountMapV1 = new HashMap<>();
		if (ArgUtil.isEmptyValue(contact)) {
			for (UniqueContactDto contactId : contactIds) {
				String contactType = "MESSAGE_" + contactId.getContactType();
				Query query = new Query();
				query.addCriteria(Criteria.where("contactId").is(contactId.getContactId()));
				if (ArgUtil.is(agent)) {
					query.addCriteria(Criteria.where("agent").is(agent));
				}
				query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));

				query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
				query.fields().include("_id").include("timestamp").include("time");
				List<MessageDoc> msgLst = mongoTemplate.find(query, MessageDoc.class, contactType);

				for (MessageDoc msg : msgLst) {
					long timeStamp = msg.getTimestamp();
					Date date = new Date(timeStamp);
					String monthWise = new SimpleDateFormat("MMM").format(date);
					timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60 * 24)));
					// Increment the count for this day in the map
					weekCountMap.put(monthWise, weekCountMap.getOrDefault(weekCountMap, 0L) + 1);
					weekCountMapV1.put(timeStamp, weekCountMapV1.getOrDefault(timeStamp, 0L) + 1);
				}
			}
		}
		mapLst.put("WEEK", weekCountMap);
		mapLst.put("WEEK_V1", weekCountMapV1);

		return mapLst;
	}

	private String getMitlToSeconds(long timeInMillis) {
		// Convert milliseconds to seconds
		double timeInSeconds = timeInMillis / 1000.0;

		// Format the result to 2 decimal places
		DecimalFormat df = new DecimalFormat("#.##");
		String formattedTime = df.format(timeInSeconds);
		return formattedTime;
	}

	public List<DashBoardResponseDto> getAgentAnalyticsMultiThreaded(List<String> allAgent, long date1, long date2,
			Object contactType) {
		List<DashBoardResponseDto> lstDto = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(Math.min(allAgent.size(), 10)); // Adjust pool
																										// size
		List<Future<DashBoardResponseDto>> futures = new ArrayList<>();

		for (String agent : allAgent) {
			// Skip blank agents

			// Each task creates its own instance of DashBoardResponseDto
			Callable<DashBoardResponseDto> task = () -> {

				DashBoardResponseDto dto = new DashBoardResponseDto();

				try {
					dto = getAgentAnalytics(agent, date1, date2, contactType);
				} catch (Exception e) {
					// Log exception details
					System.err.println("Exception occurred for agent: " + agent);
					e.printStackTrace();
				}
				// Log dto content for debugging
				if (dto != null) {
					System.out.println("dto for agent VALUE {====}{" + agent + "}: " + JsonUtil.toJson(dto));
				} else {
					System.out.println("dto for agent {" + agent + "} is null");
				}

				return dto; // Return the individual DTO created for this task
			};

			// Submit the task to the executor service
			futures.add(executorService.submit(task));
		}

		// Collect the results from the futures
		for (Future<DashBoardResponseDto> future : futures) {
			try {
				DashBoardResponseDto dto = future.get(30, TimeUnit.SECONDS); // Add timeout to prevent indefinite
																				// blocking
				if (dto != null) {
					lstDto.add(dto);
				}
			} catch (Exception e1) {
				System.err.println("Failed to retrieve result from future.");
				e1.printStackTrace();
			}
		}

		executorService.shutdown(); // Shutdown the executor service

		return lstDto; // Return the list of DTOs
	}

}
