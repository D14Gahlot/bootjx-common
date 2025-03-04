package com.boot.jx.account.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.DecimalFormat;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainSummaryMessageDoc;
import com.boot.jx.account.doc.DomainSummaryMetaDoc;
import com.boot.jx.account.doc.DomainSummaryMetaStore;
import com.boot.jx.account.dto.AccountDashBoardRequestDto;
import com.boot.jx.account.dto.AccountDashBoardResponseDto;
import com.boot.jx.account.dto.AdminAgentAccount;
import com.boot.jx.account.dto.AdminAgentAccountDto;
import com.boot.jx.account.dto.ContactTypeCountDto;
import com.boot.jx.account.dto.ContactTypeSummaryDto;
import com.boot.jx.account.dto.DateWiseHourCountDto;
import com.boot.jx.account.dto.MonthDtlsDto;
import com.boot.jx.account.dto.SummaryDocDto;
import com.boot.jx.account.dto.TimeZoneOfSet;
import com.boot.jx.account.dto.TypeCount;
import com.boot.jx.account.dto.WabaBalanceDto;
import com.boot.jx.account.dto.WabaDateWiseBalanceDto;
import com.boot.jx.account.dto.WabaSummary;
import com.boot.jx.account.dto.WabaSummaryDocDto;
import com.boot.jx.api.EventCountDto;
import com.boot.jx.api.EventCountSummary;
import com.boot.jx.common.config.CONFIG_SETUP_KEY;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DomainDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.WabaAccountBalanceDoc;
import com.boot.jx.postman.doc.WabaAnalyticsDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.tpo.WABAConversation;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.utils.CommonUtils;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.DateUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapUtils;
import com.mongodb.client.MongoCursor;

@Component
public class AccountDashBoardManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDashBoardManager.class);
//	@Autowired
//	MongoTemplate mongoTemplate;
	
	@Autowired
	CommonMongoTemplate mongoTemplate;

	@Autowired
	private DomainSummaryMetaStore domSumMetaStore;

	@Autowired
	private AccountStore accountStore;

	@Autowired
	private PMEnvironment environment;
	
	@Autowired
	AgentStore agentStore;

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
		List<Long> msgDocLst = mongoTemplate.collection("CHAT_SESSION", Long.class)
				.distinct("startSessionStamp", Long.class).asList();
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
			List<Document> list = new ArrayList<Document>();
			// Match condtion
			list.add(Aggregation.match(new Criteria("timestamp").gt(monthMinTimeStamp).lt(monthMaxTimeStamp))
					.toDocument(Aggregation.DEFAULT_CONTEXT));
			list.add(Aggregation.group("type").count().as("count").toDocument(Aggregation.DEFAULT_CONTEXT));

