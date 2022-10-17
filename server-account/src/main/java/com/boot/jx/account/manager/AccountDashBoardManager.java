package com.boot.jx.account.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.boot.jx.AppContextUtil;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.doc.DomainSummaryMessageDoc;
import com.boot.jx.account.doc.DomainSummaryMetaDoc;
import com.boot.jx.account.doc.DomainSummaryMetaStore;
import com.boot.jx.account.dto.AccountDashBoardRequestDto;
import com.boot.jx.account.dto.AccountDashBoardResponseDto;
import com.boot.jx.account.dto.TypeCount;
import com.boot.jx.account.dto.WabaSummaryDocDto;
import com.boot.jx.dict.ContactType;
import com.boot.jx.account.dto.ContactTypeCountDto;
import com.boot.jx.account.dto.ContactTypeSummaryDto;
import com.boot.jx.account.dto.DateWiseHourCountDto;
import com.boot.jx.account.dto.MonthDtlsDto;
import com.boot.jx.account.dto.SummaryDocDto;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.tpo.WABAConversation;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.DateUtil;
import com.boot.utils.JsonUtil;
import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.AggregationOptions.OutputMode;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.Message;

@Component
public class AccountDashBoardManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDashBoardManager.class);
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private DomainSummaryMetaStore domSumMetaStore;

	public List<DomainDoc> getAllDomainAccount() {
		Query query = new Query();
		query.with(new Sort(new Order(Direction.DESC, "_id")));
		List<DomainDoc> domainDocLst = mongoTemplate.find(query, DomainDoc.class);
		return domainDocLst;
	}

	@SuppressWarnings("unchecked")
	public List<MonthDtlsDto> fetchUniqueMonth() {

		Map<Long, Object> map = new HashMap<Long, Object>();
		Query query = new Query();
		query.with(new Sort(new Order(Direction.DESC, "startSessionStamp")));
		query.fields().include("startSessionStamp");
		List<Long> msgDocLst = mongoTemplate.getCollection("CHAT_SESSION").distinct("startSessionStamp",
				query.getQueryObject());
		List<MonthDtlsDto> listofMonth = new ArrayList<>();
		for (Long docTimeStamp : msgDocLst) {
			long timestamp = (docTimeStamp - (docTimeStamp % (DateUtil.ONEDAY)));
			String monthStr = DateUtil.foramtTimeStampDateAsString(timestamp, null);
			if (!map.containsValue(monthStr)) {
				map.put(timestamp, monthStr);
			}
		}
		ArrayList<Long> sortedKeys = new ArrayList<Long>(map.keySet());
		Collections.sort(sortedKeys, Collections.reverseOrder());

		// Display the TreeMap which is naturally sorted
		for (Long x : sortedKeys) {
			MonthDtlsDto dto = new MonthDtlsDto();
			dto.setTimestamp(x);
			dto.setMonthStr(map.get(x).toString());
			listofMonth.add(dto);
		}

		return listofMonth;
	}

	public ContactTypeSummaryDto getMonthWiseCount(long timestamp) {
		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		List<String> lst = getListOfContactType();
		Date dateTi = new Date(timestamp);
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
		for (String contactType : lst) {
			List<ContactTypeCountDto> messageTypeLst = new ArrayList<ContactTypeCountDto>();
			List<DBObject> list = new ArrayList<DBObject>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(monthMinTimeStamp).lt(monthMaxTimeStamp))
					.toDBObject(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("type").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));

			DBCollection col = mongoTemplate.getCollection(contactType);
			Cursor cursor = col.aggregate(list,
					AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				DBObject object = cursor.next();
				if (ArgUtil.is(object)) {
					String type = ArgUtil.parseAsString(object.get("_id"));
					long count = ArgUtil.parseAsLong(object.get("count"), 0L);
					contactDto.setType(type);
					contactDto.setTotalCount(count);
				}
				messageTypeLst.add(contactDto);
			}
			summaryMsgLstCount.addAll(messageTypeLst);
			map.put(contactType, messageTypeLst);
		}
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(monthMinTimeStamp);
		dto.setMonthMaxTimeStamp(monthMaxTimeStamp);
		summaryMap = summaryMsgLstCount.stream().collect(Collectors.groupingBy(ContactTypeCountDto::getType,
				Collectors.summingLong(ContactTypeCountDto::getTotalCount)));

		dto.setSummaryCount(summaryMap);

		return dto;

	}

	public void getMonths() {
		List<String> lstContactType = getListOfContactType();
		for (String contactType : lstContactType) {

		}
	}

	public AccountDashBoardResponseDto getAccountDashBoardDetails(AccountDashBoardRequestDto req) {
		AccountDashBoardResponseDto response = new AccountDashBoardResponseDto();
		long dateRange1 = 0;
		long dateRange2 = 0;
		if (ArgUtil.is(req.getDateRange1())) {
			dateRange1 = req.getDateRange1();
		} else {
			dateRange1 = DateUtil.todayStartTime();
		}
		if (ArgUtil.is(req.getDateRange2())) {
			dateRange2 = req.getDateRange1();
		} else {
			dateRange2 = DateUtil.todayStartTime();
		}

		List<String> lstContactType = getListOfContactType();
		long totalInMsg = 0;
		long totalOutMsg = 0;

		for (String contactType : lstContactType) {
			getTypeWiseCount(contactType, dateRange1, dateRange2);
		}
		response.setTotalInMsgExchanged(totalInMsg);
		response.setTotalOutMsgExchanged(totalOutMsg);
		response.setTotalMsgExchanged(totalInMsg + totalOutMsg);

		return response;
	}

	public TypeCount getTypeWiseCount(Object contactType, long dateRange1, long dateRange2) {
		Aggregation agg = newAggregation(match(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2)),
				group("type").count().as("count"), project("count").and("type").previousOperation(),
				sort(Sort.Direction.DESC, "count", "timestamp"));
		// Convert the aggregation result into a List
		AggregationResults<TypeCount> groupResults = mongoTemplate.aggregate(agg, contactType.toString(),
				TypeCount.class);
		TypeCount typeCountList = null;
		if (groupResults != null && !groupResults.getMappedResults().isEmpty()) {
			typeCountList = groupResults.getMappedResults().get(0);
		}
		return typeCountList;
	}

	public ContactTypeSummaryDto summaryV1(long timestamp) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		Date dateTi = new Date(timestamp);
		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(dateTi);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		long monthMinTimeStamp = DateUtil.getStartTimestamp(month, year).getTime();
		long monthMaxTimeStamp = DateUtil.getEndTimestamp(month, year).getTime();
		List<SummaryDocDto> lstSummDto = new ArrayList<>();
		List<String> hourListH = new ArrayList<String>();
		List<DateWiseHourCountDto> hourCntLst = new ArrayList<>();

		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(monthMinTimeStamp).lt(monthMaxTimeStamp));
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp").include("type").include("meta");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto hrDto = new DateWiseHourCountDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setDomain(tnt);
				String id = getSummaryId(dto);
				dto.setId(id);
				if (ArgUtil.is(dto.getId())) {
					lstSummDto.add(dto);
				}

			}

		}

		Map<Object, Long> summaryMap = new HashMap<>();
		Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));

		Map<Object, Map<Object, Object>> dateWiseCountMap = new HashMap<>();

		for (Map.Entry<String, Map<String, Long>> keyValue : datwWiseCount.entrySet()) {
			String key = keyValue.getKey();
			Map<Object, Object> dateWiseCnt = new HashMap<>();
			for (Map.Entry<String, Long> keyValueCount : keyValue.getValue().entrySet()) {
				String keyType = keyValueCount.getKey();
				Object count = keyValueCount.getValue();
				dateWiseCnt.put(keyType, count);
			}
			String[] keyId = key.split("_");
			dateWiseCnt.put("domain", ArgUtil.parseAsString(keyId[0], Constants.BLANK));
			dateWiseCnt.put("date", ArgUtil.parseAsString(keyId[1], Constants.BLANK));
			dateWiseCnt.put("channel", ArgUtil.parseAsString(keyId[2], Constants.BLANK));

			dateWiseCountMap.put(key, dateWiseCnt);

		}

		summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting()));

		// printing the count based on the designation and gender.
		// LOGGER.info("Group by on multiple properties" + datwWiseCount);

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		// dto.setDateWiseSummaryCount(datwWiseCount);
		dto.setDateWiseCountMap(dateWiseCountMap);
		dto.setSummaryCount(summaryMap);
		saveDomainSummary(dto);
		return dto;
	}

	public String getSummaryId(SummaryDocDto dto) {
		String tenant = dto.getDomain();
		if (dto.getChannel().contains(ContactType.WHATSAPP.name())) {
			return tenant + "_" + dto.getDate() + "_" + "wa";
		} else if (dto.getChannel().contains(ContactType.FACEBOOK.name())) {
			return tenant + "_" + dto.getDate() + "_" + "fb";
		} else if (dto.getChannel().contains(ContactType.TWITTER.name())) {
			return tenant + "_" + dto.getDate() + "_" + "tw";
		} else if (dto.getChannel().contains(ContactType.TELEGRAM.name())) {
			return tenant + "_" + dto.getDate() + "_" + "tg";
		} else if (dto.getChannel().contains(ContactType.INSTAGRAM.name())) {
			return tenant + "_" + dto.getDate() + "_" + "ig";
		} else if (dto.getChannel().contains(ContactType.WEBSITE.name())) {
			return tenant + "_" + dto.getDate() + "_" + "web";
		}
		return null;
	}
	
	
	public String getSummaryWithChannelId(SummaryDocDto dto) {
		String tenant = dto.getDomain();
		if (dto.getChannel().contains(ContactType.WHATSAPP.name())) {
			return tenant + "_"+ "wa";
		} else if (dto.getChannel().contains(ContactType.FACEBOOK.name())) {
			return tenant + "_" + "fb";
		} else if (dto.getChannel().contains(ContactType.TWITTER.name())) {
			return tenant + "_" + "tw";
		} else if (dto.getChannel().contains(ContactType.TELEGRAM.name())) {
			return tenant + "_" + "tg";
		} else if (dto.getChannel().contains(ContactType.INSTAGRAM.name())) {
			return tenant + "_"  + "ig";
		} else if (dto.getChannel().contains(ContactType.WEBSITE.name())) {
			return tenant + "_" + "web";
		}
		return null;
	}

	public List<String> getListOfContactType() {
		List<String> listContactType = new ArrayList<String>();
		List<String> lstOfConRemo = new ArrayList<String>();
		lstOfConRemo.add("MESSAGE_LOGS");
		lstOfConRemo.add("MESSAGE_OTHERS");
		Set<String> contactTypeSet = mongoTemplate.getCollectionNames();
		if (ArgUtil.is(contactTypeSet)) {
			listContactType = contactTypeSet.stream()
					.filter(x -> !x.isEmpty() && x.startsWith(PMConstants.COLLECTION_NAME))
					.collect(Collectors.toList());
		}
		if (!listContactType.isEmpty()) {
			listContactType.removeAll(lstOfConRemo);
		}
		return listContactType;
	}

	public void saveDomainSummary(ContactTypeSummaryDto dto) {
		DomainSummaryMessageDoc domSumMsgDoc = domSumMetaStore.findDomainAndByName(dto.getTenant(), dto.getMonth());
		if (ArgUtil.is(domSumMsgDoc)) {
			domSumMsgDoc.setDateWiseSummaryCount(dto.getDateWiseSummaryCount());
			domSumMsgDoc.setSummaryCount(dto.getSummaryCount());
			domSumMsgDoc.setDateWiseCountMap(dto.getDateWiseCountMap());
			mongoTemplate.save(domSumMsgDoc);
			saveAndUpdateDomainSummaryMeta(dto);
		} else {
			domSumMsgDoc = new DomainSummaryMessageDoc();
			domSumMsgDoc.setDomain(dto.getTenant());
			domSumMsgDoc.setDate(dto.getMonth());
			domSumMsgDoc.setDateWiseSummaryCount(dto.getDateWiseSummaryCount());
			domSumMsgDoc.setSummaryCount(dto.getSummaryCount());
			domSumMsgDoc.setDateWiseCountMap(dto.getDateWiseCountMap());
			mongoTemplate.save(domSumMsgDoc);
			saveAndUpdateDomainSummaryMeta(dto);
		}

	}

	public void saveAndUpdateDomainSummaryMeta(ContactTypeSummaryDto dto) {
		DomainSummaryMetaDoc metaSummDoc = domSumMetaStore.findDomainByName(dto.getTenant());
		if (ArgUtil.is(metaSummDoc)) {
			metaSummDoc.setDomainUpdatedStamp(System.currentTimeMillis());
			mongoTemplate.save(metaSummDoc);
		} else {
			metaSummDoc = new DomainSummaryMetaDoc();
			metaSummDoc.setDomain(dto.getTenant());
			metaSummDoc.setTimeStamp(System.currentTimeMillis());
			metaSummDoc.setDomainUpdatedStamp(System.currentTimeMillis());
			mongoTemplate.save(metaSummDoc);
		}
	}

	/** WABA summary count **/

	public List<WabaSummaryDocDto> wabaSummary(long timestamp) {
		String tnt = AppContextUtil.getTenant();
		// List<String> lst = getListOfContactType();
		Date dateTi = new Date(timestamp);
		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(dateTi);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		long monthMinTimeStamp = DateUtil.getStartTimestamp(month, year).getTime();
		long monthMaxTimeStamp = DateUtil.getEndTimestamp(month, year).getTime();

		List<WabaSummaryDocDto> wabaLst = new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("created.stamp").gt(monthMinTimeStamp).lt(monthMaxTimeStamp));
		query.with(new Sort(new Order(Direction.DESC, "created.stamp")));
		List<WABAConversation> wabaDocLst = mongoTemplate.find(query, WABAConversation.class, "TP_WABA_CONVERSATIONS");
		for (WABAConversation waba : wabaDocLst) {
			System.out.println("JSON :" + JsonUtil.toJson(waba));
			WabaSummaryDocDto dto = new WabaSummaryDocDto();
			String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(waba.getCreated().getStamp(),
					DateUtil.YYYYMMDD_DATE_FORMAT);
			dto.setId(yyyyMMdd);
			dto.setDate(monthYear);
			if (waba.getMeta() != null && waba.getMeta().get("to_country") != null) {
				dto.setCountry(waba.getMeta().get("to_country").toString());
			}
			dto.setLane(waba.getContact().getLane());
			Map<String, Object> typeMap = (Map<String, Object>) waba.getConversation().get("origin");
			if (typeMap != null && !typeMap.isEmpty()) {
				dto.setType(typeMap.get("type") == null ? "" : typeMap.get("type").toString());
			}
			dto.setChannel(waba.getContact().getContactType());
			dto.setDomain(tnt);
			dto.setPricing(waba.getPricing());
			wabaLst.add(dto);
		}

		return wabaLst;
	}

	public ContactTypeSummaryDto hourWisesummary(long timestamp, long hr) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		List<String> channelLst = getListChannelCongig();
		long currentTs = System.currentTimeMillis();
		long hour = 0;
		if (hr > 0) {
			hour = hr * 60 * 60 * 1000;
		} else {
			hour = 12 * 60 * 60 * 1000;
		}
		long lasthrTimeStmp = currentTs - hour;
		Calendar cal = Calendar.getInstance();
		Date dateTi = new Date();
		if (timestamp > 0) {
			cal.setTimeInMillis(timestamp);
			dateTi = new Date(timestamp);
		} else {
			cal.setTimeInMillis(currentTs);
			dateTi = new Date(currentTs);
		}
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
		
		Map<String, Long> hourCntMap= getHourRange(currentTs,lasthrTimeStmp);
		
		
		for(Map.Entry<String, Map<String, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<String, Long> hoCntMapAll =new HashMap<>();
			String key = keyValue.getKey();
			
			for(String channel:channelLst) {
				if(!key.contains(channel)) {
					hourWiseCount.put(tnt+"_"+channel, hourCntMap);
				}
			}
			 Map<String, Long> hoCntMap =hourWiseCountMap.get(key);
			 
			 for (Map.Entry<String, Long> keyValueCount : hourCntMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					Long count = keyValueCount.getValue();
					if(hoCntMap.containsKey(keydt)){
						hoCntMapAll.put(keydt, hoCntMap.get(keydt));
					}else {
						hoCntMapAll.put(keydt, count);
					}
				}
			 
				hourWiseCount.put(key, hoCntMapAll);
		}
		if(hourWiseCount==null || hourWiseCount.isEmpty()){
			for(String channel:channelLst) {
				hourWiseCount.put(tnt+"_"+channel, hourCntMap);
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
	/** day and channel wise summary**/
	public ContactTypeSummaryDto dayChannelWiseWisesummary(long timestamp, int days) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		List<String> channelLst = getListChannelCongig();
		long currentTs = System.currentTimeMillis();
		ZonedDateTime noOfdaysTstamp=null;
		
		if(days>0) { 
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
		}else {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(12).with(LocalTime.MIN);
		}
		// use the same datetime to create the end of the day using the maximum time for
		long lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();
		
		Map<String,Long> dateRanMap = getDatesRange(currentTs,lasDayTimeStmp);
	
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
				DateWiseHourCountDto daySummDto =new DateWiseHourCountDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setDomain(tnt);
				String id = getSummaryId(dto);
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
			
			for(String channel:channelLst) {
				if(!key.contains(channel)) {
					 dayWiseMap.put(tnt+"_"+channel, dateRanMap);
				}
			}
			
			Map<String, Long> dateWiseCnt = new HashMap<>();
			Map<String, Long> dayCntMap =dayWiseCountMap.get(key);
			 //dateRanMap
			 for (Map.Entry<String, Long> keyValueCount : dateRanMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					Long count = keyValueCount.getValue();
					if(dayCntMap.containsKey(keydt)){
						dateWiseCnt.put(keydt, dayCntMap.get(keydt));
					}else {
						dateWiseCnt.put(keydt, count);
					}
					
				}
			 dayWiseMap.put(key, dateWiseCnt);
		}
    	summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting()));

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setDateWiseSummaryCount(dayWiseMap);
		return dto;
	}
	

	public String getHour(long timeStamp) {
		Date date = new Date(timeStamp);
		/** kk-24 hr , HH-24 hr **/
		SimpleDateFormat sdfH = new SimpleDateFormat("kk");
		String formattedDateH = sdfH.format(date);
		return formattedDateH;
	}
	public Map<String,Long> getDatesRange(long curTiStmp,long lasDayTiStmp){
		Map<String,Long> mapDt = new HashMap<>();
		for(long lasDayTiSt=lasDayTiStmp;lasDayTiSt<=curTiStmp; lasDayTiSt += DateUtil.ONEDAY) {
			 String ds =DateUtil.foramtTimeStampDateAsString(lasDayTiSt,DateUtil.YYYYMMDD_DATE_FORMAT);
			 mapDt.put(ds, new Long(0));
		}
		//Sorting Map
		  Map<String, Long> result = mapDt.entrySet().stream()
	                .sorted(Map.Entry.comparingByKey())
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		
		return mapDt;
	}
	
	public Map<String,Long> getHourRange(long currentTStamp,long lastTimeStamp){
		Map<String,Long> mapHr = new HashMap<>();
		String curHr = getHour(currentTStamp);
		String lastHr = getHour(lastTimeStamp);
		
		int currHrInt = Integer.parseInt(curHr);
		int lastHrInt = Integer.parseInt(lastHr);
		if(currHrInt<12) {
			currHrInt =currHrInt+24; 
		}
		for(int i=lastHrInt;i<=currHrInt;i++) {
			int k=i;
			if(i>24) {
				k=i-24;
			}
			String keyS=String.valueOf(k);
			if(keyS.length()==1) {
				keyS="0"+keyS;
			}
			mapHr.put(keyS, new Long(0));
		
		}
		  Map<String, Long> result = mapHr.entrySet().stream()
	                .sorted(Map.Entry.comparingByKey())
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		  
		  
		  
		
		return result;
	}
	
	
	/** Read ,Unread,sent,deliver msg count  Hour Wise */
	@SuppressWarnings("unused")
	public ContactTypeSummaryDto getHourWiseMsgStatusSummary(long timestamp,long hr) {
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
		List<Map<String,Object>> lstMap= new ArrayList<>();
		for (String contactType : lst) {
			List<ContactTypeCountDto> messageTypeLst = new ArrayList<ContactTypeCountDto>();
			List<DBObject> list = new ArrayList<DBObject>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(lasthrTimeStmp).lt(currentTs))
					.toDBObject(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.match(new Criteria("bulkSessionId").exists(true))
					.toDBObject(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("stamps").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));

			DBCollection col = mongoTemplate.getCollection(contactType);
			Cursor cursor = col.aggregate(list,
					AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				DBObject object = cursor.next();
				if (ArgUtil.is(object)) {
					String type = ArgUtil.parseAsString(object.get("_id"));
					if(ArgUtil.is(type)) {
					Map<String,Object> mapValue = JsonUtil.fromJsonToMap(type);
					long count = ArgUtil.parseAsLong(object.get("count"), 0L);
					contactDto.setType(type);
					contactDto.setTotalCount(count);
					lstMap.add(mapValue);
					}
				}
				messageTypeLst.add(contactDto);
			}
		}

		for(Map<String,Object> mapv:lstMap) {
			for (Map.Entry<String, Object> keyValueCount : mapv.entrySet()) {
				DateWiseHourCountDto daySummDto =new DateWiseHourCountDto();
					String key = keyValueCount.getKey();
					if(ArgUtil.is(key) && !key.equalsIgnoreCase("session")) {
					String value =getHour((Long)keyValueCount.getValue());
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
		Map<String, Long> hourCntMap= getHourRange(currentTs,lasthrTimeStmp);
		
		Map<String, Map<String, Long>> hourWiseCount = addDefaultHour(hourWiseCountMap, hourCntMap);
		
	
		hourWiseCount = fetchAndAddAllMsgStatus(hourWiseCount,hourCntMap);
		
		
		dto.setTenant(tnt);
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(lasthrTimeStmp);
		dto.setMonthMaxTimeStamp(currentTs);
		dto.setHourWiseCountMap(hourWiseCount);

		return dto;

	}
	
	
	
	@SuppressWarnings("unused")
	public ContactTypeSummaryDto getDayWiseMsgStatusSummary(long timestamp,int days) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		
		List<DateWiseHourCountDto> dayCntLst = new ArrayList<>();
		
		long currentTs = System.currentTimeMillis();
		ZonedDateTime noOfdaysTstamp=null;
		
		if(days>0) { 
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
		}else {
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
		List<Map<String,Object>> lstMap= new ArrayList<>();
		for (String contactType : lst) {
			List<ContactTypeCountDto> messageTypeLst = new ArrayList<ContactTypeCountDto>();
			List<DBObject> list = new ArrayList<DBObject>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(lasDayTimeStmp).lt(currentTs))
					.toDBObject(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("stamps").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));

			DBCollection col = mongoTemplate.getCollection(contactType);
			Cursor cursor = col.aggregate(list,
					AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				DBObject object = cursor.next();
				if (ArgUtil.is(object)) {
					String type = ArgUtil.parseAsString(object.get("_id"));
					if(ArgUtil.is(type)) {
					Map<String,Object> mapValue = JsonUtil.fromJsonToMap(type);
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
		for(Map<String,Object> mapv:lstMap) {
			for (Map.Entry<String, Object> keyValueCount : mapv.entrySet()) {
				DateWiseHourCountDto daySummDto =new DateWiseHourCountDto();
					String key = keyValueCount.getKey();
					if(ArgUtil.is(key) && !key.equalsIgnoreCase("session")) {
					String yyyyMMdd = DateUtil.foramtTimeStampDateAsString((Long)keyValueCount.getValue(),DateUtil.YYYYMMDD_DATE_FORMAT);	
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
		Map<String,Long> dateRanMap = getDatesRange(currentTs,lasDayTimeStmp);
		
		Map<String, Map<String, Long>> dateWiseSummary = addDefaultHour(dateWiseCountMap, dateRanMap);
		
		dateWiseSummary = fetchAndAddAllMsgStatus(dateWiseSummary,dateRanMap);
		
		
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
	
	public Map<String, Map<String, Long>>  addDefaultHour(Map<String, Map<String, Long>> hourWiseCountMap,Map<String, Long> hourCntMap) {
		Map<String, Map<String, Long>> countSummary = new HashMap<>();
		Map<String, Long> defaultMap=null; 
		
		for(Map.Entry<String, Map<String, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<String, Long> hoCntMapAll =new HashMap<>();
			String key = keyValue.getKey();
			if(ArgUtil.is(key)) {
			 defaultMap =hourWiseCountMap.get(key);
			 
			 for (Map.Entry<String, Long> keyValueCount : hourCntMap.entrySet()) {
					String keydt = keyValueCount.getKey();
					if(ArgUtil.is(keydt)) {
					Long count = keyValueCount.getValue();
					if(defaultMap.containsKey(keydt)){
						hoCntMapAll.put(keydt, defaultMap.get(keydt));
					}else {
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
	public Map<String, Map<String, Long>> fetchAndAddAllMsgStatus(Map<String, Map<String, Long>> hourWiseCount,Map<String, Long> hourCntMap){
		
		Message.Status[] msgSta = Message.Status.values();
		
		for(Message.Status stsobj:msgSta) {
			String sts =ArgUtil.parseAsString(stsobj);
			if(sts!=null && (hourWiseCount==null || hourWiseCount.isEmpty())) {
				hourWiseCount.put(sts.toString(), hourCntMap);
			}else if(sts!=null && hourWiseCount!=null && !hourWiseCount.containsKey(sts)) {
				hourWiseCount.put(sts.toString(), hourCntMap);
			}
		}
		return hourWiseCount;
	}
	
	public List<String> getListChannelCongig() {
		List<String> listOfChannelConfig = new ArrayList<String>();
		
		Query query = new Query();
		query.addCriteria(Criteria.where("isDisabled").is(false));
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		for(ChannelConfigDoc cofigDoc:cofigDocLst) {
			listOfChannelConfig.add(cofigDoc.getChannelType());
		}
		
		listOfChannelConfig = new ArrayList<>(new HashSet<>(listOfChannelConfig));
		
		return listOfChannelConfig;
	}
	

}
