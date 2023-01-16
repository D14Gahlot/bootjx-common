package com.boot.jx.account.manager;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.boot.jx.account.dto.ContactTypeCountDto;
import com.boot.jx.account.dto.ContactTypeSummaryDto;
import com.boot.jx.account.dto.DateWiseHourCountDto;
import com.boot.jx.account.dto.MonthDtlsDto;
import com.boot.jx.account.dto.SummaryDocDto;
import com.boot.jx.account.dto.TimeZoneOfSet;
import com.boot.jx.account.dto.TypeCount;
import com.boot.jx.account.dto.WabaSummaryDocDto;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.tpo.WABAConversation;
import com.boot.jx.postman.model.Message;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.DateUtil;
import com.boot.utils.JsonUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Component
public class AccountDashBoardManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDashBoardManager.class);
	@Autowired
	CommonMongoTemplate mongoTemplate;

	@Autowired
	private DomainSummaryMetaStore domSumMetaStore;
	
	@Autowired
	private AccountStore accountStore;
	
	@Autowired
	private PMEnvironment environment;


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
		List<Long> msgDocLst = mongoTemplate.distinctValues("CHAT_SESSION", "startSessionStamp", Long.class);
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

			MongoCollection<Document> col = mongoTemplate.getCollection(contactType);
			MongoCursor<Document> cursor = col.aggregate(list).iterator();
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
			return tenant + "_" + dto.getDate() + "_" + "wa"+"_"+dto.getLane();
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
			return tenant + "_" + "wa"+"_"+dto.getLane();
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
					String min = sdfm.format(date);

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

		for (Map.Entry<String, Map<Object, Long>> keyValue : hourWiseCountMap.entrySet()) {
			Map<Object, Long> hoCntMapAll = new HashMap<>();
			String key = keyValue.getKey();
			if (ArgUtil.is(key)) {
				for (String channel : channelLst) {
					if (!key.contains(channel)) {
						hourWiseCount.put(tnt + "_" + channel, hourCntMap);
					}
				}
				Map<Object, Long> hoCntMap = hourWiseCountMap.get(key);

				for (Map.Entry<Object, Long> keyValueCount : hourCntMap.entrySet()) {
					Object keydt = keyValueCount.getKey();
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
		if (hourWiseCount == null || hourWiseCount.isEmpty()) {
			for (String channel : channelLst) {
				hourWiseCount.put(tnt + "_" + channel, hourCntMap);
			}
		}

		hourWiseCount = sortMap(hourWiseCount);

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

		long offsetts= countryTimeZoneOffset(tnt);
		ZonedDateTime noOfdaysTstamp = null;
		long lasDayTimeStmp =0;

		String offsett= environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_TIMEZONE_OFFSET).asString();
		LOGGER.info("dayChannelWiseWisesummary dateRange1 :"+dateRange1+"\t dateRange2 :"+dateRange2 +"\t offsett :"+offsett);
		
		DomainDoc dDoc = getDomainTimeZone(tnt);
		String zone = getTimeZone(offsett==null?dDoc.getTimeZoneOffSet():offsett);
		String offset= getOffSet(offsett==null?dDoc.getTimeZoneOffSet():offsett);
		String[] hm = offset.split(":");
		
		int hr = ArgUtil.parseAsInteger(hm[0]);
		int mm = ArgUtil.parseAsInteger(hm[1]);
		
		if (ArgUtil.is(dateRange1)) {
			//lasDayTimeStmp = dateRange1+offsetts;
			lasDayTimeStmp=DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange1), hr, mm, zone, LocalTime.MIN);
			lasDayTimeStmp =lasDayTimeStmp+offsetts;
			
		}
		if (ArgUtil.is(dateRange2)) {
			//currentTs = dateRange2+offsetts;
			currentTs =DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs+offsetts;
		}
		
		

		if (lasDayTimeStmp==0  && days > 0) {
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
				long doctimestamp = doc.getTimestamp();
				///doctimestamp =doctimestamp+offsetts;  
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doctimestamp,
						DateUtil.YYYYMMDD_DATE_FORMAT);
				
				String lane = getLane(doc.getContactId());
				dto.setDate(yyyyMMdd);
				dto.setType(doc.getType());
				dto.setChannel(contactType.toString());
				dto.setMeta(doc.getMeta());
				dto.setLane(lane);
				dto.setDomain(tnt);
				String id = getSummaryId(dto);
				
				dto.setId(id);
				if (ArgUtil.is(dto.getId())) {
					lstSummDto.add(dto);
				}
				String channelid = getSummaryWithChannelId(dto);
				//System.out.println("contactType :"+contactType+"\t yyyyMMdd "+yyyyMMdd+"\t doctimestamp :"+doctimestamp+"\t channelid :"+channelid);
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
		Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));

		/** day wise count **/
		Map<String, Map<String, Long>> dayWiseCountMap = hourCntLst.stream()
				.collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel,
						Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.counting())));

		Map<Object, Map<Object, Long>> dayWiseMap = new HashMap<>();
		for (String channel : channelLst) {
		for (Map.Entry<String, Map<String, Long>> keyValue : dayWiseCountMap.entrySet()) {
			String key = keyValue.getKey();
			if (ArgUtil.is(key)) {
//				//for (String channel : channelLst) {
				String  tnt_channel=tnt + "_" + channel; 
				if (!key.contains(tnt_channel)) {
						dayWiseMap.put(tnt_channel, dateRanMap);
					}
				Map<Object, Long> dateWiseCnt = new HashMap<>();
				Map<String, Long> dayCntMap = dayWiseCountMap.get(key);
				// dateRanMap
				for (Map.Entry<Object, Long> keyValueCount : dateRanMap.entrySet()) {
					Object keydt = keyValueCount.getKey();
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
		}

		dayWiseMap = sortMap(dayWiseMap);

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
			tStamp = timeStamp + ((30-m) * 60 * 1000L);
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
		int m =Integer.parseInt(formattedHM);
		long currTimeStM=currentTStamp;
		long lastTimeStampWm=lastTimeStamp;
		/** for Upper round **/
		if(m>30) {
			m = 60-m;
			currTimeStM=currentTStamp+(m * 60 * 1000L);
			lastTimeStampWm = lastTimeStamp -((30-m) * 60 * 1000L);
		}else {
			lastTimeStampWm = lastTimeStamp -(m* 60 * 1000L);
			m = 30-m;
			currTimeStM=currentTStamp+(m * 60 * 1000L);
		}
		
	    //long currTimeStM=currentTStamp+(m * 60 * 1000L);
		//long lastTimeStampWm = lastTimeStamp +((m+30) * 60 * 1000L);
		
//		if(m>30) {
//			m = m-30;
//		}
//		long currTimeStM=currentTStamp-(m * 60 * 1000L);
	   // long lastTimeStampWm = lastTimeStamp -(lts * 60 * 1000L);
		lastTimeStampWm = (lastTimeStampWm - (lastTimeStampWm % (1000 * 60)));
		for (long lastTS = lastTimeStampWm; lastTS <=currTimeStM; lastTS = lastTS + DateUtil.MIN_30) {
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
			list = getAggregationMatchForMsgStatus(lasthrTimeStmp, currentTs);
//			DBCollection col = mongoTemplate.getCollection(contactType);
//			Cursor cursor = col.aggregate(list,
//					AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());

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
		
		
		long offsetts= countryTimeZoneOffset(tnt);
		
		String offsett= environment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_TIMEZONE_OFFSET).asString();
		LOGGER.info("dayChannelWiseWisesummary dateRange1 :"+dateRange1+"\t dateRange2 :"+dateRange2 +"\t offsett :"+offsett);
		
		DomainDoc doc = getDomainTimeZone(tnt);
		String zone = getTimeZone(offsett==null?doc.getTimeZoneOffSet():offsett);
		String offset= getOffSet(offsett==null?doc.getTimeZoneOffSet():offsett);
		String[] hm = offset.split(":");
		
		int hr = ArgUtil.parseAsInteger(hm[0]);
		int mm = ArgUtil.parseAsInteger(hm[1]);

		long currentTs = System.currentTimeMillis();
		
		ZonedDateTime noOfdaysTstamp = null;
		long lasDayTimeStmp =0;
		if (ArgUtil.is(dateRange1)) {
			//lasDayTimeStmp = dateRange1+offsetts;
			lasDayTimeStmp=DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange1), hr, mm, zone, LocalTime.MIN);
			lasDayTimeStmp = lasDayTimeStmp+offsetts;
		}
		if (ArgUtil.is(dateRange2)) {
			//currentTs = dateRange2+offsetts;
			currentTs =DateUtil.getDateMinAndMaxTime(getCovertDate(dateRange2), hr, mm, zone, LocalTime.MAX);
			currentTs = currentTs+offsetts;
		}
		if (lasDayTimeStmp==0  && days > 0) {
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
		// list.add(Aggregation.match(new Criteria("meta").is(metaQry))
		// .toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.match(new Criteria("meta.composeType").is("N")).toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.match(new Criteria("meta.sendType").is("PM")).toDocument(Aggregation.DEFAULT_CONTEXT));

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

	public ContactTypeSummaryDto getNonWhatsUpSummary(long dateRange1, long dateRange2) {

		List<String> lst = getListOfContactType();
		lst.remove("MESSAGE_WHATSAPP");
		lst.remove("MESSAGE_REJECTED");
		lst.remove("MESSAGE_QUEUED");
		lst.remove("MESSAGE_HOLD");

		String tnt = AppContextUtil.getTenant();
		List<SummaryDocDto> lstSummDto = new ArrayList<>();

		for (String contactType : lst) {
			LOGGER.info("contactType :" + contactType);
			Query query = new Query();
			query.addCriteria(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2));
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
				if (ArgUtil.is(dto.getId())) {
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

		Map<Object, Map<Object, Long>> datwWiseCount = uniqueList.stream().collect(Collectors.groupingBy(
				SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getDate, Collectors.counting())));

		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setDateWiseSummaryCount(datwWiseCount);

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
		query.addCriteria(Criteria.where("isDisabled").is(false));
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		for (ChannelConfigDoc cofigDoc : cofigDocLst) {
				 listOfChannelConfig.add(cofigDoc.getChannelType());
			}

		listOfChannelConfig = new ArrayList<>(new HashSet<>(listOfChannelConfig));

		return listOfChannelConfig;
	}
	
	public String getLane(String contactid) {
		String lane="";
		if(ArgUtil.is(contactid)) {
			String[] contactids =contactid.split("_");
			if(contactids!=null && contactids[1]!=null) {
				lane =contactids[1];
			}
		}
		return lane;
	}
	
	public Long countryTimeZoneOffset(String domain) {
		AppContextUtil.setTenant(Tenants.getDefault());
		DomainDoc domainDoc = accountStore.findDomainByName(domain);
		AppContextUtil.setTenant(domain);
		long offsettimestamp =0;
		String offset =null;
		if(ArgUtil.is(domainDoc)) {
			offset = domainDoc.getTimeZoneOffSet();
		}
		
		if(ArgUtil.is(offset)) {
			String hrStr = offset.substring(offset.indexOf('+')+1);
			String[] hrMin = hrStr.split(":");
			int hr =Integer.parseInt(hrMin[0]);
			int  min =Integer.parseInt(hrMin[1]); 
			offsettimestamp = hr*DateUtil.ONE_HR+min*DateUtil.MIN;
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
		if(ArgUtil.is(toffset)) {
			String[] hrStr =toffset.split("::");
			if(ArgUtil.is(hrStr)) {
				timeZone =hrStr[0]; 
			}
		}
		return timeZone;
	}
	
	public String getOffSet(String toffset) {
		String offset = "00:00";
		if(ArgUtil.is(toffset)) {
			String[] hrStr =toffset.split("::");
			if(ArgUtil.is(hrStr) && hrStr.length>1) {
				offset =hrStr[1].substring(hrStr[1].indexOf('+')+1);
			}
		}
		return offset;
	}
	
	
	
	public TimeZoneOfSet getTimeZoneOffset(){
		TimeZoneOfSet tzo = new TimeZoneOfSet();
		Map<String,String> hm = new HashMap<>();
		String[] ids = TimeZone.getAvailableIDs();
		for (String id : ids) {
		 TimeZone tz=TimeZone.getTimeZone(id);
		long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
		long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) 
                                  - TimeUnit.HOURS.toMinutes(hours);
		// avoid -4:-30 issue
		minutes = Math.abs(minutes);

		String key = null;
		String contry=null;
		String timezoffset=null;
		
		if (hours > 0) {
			contry = tz.getID();
			timezoffset =  String.format("GMT+%d:%02d",hours, minutes);
			key = contry+"::"+timezoffset;
			
			hm.put(key, contry);
		} else {
			contry = tz.getID();
			timezoffset =  String.format("GMT+%d:%02d",hours, minutes);
			key = contry+"::"+timezoffset;
			hm.put(key, contry);
		}
		
		}
		tzo.setTimeZmap(hm);
		
		return tzo;
	}
	
	public String getCovertDate(String date) {
		String dt = null;
		if(ArgUtil.is(date)){
			String[] dtStr = date.split("/");
			dt =dtStr[2]+"-"+dtStr[1]+"-"+dtStr[0]; 
		}
		return dt;
	}

}
