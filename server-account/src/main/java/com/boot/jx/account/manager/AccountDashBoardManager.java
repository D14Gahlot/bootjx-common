package com.boot.jx.account.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.dto.AccountDashBoardRequestDto;
import com.boot.jx.account.dto.AccountDashBoardResponseDto;
import com.boot.jx.account.dto.TypeCount;
import com.boot.jx.account.dto.ContactTypeCountDto;
import com.boot.jx.account.dto.ContactTypeSummaryDto;
import com.boot.jx.postman.PMConstants;
import com.boot.utils.ArgUtil;
import com.boot.utils.DateUtil;
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

	
	
	public List<DomainDoc> getAllDomainAccount(){
		Query query = new Query();
		query.with(new Sort(new Order(Direction.DESC, "_id")));
		List<DomainDoc> domainDocLst =mongoTemplate.find(query,DomainDoc.class);
		return domainDocLst;
	}
	
	
	public Map<Object, Object> fetchUniqueMonth() {
		List<String> lst = getListOfContactType();
		 Map<Object, Object> map= new HashMap<Object, Object>();
		for(String contactType:lst) {
			Query query = new Query();
			query.with(new Sort(new Order(Direction.DESC, "timestamp")));
			query.fields().include("timestamp");
			List<Long> msgDocLst = mongoTemplate.getCollection(contactType.toString()).distinct("timestamp",query.getQueryObject());
			for(Long docTimeStamp :msgDocLst) {
				long timestamp=(docTimeStamp-(docTimeStamp%(DateUtil.ONEDAY))); 
				String monthStr = DateUtil.foramtTimeStampDateAsString(timestamp, null);
				if(!map.containsValue(monthStr)) {
					map.put(timestamp,monthStr);
				}
			}
		}
		return map;
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
	
	
	
	
}