//			DBCollection col = mongoTemplate.getCollection(contactType);
//			Cursor cursor = col.aggregate(list,
//					AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());
			
			MongoCursor<Document> cursor = mongoTemplate.collection(contactType).aggregate(list).iterator();
			
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				Document object = cursor.next();
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
				if (ArgUtil.is(dto.getId()) && ArgUtil.is(doc.getType())) {
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
			return tenant + "_" + dto.getDate() + "_" + "wa" + "_" + dto.getLane();
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
			return tenant + "_" + "wa" + "_" + dto.getLane();
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

	public WabaSummary wabaSummary(long timestamp) {
		WabaSummary summary = new WabaSummary();
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
		
		
		Map<String,Long> mediaTemSum= getMediaTemplateCountV1(monthMinTimeStamp,monthMaxTimeStamp);
		
		summary.setWabaSummaryCount(wabaLst);
		summary.setMediaSummaryCount(mediaTemSum);

		return summary;
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
			query.fields().include("timestamp").include("type").include("meta").include("contactId");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto hrDto = new DateWiseHourCountDto();
				long timeStamp = doc.getTimestamp();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				String contactid = doc.getContactId();
				dto.setChannel(contactType.toString());
				String lane = getLane(contactid);
				dto.setLane(lane);
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setMeta(doc.getMeta());
				dto.setDomain(tnt);
				String id = getSummaryWithChannelId(dto);
				if (ArgUtil.is(id)) {
					dto.setId(id);
					Date date = new Date(timeStamp);
					SimpleDateFormat sdfH = new SimpleDateFormat(DateUtil.DEFAULT_DATE_TIME_FORMAT);
					String formattedDateHm = sdfH.format(date);
					SimpleDateFormat sdfm = new SimpleDateFormat("mm");
					long tStampWmS = getHour(timeStamp);
					hourListH.add(formattedDateHm);
					hrDto.setDate(yyyyMMdd);
					hrDto.setHour(formattedDateHm);
					hrDto.setHourStamp(tStampWmS);
					hrDto.setChannel(id);
					if (hrDto != null) {
						hourCntLst.add(hrDto);
					}
				}
			}

		}
		Map<Object, Long> summaryMap = new HashMap<>();

		/** hour wise count **/
		Map<String, Map<Object, Long>> hourWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel,
						Collectors.groupingBy(DateWiseHourCountDto::getHourStamp, Collectors.counting())));

		Map<Object, Map<Object, Long>> hourWiseCount = new HashMap<>();

		Map<Object, Long> hourCntMap = getHourRange(currentTs, lasthrTimeStmp);

	
		
		hourWiseCount = MapUtils.getHourdefaultValue(hourWiseCountMap, channelLst, hourCntMap, tnt);

		hourWiseCount = sortMap(hourWiseCount);
		hourWiseCount =removeSandBoxNumber(hourWiseCount);

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setHourWiseCountMap(hourWiseCount);
		return dto;
	}

	/** day and channel wise summary **/
	public ContactTypeSummaryDto dayChannelWiseWisesummary(String dateRange1, String dateRange2, int days) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		List<String> channelLst = getListChannelCongig();
		long currentTs = System.currentTimeMillis();

		long offsetts = countryTimeZoneOffset(tnt);
		ZonedDateTime noOfdaysTstamp = null;
		long lasDayTimeStmp = 0;
		String offsett = getTimeZoneFromSetup();
		LOGGER.info("dayChannelWiseWisesummary dateRange1 :" + dateRange1 + "\t dateRange2 :" + dateRange2
				+ "\t offsett :" + offsett);

		DomainDoc dDoc = getDomainTimeZone(tnt);
		String zone = getTimeZone(offsett == null ? dDoc.getTimeZoneOffSet() : offsett);
		String offset = getOffSet(offsett == null ? dDoc.getTimeZoneOffSet() : offsett);
		String[] hm = offset.split(":");

		int hr = ArgUtil.parseAsInteger(hm[0]);
		int mm = ArgUtil.parseAsInteger(hm[1]);

		if (ArgUtil.is(dateRange1)) {
			lasDayTimeStmp = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange1), hr, mm, zone, LocalTime.MIN);
			lasDayTimeStmp = lasDayTimeStmp + offsetts;

		}
		if (ArgUtil.is(dateRange2)) {
			currentTs = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs + offsetts;
		}

		if (lasDayTimeStmp == 0 && days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
			lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();
		}

		Map<Object, Long> dateRanMap = getDatesRange(currentTs, lasDayTimeStmp);

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(currentTs);
		List<SummaryDocDto> lstSummDto = new ArrayList<>();
		List<DateWiseHourCountDto> hourCntLst = new ArrayList<>();

		for (String contactType : lst) {
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(lasDayTimeStmp).lt(currentTs));
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp").include("type").include("meta").include("contactId");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto daySummDto = new DateWiseHourCountDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				String lane = getLane(doc.getContactId());
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setLane(lane);
				dto.setDomain(tnt);
				dto.setLane(getLane(doc.getContactId()));
				String id = getSummaryId(dto);
				dto.setId(id);
				if (ArgUtil.is(dto.getId()) && ArgUtil.is(doc.getType())) {
					lstSummDto.add(dto);
				}
				String channelid = getSummaryWithChannelId(dto);
				if (ArgUtil.is(channelid)) {
					daySummDto.setDate(yyyyMMdd);
					daySummDto.setChannel(channelid);
					if (daySummDto != null) {
						hourCntLst.add(daySummDto);
					}
				}

			}

		}
		Map<Object, Long> summaryMap = new HashMap<>();

		/** day wise count **/
		Map<String, Map<String, Long>> dayWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel,
						Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.counting())));

		Map<Object, Map<Object, Long>> dayWiseMap = new HashMap<>();
		dayWiseMap = MapUtils.defaultValue(dayWiseCountMap, channelLst, dateRanMap, tnt);

		dayWiseMap = sortMap(dayWiseMap);
		dayWiseMap = removeSandBoxNumber(dayWiseMap);
		
		summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting()));

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setDateWiseSummaryCount(dayWiseMap);
		return dto;
	}

	public Long getHour(long timeStamp) {
		Date date = new Date(timeStamp);
		/**
		 * kk-24 hr , HH-24 hr SimpleDateFormat sdfH = new SimpleDateFormat("kk");
		 * String formattedDateH = sdfH.format(date); return formattedDateH;
		 */

		SimpleDateFormat sdfm = new SimpleDateFormat("mm");
		String min = sdfm.format(date);
		int m = Integer.parseInt(min);
		long tStamp = timeStamp;
		if (m > 30) {
			tStamp = timeStamp + ((60 - m) * 60 * 1000L);
		} else {
			tStamp = timeStamp + ((30 - m) * 60 * 1000L);
		}

		long tStampWmS = (tStamp - (tStamp % (1000 * 60)));

		return tStampWmS;
	}

	public Map<Object, Long> getDatesRange(long curTiStmp, long lasDayTiStmp) {
		Map<Object, Long> mapDt = new HashMap<>();
		for (long lasDayTiSt = lasDayTiStmp; lasDayTiSt <= curTiStmp; lasDayTiSt += DateUtil.ONEDAY) {
			String ds = DateUtil.foramtTimeStampDateAsString(lasDayTiSt, DateUtil.YYYYMMDD_DATE_FORMAT);
			mapDt.put(ds, new Long(0));
		}
		Map<Object, Long> result = new TreeMap<Object, Long>(mapDt);
		return result;
	}

	public Map<Object, Long> getHourRange(long currentTStamp, long lastTimeStamp) {
		Map<String, Long> mapHr = new HashMap<>();
		Map<Object, Long> mapMinWise = new HashMap<>();
		Date date = new Date(lastTimeStamp);
		SimpleDateFormat sdfHM = new SimpleDateFormat("mm");
		String formattedHM = sdfHM.format(date);
		int m = Integer.parseInt(formattedHM);
		long currTimeStM = currentTStamp;
		long lastTimeStampWm = lastTimeStamp;
		/** for Upper round **/
		if (m > 30) {
			m = 60 - m;
			currTimeStM = currentTStamp + (m * 60 * 1000L);
			lastTimeStampWm = lastTimeStamp - ((30 - m) * 60 * 1000L);
		} else {
			lastTimeStampWm = lastTimeStamp - (m * 60 * 1000L);
			m = 30 - m;
			currTimeStM = currentTStamp + (m * 60 * 1000L);
		}
		lastTimeStampWm = (lastTimeStampWm - (lastTimeStampWm % (1000 * 60)));
		for (long lastTS = lastTimeStampWm; lastTS <= currTimeStM; lastTS = lastTS + DateUtil.MIN_30) {
			mapMinWise.put(lastTS, new Long(0));
		}
		Map<Object, Long> result = new TreeMap<Object, Long>(mapMinWise);
		return result;
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
			//List<Document> list
			list = getAggregationMatchForMsgStatus(lasthrTimeStmp, currentTs);

			MongoCursor<Document> cursor = mongoTemplate.collection(contactType).aggregate(list).iterator();
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				Document object = cursor.next();
				if (ArgUtil.is(object)) {
					JSONObject jsonObject = new JSONObject(JsonUtil.toJson(object));
					String type = ArgUtil.parseAsString(jsonObject.get("_id"));
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
					long value = getHour((Long) keyValueCount.getValue());
					daySummDto.setMsgType(key);
					daySummDto.setHourStamp(value);
					hourCntLst.add(daySummDto);
				}
			}

		}

		// /** Hour wise couunt
		Map<Object, Map<Object, Long>> hourWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getMsgType,
						Collectors.groupingBy(DateWiseHourCountDto::getHourStamp, Collectors.counting())));
		/// ** default hour
		// Map<String, Long> hourCntMap = getHourRange(currentTs, lasthrTimeStmp);

		Map<Object, Long> hourCntMap = getHourRange(currentTs, lasthrTimeStmp);

		Map<Object, Map<Object, Long>> hourWiseCount = addDefaultHour(hourWiseCountMap, hourCntMap);

		hourWiseCount = fetchAndAddAllMsgStatus(hourWiseCount, hourCntMap);
		hourWiseCount = sortMap(hourWiseCount);

		dto.setTenant(tnt);
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(lasthrTimeStmp);
		dto.setMonthMaxTimeStamp(currentTs);
		dto.setHourWiseCountMap(hourWiseCount);

		return dto;

	}

	@SuppressWarnings("unused")
	public ContactTypeSummaryDto getDayWiseMsgStatusSummary(String dateRange1, String dateRange2, int days) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();

		List<DateWiseHourCountDto> dayCntLst = new ArrayList<>();

		long offsetts = countryTimeZoneOffset(tnt);
		String offsett = getTimeZoneFromSetup();
		LOGGER.info("dayChannelWiseWisesummary dateRange1 :" + dateRange1 + "\t dateRange2 :" + dateRange2
				+ "\t offsett :" + offsett);

		DomainDoc doc = getDomainTimeZone(tnt);
		String zone = getTimeZone(offsett == null ? doc.getTimeZoneOffSet() : offsett);
		String offset = getOffSet(offsett == null ? doc.getTimeZoneOffSet() : offsett);
		String[] hm = offset.split(":");

		int hr = ArgUtil.parseAsInteger(hm[0]);
		int mm = ArgUtil.parseAsInteger(hm[1]);

		long currentTs = System.currentTimeMillis();

		ZonedDateTime noOfdaysTstamp = null;
		long lasDayTimeStmp = 0;
		if (ArgUtil.is(dateRange1)) {
			// lasDayTimeStmp = dateRange1+offsetts;
			lasDayTimeStmp = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange1), hr, mm, zone, LocalTime.MIN);
			lasDayTimeStmp = lasDayTimeStmp + offsetts;
		}
		if (ArgUtil.is(dateRange2)) {
			// currentTs = dateRange2+offsetts;
			currentTs = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs + offsetts;
		}
		if (lasDayTimeStmp == 0 && days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
			lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();
		}

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(currentTs);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTs);
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
			list = getAggregationMatchForMsgStatus(lasDayTimeStmp, currentTs);
			//DBCollection col = mongoTemplate.getCollection(contactType);
			MongoCursor<Document> cursor = mongoTemplate.collection(contactType).aggregate(list).iterator();
	
			while (cursor.hasNext()) {
				ContactTypeCountDto contactDto = new ContactTypeCountDto();
				Document object = cursor.next();
				if (ArgUtil.is(object)) {
					JSONObject jsonObject = new JSONObject(JsonUtil.toJson(object));
					String type = ArgUtil.parseAsString(jsonObject.get("_id"));
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
		// /** Hour wise couunt
		Map<Object, Map<Object, Long>> dateWiseCountMap = dayCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getMsgType,
						Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.counting())));
		/// ** default hour
		Map<Object, Long> dateRanMap = getDatesRange(currentTs, lasDayTimeStmp);

		Map<Object, Map<Object, Long>> dateWiseSummary = addDefaultHour(dateWiseCountMap, dateRanMap);

		dateWiseSummary = fetchAndAddAllMsgStatus(dateWiseSummary, dateRanMap);

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();

		dateWiseSummary = sortMap(dateWiseSummary);

		dto.setTenant(tnt);
		dto.setMap(map);
		dto.setMonth(monthYear);
		dto.setMonthMinTimeStamp(lasDayTimeStmp);
		dto.setMonthMaxTimeStamp(currentTs);
		dto.setDateWiseSummaryCount(dateWiseSummary);

		return dto;

	}

	/** add default hour **/

	public Map<Object, Map<Object, Long>> addDefaultHour(Map<Object, Map<Object, Long>> hourWiseCountMap,
			Map<Object, Long> hourCntMap) {
		Map<Object, Map<Object, Long>> countSummary = new HashMap<>();
		Map<Object, Long> defaultMap = null;

		for (Map.Entry<Object, Map<Object, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<Object, Long> hoCntMapAll = new HashMap<>();
			Object key = keyValue.getKey();
			if (ArgUtil.is(key)) {
				defaultMap = hourWiseCountMap.get(key);

				for (Map.Entry<Object, Long> keyValueCount : hourCntMap.entrySet()) {
					Object keydt = keyValueCount.getKey();
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
	public Map<Object, Map<Object, Long>> fetchAndAddAllMsgStatus(Map<Object, Map<Object, Long>> hourWiseCount,
			Map<Object, Long> hourCntMap) {

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

	/** Agreegration Query to fetch msg status **/
	public List<Document> getAggregationMatchForMsgStatus(long lasthrTimeStmp, long currentTs) {
		String metaQry = "{\"categoryType\" : \"AUTO_REPLY\",\r\n" + "\"composeType\" : \"N\",\r\n"
				+ "\"sendType\" : \"PM\"}";
		// queryFilter = "{'FirstName':'Ian'}";

		List<Document> list = new ArrayList<Document>();
		// Match condtion
		list.add(Aggregation.match(new Criteria("type").is("O")).toDocument(Aggregation.DEFAULT_CONTEXT));
		
		list.add(Aggregation.match(new Criteria("meta.composeType").is("N")).toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.match(new Criteria("meta.sendType").is("PM")).toDocument(Aggregation.DEFAULT_CONTEXT));

		
//		Criteria composeTypeCriteria = new Criteria().orOperator(
//			    Criteria.where("meta.composeType").is("N"),
//			    Criteria.where("meta.composeType").is("R")
//			);
//		list.add(Aggregation.match(composeTypeCriteria).toDocument(Aggregation.DEFAULT_CONTEXT));
//			
//		Criteria sendTypeCriteria = new Criteria().orOperator(
//			    Criteria.where("meta.sendType").is("PM"),
//			    Criteria.where("meta.sendType").is("SM")
//			);
//
//			
//		list.add(Aggregation.match(sendTypeCriteria).toDocument(Aggregation.DEFAULT_CONTEXT));
		
		
		list.add(Aggregation.match(new Criteria("timestamp").gt(lasthrTimeStmp).lt(currentTs))
				.toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.group("stamps").count().as("count").toDocument(Aggregation.DEFAULT_CONTEXT));

		return list;
	}
	public Map<Object, Map<Object, Long>> sortMap(Map<Object, Map<Object, Long>> map) {
		Map<Object, Map<Object, Long>> sortedMap = new HashMap<>();
		for (Map.Entry<Object, Map<Object, Long>> entry : map.entrySet()) {
			Object k = entry.getKey();
			Map<Object, Long> v = new TreeMap<>(entry.getValue());
			sortedMap.put(k, v);
		}
		return sortedMap;

	}

	public ContactTypeSummaryDto getNonWhatsUpSummary(String dateRange1, String dateRange2,int days) {
		List<String> channelLst = getListChannelCongig();
		List<String> lst = getListOfContactType();
		lst.remove("MESSAGE_WHATSAPP");
		lst.remove("MESSAGE_REJECTED");
		lst.remove("MESSAGE_QUEUED");
		lst.remove("MESSAGE_HOLD");
		channelLst = getChannelShortCode(lst);

		String tnt = AppContextUtil.getTenant();
		List<SummaryDocDto> lstSummDto = new ArrayList<>();
		long currentTs = System.currentTimeMillis();
		
		long offsetts = countryTimeZoneOffset(tnt);
		ZonedDateTime noOfdaysTstamp = null;
		long lasDayTimeStmp = 0;
		String offsett = getTimeZoneFromSetup();
		
		DomainDoc dDoc = getDomainTimeZone(tnt);
		String zone = getTimeZone(offsett == null ? dDoc.getTimeZoneOffSet() : offsett);
		String offset = getOffSet(offsett == null ? dDoc.getTimeZoneOffSet() : offsett);
		String[] hm = offset.split(":");

		int hr = ArgUtil.parseAsInteger(hm[0]);
		int mm = ArgUtil.parseAsInteger(hm[1]);

		
		
		if (ArgUtil.is(dateRange1)) {
			lasDayTimeStmp = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange1), hr, mm, zone, LocalTime.MIN);
			lasDayTimeStmp = lasDayTimeStmp + offsetts;
		}
		if (ArgUtil.is(dateRange2)) {
			currentTs = DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs + offsetts;
		}
		if (lasDayTimeStmp == 0 && days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
			lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();
		}
				
		Date dateTi = new Date(lasDayTimeStmp);
		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(dateTi);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTs);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);

		for (String contactType : lst) {
			LOGGER.info("contactType :" + contactType);
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(lasDayTimeStmp).lt(currentTs));
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp").include("contactId");
			List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
			for (MessageDoc doc : msgDocLst) {
				SummaryDocDto dto = new SummaryDocDto();
				DateWiseHourCountDto daySummDto = new DateWiseHourCountDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setDate(yyyyMMdd);
				dto.setUniqueContactId(doc.getContactId());
				dto.setChannel(contactType.toString());
				dto.setDomain(tnt);
				String id = getSummaryWithChannelId(dto);
				dto.setId(id);
				if (ArgUtil.is(dto.getId()) && ArgUtil.is(dto.getUniqueContactId())) {
					lstSummDto.add(dto);
				}
			}
		}

		Set<SummaryDocDto> uniqueStudentSet = lstSummDto.stream() // get stream for original list
				.collect(Collectors.toCollection(// distinct elements stored into new SET
						() -> new TreeSet<>(Comparator.comparing(SummaryDocDto::getUniqueContactId)))); // Id comparison

		List<SummaryDocDto> uniqueList = uniqueStudentSet.stream() // get stream for unique SET
				.sorted(Comparator.comparing(SummaryDocDto::getDate)) // rank comparing
				.collect(Collectors.toList()); // elements stored to new list

		Map<String, Map<String, Long>> datwWiseCount = uniqueList.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getDate, Collectors.counting())));
		Map<Object, Long> dateRanMap = MapUtils.getDatesRange(currentTs, lasDayTimeStmp);
		
		Map<Object, Map<Object, Long>> dayWiseMap = new HashMap<>();
		dayWiseMap = MapUtils.defaultValue(datwWiseCount, channelLst, dateRanMap, tnt);

		dayWiseMap = sortMap(dayWiseMap);

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setDateWiseSummaryCount(dayWiseMap);
		dto.setTenant(tnt);
		dto.setMonth(monthYear);

		return dto;

	}

	/** Agreegration Query to fetch msg status **/
	public List<Document> getAggregationMatchForNonWhatsUpContactId(long lasthrTimeStmp, long currentTs) {

		List<Document> list = new ArrayList<Document>();
		// Match condtion
		list.add(Aggregation.match(new Criteria("timestamp").gt(lasthrTimeStmp).lt(currentTs))
				.toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.group("timestamp", "contactId").count().as("count")
				.toDocument(Aggregation.DEFAULT_CONTEXT));

		return list;
	}

	public List<String> getListChannelCongig() {
		List<String> listOfChannelConfig = new ArrayList<String>();
		Query query = new Query();
		query.addCriteria(Criteria.where("isDisabled").is(false).and("isSandbox").is(false));
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		for (ChannelConfig cofigDoc : cofigDocLst) {
			listOfChannelConfig.add(cofigDoc.getChannelType());
		}

		listOfChannelConfig = new ArrayList<>(new HashSet<>(listOfChannelConfig));
		listOfChannelConfig.remove("wa360");

		return listOfChannelConfig;
	}

	public String getLane(String contactid) {
		String lane = "";
		if (ArgUtil.is(contactid)) {
			String[] contactids = contactid.split("_");
			if (contactids != null && contactids[1] != null) {
				lane = contactids[1];
			}
		}
		return lane;
	}

	public Long countryTimeZoneOffset(String domain) {
		AppContextUtil.setTenant(Tenants.getDefault());
		DomainDoc domainDoc = accountStore.findDomainByName(domain);
		AppContextUtil.setTenant(domain);
		long offsettimestamp = 0;
		String offset = null;
		if (ArgUtil.is(domainDoc)) {
			offset = domainDoc.getTimeZoneOffSet();
		}

		if (ArgUtil.is(offset)) {
			try {
			String hrStr = offset.substring(offset.indexOf('+') + 1);
			String[] hrMin = hrStr.split(":");
			if(hrMin!=null && hrMin.length>1) {
				int hr = Integer.parseInt(hrMin[0]);
				int min = Integer.parseInt(hrMin[1]);
			    offsettimestamp = hr * DateUtil.ONE_HR + min * DateUtil.MIN;
			}
			}catch (Exception e) {
				e.printStackTrace();
	            // Handle the case where parsing fails
	            System.err.println("Invalid timezone offset format: " + offset+"\t error"+e);
	        }
		}
		return offsettimestamp;
	}

	public DomainDoc getDomainTimeZone(String domain) {
		AppContextUtil.setTenant(Tenants.getDefault());
		DomainDoc domainDoc = accountStore.findDomainByName(domain);
		AppContextUtil.setTenant(domain);
		return domainDoc;
	}

	public String getTimeZone(String toffset) {
		String timeZone = java.util.TimeZone.getDefault().getID();
		if (ArgUtil.is(toffset)) {
			String[] hrStr = toffset.split("::");
			if (ArgUtil.is(hrStr)) {
				timeZone = hrStr[0];
			}
		}
		return timeZone;
	}

	public String getOffSet(String toffset) {
		String offset = "00:00";
		if (ArgUtil.is(toffset)) {
			String[] hrStr = toffset.split("::");
			if (ArgUtil.is(hrStr)) {
				offset = hrStr[1].substring(hrStr[1].indexOf('+') + 1);
			}
		}
		return offset;
	}

	public TimeZoneOfSet getTimeZoneOffset() {
		TimeZoneOfSet tzo = new TimeZoneOfSet();
		Map<String, String> hm = new HashMap<>();
		String[] ids = TimeZone.getAvailableIDs();
		for (String id : ids) {
			TimeZone tz = TimeZone.getTimeZone(id);
			long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
			long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours);
			// avoid -4:-30 issue
			minutes = Math.abs(minutes);

			String key = null;
			String contry = null;
			String timezoffset = null;

			if (hours > 0) {
				contry = tz.getID();
				timezoffset = String.format("GMT+%d:%02d", hours, minutes);
				key = contry + "::" + timezoffset;

				hm.put(key, contry);
			} else {
				contry = tz.getID();
				timezoffset = String.format("GMT+%d:%02d", hours, minutes);
				key = contry + "::" + timezoffset;
				hm.put(key, contry);
			}

		}
		tzo.setTimeZmap(hm);

		return tzo;
	}

	public String getCovertDate(String date) {
		String dt = null;
		if (ArgUtil.is(date)) {
			String[] dtStr = date.split("/");
			dt = dtStr[2] + "-" + dtStr[1] + "-" + dtStr[0];
		}
		return dt;
	}

	public String getTimeZoneFromSetup() {
		String offset = environment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_TIMEZONE_OFFSET)
				.asString("Asia/Kolkata::GMT+5:30");
		return offset;
	}

	/** day wise event count summary **/
	public EventCountSummary getEventCountSummary(String dateRange1, String dateRange2, int days) {
		EventCountSummary eventCountSummary = new EventCountSummary();
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();

		long currentTs = System.currentTimeMillis();
		ZonedDateTime noOfdaysTstamp = null;

		String offset = getTimeZoneFromSetup();
		long offsetts = countryTimeZoneOffset(offset);
		String zone = DateUtil.getTimeZone(offset);
		long lasDayTimeStmp = 0;
		int hr = 0;
		int mm = 0;
		if (ArgUtil.is(dateRange1)) {
			lasDayTimeStmp = DateUtil.getDateMinAndMaxTime(DateUtil.getCovertDate(dateRange1), hr, mm, zone,
					LocalTime.MIN);
			lasDayTimeStmp = lasDayTimeStmp + offsetts;

		}
		if (ArgUtil.is(dateRange2)) {
			currentTs = DateUtil.getDateMinAndMaxTime(DateUtil.getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs + offsetts;
		}

		if (lasDayTimeStmp == 0 && days > 0) {
			noOfdaysTstamp = ZonedDateTime.now().minusDays(days).with(LocalTime.MIN);
			lasDayTimeStmp = noOfdaysTstamp.toInstant().toEpochMilli();
		}

		String monthYear = new SimpleDateFormat(DateUtil.MMM_YYYY_FORMAT).format(currentTs);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTs);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);

		/** langage summary count **/
		List<EventCountDto> lstLangSummary = new ArrayList<>();

		for (String contactType : lst) {

			List<MessageDoc> msgDocLst = getLanguageCount(contactType, lasDayTimeStmp, currentTs);
			for (MessageDoc msg : msgDocLst) {
				EventCountDto entLang = new EventCountDto();
				if (msg.getForm() != null && !msg.getForm().isEmpty()) {
					entLang.setLanguage(msg.getForm().get("reply_title").toString());
				}
				if (msg.getContact() != null && msg.getContact().getContactType() != null) {
					entLang.setChannel(msg.getContact().getContactType());
				}
				if (ArgUtil.is(entLang) && ArgUtil.is(entLang.getChannel()) && ArgUtil.is(entLang.getLanguage())) {
					lstLangSummary.add(entLang);
				}
			}
		}

		Map<Object, Map<Object, Long>> langWiseCountMap = lstLangSummary.stream().collect(Collectors.groupingBy(
				EventCountDto::getChannel, Collectors.groupingBy(EventCountDto::getLanguage, Collectors.counting())));

		eventCountSummary.setLangWiseCountMap(langWiseCountMap);

		List<EventCountDto> lstEventSummary = new ArrayList<>();
		for (String contactType : lst) {
			List<MessageDoc> msgDocLst = getEventCount(contactType, lasDayTimeStmp, currentTs);
			for (MessageDoc msg : msgDocLst) {
				EventCountDto entLang = new EventCountDto();
				if (msg.getForm() != null && !msg.getForm().isEmpty()) {
					entLang.setEvent(msg.getForm().get("reply_title").toString());
				}
				if (msg.getContact() != null && msg.getContact().getContactType() != null) {
					entLang.setChannel(msg.getContact().getContactType());
				}

				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(msg.getTimestamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				entLang.setDate(yyyyMMdd);
				if (ArgUtil.is(entLang) && ArgUtil.is(entLang.getChannel()) && ArgUtil.is(entLang.getEvent())) {
					lstEventSummary.add(entLang);
				}
			}
		}

		Map<Object, Map<Object, Long>> eventWiseCountMap = lstEventSummary.stream().collect(Collectors.groupingBy(
				EventCountDto::getChannel, Collectors.groupingBy(EventCountDto::getEvent, Collectors.counting())));

		eventCountSummary.setEventWiseCountMap(eventWiseCountMap);

		eventCountSummary.setTenant(tnt);
		eventCountSummary.setMonth(monthYear);

		return eventCountSummary;

	}

	/** Agreegration Query to event msg status **/

	public List<MessageDoc> getLanguageCount(Object contactType, long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("form").exists(true));
		query.addCriteria(Criteria.where("form.reply_id").in("en", "ar"));
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		includeMsgFields(query);
		List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return msgDocLst;
	}

	public List<MessageDoc> getEventCount(Object contactType, long dateRange1, long dateRange2) {
		Query query = new Query();
		query.addCriteria(Criteria.where("form").exists(true));
		query.addCriteria(Criteria.where("form.reply_id").nin("en", "ar"));
		query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
		includeMsgFieldsEvent(query);
		List<MessageDoc> msgDocLst = mongoTemplate.find(query, MessageDoc.class, contactType.toString());
		return msgDocLst;
	}

	public void includeMsgFields(Query query) {
		query.fields().include("form.reply_title").include("contactId").include("contact.contactType");
	}

	public void includeMsgFieldsEvent(Query query) {
		query.fields().include("form.reply_title").include("contactId").include("contact.contactType")
				.include("timestamp");
	}
	
	public List<String> getChannelShortCode(List<String> lstofChann){
		List<String> lstofChanelShotCode = new ArrayList<>();
		
		for(String str:lstofChann) {
			if (str.contains(ContactType.WHATSAPP.name())) {
				lstofChanelShotCode.add("wa") ;
			} else if (str.contains(ContactType.FACEBOOK.name())) {
				lstofChanelShotCode.add("fb");
			} else if (str.contains(ContactType.TWITTER.name())) {
				lstofChanelShotCode.add("tw");
			} else if (str.contains(ContactType.TELEGRAM.name())) {
				lstofChanelShotCode.add("tg");
			} else if (str.contains(ContactType.INSTAGRAM.name())) {
				lstofChanelShotCode.add("ig");
			} else if (str.contains(ContactType.WEBSITE.name())) {
				lstofChanelShotCode.add("web");
			}
		}
		
		return lstofChanelShotCode;
	}
	
	/** day wise event count summary **/


	public AdminAgentAccountDto getAdminAgentSummary() {
		String tnt = AppContextUtil.getTenant();
		AdminAgentAccountDto dto = new AdminAgentAccountDto();
		List<AdminAgentAccountDto> adminAgentLstAccountDtos = new ArrayList<>();
		List<AdminAgentAccount> adminAgentAccount= new ArrayList<>();
		
		
		
		List<DomainDoc> domainDocLst =getAllDomainAccount();
			List<AgentDoc> agentDocLst=agentStore.findAllAgents(false);
			
		
		for(AgentDoc agent:agentDocLst) {
			AdminAgentAccount dtoa = new AdminAgentAccount();
			dtoa.setAgent_name(agent.getAgent_name());
			dtoa.setAgent_code(agent.getAgent_code());
			dtoa.setAgent_email(agent.getAgent_email());
			dtoa.setAgent_number(agent.getAgent_number());
			dtoa.setAdmin(agent.isAdmin());
			adminAgentAccount.add(dtoa);
		}
		dto.setDomain(tnt);
		dto.setAdminAgentAccountDtls(adminAgentAccount);
		//}
		
		return dto;
	}
	
	
	public Map<Object, Map<Object, Long>> removeSandBoxNumber(Map<Object, Map<Object, Long>> hourWiseCount) {
		if (hourWiseCount!=null && !hourWiseCount.isEmpty()) {
		// Remove entries with keys containing "wa_" and no characters after "wa_"
        Iterator<Map.Entry<Object, Map<Object, Long>>> iterator = hourWiseCount.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Map<Object, Long>> entry = iterator.next();
            if (entry.getKey().toString().matches(".*wa_\\b")) {
                iterator.remove();
            }
        }
		}
       return hourWiseCount;
	}
	
	
	/** api for counting mediaTemp **/
	public Map<String,Long> getMediaTemplateCountV1(long monthMinTimeStamp,long monthMaxTimeStamp ) {
			
		// Match operation to filter based on format, timestamp range, and non-null mediaTemplate
	        MatchOperation matchOperation = Aggregation.match(
	                Criteria.where("msg.lastOutBoundMsg.options.attachment.mediaTemplate").ne(null)
	                        .and("msg.lastMsg.options.waba.components.format").is("IMAGE")
	                        .and("msg.lastMsg.timestamp").gte(monthMinTimeStamp).lte(monthMaxTimeStamp)
	        );

	        // Group operation to group by categoryType and count total documents
	        GroupOperation groupOperation = Aggregation.group("msg.lastMsg.meta.categoryType")
	                .count().as("totalDocuments");

	        // Create aggregation pipeline
	        Aggregation aggregation = Aggregation.newAggregation(
	                matchOperation,   // Apply the match operation
	                groupOperation    // Apply the group operation
	        );

	        // Execute the aggregation query
	        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "CHAT_SESSION", Document.class);

	        // Initialize a Map to store the result
	        Map<String, Long> categoryCountMap = new HashMap<>();

	        // Iterate over the results and populate the map
	        for (Document document : results.getMappedResults()) {
	            String categoryType = document.getString("_id");  // _id contains the categoryType
	            Long count = Long.valueOf(document.getInteger("totalDocuments").longValue());  // totalDocuments contains the count 
	            categoryCountMap.put(categoryType, count);
	        }
	        // List of all expected categories
	        List<String> expectedKeys = Arrays.asList("MARKETING", "UTILITY", "SERVICE", "AUTHENTICATION");
	        
	        expectedKeys.forEach(key -> categoryCountMap.putIfAbsent(key, Long.valueOf(0)));

	        // Return the map
	        return categoryCountMap;
		}

	/** waba analytics cost api*/
 public WabaBalanceDto getWabaCostAnalyticsV1(long timestamp) {
		 
		 WabaBalanceDto wDto=new WabaBalanceDto();
		 List<WabaDateWiseBalanceDto> lstList = new ArrayList<>();
		 DecimalFormat df = new DecimalFormat("####0.000");
		 
		 List<DomainDoc> domains=getAllDomainAccount();
		 if(!ArgUtil.is(domains)) {
			 DomainDoc doc =new DomainDoc();
			 doc.setDomain(AppContextUtil.getTenant());
			 domains.add(doc);
		 }
		 for(DomainDoc domDoc:domains) {
			// lstList = new ArrayList<>();
		 List<WabaAnalyticsDoc>  chDocs=getListChannelCongigFowWaV1(domDoc.getDomain());
		 if(ArgUtil.is(chDocs)) {
		 for(WabaAnalyticsDoc chdoc:chDocs) {
			// Get the month, start, and end timestamp using your DateUtil utility
	        String month = CommonUtils.monthNameByTimestamp(timestamp);
	        long startTStamp = CommonUtils.startTStampForaMonthV1(timestamp);
	        long endTStamp = CommonUtils.endTStampForaMonthV1(timestamp);
	        if(ArgUtil.is(chdoc.getWabaId())) {
	        String wabaId =chdoc.getWabaId();
	        String number =chdoc.getNumber();
	        String tnt=chdoc.getTenant();
	        
	        WabaDateWiseBalanceDto dto = new WabaDateWiseBalanceDto();
	        Integer totalConvCnt=0;
	        Double totalConvCost=0.0;
	       
	        
	        // Define the aggregation pipeline
	        Aggregation aggregation = Aggregation.newAggregation(
	            // $match stage to filter by wabaId, start, and end
	            Aggregation.match(Criteria.where("wabaId").is(wabaId)
	            	.and("number").is(number)
	            	.and("tenant").is(tnt)
	                .and("start").gte(startTStamp)
	                .and("end").lte(endTStamp)),

	            // $group stage to group by conversation_type and conversation_category
	            Aggregation.group("conversation_type", "conversation_category")
	                .sum("conversation").as("totalConversations")
	                .sum("cost").as("totalCost"),

	            // $group again by conversation_type to restructure the output
	            Aggregation.group("_id.conversation_type")
	                .push(new Document("category", "$_id.conversation_category")
	                    .append("totalConversations", "$totalConversations")
	                    .append("totalCost", "$totalCost"))
	                .as("conversationCategory")
	        )
	        .withOptions(AggregationOptions.builder().allowDiskUse(true).build());

	        // Execute the aggregation
	        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "TP_WABA_ANALYTICS", Document.class);

	        // Convert the results to List<Map<String, Object>>
	        List<Map<String, Object>> resultList = new ArrayList<>();
	        for (Document doc : results) {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("conversation_type", doc.getString("_id"));

	            // Manually convert the conversationCategory array into a map in Java
	            List<Document> conversationCategoryList = (List<Document>)doc.get("conversationCategory");
	            Map<String, Map<String, Object>> categoryMap = new HashMap<>();

	            for (Document categoryDoc : conversationCategoryList) {
	                String category = categoryDoc.getString("category");
	                Map<String, Object> valuesMap = new HashMap<>();
	                totalConvCnt+=(Integer)categoryDoc.get("totalConversations");
	                
	                Object totalCostObj = categoryDoc.get("totalCost");
	                
	                if (totalCostObj instanceof Number) {
	                	totalConvCost += ((Number) totalCostObj).doubleValue();
	                }
	                totalConvCost =Double.valueOf(df.format(totalConvCost));
	                
	                valuesMap.put("totalConversations", categoryDoc.get("totalConversations"));
	                valuesMap.put("totalCost", categoryDoc.get("totalCost"));
	               
	                categoryMap.put(category, valuesMap);
	            }
	            resultMap.put("conversationCategory", categoryMap);

	            resultList.add(resultMap);
	        }
	         
	        
//	     // Expected keys
	        List<String> expectedKeys = Arrays.asList("MARKETING", "UTILITY", "SERVICE", "AUTHENTICATION");

	        // Iterate through countCostMap
	        for (Map<String, Object> entry : resultList) {
	            String conversationType = (String) entry.get("conversation_type");
	            Map<String, Object> conversationCategory = (Map<String, Object>) entry.get("conversationCategory");

	            // Check for missing keys
	            for (String key : expectedKeys) {
	                if (!conversationCategory.containsKey(key)) {
	                	 // Add the missing key with default values using HashMap
	                    Map<String, Object> defaultValues = new HashMap<>();
	                    defaultValues.put("totalConversations", 0);  // Default value
	                    defaultValues.put("totalCost", 0.0);         // Default value
	                    conversationCategory.put(key, defaultValues);
	                }
	            }
	        }
	        
	        
	        dto.setCountCostMap(resultList);
	        dto.setMonth(month);
			dto.setDateTimeStamp(timestamp);
			dto.setWabaId(wabaId);
			dto.setNumber(number);
			WabaAccountBalanceDoc waAccBal=null;
			double deposiTamt=0.0;
			if(ArgUtil.is(wabaId)) {
			 waAccBal=getAccountBalance(wabaId,tnt,number);
			if(ArgUtil.is(waAccBal)) {
				deposiTamt=waAccBal.getDepositAmt();
				dto.setCurrencyCode(waAccBal.getCurrencyCode());
				dto.setId(waAccBal.getId());
				dto.setDepostAmt(deposiTamt);
				}
			
			}
			dto.setTotalCount(totalConvCnt);
			dto.setTotalCost(totalConvCost);
			if(ArgUtil.is(dto.getDepostAmt())) {
				dto.setBalanceAmt(deposiTamt-totalConvCost);
			}
			
			dto.setTnt(ArgUtil.parseAsString(tnt,AppContextUtil.getTenant()));
			
			
			lstList.add(dto);
	        }
		 
		 }
		 }
		 wDto.setDateWiseBaL(lstList);
		 }
		 
			
			wDto.setDateWiseBaL(lstList);
			
	        return wDto; 
	    }

	
 public WabaAccountBalanceDoc getAccountBalance(String wabaId,String tenant,String number) {
		List<WabaAccountBalanceDoc> docLst = null; 
		WabaAccountBalanceDoc doc=null;
		Query query=new Query();
		if(ArgUtil.is(wabaId)) {
			query.addCriteria(Criteria.where("wabaId").is(wabaId).and("tenant").is(tenant).and("number").is(number));
			docLst =mongoTemplate.find(query, WabaAccountBalanceDoc.class);
			if(ArgUtil.is(docLst)) {
				doc=docLst.get(0);
			}
		}
		return doc;
	}
 
 public List<ChannelConfigDoc> getListChannelCongigFowWa(String domain) {
		Query query = new Query();
		query.addCriteria(Criteria.where("domain").is(domain).and("isDisabled").is(false).and("contactType").is(ContactType.WHATSAPP.name()));
		query.fields().include("domain").include("wacfb.number").include("wacfb.wabaId").include("contactType").include("isDisabled");
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		return cofigDocLst;
	}
	
 
 public List<WabaAnalyticsDoc> getListChannelCongigFowWaV1(String domain) {
	 List<WabaAnalyticsDoc> cofigDocLst =new ArrayList<>();
		Query query = new Query();
		query.addCriteria(Criteria.where("tenant").is(domain));
		query.fields().include("tenant").include("number").include("wabaId").include("contactType").include("isDisabled");

		Aggregation aggregation = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("tenant").is(domain)), // Add filter for tenant
			    Aggregation.group("tenant", "wabaId", "number") // Group by tenant, wabaId, and number
			        .first(Aggregation.ROOT).as("uniqueRecord"), // Select the first document as representative
			    Aggregation.replaceRoot("uniqueRecord") // Return the unique records as the root
			);

			List<Document> uniqueRecords = mongoTemplate.aggregate(aggregation, "TP_WABA_ANALYTICS", Document.class).getMappedResults();
			// Process the unique records
			for(Document doc:uniqueRecords) {
				WabaAnalyticsDoc wadoc =new WabaAnalyticsDoc();
				wadoc.setTenant(doc.getString("tenant"));
				wadoc.setWabaId(doc.getString("wabaId"));
				wadoc.setNumber(doc.getString("number"));
				cofigDocLst.add(wadoc);
			}
		return cofigDocLst;
	}
	

	
}
