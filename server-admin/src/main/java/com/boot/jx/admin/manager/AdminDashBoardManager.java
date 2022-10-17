package com.boot.jx.admin.manager;

//imports as static
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.ContactTypeCountDto;
import com.boot.jx.admin.dto.ContactTypeSummaryDto;
import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.DateWiseHourCountDto;
import com.boot.jx.admin.dto.LeadMessanger;
import com.boot.jx.admin.dto.PeakLoadDto;
import com.boot.jx.admin.dto.SummaryDocDto;
import com.boot.jx.admin.dto.TagDocumentDto;
import com.boot.jx.admin.dto.TagDocumentLst;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.TagDocument;
import com.boot.utils.ArgUtil;
import com.boot.utils.DateUtil;
import com.boot.utils.JsonUtil;
import com.mongodb.client.MongoCursor;

@Component
public class AdminDashBoardManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminDashBoardManager.class);

	private static final String COLLECTION = "MessageDoc.class";

	public static final String COLLECTION_NAME = "MESSAGE_";

	public static final String DEFAULT_TEAM = "TEAM";

	@Autowired
	CommonMongoTemplate mongoTemplate;

	@Autowired
	AgentAnalyticsManager agentAnaMgr;

	private String getCollectionName(Object contactType) {
		return (MessageDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(contactType, "OTHERS"));
	}

	public List<MessageDoc> testDashBoard() {
		LOGGER.info("Collection Exists? " + mongoTemplate.collectionExists("MESSAGE_TWITTER"));
		LOGGER.info("Collection Exists? " + mongoTemplate.collectionExists(COLLECTION));

		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("I"));
		List<MessageDoc> msgDoc = mongoTemplate.find(query, MessageDoc.class, "MESSAGE_TWITTER");
		LOGGER.info("Total Out Msg :" + msgDoc.size());

		Set<String> contactTypeSet = mongoTemplate.getCollectionNames();
		List<String> aList = new ArrayList<String>();
		for (String channel : contactTypeSet) {
			if (channel.contains(COLLECTION_NAME)) {
				aList.add(channel);
			}
		}
		List<String> filtered = contactTypeSet.stream().filter(x -> !x.isEmpty() && x.startsWith(COLLECTION_NAME))
				.collect(Collectors.toList());

		for (String s : filtered) {
			LOGGER.info("Array List Stream :" + s);
		}

		return msgDoc;
	}

	public List<DashBoardResponseDto> getContactWiseDashBoardAnalytics(DashBoardRequestDto req) {
		// System.out.println("getContactWiseDashBoardAnalytics { }
		// :"+JsonUtil.toJson(req));
		List<DashBoardResponseDto> dtoLst = new ArrayList<DashBoardResponseDto>();
		DashBoardResponseDto dto = null;
		List<String> lstContactType = new ArrayList<String>();

		long dateRange1 = 0;
		long dateRange2 = 0;
		if (ArgUtil.is(req.getDateRange1()) && req.getDateRange1() > 0) {
			dateRange1 = req.getDateRange1();
		} else {
			dateRange1 = agentAnaMgr.todayStartTime();
		}
		if (ArgUtil.is(req.getDateReange2()) && req.getDateReange2() > 0) {
			dateRange2 = req.getDateReange2();
		} else {
			dateRange2 = agentAnaMgr.todayEndTime();
		}

		if (req != null && (req.getContactType() == null || ArgUtil.isEmpty(req.getContactType()))) {
			lstContactType = getListOfContactType();
		}

		if (lstContactType != null && !lstContactType.isEmpty()) {
			for (String messageDoc : lstContactType) {
				dto = new DashBoardResponseDto();
				String contactType = (String) messageDoc;
				dto = getCotactWiseAnalytics(contactType, dateRange1, dateRange2);
				dtoLst.add(dto);
			}
		} else {
			dto = getCotactWiseAnalytics(req.getContactType().toString(), dateRange1, dateRange2);
			dtoLst.add(dto);
		}

		if (!dtoLst.isEmpty() && dtoLst.size() > 1) {
			DashBoardResponseDto dtoTeam = getTeamWiseAnalytics(dtoLst);
			dtoLst.add(dtoTeam);
		}
		return dtoLst;
	}

	public DashBoardResponseDto getTeamWiseAnalytics(List<DashBoardResponseDto> dtoLst) {
		DashBoardResponseDto dto = agentAnaMgr.getSummery(dtoLst);
		dto.setContactType(DEFAULT_TEAM);
		dto.setAgentName("");
		return dto;
	}

	public DashBoardResponseDto getCotactWiseAnalytics(String contactType, long dateRange1, long dateRange2) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		dto.setContactType(contactType);

		/** total In msg **/
		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		/** total out msg **/
		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}

		/** To fetch all the records for a collection **/
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}
		/** Get the distinct stuff from MongoDB **/
		List<String> distinctIdList = getUniqueConversation(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		/** Peak Load **/
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);
		dto.setPeakLoad(peakLoadResult);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, dateRange1, dateRange2);
		dto.setLeadMessanger(leadMsg);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		/** find the date diff between two dates **/
		Map<String, Integer> dateDiffMAp = agentAnaMgr.getDateDiff(dateRange1, dateRange2);
		int hour = 0;
		int days = 0;
		if (ArgUtil.is(dateDiffMAp)) {
			hour = dateDiffMAp.get("HOUR");
			days = dateDiffMAp.get("DAYS");
		}
		if (hour <= 24) {
			Map<Object, Object> hourWiseCount = getHourWiseCount(totalMsgDoc);
			dto.setGraphApiDetails(hourWiseCount);
		} else if (hour > 24 && days <= 30) {
			Map<Object, Object> dateWiseCount = getDateWiseCount(totalMsgDoc);
			dto.setGraphApiDetails(dateWiseCount);
		} else {
			Map<Object, Object> dweekWiseCount = getWeekWiseCount(totalMsgDoc);
			dto.setGraphApiDetails(dweekWiseCount);
		}

		return dto;
	}

	public List<DashBoardResponseDto> getDashBoardAnalytics(DashBoardRequestDto requestDto) {
		List<DashBoardResponseDto> dtoLst = new ArrayList<DashBoardResponseDto>();

		long epochTime = System.currentTimeMillis();
		// System.out.println("epochTime :" + epochTime);

		DashBoardResponseDto today = todayAnalystics(requestDto);
		DashBoardResponseDto yesterday = yesterdayAnalystics(requestDto);
		DashBoardResponseDto week = weekAnalystics(requestDto);
		DashBoardResponseDto month = monthAnalystics(requestDto);
		DashBoardResponseDto quater = quaterAnalystics(requestDto);
		DashBoardResponseDto dateRange = dateRange(requestDto);

		dtoLst.add(today);
		dtoLst.add(yesterday);
		dtoLst.add(week);
		dtoLst.add(month);
		dtoLst.add(quater);
		dtoLst.add(dateRange);

		return dtoLst;

	}

	public DashBoardResponseDto todayAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();

		long epochTime = System.currentTimeMillis();
		Object contactType = requestDto.getContactType();

		// get a datetime plus time zone information using the system time zone
		// subtract a day
		// and take the minimum time a day can have
		ZonedDateTime todayStartTime = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		ZonedDateTime endToday = todayStartTime.with(LocalTime.MAX);
		long longTodayStartTime = todayStartTime.toInstant().toEpochMilli();
		long longTodayendTime = endToday.toInstant().toEpochMilli();

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, longTodayStartTime, longTodayendTime);
		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, longTodayStartTime, longTodayendTime);
		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, longTodayStartTime, longTodayendTime);
		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, longTodayStartTime, longTodayendTime);
		// System.out.println("distinctIdList :" + distinctIdList.size());

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, longTodayStartTime, longTodayendTime);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, longTodayStartTime, longTodayendTime);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		Map<Object, Object> hourWiseCount = getHourWiseCount(totalMsgDoc);

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}
		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		if (ArgUtil.is(hourWiseCount)) {
			dto.setGraphApiDetails(hourWiseCount);
		}

		dto.setContactType(contactType);
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);
		dto.setFilter("TODAY");

		return dto;

	}

	public DashBoardResponseDto yesterdayAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();
		// get a datetime plus time zone information using the system time zone
		// subtract a day
		// and take the minimum time a day can have
		ZonedDateTime todayStartTime = ZonedDateTime.now().minusDays(1).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		ZonedDateTime endToday = todayStartTime.with(LocalTime.MAX);
		long longTodayStartTime = todayStartTime.toInstant().toEpochMilli();
		long longTodayendTime = endToday.toInstant().toEpochMilli();

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, longTodayStartTime, longTodayendTime);
		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, longTodayStartTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, longTodayStartTime, longTodayendTime);
		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, longTodayStartTime, longTodayendTime);
		Map<Object, Object> hourWiseCount = getHourWiseCount(totalMsgDoc);

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, longTodayStartTime, longTodayendTime);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, longTodayStartTime, longTodayendTime);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}

		// dto.setTotalMsgExchanged(dto.getTotalInMsgExchanged()+dto.getTotalOutMsgExchanged());
		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		if (ArgUtil.is(hourWiseCount)) {
			dto.setGraphApiDetails(hourWiseCount);
		}

		dto.setContactType(contactType);
		dto.setFilter("YESTERDAY");
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);

		return dto;

	}

	public DashBoardResponseDto weekAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();
		// get a datetime plus time zone information using the system time zone
		// subtract a day
		// and take the minimum time a day can have
		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		ZonedDateTime weekStartDay = ZonedDateTime.now().minusWeeks(1).with(LocalTime.MIN);

		long longTodayStartTime = todayDate.toInstant().toEpochMilli();
		long longTodayendTime = endToday.toInstant().toEpochMilli();
		long longWStartTime = weekStartDay.toInstant().toEpochMilli();

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, longWStartTime, longTodayendTime);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, longWStartTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, longWStartTime, longTodayendTime);
		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, longWStartTime, longTodayendTime);

		Map<Object, Object> dateWiseCount = getDateWiseCount(totalMsgDoc);

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, longWStartTime, longTodayendTime);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, longWStartTime, longTodayendTime);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}
		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		if (ArgUtil.is(dateWiseCount)) {
			dto.setGraphApiDetails(dateWiseCount);
		}
		dto.setContactType(contactType);
		dto.setFilter("WEEK");
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);

		return dto;

	}

	public DashBoardResponseDto monthAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();

		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		ZonedDateTime monthStartDate = ZonedDateTime.now().with(ChronoField.DAY_OF_MONTH, 1);

		long longTodayStartTime = todayDate.toInstant().toEpochMilli();
		long longTodayendTime = endToday.toInstant().toEpochMilli();
		long monthStartDateEpocTime = monthStartDate.toInstant().toEpochMilli();
		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, monthStartDateEpocTime, longTodayendTime);
		// System.out.println("distinctIdList :" + distinctIdList.size());

		Map<Object, Object> dateWiseCount = getDateWiseCount(totalMsgDoc);

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, monthStartDateEpocTime, longTodayendTime);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, monthStartDateEpocTime, longTodayendTime);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}

		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		if (ArgUtil.is(dateWiseCount)) {
			dto.setGraphApiDetails(dateWiseCount);
		}
		dto.setContactType(contactType);
		dto.setFilter("MONTH");
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);

		return dto;

	}

	public DashBoardResponseDto quaterAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();

		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		long quaterStratDateTime = getStartAndEndQuarter();
		long longTodayendTime = endToday.toInstant().toEpochMilli();
		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, quaterStratDateTime, longTodayendTime);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, quaterStratDateTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, quaterStratDateTime, longTodayendTime);
		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, quaterStratDateTime, longTodayendTime);
		// System.out.println("distinctIdList :" + distinctIdList.size());
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);

		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, quaterStratDateTime, longTodayendTime);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, quaterStratDateTime, longTodayendTime);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		Map<Object, Object> dweekWiseCount = getWeekWiseCount(totalMsgDoc);

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}

		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		if (ArgUtil.is(dweekWiseCount)) {
			dto.setGraphApiDetails(dweekWiseCount);
		}

		dto.setContactType(contactType);
		dto.setFilter("QUATER");
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);

		return dto;

	}

	public DashBoardResponseDto dateRange(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();
		long dateRange1 = requestDto.getDateRange1();
		long dateRange2 = requestDto.getDateReange2();

		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		long quaterStratDateTime = getStartAndEndQuarter();
		long longTodayendTime = endToday.toInstant().toEpochMilli();

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, dateRange1, dateRange2);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, dateRange1, dateRange2);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, dateRange1, dateRange2);
		// Get the distinct stuff from MongoDB
		List<String> distinctIdList = getUniqueConversation(contactType, dateRange1, dateRange1);

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(totalMsgDoc);
		/** lead Messanger **/
		LeadMessanger leadMsg = getLeadMessenger(contactType, dateRange1, dateRange2);

		/** Open conversation **/
		List<ChatSessionDoc> openConvesLst = getOpenConversation(contactType, dateRange1, dateRange2);
		if (ArgUtil.is(openConvesLst)) {
			dto.setOpenConversation(openConvesLst.size());
		}

		if (ArgUtil.is(totalInmsgDoc)) {
			dto.setTotalInMsgExchanged(totalInmsgDoc.size());
		}
		if (ArgUtil.is(totalOutmsgDoc)) {
			dto.setTotalOutMsgExchanged(totalOutmsgDoc.size());
		}

		if (ArgUtil.is(totalMsgDoc)) {
			dto.setTotalMsgExchanged(totalMsgDoc.size());
		}

		if (ArgUtil.is(distinctIdList)) {
			dto.setUniqueConversation(distinctIdList.size());
		}
		dto.setContactType(contactType);
		dto.setFilter("DATE_RANGE");
		dto.setPeakLoad(peakLoadResult);
		dto.setLeadMessanger(leadMsg);
		return dto;

	}

	public List<String> getListOfContactType() {
		List<String> listContactType = new ArrayList<String>();
		List<String> lstOfConRemo = new ArrayList<String>();
		lstOfConRemo.add("MESSAGE_LOGS");
		lstOfConRemo.add("MESSAGE_OTHERS");
		// )
		Set<String> contactTypeSet = mongoTemplate.getCollectionNames();
		if (ArgUtil.is(contactTypeSet)) {
			listContactType = contactTypeSet.stream().filter(x -> !x.isEmpty() && x.startsWith(COLLECTION_NAME))
					.collect(Collectors.toList());
		}
		if (!listContactType.isEmpty()) {
			listContactType.removeAll(lstOfConRemo);
		}
		return listContactType;
	}

	/** fetch lead mesenger **/
	public LeadMessanger getLeadMessenger(Object contactype, long startTime, long endTime) {
		long dateRange1 = 0;
		long dateRange2 = 0;
		if (ArgUtil.is(startTime)) {
			dateRange1 = agentAnaMgr.todayStartTime();
		}
		if (ArgUtil.is(endTime)) {
			dateRange2 = agentAnaMgr.todayEndTime();
		}

		LeadMessanger leadMessanger = new LeadMessanger();
		double percentageWithDecimal = 0.0;
		List<String> lst = getListOfContactType();
		Map<String, Integer> leasMsgLst = new HashMap<String, Integer>();
		for (String contactType : lst) {
			List<MessageDoc> msgDocLst = getTotalMsgCount(contactType, dateRange1, dateRange2);
			leasMsgLst.put(contactType, msgDocLst.size());
		}

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

	// To fetch all the records from a collection
	public List<MessageDoc> getTotalMsgCount(Object contactType, long dateRange1, long dateRange2) {

		Query queryAll = new Query();
		queryAll.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		queryAll.addCriteria(Criteria.where("type").in("I", "O"));
		queryAll.with(new Sort(new Order(Direction.ASC, "timestamp")));
		agentAnaMgr.removeMsgFields(queryAll);
		List<MessageDoc> totalMsgDoc = mongoTemplate.find(queryAll, MessageDoc.class, contactType.toString());
		return totalMsgDoc;
	}

	// To fetch In msg records from a collection
	public List<MessageDoc> getTotalInMsgCount(Object contactType, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("I"));
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		List<MessageDoc> totalInmsgDoc = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return totalInmsgDoc;
	}

	// To fetch Out msg the records from a collection
	public List<MessageDoc> getTotalOutMsgCount(Object contactType, long dateRange1, long dateRange2) {

		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("O"));
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		List<MessageDoc> totalInmsgDoc = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return totalInmsgDoc;
	}

	public long getStartAndEndQuarter() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int yearC = cal.get(Calendar.YEAR);
		int monthC = cal.get(Calendar.MONTH);
		Month month = Month.of(cal.get(Calendar.MONTH));
		LocalDate todayDt = LocalDate.now();
		LocalDate quaterStartDt = LocalDate.of(yearC, month.firstMonthOfQuarter(), 1);
		Month endMonth = quaterStartDt.getMonth().plus(2);
		LocalDate quaerEndDt = LocalDate.of(yearC, endMonth, endMonth.length(quaterStartDt.isLeapYear()));
		// long epocTime =start.

		// System.out.println("Today Date :" + todayDt + "\t quaterStartDt :" +
		// quaterStartDt + "\t quaerEndDt :" + quaerEndDt);
		long daysBetween = ChronoUnit.DAYS.between(quaterStartDt, todayDt);
		// System.out.println("No of days between quater startdate and today date :" +
		// daysBetween);

		ZonedDateTime startToday = ZonedDateTime.now().minusDays(daysBetween).with(LocalTime.MIN);

		long longStartTime = startToday.toInstant().toEpochMilli();
		// System.out.println("start Date of Quatr :" + startToday + "\t |" +
		// longStartTime);
		return longStartTime;
	}

	public PeakLoadDto getPeakLoadMsgCount(List<MessageDoc> totalMsgDoc) {
		PeakLoadDto peakLoadResult = new PeakLoadDto();
		List<Integer> hourList = new ArrayList<Integer>();
		List<String> dateWithTimeList = new ArrayList<String>();
		Map<Object, Integer> mapLst = new HashMap<Object, Integer>();
		for (MessageDoc msg : totalMsgDoc) {
			long timeStamp = msg.getTimestamp();
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
			// System.out.println("Peak Load Date Time and Value:"+maxEntryKey +"-
			// "+maxEntryKeyValue);
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm");
			try {
				Date d = df.parse(maxEntryKey.toString());
				long milliseconds = d.getTime();
				if (milliseconds != 0) {
					peakLoadResult.setEpochStamp(milliseconds);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			peakLoadResult.setTimestamp(maxEntryKey);
			peakLoadResult.setTotal(maxEntryKeyValue.longValue());

		}

		return peakLoadResult;
	}

	public PeakLoadDto getPeakLoadMsgCountOld(Object contactType, long dateRange1, long dateRange2) {
		Aggregation agg = newAggregation(match(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2)),
				group("timestamp").count().as("total"), project("total").and("timestamp").previousOperation(),
				sort(Sort.Direction.DESC, "total", "timestamp"));
		// Convert the aggregation result into a List
		AggregationResults<PeakLoadDto> groupResults = mongoTemplate.aggregate(agg, contactType.toString(),
				PeakLoadDto.class);
		PeakLoadDto peakLoadResult = null;
		if (groupResults != null && !groupResults.getMappedResults().isEmpty()) {
			peakLoadResult = groupResults.getMappedResults().get(0);
		}
		return peakLoadResult;
	}

	// To fetch unique conversation
	@SuppressWarnings("unchecked")
	public List<String> getUniqueConversation(Object contactType, long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		query.addCriteria(Criteria.where("type").in("O", "I"));

		List<String> distinctIdList = mongoTemplate.distinctValues(contactType.toString(), "contactId", String.class);
		return distinctIdList;
	}

	/** Timestamp **/

	public Map<Object, Object> getHourWiseCount(List<MessageDoc> msgLst) {
		// List<Long> hourList = new ArrayList<Long>();
		List<String> hourList = new ArrayList<String>();
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			Date date = new Date(timeStamp);
			String dateWithTime = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(date);
			String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			SimpleDateFormat sdfH = new SimpleDateFormat("hh aa");
			String formattedDateH = sdfH.format(date);
			dateWiseList.add(ddMMyyyyFormat);
			// LOGGER.info("getHourWiseCount :"+formattedDateH);
			/** 1 hr gap **/
			// long hourTimeSamp = (long) (timeStamp / (60 * 1000));
			// long hh = timeStamp / hourTimeSamp;
			// hourList.add(hh);
			// hourList.add(Long.parseLong(formattedDateH));// hour wise count
			hourList.add(formattedDateH);
		}
		Collections.sort(hourList);
		Set<Object> hourWiseCount = new HashSet<Object>(hourList);
		for (Object key : hourWiseCount) {
			mapLst.put(key, Collections.frequency(hourList, key));
			// System.out.println("House wise VAlue :"+key + ": " +
			// Collections.frequency(hourList, key));
		}

		return mapLst;
	}

	public Map<Object, Object> getHourWiseCountV1(List<MessageDoc> msgLst) {
		List<Long> hourList = new ArrayList<Long>();
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60)));
			hourList.add(timeStamp);// hour wise count
		}
		Collections.sort(hourList);
		Set<Object> hourWiseCount = new HashSet<Object>(hourList);
		for (Object key : hourWiseCount) {
			mapLst.put(key, Collections.frequency(hourList, key));
		}

		return mapLst;
	}

	/** date wise count **/
	public Map<Object, Object> getDateWiseCount(List<MessageDoc> msgLst) {
		List<Object> dateWiseList = new ArrayList<Object>();
		List<Long> dateWiseLongList = new ArrayList<Long>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			Date date = new Date(timeStamp);
			// String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			String ddMMyyyyFormat = new SimpleDateFormat("d").format(date);
			dateWiseList.add(ddMMyyyyFormat);
			dateWiseLongList.add(Long.parseLong(ddMMyyyyFormat));
		}
		Collections.sort(dateWiseLongList);
		// Datewise count

		Set<Object> dateWiseCount = new HashSet<Object>(dateWiseLongList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(dateWiseLongList, key));
			// System.out.println(key + ": " + Collections.frequency(dateWiseList, key));
		}

		return mapLst;
	}

	/** date wise count **/
	public Map<Object, Object> getTimeStampWiseCount(List<MessageDoc> msgLst) {
		List<Object> dateWiseTimeStampList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60 * 24)));
			dateWiseTimeStampList.add(timeStamp);
		}

		// Datewise count
		Set<Object> timeStampWiseCount = new HashSet<Object>(dateWiseTimeStampList);
		for (Object key : timeStampWiseCount) {
			mapLst.put(key, Collections.frequency(dateWiseTimeStampList, key));
		}

		return mapLst;
	}

	/** week wise count **/
	public Map<Object, Object> getWeekWiseCount(List<MessageDoc> msgLst) {
		List<Object> weekWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		Calendar cal = Calendar.getInstance();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			Date date = new Date(timeStamp);
			/*
			 * String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
			 * cal.setTime(date); String month = cal.getDisplayName(Calendar.MONTH,
			 * Calendar.LONG, Locale.getDefault()).toUpperCase(); int weekOfMonth =
			 * cal.get(Calendar.WEEK_OF_MONTH); // int weekOfYear =
			 * cal.get(Calendar.WEEK_OF_MONTH); String str = month + "  " + weekOfMonth;
			 * weekWiseList.add(str);
			 */
			String monthWise = new SimpleDateFormat("MMM").format(date);
			weekWiseList.add(monthWise);
		}

		// Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(weekWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(weekWiseList, key));
		}

		return mapLst;
	}

	/** week wise count **/
	public Map<Object, Object> getWeekWiseCountV1(List<MessageDoc> msgLst) {
		List<Object> weekWiseList = new ArrayList<Object>();
		Map<Object, Object> mapLst = new HashMap<Object, Object>();
		Calendar cal = Calendar.getInstance();
		for (MessageDoc msg : msgLst) {
			long timeStamp = msg.getTimestamp();
			timeStamp = (timeStamp - (timeStamp % (1000 * 60 * 60 * 24)));
			weekWiseList.add(timeStamp);
		}

		// Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(weekWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(weekWiseList, key));
			// System.out.println(key + ": " + Collections.frequency(weekWiseList, key));
		}

		return mapLst;
	}

	// Open conversation
	public List<ChatSessionDoc> getOpenConversation(Object contactType, long dateRange1, long dateRange2) {

		List<String> uniqueConvesationLst = getUniqueConversation(contactType, dateRange1, dateRange2);
		List<ChatSessionDoc> chatSessionLst = new ArrayList<ChatSessionDoc>();

		for (Object msgDoc : uniqueConvesationLst) {
			String strConId = (String) msgDoc;
			// System.out.println("Open Conversation :"+strConId);
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(strConId).and("active").is(true));
			List<ChatSessionDoc> chatSessionValue = mongoTemplate.find(query, ChatSessionDoc.class,
					AgentAnalyticsManager.CHAT_SESSION);
			chatSessionLst.addAll(chatSessionValue);
		}
		return chatSessionLst;
	}

	public TagDocumentDto getTagDocumentDetails(DashBoardRequestDto req) {
		long dateRange1 = 0;
		long dateRange2 = 0;

		if (ArgUtil.is(req.getDateRange1()) && req.getDateRange1() > 0) {
			dateRange1 = req.getDateRange1();
		} else {
			dateRange1 = agentAnaMgr.todayStartTime();
		}
		if (ArgUtil.is(req.getDateReange2()) && req.getDateReange2() > 0) {
			dateRange2 = req.getDateReange2();
		} else {
			dateRange2 = agentAnaMgr.todayEndTime();
		}
		TagDocumentDto responseDto = new TagDocumentDto();
		List<String> lst = getListOfContactType();
		List<TagDocumentLst> tagKeyValyeLst = new ArrayList<TagDocumentLst>();

		TagDocument allTagDocument = new TagDocument();
		allTagDocument.categories();
		allTagDocument.cities();
		allTagDocument.langs();
		allTagDocument.locations();
		allTagDocument.organizations();
		allTagDocument.persons();
		allTagDocument.sentiments();

		for (String contactType : lst) {
			List<MessageDoc> msgTagDocLst = getTagDocumentDetails(contactType, dateRange1, dateRange2);
			LOGGER.debug("Tagwise contactType :" + contactType);
			for (MessageDoc msgTagDoc : msgTagDocLst) {
				if (msgTagDoc != null && msgTagDoc.getTags() != null) {
					TagDocument tagDocument = msgTagDoc.getTags();

					// Aggregate all TagsType wise by meergin this tagDocument to allTagDocument
					allTagDocument.categories().addAll(tagDocument.categories());
					allTagDocument.cities().addAll(tagDocument.cities());
					allTagDocument.langs().addAll(tagDocument.langs());
					allTagDocument.locations().addAll(tagDocument.locations());
					allTagDocument.organizations().addAll(tagDocument.organizations());
					allTagDocument.persons().addAll(tagDocument.persons());
					allTagDocument.sentiments().addAll(tagDocument.sentiments());
				}

			}
		}

		// Caculate for each TagType and append to master list
		appendTagCount("categories", allTagDocument.categories(), tagKeyValyeLst);
		appendTagCount("cities", allTagDocument.cities(), tagKeyValyeLst);
		appendTagCount("langs", allTagDocument.langs(), tagKeyValyeLst);
		appendTagCount("locations", allTagDocument.locations(), tagKeyValyeLst);
		appendTagCount("organizations", allTagDocument.organizations(), tagKeyValyeLst);
		appendTagCount("persons", allTagDocument.persons(), tagKeyValyeLst);
		appendTagCount("sentiments", allTagDocument.sentiments(), tagKeyValyeLst);

		responseDto.setLstTagDocument(tagKeyValyeLst);

		return responseDto;
	}

	private List<TagDocumentLst> appendTagCount(String tagType, List<String> tagValues,
			List<TagDocumentLst> tagKeyValyeLst) {
		Set<String> tagWiseCount = new HashSet<String>(tagValues);
		for (String tag : tagWiseCount) {
			TagDocumentLst tagKeyValye = new TagDocumentLst();
			tagKeyValye.setType(tagType);
			tagKeyValye.setTag(tag);
			tagKeyValye.setCount(Collections.frequency(tagValues, tag));
			tagKeyValyeLst.add(tagKeyValye);
		}
		return tagKeyValyeLst;
	}

	private void getTagCount(List<Object> toalTagLst, List<TagDocumentLst> tagKeyValyeLst,
			Map<Object, Object> mapTagLst) {
		Set<Object> tagWiseCount = new HashSet<Object>(toalTagLst);
		for (Object key : tagWiseCount) {
			mapTagLst.put(key, Collections.frequency(toalTagLst, key));
			TagDocumentLst tagKeyValye = new TagDocumentLst();
			tagKeyValye.setTag(key.toString());
			tagKeyValye.setCount(Collections.frequency(toalTagLst, key));
			tagKeyValyeLst.add(tagKeyValye);
			// LOGGER.info("{ +++++++++ }"+key + ": " + Collections.frequency(toalTagLst,
			// key));
		}
	}

	public List<MessageDoc> getTagDocumentDetails(Object contactType, long dateRange1, long dateRange2) {
		// List<MessageDoc> msgDocLst =null;
		Query query = new Query();
		// query.addCriteria(Criteria.where("tag").exists(true));
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return msgDocLst;
	}

	public static List<Object> showFieldsUsingBean(Object obj) {
		List<Object> lstTagStr = new ArrayList<Object>();
		// Getting the PropertyDescriptors for the object
		PropertyDescriptor[] objDescriptors = PropertyUtils.getPropertyDescriptors(obj);

		// Iterating through each of the PropertyDescriptors
		for (PropertyDescriptor objDescriptor : objDescriptors) {
			try {

				String propertyName = objDescriptor.getName();
				Object propType = PropertyUtils.getPropertyType(obj, propertyName);
				Object propValue = PropertyUtils.getProperty(obj, propertyName);
				if (propValue != null) {
					List<?> objLst = convertObjectToList(propValue);
					lstTagStr.addAll(objLst);

				}
				// Printing the details
				// LOGGER.info(" ========================= {====}Property="+propertyName+",
				// Type="+propType+", Value="+propValue);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return lstTagStr;
	}

	public static List<?> convertObjectToList(Object obj) {
		List<?> list = new ArrayList<>();
		if (obj != null) {
			if (obj.getClass().isArray()) {
				list = Arrays.asList((Object[]) obj);
			} else if (obj instanceof Collection) {
				list = new ArrayList<>((Collection<?>) obj);
			} else if (obj instanceof Integer[]) {
				list = Arrays.asList((Integer[]) obj);
			}
		}
		return list;
	}

	/** Hour wise and day wise summary count **/

	public ContactTypeSummaryDto hourWisesummary() {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		List<String> channelLst = getListChannelCongig();
		long currentTs = System.currentTimeMillis();
		long hour = 0;
		long hr = 12;
		if (hr > 0) {
			hour = hr * 60 * 60 * 1000;
		} else {
			hour = 12 * 60 * 60 * 1000;
		}
		long lasthrTimeStmp = currentTs - hour;
		Calendar cal = Calendar.getInstance();
		Date dateTi = new Date();
		cal.setTimeInMillis(currentTs);
		dateTi = new Date(currentTs);

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(dateTi);
		List<SummaryDocDto> lstSummDto = new ArrayList<>();
		List<String> hourListH = new ArrayList<String>();
		List<DateWiseHourCountDto> hourCntLst = new ArrayList<>();
		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(lasthrTimeStmp).lt(currentTs));
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp").include("type").include("meta");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto hrDto = new DateWiseHourCountDto();
				long timeStamp = doc.getTimestamp();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setDomain(tnt);
				String id = getSummaryWithChannelId(dto);
				dto.setId(id);
				Date date = new Date(timeStamp);
				SimpleDateFormat sdfH = new SimpleDateFormat("kk");
				String formattedDateH = sdfH.format(date);
				hourListH.add(formattedDateH);
				hrDto.setDate(yyyyMMdd);
				hrDto.setHour(formattedDateH);
				hrDto.setChannel(id);
				if (hrDto != null) {
					hourCntLst.add(hrDto);
				}
			}

		}
		Map<Object, Long> summaryMap = new HashMap<>();
		Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));
		/** hour wise count **/
		Map<String, Map<String, Long>> hourWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel,
						Collectors.groupingBy(DateWiseHourCountDto::getHour, Collectors.counting())));

		Map<String, Map<String, Long>> hourWiseCount = new HashMap<>();

		Map<String, Long> hourCntMap = getHourRange(currentTs, lasthrTimeStmp);

		for (Map.Entry<String, Map<String, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<String, Long> hoCntMapAll = new HashMap<>();
			String key = keyValue.getKey();
			if (ArgUtil.is(key)) {

				for (String channel : channelLst) {
					if (!key.contains(channel)) {
						hourWiseCount.put(tnt + "_" + channel, hourCntMap);
					}
				}
				Map<String, Long> hoCntMap = hourWiseCountMap.get(key);

				for (Map.Entry<String, Long> keyValueCount : hourCntMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					if (ArgUtil.is(keydt)) {
						Long count = keyValueCount.getValue();
						if (hoCntMap.containsKey(keydt)) {
							hoCntMapAll.put(keydt, hoCntMap.get(keydt));
						} else {
							hoCntMapAll.put(keydt, count);
						}
					}
				}

				hourWiseCount.put(key, hoCntMapAll);
			}
		}

		summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting()));

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setHourWiseCountMap(hourWiseCount);
		return dto;
	}

	/** day and channel wise summary **/
	public ContactTypeSummaryDto dayChannelWiseWisesummary() {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		List<String> channelLst = getListChannelCongig();
		long currentTs = System.currentTimeMillis();
		ZonedDateTime noOfdaysTstamp = null;
		int days = 12;

		if (days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
		} else {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(12).with(LocalTime.MIN);
		}
		// use the same datetime to create the end of the day using the maximum time for
		long lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();

		Map<String, Long> dateRanMap = getDatesRange(currentTs, lasDayTimeStmp);

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(currentTs);
		List<SummaryDocDto> lstSummDto = new ArrayList<>();
		List<DateWiseHourCountDto> hourCntLst = new ArrayList<>();

		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(lasDayTimeStmp).lt(currentTs));
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp").include("type").include("meta");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto daySummDto = new DateWiseHourCountDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setDomain(tnt);
				String id = getSummaryWithChannelId(dto);
				dto.setId(id);
				if (ArgUtil.is(dto.getId())) {
					lstSummDto.add(dto);
				}
				String channelid = getSummaryWithChannelId(dto);
				daySummDto.setDate(yyyyMMdd);
				daySummDto.setChannel(channelid);
				if (daySummDto != null) {
					hourCntLst.add(daySummDto);
				}

			}

		}
		Map<Object, Long> summaryMap = new HashMap<>();
		Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));

		/** day wise count **/
		Map<String, Map<String, Long>> dayWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel,
						Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.counting())));

		Map<String, Map<String, Long>> dayWiseMap = new HashMap<>();

		for (Map.Entry<String, Map<String, Long>> keyValue : dayWiseCountMap.entrySet()) {
			String key = keyValue.getKey();
			if (ArgUtil.is(key)) {
				for (String channel : channelLst) {
					if (!key.contains(channel)) {
						dayWiseMap.put(tnt + "_" + channel, dateRanMap);
					}
				}

				Map<String, Long> dateWiseCnt = new HashMap<>();
				Map<String, Long> dayCntMap = dayWiseCountMap.get(key);
				// dateRanMap
				for (Map.Entry<String, Long> keyValueCount : dateRanMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					if (ArgUtil.is(keydt)) {
						Long count = keyValueCount.getValue();
						if (dayCntMap.containsKey(keydt)) {
							dateWiseCnt.put(keydt, dayCntMap.get(keydt));
						} else {
							dateWiseCnt.put(keydt, count);
						}
					}
				}
				dayWiseMap.put(key, dateWiseCnt);
			}
		}
		summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting()));

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setDateWiseSummaryCount(dayWiseMap);
		return dto;
	}

	/** Read ,Unread,sent,deliver msg count Hour Wise */
	@SuppressWarnings("unused")
	public ContactTypeSummaryDto getHourWiseMsgStatusSummary(long timestamp, long hr) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		List<DateWiseHourCountDto> hourCntLst = new ArrayList<>();
		long currentTs = System.currentTimeMillis();
		long hour = 0;
		if (hr > 0) {
			hour = hr * 60 * 60 * 1000;
		} else {
			hour = 12 * 60 * 60 * 1000;
		}
		long lasthrTimeStmp = currentTs - hour;

		Date dateTi = new Date(currentTs);
		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(dateTi);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		long monthMinTimeStamp = DateUtil.getStartTimestamp(month, year).getTime();
		long monthMaxTimeStamp = DateUtil.getEndTimestamp(month, year).getTime();
		Map<Object, Long> summaryMap = new HashMap<>();
		List<ContactTypeCountDto> summaryMsgLstCount = new ArrayList<ContactTypeCountDto>();

		Map<Object, List<ContactTypeCountDto>> map = new HashMap<>();
		List<Map<String, Object>> lstMap = new ArrayList<>();
		for (String contactType : lst) {
			List<ContactTypeCountDto> messageTypeLst = new ArrayList<ContactTypeCountDto>();
			List<Document> list = new ArrayList<Document>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(lasthrTimeStmp).lt(currentTs))
					.toDocument(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.match(new Criteria("bulkSessionId").exists(true))
					.toDocument(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("stamps").count().as("count").toDocument(Aggregation.DEFAULT_CONTEXT));

			MongoCursor<Document> cursor = mongoTemplate.collection(contactType).aggregate(list).iterator();
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				Document object = cursor.next();
				if (ArgUtil.is(object)) {
					String type = ArgUtil.parseAsString(object.get("_id"));
					if (ArgUtil.is(type)) {
						Map<String, Object> mapValue = JsonUtil.fromJsonToMap(type);
						long count = ArgUtil.parseAsLong(object.get("count"), 0L);
						contactDto.setType(type);
						contactDto.setTotalCount(count);
						lstMap.add(mapValue);
					}
				}
				messageTypeLst.add(contactDto);
			}
		}

		for (Map<String, Object> mapv : lstMap) {
			for (Map.Entry<String, Object> keyValueCount : mapv.entrySet()) {
				DateWiseHourCountDto daySummDto = new DateWiseHourCountDto();
				String key = keyValueCount.getKey();
				if (ArgUtil.is(key) && !key.equalsIgnoreCase("session")) {
					String value = getHour((Long) keyValueCount.getValue());
					daySummDto.setMsgType(key);
					daySummDto.setHour(value);
					hourCntLst.add(daySummDto);
				}
			}

		}

		/** Hour wise couunt **/
		Map<String, Map<String, Long>> hourWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getMsgType,
						Collectors.groupingBy(DateWiseHourCountDto::getHour, Collectors.counting())));
		/** default hour **/
		Map<String, Long> hourCntMap = getHourRange(currentTs, lasthrTimeStmp);

		Map<String, Map<String, Long>> hourWiseCount = addDefaultHour(hourWiseCountMap, hourCntMap);

		hourWiseCount = fetchAndAddAllMsgStatus(hourWiseCount, hourCntMap);

		dto.setTenant(tnt);
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(lasthrTimeStmp);
		dto.setMonthMaxTimeStamp(currentTs);
		dto.setHourWiseCountMap(hourWiseCount);

		return dto;

	}

	@SuppressWarnings("unused")
	public ContactTypeSummaryDto getDayWiseMsgStatusSummary(long timestamp, int days) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();

		List<DateWiseHourCountDto> dayCntLst = new ArrayList<>();

		long currentTs = System.currentTimeMillis();
		ZonedDateTime noOfdaysTstamp = null;

		if (days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
		} else {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(12).with(LocalTime.MIN);
		}
		// use the same datetime to create the end of the day using the maximum time for
		long lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(currentTs);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		long monthMinTimeStamp = DateUtil.getStartTimestamp(month, year).getTime();
		long monthMaxTimeStamp = DateUtil.getEndTimestamp(month, year).getTime();
		Map<Object, Long> summaryMap = new HashMap<>();
		List<ContactTypeCountDto> summaryMsgLstCount = new ArrayList<ContactTypeCountDto>();

		Map<Object, List<ContactTypeCountDto>> map = new HashMap<>();
		List<Map<String, Object>> lstMap = new ArrayList<>();
		for (String contactType : lst) {
			List<ContactTypeCountDto> messageTypeLst = new ArrayList<ContactTypeCountDto>();
			List<Document> list = new ArrayList<Document>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(lasDayTimeStmp).lt(currentTs))
					.toDocument(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("stamps").count().as("count").toDocument(Aggregation.DEFAULT_CONTEXT));

			MongoCursor<Document> cursor = mongoTemplate.collection(contactType).aggregate(list).iterator();
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				Document object = cursor.next();
				if (ArgUtil.is(object)) {
					String type = ArgUtil.parseAsString(object.get("_id"));
					if (ArgUtil.is(type)) {
						Map<String, Object> mapValue = JsonUtil.fromJsonToMap(type);
						long count = ArgUtil.parseAsLong(object.get("count"), 0L);
						contactDto.setType(type);
						contactDto.setTotalCount(count);
						lstMap.add(mapValue);
					}
				}
				messageTypeLst.add(contactDto);
			}
		}
		// for (Map.Entry<String, Long> keyValueCount : dateRanMap.entrySet()) {
		for (Map<String, Object> mapv : lstMap) {
			for (Map.Entry<String, Object> keyValueCount : mapv.entrySet()) {
				DateWiseHourCountDto daySummDto = new DateWiseHourCountDto();
				String key = keyValueCount.getKey();
				if (ArgUtil.is(key) && !key.equalsIgnoreCase("session")) {
					String yyyyMMdd = DateUtil.foramtTimeStampDateAsString((Long) keyValueCount.getValue(),
							DateUtil.YYYYMMDD_DATE_FORMAT);
					daySummDto.setMsgType(key);
					daySummDto.setDate(yyyyMMdd);
					dayCntLst.add(daySummDto);
				}
			}

		}
		/** Hour wise couunt **/
		Map<String, Map<String, Long>> dateWiseCountMap = dayCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getMsgType,
						Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.counting())));
		/** default hour **/
		Map<String, Long> dateRanMap = getDatesRange(currentTs, lasDayTimeStmp);

		Map<String, Map<String, Long>> dateWiseSummary = addDefaultHour(dateWiseCountMap, dateRanMap);

		dateWiseSummary = fetchAndAddAllMsgStatus(dateWiseSummary, dateRanMap);

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(lasDayTimeStmp);
		dto.setMonthMaxTimeStamp(currentTs);
		dto.setDateWiseSummaryCount(dateWiseSummary);

		return dto;

	}

	/** add default hour **/

	public Map<String, Map<String, Long>> addDefaultHour(Map<String, Map<String, Long>> hourWiseCountMap,
			Map<String, Long> hourCntMap) {
		Map<String, Map<String, Long>> countSummary = new HashMap<>();
		Map<String, Long> defaultMap = null;

		for (Map.Entry<String, Map<String, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<String, Long> hoCntMapAll = new HashMap<>();
			String key = keyValue.getKey();
			if (ArgUtil.is(key)) {
				defaultMap = hourWiseCountMap.get(key);

				for (Map.Entry<String, Long> keyValueCount : hourCntMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					if (ArgUtil.is(keydt)) {
						Long count = keyValueCount.getValue();
						if (defaultMap.containsKey(keydt)) {
							hoCntMapAll.put(keydt, defaultMap.get(keydt));
						} else {
							hoCntMapAll.put(keydt, count);
						}
					}
				}

				countSummary.put(key, hoCntMapAll);
			}
		}

		return countSummary;
	}

	/** add default msg status **/
	public Map<String, Map<String, Long>> fetchAndAddAllMsgStatus(Map<String, Map<String, Long>> hourWiseCount,
			Map<String, Long> hourCntMap) {

		Message.Status[] msgSta = Message.Status.values();

		for (Message.Status stsobj : msgSta) {
			String sts = ArgUtil.parseAsString(stsobj);
			if (ArgUtil.is(sts)) {
				if (sts != null && (hourWiseCount == null || hourWiseCount.isEmpty())) {
					hourWiseCount.put(sts.toString(), hourCntMap);
				} else if (sts != null && hourWiseCount != null && !hourWiseCount.containsKey(sts)) {
					hourWiseCount.put(sts.toString(), hourCntMap);
				}
			}
		}
		return hourWiseCount;
	}

	public String getHour(long timeStamp) {
		Date date = new Date(timeStamp);
		/** kk-24 hr , HH-24 hr **/
		SimpleDateFormat sdfH = new SimpleDateFormat("kk");
		String formattedDateH = sdfH.format(date);
		return formattedDateH;
	}

	public Map<String, Long> getDatesRange(long curTiStmp, long lasDayTiStmp) {
		Map<String, Long> mapDt = new HashMap<>();
		for (long lasDayTiSt = lasDayTiStmp; lasDayTiSt <= curTiStmp; lasDayTiSt += DateUtil.ONEDAY) {
			String ds = DateUtil.foramtTimeStampDateAsString(lasDayTiSt, DateUtil.YYYYMMDD_DATE_FORMAT);
			mapDt.put(ds, new Long(0));
		}
		// Sorting Map
		Map<String, Long> result = mapDt.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		return mapDt;
	}

	public Map<String, Long> getHourRange(long currentTStamp, long lastTimeStamp) {
		Map<String, Long> mapHr = new HashMap<>();
		String curHr = getHour(currentTStamp);
		String lastHr = getHour(lastTimeStamp);

		int currHrInt = Integer.parseInt(curHr);
		int lastHrInt = Integer.parseInt(lastHr);
		if (currHrInt < 12) {
			currHrInt = currHrInt + 24;
		}
		for (int i = lastHrInt; i <= currHrInt; i++) {
			int k = i;
			if (i > 24) {
				k = i - 24;
			}
			String keyS = String.valueOf(k);
			if (keyS.length() == 1) {
				keyS = "0" + keyS;
			}
			mapHr.put(keyS, new Long(0));

		}
		Map<String, Long> result = mapHr.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
				.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

		return result;
	}

	public List<String> getListChannelCongig() {
		List<String> listOfChannelConfig = new ArrayList<String>();

		Query query = new Query();
		query.addCriteria(Criteria.where("isDisabled").is(false));
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		for (ChannelConfigDoc cofigDoc : cofigDocLst) {
			listOfChannelConfig.add(cofigDoc.getChannelType());
		}

		listOfChannelConfig = new ArrayList<>(new HashSet<>(listOfChannelConfig));

		return listOfChannelConfig;
	}

	public String getSummaryWithChannelId(SummaryDocDto dto) {
		String tenant = dto.getDomain();
		if (dto.getChannel().contains(ContactType.WHATSAPP.name())) {
			return tenant + "_" + "wa";
		} else if (dto.getChannel().contains(ContactType.FACEBOOK.name())) {
			return tenant + "_" + "fb";
		} else if (dto.getChannel().contains(ContactType.TWITTER.name())) {
			return tenant + "_" + "tw";
		} else if (dto.getChannel().contains(ContactType.TELEGRAM.name())) {
			return tenant + "_" + "tg";
		} else if (dto.getChannel().contains(ContactType.INSTAGRAM.name())) {
			return tenant + "_" + "ig";
		} else if (dto.getChannel().contains(ContactType.WEBSITE.name())) {
			return tenant + "_" + "web";
		}
		return null;
	}

}
