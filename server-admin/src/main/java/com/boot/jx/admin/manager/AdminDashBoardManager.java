package com.boot.jx.admin.manager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.DashBoardRequestDto;
import com.boot.jx.admin.dto.DashBoardResponseDto;
import com.boot.jx.admin.dto.PeakLoadDto;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.utils.ArgUtil;


//imports as static
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import org.springframework.data.domain.Sort;



@Component
public class AdminDashBoardManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminDashBoardManager.class);

	private static final String COLLECTION = "MessageDoc.class";

	public static final String COLLECTION_NAME = "MESSAGE";
	@Autowired
	MongoTemplate mongoTemplate;

	private String getCollectionName(Object contactType) {
		return (MessageDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(contactType, "OTHERS"));
	}

	public List<MessageDoc> testDashBoard() {
		System.out.println("Collection Exists? " + mongoTemplate.collectionExists("MESSAGE_TWITTER"));
		System.out.println("Collection Exists? " + mongoTemplate.collectionExists(COLLECTION));
		
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("I"));
		List<MessageDoc> msgDoc = mongoTemplate.find(query, MessageDoc.class, "MESSAGE_TWITTER");
		System.out.println("Total Out Msg :" + msgDoc.size());

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
			System.out.println("Array List Stream :" + s);
		}

		return msgDoc;
	}

	public List<DashBoardResponseDto> getDashBoardAnalytics(DashBoardRequestDto requestDto) {
		List<DashBoardResponseDto> dtoLst = new ArrayList<DashBoardResponseDto>();

		long epochTime = System.currentTimeMillis();
		System.out.println("epochTime :" + epochTime);

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
		System.out.println("epochTime :" + epochTime);
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
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());

		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,longTodayStartTime,longTodayendTime);
		
		
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
		dto.setContactType(contactType);
		dto.setFilter("TODAY");
		dto.setPeakLoad(peakLoadResult);

		return dto;

	}

	public DashBoardResponseDto yesterdayAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();

		long epochTime = System.currentTimeMillis();
		System.out.println("epochTime :" + epochTime);
		Object contactType=requestDto.getContactType();
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
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());
		
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,longTodayStartTime,longTodayendTime);
		

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
		dto.setContactType(contactType);
		dto.setFilter("YESTERDAY");
		dto.setPeakLoad(peakLoadResult);

		return dto;

	}

	public DashBoardResponseDto weekAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();
		long epochTime = System.currentTimeMillis();
		System.out.println("epochTime :" + epochTime);

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
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());
		
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,longWStartTime,longTodayendTime);

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
		dto.setFilter("WEEK");
		dto.setPeakLoad(peakLoadResult);

		return dto;

	}

	public DashBoardResponseDto monthAnalystics(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();

		// get a datetime plus time zone information using the system time zone
		// subtract a day
		// and take the minimum time a day can have
		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		ZonedDateTime monthStartDate = ZonedDateTime.now().with(ChronoField.DAY_OF_MONTH, 1);

		long longTodayStartTime = todayDate.toInstant().toEpochMilli();
		long longTodayendTime = endToday.toInstant().toEpochMilli();
		long monthStartDateEpocTime = monthStartDate.toInstant().toEpochMilli();

		System.out.println("today date end:\t" + endToday + "\t| " + longTodayendTime);
		System.out.println("This monthStartDate :\t " + monthStartDate + "\t | " + monthStartDateEpocTime);

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, monthStartDateEpocTime, longTodayendTime);

		// Get the distinct stuff from MongoDB
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());
		
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,monthStartDateEpocTime,longTodayendTime);

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
		dto.setContactType(contactType);
		dto.setFilter("MONTH");
		dto.setPeakLoad(peakLoadResult);

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

		System.out.println("This quaterMonthStartDate :\t " + quaterStratDateTime + "\t | " + longTodayendTime);

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, quaterStratDateTime, longTodayendTime);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, quaterStratDateTime, longTodayendTime);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, quaterStratDateTime, longTodayendTime); 
		// Get the distinct stuff from MongoDB
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,quaterStratDateTime,longTodayendTime);

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
		dto.setContactType(contactType);
		dto.setFilter("QUATER");
		dto.setPeakLoad(peakLoadResult);

		return dto;

	}
	
	
	
	public DashBoardResponseDto dateRange(DashBoardRequestDto requestDto) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		Object contactType = requestDto.getContactType();
		long dateRange1 =requestDto.getDateRange1();
		long dateRange2 =requestDto.getDateReange2(); 		

		ZonedDateTime todayDate = ZonedDateTime.now().minusDays(0).with(LocalTime.MIN);
		// use the same datetime to create the end of the day using the maximum time for
		// a day
		ZonedDateTime endToday = todayDate.with(LocalTime.MAX);

		long quaterStratDateTime = getStartAndEndQuarter();
		long longTodayendTime = endToday.toInstant().toEpochMilli();

		System.out.println("This quaterMonthStartDate :\t " + quaterStratDateTime + "\t | " + longTodayendTime);

		List<MessageDoc> totalInmsgDoc = getTotalInMsgCount(contactType, dateRange1, dateRange2);

		List<MessageDoc> totalOutmsgDoc = getTotalOutMsgCount(contactType, dateRange1, dateRange2);

		// To fetch all the records for a collection
		List<MessageDoc> totalMsgDoc = getTotalMsgCount(contactType, dateRange1, dateRange2); 
		// Get the distinct stuff from MongoDB
		List<MessageDoc> distinctIdList = mongoTemplate.getCollection(contactType.toString()).distinct("contactId");
		System.out.println("distinctIdList :" + distinctIdList.size());
		
		PeakLoadDto peakLoadResult = getPeakLoadMsgCount(contactType,dateRange1,dateRange2);

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
		dto.setContactType(contactType);
		dto.setFilter("DATE_RANGE");
		dto.setPeakLoad(peakLoadResult);
		return dto;

	}

	public List<String> getListOfContactType() {
		List<String> listContactType = new ArrayList<String>();
		Set<String> contactTypeSet = mongoTemplate.getCollectionNames();
		if (ArgUtil.is(contactTypeSet)) {
			listContactType = contactTypeSet.stream().filter(x -> !x.isEmpty() && x.startsWith(COLLECTION_NAME))
					.collect(Collectors.toList());
		}
		return listContactType;
	}

	// To fetch all the records from a collection
	public List<MessageDoc> getTotalMsgCount(Object contactType, long startTime, long endTime) {
		Query queryAll = new Query();
		queryAll.addCriteria(Criteria.where("timestamp").gt(startTime).lt(endTime));
		List<MessageDoc> totalMsgDoc = mongoTemplate.find(queryAll, MessageDoc.class, contactType.toString());
		return totalMsgDoc;
	}

	// To fetch In msg records from a collection
	public List<MessageDoc> getTotalInMsgCount(Object contactType, long startTime, long endTime) {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("I"));
		query.addCriteria(Criteria.where("timestamp").gt(startTime).lt(endTime));
		List<MessageDoc> totalInmsgDoc = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return totalInmsgDoc;
	}

	// To fetch Out msg the records from a collection
	public List<MessageDoc> getTotalOutMsgCount(Object contactType, long startTime, long endTime) {
		Query query = new Query();
		query.addCriteria(Criteria.where("type").is("O"));
		query.addCriteria(Criteria.where("timestamp").gt(startTime).lt(endTime));
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

		System.out.println(
				"Today Date :" + todayDt + "\t quaterStartDt :" + quaterStartDt + "\t quaerEndDt :" + quaerEndDt);
		long daysBetween = ChronoUnit.DAYS.between(quaterStartDt, todayDt);
		System.out.println("No of days  between quater startdate and today date  :" + daysBetween);

		ZonedDateTime startToday = ZonedDateTime.now().minusDays(daysBetween).with(LocalTime.MIN);

		long longStartTime = startToday.toInstant().toEpochMilli();
		System.out.println("start Date of Quatr :" + startToday + "\t |" + longStartTime);
		return longStartTime;
	}
	
	public PeakLoadDto getPeakLoadMsgCount(Object contactType, long startTime, long endTime) {
	 Aggregation agg = newAggregation(
			    match(Criteria.where("timestamp").gt(startTime).lt(endTime)),
	            group("timestamp").count().as("total"),
	            project("total").and("timestamp").previousOperation(),
	            sort(Sort.Direction.DESC, "total","timestamp")
	        );
	 	//Convert the aggregation result into a List
		 AggregationResults<PeakLoadDto> groupResults = mongoTemplate.aggregate(agg, contactType.toString(), PeakLoadDto.class);
		 PeakLoadDto peakLoadResult =null;
		 if(groupResults!=null && !groupResults.getMappedResults().isEmpty()) {
		  peakLoadResult = groupResults.getMappedResults().get(0);
		 }
		 return peakLoadResult;
	}
}