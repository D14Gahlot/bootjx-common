package com.boot.jx.admin.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.LeadMessanger;
import com.boot.jx.admin.dto.PeakLoadDto;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageMetaWrapper;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
public class AgentAnalyticsManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentAnalyticsManager.class);
	public static final String CHAT_SESSION = "CHAT_SESSION";

	public static final String DEFAULT_AGENT = "TEAM";
	public static final int OPEN_CONV_HR_LMT = 5;

	public static final int OPEN_CONV_HR = 1;

	public static final String MY_BOT = "BOT";

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	AdminDashBoardManager adminDbMgr;

	public List<DashBoardResponseDto> getAgentWiseAnalytics(DashBoardRequestDto req) {
		LOGGER.info("getAgentWiseAnalytics {} :" + JsonUtil.toJson(req));
		List<DashBoardResponseDto> lstDto = new ArrayList<>();
		DashBoardResponseDto dto = null;
		List<ChatSessionDoc> allAgent = null;
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
			for (Object chatSess : allAgent) {
				dto = new DashBoardResponseDto();
				String agent = (String) chatSess;
				//dto = getAgentAnalytics(agent, date1, date2);
				dto = getAgentAnalytics(agent, date1, date2,req.getContactType());
				lstDto.add(dto);
			}
		} else {
			//dto = getAgentAnalytics(req.getAgent(), date1, date2);
			dto = getAgentAnalytics(req.getAgent(), date1, date2,req.getContactType());
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
		int teamSize = dtoLst.size();
		long totSatisScore = 0;
		long totSatisFeedback = 0;
		Map<Object, Object> graphApiMap = new HashMap<Object, Object>();
		Map<Object, Object> graphApiMapV1 = new HashMap<Object, Object>();

		dto.setPeakLoad(new PeakLoadDto());
		for (DashBoardResponseDto dt : dtoLst) {
			LOGGER.debug("get Agent/channel  wise {  ==== }:" + JsonUtil.toJson(dto));
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
			dto.setLeadMessanger(dt.getLeadMessanger());
			graphApiMap = mergerMapKyAndValue(graphApiMap, dt.getGraphApiDetails());
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
		dto.setGraphApiDetails(graphApiMap);
		if (graphApiMapV1 != null && !graphApiMapV1.isEmpty()) {
			dto.setGraphApiDetailsV1(graphApiMapV1);
		}
		if (totSatisScore > 0) {
			dto.setSatisfactionScore(totSatisScore / totSatisFeedback);
		}

		LOGGER.debug("\n\n get Summary ========:" + JsonUtil.toJson(dto));
		return dto;
	}

	public DashBoardResponseDto getAgentAnalytics(String agent, long dateRange1, long dateRange2,Object contact) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LOGGER.info(dtf.format(LocalDateTime.now()) + " Get Analytics for  :" + agent);
		DashBoardResponseDto dto = new DashBoardResponseDto();
		dto.setAgentName(agent == null ? MY_BOT : agent);
		/** Unique agent list **/

		List<ChatSessionDoc> distinctContactLst = getUniqueAgentWiseContactList(agent, dateRange1, dateRange2);
		if (ArgUtil.is(distinctContactLst)) {
			dto.setUniqueConversation(distinctContactLst.size());
		}
		/** Total Msg exchanged chat session . **/
		// List<ChatSessionDoc> totalMsgExchanged
		// =getAgentWiseTotalMsgExchanged(agent,dateRange1,dateRange2);
		/*
		 * if(ArgUtil.is(totalMsgExchanged)) {
		 * dto.setTotalMsgExchanged(totalMsgExchanged.size()); }
		 */
		/** Total Agent-contact wise msg **/

		List<MessageDoc> totalAgConMsgExchanged = getTotalMessageAgentAndContactWise(distinctContactLst, dateRange1,
				dateRange2,contact);
		if (ArgUtil.is(totalAgConMsgExchanged)) {
			dto.setTotalMsgExchanged(totalAgConMsgExchanged.size());

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

		List<ChatSessionDoc> openConvesLst = getAgentWiseOpenConversation(agent, dateRange1, dateRange2);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		/** Resolved Conversation **/

		List<ChatSessionDoc> resolvedConversation = getAgentWiseResolvedConversation(agent, dateRange1, dateRange2);
		if (ArgUtil.is(resolvedConversation)) {
			dto.setResolvedConversation(resolvedConversation.size());
		}

		/** Peak Load **/

		PeakLoadDto peakLoadResult = adminDbMgr.getPeakLoadMsgCount(totalAgConMsgExchanged);// getAgentPeakLoadMsgCount(totalMsgExchanged);
		dto.setPeakLoad(peakLoadResult);

		/** lead Messanger **/

		LeadMessanger leadMsg = getLeadMessenger(agent, dateRange1, dateRange2,contact);
		dto.setLeadMessanger(leadMsg);

		/** Converation duration **/

		long conVerDuration = getConversationDuration(agent, dateRange1, dateRange2);
		if (ArgUtil.is(conVerDuration)) {
			dto.setConverDuration(conVerDuration);
		}

		/** startLag **/

		double startLag = getStartLag(agent, dateRange1, dateRange2);
		dto.setStartLag(startLag);

		/** bot score **/

		long botScore = getBotScore(dateRange1, dateRange2);
		dto.setBotScore(botScore);

		/** bot closure **/

		double botClosure = getBotClosure(dateRange1, dateRange2, dto.getTotalMsgExchanged());
		dto.setBotClosure(botClosure);

		/** Satisfaction score **/
		double satisfactionScore = getSatisfactionScore(dateRange1, dateRange2, agent);
		dto.setSatisfactionScore(satisfactionScore);

		/** find the date diff between two dates **/

		Map<String, Integer> dateDiffMAp = getDateDiff(dateRange1, dateRange2);
		int hour = 0;
		int days = 0;
		if (ArgUtil.is(dateDiffMAp)) {
			hour = dateDiffMAp.get("HOUR");
			days = dateDiffMAp.get("DAYS");
		}
		LOGGER.debug("mru hour :" + hour + "\t dateDiffMAp :" + dateDiffMAp);

		if (hour <= 24) {
			Map<Object, Object> hourWiseCount = adminDbMgr.getHourWiseCount(totalAgConMsgExchanged);
			dto.setGraphApiDetails(hourWiseCount);
			Map<Object, Object> hourWiseCountV1 = adminDbMgr.getHourWiseCountV1(totalAgConMsgExchanged);
			dto.setGraphApiDetailsV1(hourWiseCountV1);
		} else if (hour > 24 && days <= 31) {
			Map<Object, Object> dateWiseCount = adminDbMgr.getDateWiseCount(totalAgConMsgExchanged);
			dto.setGraphApiDetails(dateWiseCount);
			Map<Object, Object> timeStampWiseCount = adminDbMgr.getTimeStampWiseCount(totalAgConMsgExchanged);
			dto.setGraphApiDetailsV1(timeStampWiseCount);
		} else {
			Map<Object, Object> dweekWiseCount = adminDbMgr.getWeekWiseCount(totalAgConMsgExchanged);
			dto.setGraphApiDetails(dweekWiseCount);
			Map<Object, Object> timeStampWiseCount = adminDbMgr.getWeekWiseCountV1(totalAgConMsgExchanged);
			dto.setGraphApiDetailsV1(timeStampWiseCount);

		}
		return dto;
	}

	public List<ChatSessionDoc> getAgentList() {
		List<ChatSessionDoc> distinceAgentList = mongoTemplate.getCollection("CHAT_SESSION")
				.distinct("assignedToAgent");
		return distinceAgentList;
	}

	@SuppressWarnings("unchecked")
	public List<ChatSessionDoc> getAgentList(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		List<ChatSessionDoc> distinceAgentList = mongoTemplate.getCollection("CHAT_SESSION").distinct("assignedToAgent",
				query.getQueryObject());
		if (distinceAgentList == null || distinceAgentList.isEmpty()) {
			distinceAgentList = getDefaultAgent(dateRange1, dateRange2);
		}
		return distinceAgentList;
	}

	public List<ChatSessionDoc> getDefaultAgent(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		List<ChatSessionDoc> distinceAgentList = mongoTemplate.getCollection("CHAT_SESSION").distinct("mode",
				query.getQueryObject());
		return distinceAgentList;
	}

	public List<ChatSessionDoc> getDefaultDistinctContact(long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessField(query);
		List<ChatSessionDoc> distinceAgentList = mongoTemplate.getCollection("CHAT_SESSION").distinct("contactId",
				query.getQueryObject());
		return distinceAgentList;
	}

	@SuppressWarnings("unchecked")
	public List<ChatSessionDoc> getUniqueAgentWiseContactList(String agent, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessField(query);
		List<ChatSessionDoc> distinctIdList = mongoTemplate.getCollection(CHAT_SESSION).distinct("contactId",
				query.getQueryObject());
		if (distinctIdList == null || distinctIdList.isEmpty()) {
			distinctIdList = getDefaultDistinctContact(dateRange1, dateRange2);
		}

		return distinctIdList;
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
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);

		for (ChatSessionDoc chatDoc : totalMsgDoc) {
			long assignToAgent = chatDoc.getAssignedAgentStamp();
			long diffInMilliSeconds = currentTimeStamp - assignToAgent;
			int diffInHours = (int) (diffInMilliSeconds / (60 * 60 * 1000));
			// if(diffInHours>OPEN_CONV_HR_LMT) {
			totalOpenMsgDoc.add(chatDoc);
			// }
		}
		return totalOpenMsgDoc;
	}

	public List<ChatSessionDoc> getAgentWiseResolvedConversation(String agent, long dateRange1, long dateRange2) {
		List<ChatSessionDoc> totalResolvedMsgDoc = new ArrayList<ChatSessionDoc>();
		long currentTimeStamp = System.currentTimeMillis();

		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("resolved").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		for (ChatSessionDoc chatDoc : totalMsgDoc) {
			totalResolvedMsgDoc.add(chatDoc);
		}
		return totalResolvedMsgDoc;
	}

	public long getConversationDuration(String agent, long startTime, long endTime) {
		Map<String, Long> conVerMsgLst = new HashMap<String, Long>();
		Long maxEntryKeyValue = new Long(0);
		List<ChatSessionDoc> uniquContactIdLst = getUniqueAgentWiseContactList(agent, startTime, endTime);
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

	public double getStartLag(String agent, long dateRange1, long dateRange2) {
		Map<String, Double> startLagMapLst = new HashMap<String, Double>();
		double startLag = 0.0d;
		double percentageWithDecimal = 0.0d;

		List<ChatSessionDoc> uniquContactIdLst = getUniqueAgentWiseContactList(agent, dateRange1, dateRange2);
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
		List<String> lst = getContactType(contact);
		Map<String, Integer> leasMsgLst = new HashMap<String, Integer>();
		for (String contactType : lst) {
			List<MessageDoc> msgDocLst = adminDbMgr.getTotalMsgCount(contactType, startTime, endTime);
			leasMsgLst.put(contactType, msgDocLst.size());
		}
		LOGGER.debug("lead Msg  :" + leasMsgLst.toString());

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
		// System.out.println("difference in minutes: " +
		// decimalFormatter.format(diffInMin));

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
		map2.forEach(
				(key, value) -> mergeValue.merge(key, value, (v1, v2) -> v1 == v2 ? v1 : (Integer) v1 + (Integer) v2));
		return mergeValue;
	}

	// Get Total Msg from

	public List<MessageDoc> getTotalMessageAgentAndContactWise(List<ChatSessionDoc> lstChatSession, long dateRange1,
			long dateRange2,Object contact) {
		List<MessageDoc> totalMsgDocLst = new ArrayList<MessageDoc>();
		for (Object chatSession : lstChatSession) {
			String contactId = (String) chatSession;
			List<MessageDoc> msgDocLst = getMsgCountAgentContactWise(contactId, dateRange1, dateRange2,contact);
			totalMsgDocLst.addAll(msgDocLst);
		}
		// LOGGER.debug("getTotalMessageAgentAndContactWise
		// :"+totalMsgDocLst==null?BigDecimal.ZERO:totalMsgDocLst.size());
		return totalMsgDocLst;
	}

	// To fetch all the records from a collection
	public List<MessageDoc> getMsgCountAgentContactWise(String contactId, long dateRange1, long dateRange2,Object contact) {

		List<MessageDoc> totalMsgDoc = new ArrayList<MessageDoc>();

		//List<String> lst = adminDbMgr.getListOfContactType();
		List<String> lst = getContactType(contact);
		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(contactId));
			query.addCriteria(Criteria.where("timestamp").gte(dateRange1).lt(dateRange2));
			query.with(new Sort(new Order(Direction.ASC, "timestamp")));
			removeMsgFields(query);
			List<MessageDoc> totalMsg = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			totalMsgDoc.addAll(totalMsg);
		}
		return totalMsgDoc;
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

	/** get Satisfaction Score **/
	public double getSatisfactionScore(long dateRange1, long dateRange2, String agent) {

		double satisfactionScore = 0;
		double totSatisScore = 0;
		Query query = new Query();
		query.addCriteria(Criteria.where("mode").is("BOT"));
		query.addCriteria(Criteria.where("assignedToQueue").is("feedback"));
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("startSessionStamp").gt(dateRange1).lt(dateRange2));
		removeChatSessFieldForSatisScore(query);
		List<ChatSessionDoc> botLst = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		if (botLst != null && !botLst.isEmpty()) {
			LOGGER.info("getSatisfactionScore :"+botLst.size());
			for (ChatSessionDoc doc : botLst) {
				if(doc.getFeedback()!=null) {
				totSatisScore += doc.getFeedback().get("score") == null ? 0 : (double) doc.getFeedback().get("score");
				}
			}
			LOGGER.info("totSatisScore :"+totSatisScore);
			if (totSatisScore > 0) {
				satisfactionScore = totSatisScore / botLst.size();
			}
		}
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
	
	@SuppressWarnings("rawtypes")
	public List<String> getContactType(Object contact){
			List<String> lst =null;
			if(ArgUtil.is(contact)) {
				lst =new ArrayList<String>();
				lst = (ArrayList)contact;
			}else{
			  lst = adminDbMgr.getListOfContactType();
			}
			return lst;
	}
}
