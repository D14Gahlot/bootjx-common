package com.boot.jx.account.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.boot.jx.postman.doc.MessageDoc;
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

@Component
public class AccountDashBoardManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDashBoardManager.class);
	@Autowired
	MongoTemplate mongoTemplate;
	
	 @Autowired
	 private DomainSummaryMetaStore domSumMetaStore;

	
	
	public List<DomainDoc> getAllDomainAccount(){
		Query query = new Query();
		query.with(new Sort(new Order(Direction.DESC, "_id")));
		List<DomainDoc> domainDocLst =mongoTemplate.find(query,DomainDoc.class);
		return domainDocLst;
	}
	
	@SuppressWarnings("unchecked")
	public List<MonthDtlsDto> fetchUniqueMonth() {
		
		Map<Long, Object> map= new HashMap<Long, Object>();
		Query query = new Query();
		query.with(new Sort(new Order(Direction.DESC, "startSessionStamp")));
		query.fields().include("startSessionStamp");
		List<Long> msgDocLst = mongoTemplate.getCollection("CHAT_SESSION").distinct("startSessionStamp",query.getQueryObject());
		List<MonthDtlsDto> listofMonth= new ArrayList<>();
		for(Long docTimeStamp :msgDocLst) {
			long timestamp=(docTimeStamp-(docTimeStamp%(DateUtil.ONEDAY))); 
			String monthStr = DateUtil.foramtTimeStampDateAsString(timestamp, null);
			if(!map.containsValue(monthStr)) {
				map.put(timestamp,monthStr);
			}
		}
		ArrayList<Long> sortedKeys= new ArrayList<Long>(map.keySet());
        Collections.sort(sortedKeys,Collections.reverseOrder());
       

     // Display the TreeMap which is naturally sorted
     for (Long  x : sortedKeys) {
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
	    long monthMinTimeStamp =DateUtil.getStartTimestamp(month,year).getTime();
	    long monthMaxTimeStamp =DateUtil.getEndTimestamp(month, year).getTime();
	    Map<Object,Long> summaryMap = new HashMap<>();
	    List<ContactTypeCountDto> summaryMsgLstCount =new ArrayList<ContactTypeCountDto>();
	   
	    Map<Object,List<ContactTypeCountDto>> map = new HashMap<>();
		for(String contactType:lst) {
		 List<ContactTypeCountDto> messageTypeLst =new ArrayList<ContactTypeCountDto>();
				List<DBObject> list = new ArrayList<DBObject>();
				//Match condtion 
				list.add(Aggregation.match(new Criteria("timestamp").gt(monthMinTimeStamp).lt(monthMaxTimeStamp)).toDBObject(Aggregation.DEFAULT_CONTEXT));
				list.add(Aggregation.group("type").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));

				DBCollection col = mongoTemplate.getCollection(contactType);
				Cursor cursor = col.aggregate(list,AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());
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
	    summaryMap =summaryMsgLstCount.stream().collect(Collectors.groupingBy(ContactTypeCountDto::getType,Collectors.summingLong(ContactTypeCountDto::getTotalCount)));
		
		dto.setSummaryCount(summaryMap);
	
		return dto;
		
}
	
	
	public void getMonths() {
		List<String> lstContactType = getListOfContactType();
		for(String contactType:lstContactType) {
			
		}
	}
	
	public AccountDashBoardResponseDto getAccountDashBoardDetails(AccountDashBoardRequestDto req) {
		AccountDashBoardResponseDto response = new AccountDashBoardResponseDto();
		long dateRange1=0;
		long dateRange2 =0;
		if(ArgUtil.is(req.getDateRange1())) {
			dateRange1 = req.getDateRange1();
		}else {
			dateRange1 = DateUtil.todayStartTime();
		}
		if(ArgUtil.is(req.getDateRange2())) {
			dateRange2 = req.getDateRange1();
		}else {
			dateRange2 = DateUtil.todayStartTime();
		}
		
		List<String> lstContactType = getListOfContactType();
		long totalInMsg=0;
		long totalOutMsg=0;
		
		for(String contactType:lstContactType) {
			getTypeWiseCount(contactType,dateRange1,dateRange2);
		}
		response.setTotalInMsgExchanged(totalInMsg);
		response.setTotalOutMsgExchanged(totalOutMsg);
		response.setTotalMsgExchanged(totalInMsg+totalOutMsg);
		
		return response;
	}
	
			
		
		public TypeCount getTypeWiseCount(Object contactType, long dateRange1, long dateRange2) {
			Aggregation agg = newAggregation(match(Criteria.where("timestamp").gt(dateRange1).lt(dateRange2)),
					group("type").count().as("count"), project("count").and("type").previousOperation(),
					sort(Sort.Direction.DESC, "count", "timestamp"));
			// Convert the aggregation result into a List
			AggregationResults<TypeCount> groupResults = mongoTemplate.aggregate(agg, contactType.toString(),TypeCount.class);
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
			//System.out.println("Year: " + cal.get(Calendar.YEAR) + "\t Month :" + cal.get(Calendar.MONTH));
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
					long timeStamp = doc.getTimestamp();
					String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(doc.getTimestamp(),
							DateUtil.YYYYMMDD_DATE_FORMAT);
					dto.setDate(yyyyMMdd);
					dto.setType(doc.getType());
					dto.setChannel(contactType.toString());
					dto.setMeta(doc.getMeta());
					dto.setDomain(tnt);
					String id = getSummaryId(dto);
					dto.setId(id);
					
					//LOGGER.info("datewaise data :" + JsonUtil.toJson(dto));
					if(ArgUtil.is(dto.getId())) {
					lstSummDto.add(dto);
					}
					
					Date date=new Date(timeStamp);
					SimpleDateFormat sdfH = new SimpleDateFormat("kk");
					String formattedDateH = sdfH.format(date);
				    hourListH.add(formattedDateH);
					hrDto.setDate(yyyyMMdd);
				    hrDto.setHour(formattedDateH);
					if(hrDto!=null) {
					hourCntLst.add(hrDto);
					}
					
				}

			}
		
			Map<Object, Long> summaryMap = new HashMap<>();
			Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));
		
			Map<Object,Map<Object,Object>> dateWiseCountMap = new HashMap<>();
			
			
			/** hour wise count **/
			Map<String, Map<String, Long>> hourWiseCountMap = hourCntLst.stream().collect(Collectors.groupingBy(DateWiseHourCountDto::getDate, Collectors.groupingBy(DateWiseHourCountDto::getHour, Collectors.counting())));
			
			for(Map.Entry<String, Map<String,Long>> keyValue:datwWiseCount.entrySet()) {
				String key =keyValue.getKey();
				Map<Object,Object> dateWiseCnt = new HashMap<>();
				for(Map.Entry<String, Long> keyValueCount:keyValue.getValue().entrySet() ) {
					String keyType = keyValueCount.getKey();
					Object count = keyValueCount.getValue();
					dateWiseCnt.put(keyType, count);
				}
				String[] keyId=key.split("_");
				dateWiseCnt.put("domain", ArgUtil.parseAsString(keyId[0], Constants.BLANK));
				dateWiseCnt.put("date", ArgUtil.parseAsString(keyId[1], Constants.BLANK));
				dateWiseCnt.put("channel",ArgUtil.parseAsString(keyId[2], Constants.BLANK));
				
				dateWiseCountMap.put(key, dateWiseCnt);
				
			}
			
			
			summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType,Collectors.counting()));
			
			// printing the count based on the designation and gender.
			//LOGGER.info("Group by on multiple properties" + datwWiseCount);

			ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
			dto.setTenant(tnt);
			dto.setMonth(monthYear);
			//dto.setDateWiseSummaryCount(datwWiseCount);
			dto.setDateWiseCountMap(dateWiseCountMap);
			dto.setSummaryCount(summaryMap);
			dto.setHourWiseCountMap(hourWiseCountMap);
			saveDomainSummary(dto);
			return dto;
		}

		

		public String getSummaryId(SummaryDocDto dto) {
			String tenant =dto.getDomain();
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
		
	public List<String> getListOfContactType() {
		List<String> listContactType = new ArrayList<String>();
		List<String> lstOfConRemo = new ArrayList<String>();
		lstOfConRemo.add("MESSAGE_LOGS");
		lstOfConRemo.add("MESSAGE_OTHERS");
		Set<String> contactTypeSet = mongoTemplate.getCollectionNames();
		if (ArgUtil.is(contactTypeSet)) {
			listContactType = contactTypeSet.stream().filter(x -> !x.isEmpty() && x.startsWith(PMConstants.COLLECTION_NAME)).collect(Collectors.toList());
		}
		if (!listContactType.isEmpty()) {
			listContactType.removeAll(lstOfConRemo);
		}
		return listContactType;
	}
	
	public void saveDomainSummary(ContactTypeSummaryDto dto) {
		DomainSummaryMessageDoc domSumMsgDoc =domSumMetaStore.findDomainAndByName(dto.getTenant(),dto.getMonth());
		if(ArgUtil.is(domSumMsgDoc)) {
			domSumMsgDoc.setDateWiseSummaryCount(dto.getDateWiseSummaryCount());
			domSumMsgDoc.setSummaryCount(dto.getSummaryCount());
			mongoTemplate.save(domSumMsgDoc);
			saveAndUpdateDomainSummaryMeta(dto);
		}else {
			domSumMsgDoc =new DomainSummaryMessageDoc();
			domSumMsgDoc.setDomain(dto.getTenant());
			domSumMsgDoc.setDate(dto.getMonth());
			domSumMsgDoc.setDateWiseSummaryCount(dto.getDateWiseSummaryCount());
			domSumMsgDoc.setSummaryCount(dto.getSummaryCount());
			mongoTemplate.save(domSumMsgDoc);
			saveAndUpdateDomainSummaryMeta(dto);
		}
				
				
		
				
	}
	
	public void saveAndUpdateDomainSummaryMeta(ContactTypeSummaryDto dto) {
		DomainSummaryMetaDoc metaSummDoc =domSumMetaStore.findDomainByName(dto.getTenant());
		if(ArgUtil.is(metaSummDoc)){
			metaSummDoc.setDomainUpdatedStamp(System.currentTimeMillis());
			mongoTemplate.save(metaSummDoc);
		}else {
			metaSummDoc =new DomainSummaryMetaDoc();
			metaSummDoc.setDomain(dto.getTenant());
			metaSummDoc.setTimeStamp(System.currentTimeMillis());
			metaSummDoc.setDomainUpdatedStamp(System.currentTimeMillis());
			mongoTemplate.save(metaSummDoc);
		}
	}
	
	/** WABA summary count **/
	
	public List<WabaSummaryDocDto> wabaSummary(long timestamp) {
		String tnt = AppContextUtil.getTenant();
		//List<String> lst = getListOfContactType();
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
			for(WABAConversation waba:wabaDocLst) {
				System.out.println("JSON :"+JsonUtil.toJson(waba));
				WabaSummaryDocDto dto = new WabaSummaryDocDto();
				String yyyyMMdd = DateUtil.foramtTimeStampDateAsString(waba.getCreated().getStamp(),
						DateUtil.YYYYMMDD_DATE_FORMAT);
				dto.setId(yyyyMMdd);
				dto.setDate(monthYear);
				if(waba.getMeta()!=null && waba.getMeta().get("to_country")!=null) {
				dto.setCountry(waba.getMeta().get("to_country").toString());
				}
				dto.setLane(waba.getContact().getLane());
				Map<String,Object> typeMap =(Map<String,Object>)waba.getConversation().get("origin"); 
				if(typeMap!=null && !typeMap.isEmpty()) {
				dto.setType(typeMap.get("type")==null?"":typeMap.get("type").toString());
				}
				dto.setChannel(waba.getContact().getContactType());
				dto.setDomain(tnt);
				dto.setPricing(waba.getPricing());
				wabaLst.add(dto);
			}
			
		return wabaLst;
	}
	
	
	public ContactTypeSummaryDto hourWisesummary(long timestamp,long hr) {
		String tnt = AppContextUtil.getTenant();
		List<String> lst = getListOfContactType();
		long currentTs=System.currentTimeMillis();
		
		long hour=0;
		if(hr>0) {
			hour =hr*60*60*1000;
		}else {
			hour =12*60*60*1000;
		}
	 long lasthrTimeStmp=currentTs-hour;
	 long curHr=getHour(currentTs);
	 long lastHr=getHour(lasthrTimeStmp);
	 System.out.println("curHr :"+curHr+"\t lastHr :"+lastHr);
	
		Calendar cal = Calendar.getInstance();
		Date dateTi =new Date();
		if(timestamp>0) {
			cal.setTimeInMillis(timestamp);
			dateTi =new Date(timestamp);
			}else {
				cal.setTimeInMillis(currentTs);
				dateTi =new Date(currentTs);
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
				String id = getSummaryId(dto);
				dto.setId(id);
				if(ArgUtil.is(dto.getId())) {
				lstSummDto.add(dto);
				}
				Date date=new Date(timeStamp);
				SimpleDateFormat sdfH = new SimpleDateFormat("kk");
				String formattedDateH = sdfH.format(date);
				hourListH.add(formattedDateH);
				hrDto.setDate(yyyyMMdd);
				hrDto.setHour(formattedDateH);
				hrDto.setChannel(id);
				if(hrDto!=null) {
					hourCntLst.add(hrDto);
				}
			}

		}
		Map<Object, Long> summaryMap = new HashMap<>();
		Map<String, Map<String, Long>> datwWiseCount = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getId, Collectors.groupingBy(SummaryDocDto::getType, Collectors.counting())));
	
		Map<Object,Map<Object,Object>> dateWiseCountMap = new HashMap<>();
		/** hour wise count **/
		Map<String, Map<String, Long>> hourWiseCountMap = hourCntLst.stream().collect(Collectors.groupingBy(DateWiseHourCountDto::getChannel, Collectors.groupingBy(DateWiseHourCountDto::getHour, Collectors.counting())));
		
		System.out.println("hourWiseCountMap :"+hourWiseCountMap);
		for(Map.Entry<String, Map<String,Long>> keyValue:datwWiseCount.entrySet()) {
			String key =keyValue.getKey();
			Map<Object,Object> dateWiseCnt = new HashMap<>();
			for(Map.Entry<String, Long> keyValueCount:keyValue.getValue().entrySet() ) {
				String keyType = keyValueCount.getKey();
				Object count = keyValueCount.getValue();
				dateWiseCnt.put(keyType, count);
			}
			String[] keyId=key.split("_");
			dateWiseCnt.put("domain", ArgUtil.parseAsString(keyId[0], Constants.BLANK));
			dateWiseCnt.put("date", ArgUtil.parseAsString(keyId[1], Constants.BLANK));
			dateWiseCnt.put("channel",ArgUtil.parseAsString(keyId[2], Constants.BLANK));
			dateWiseCountMap.put(key, dateWiseCnt);
			
		}
		summaryMap = lstSummDto.stream().collect(Collectors.groupingBy(SummaryDocDto::getType,Collectors.counting()));
		
		
		ContactTypeSummaryDto dto = new ContactTypeSummaryDto();
		dto.setTenant(tnt);
		dto.setMonth(monthYear);
		dto.setSummaryCount(summaryMap);
		dto.setHourWiseCountMap(hourWiseCountMap);
		return dto;
	}

	public long getHour(long timeStamp) {
		Date date= new Date(timeStamp);
		SimpleDateFormat sdfH = new SimpleDateFormat("kk");
		String formattedDateH = sdfH.format(date);
		System.out.println("formattedDateH :"+formattedDateH);
		return Long.parseLong(formattedDateH);
		
	}

	
}
