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
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
import com.boot.utils.ArgUtil;

@Component
public class AgentAnalyticsManager {
	public static final String CHAT_SESSION = "CHAT_SESSION";
	
	public static final String DEFAULT_AGENT = "TEAM";
	public static final int OPEN_CONV_HR_LMT =5; 
	
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	AdminDashBoardManager adminDbMgr;
	
	
	


	public  List<DashBoardResponseDto> getAgentWiseAnalytics(DashBoardRequestDto req) {
 		 List<DashBoardResponseDto> lstDto = new ArrayList<>();
		 DashBoardResponseDto dto = null;
		 List<ChatSessionDoc> allAgent =null;
		 long date1=0;
		 long date2=0;
		 if(ArgUtil.is(req.getDateRange1()) && req.getDateRange1()>0) {
				date1 =req.getDateRange1();
			}else {
				date1 =todayStartTime();
			}
			if(ArgUtil.is(req.getDateReange2()) && req.getDateReange2()>0) {
				date2 =req.getDateReange2();
			}else {
				date2 =todayEndTime();
			}
			
		
		 if(req!=null && (ArgUtil.isEmptyString(req.getAgent()) || req.getAgent().equalsIgnoreCase(DEFAULT_AGENT))) {
			 allAgent = getAgentList(); 
		 }
			 
		 if(allAgent !=null && !allAgent.isEmpty()) {
			 for(Object chatSess : allAgent) {
				 dto = new DashBoardResponseDto();
				String agent=(String)chatSess;
				 dto = getAgentAnalytics(agent,date1,date2);
				lstDto.add(dto);
			 }
		 }else {
			 dto = getAgentAnalytics(req.getAgent(),date1,date2);
			 lstDto.add(dto);
		 }
		return lstDto;
	}
	
	
	public DashBoardResponseDto getSummery(List<DashBoardResponseDto> dtoLst) {
		DashBoardResponseDto dto = new DashBoardResponseDto();
		dto.setAgentName(DEFAULT_AGENT);
		long totalInMsg =0;
		long totalOutMsg =0;
		long totalMsg =0;
		long totalUniqCon =0;
		long totalOpenMsg =0;
		long convDuration=0;
		double totalStartLag=0.0d;
		int teamSize = dtoLst.size();
		Map<Object,Object> graphApiMap = new HashMap<Object,Object>(); 
		
		for (DashBoardResponseDto dt : dtoLst) {
			totalInMsg +=dt.getTotalInMsgExchanged();
			totalOutMsg+=dt.getTotalOutMsgExchanged();
			totalMsg+=dt.getTotalMsgExchanged();
			totalOpenMsg+=dt.getOpenConversation();
			convDuration+=dt.getConverDuration();
			totalUniqCon+=dt.getUniqueConversation();
			totalStartLag+=dt.getStartLag();
			dto.setLeadMessanger(dt.getLeadMessanger());
			graphApiMap = mergerMapKyAndValue(graphApiMap, dt.getGraphApiDetails());
			
		}
		dto.setTotalInMsgExchanged(totalInMsg);
		dto.setTotalOutMsgExchanged(totalOutMsg);
		dto.setTotalMsgExchanged(totalMsg);
		dto.setOpenConversation(totalOpenMsg);
		dto.setUniqueConversation(totalUniqCon);
		dto.setConverDuration(convDuration/teamSize);
		dto.setStartLag(totalStartLag);
		dto.setGraphApiDetails(graphApiMap);
		return dto;
	}
	
	public DashBoardResponseDto getAgentAnalytics(String agent,long dateRange1,long dateRange2) {
		    DashBoardResponseDto dto = new  DashBoardResponseDto();
			dto.setAgentName(agent);
			/** Unique agent list  **/
			List<ChatSessionDoc> distinctContactLst = getUniqueAgentWiseContactList(agent,dateRange1,dateRange2);
			if(ArgUtil.is(distinctContactLst)) {
				dto.setUniqueConversation(distinctContactLst.size());
			}
			/** Total Msg exchanged. **/
			List<ChatSessionDoc> totalMsgExchanged =getAgentWiseTotalMsgExchanged(agent,dateRange1,dateRange2);
			if(ArgUtil.is(totalMsgExchanged)) {
				dto.setTotalMsgExchanged(totalMsgExchanged.size());
			}
			
			/** Open conversation **/
			List<ChatSessionDoc> openConvesLst = getAgentWiseOpenConversation(agent,dateRange1,dateRange2);
			if (ArgUtil.is(openConvesLst)) {
				dto.setOpenConversation(openConvesLst.size());
			}
			/** Peak Load **/
			PeakLoadDto peakLoadResult = getAgentPeakLoadMsgCount(agent,dateRange1,dateRange2);
			dto.setPeakLoad(peakLoadResult);
			
			/** lead Messanger **/
			LeadMessanger  leadMsg =getLeadMessenger(agent,dateRange1,dateRange2);
			dto.setLeadMessanger(leadMsg);
			
			/** Converation duration **/
			long conVerDuration = getConversationDuration(agent,dateRange1,dateRange2);
			if(ArgUtil.is(conVerDuration)) {
			dto.setConverDuration(conVerDuration);
			}
			/** startLag **/
			double startLag = getStartLag(agent,dateRange1,dateRange2);
			dto.setStartLag(startLag);
			
			/** find the date diff between two dates **/
			Map<String,Integer> dateDiffMAp = getDateDiff(dateRange1,dateRange2);
			int hour=0;
			int days=0;
			if(ArgUtil.is(dateDiffMAp)) {
				hour = dateDiffMAp.get("HOUR");
				days = dateDiffMAp.get("DAYS");
			}
			if(hour<=24) {
				Map<Object,Object> hourWiseCount = getHourWiseCount(totalMsgExchanged);
				dto.setGraphApiDetails(hourWiseCount);
			}else if(hour >24 && days<=30){
				Map<Object,Object> dateWiseCount = getDateWiseCount(totalMsgExchanged);
				dto.setGraphApiDetails(dateWiseCount);
			}else {
				Map<Object,Object> dweekWiseCount = getWeekWiseCount(totalMsgExchanged);
				dto.setGraphApiDetails(dweekWiseCount);
			}
			
		    return dto;
	}
	
	

	public List<ChatSessionDoc> getAgentList() {
		List<ChatSessionDoc> distinceAgentList = mongoTemplate.getCollection("CHAT_SESSION").distinct("assignedToAgent");
		return distinceAgentList;
	}
	
	
	public List<ChatSessionDoc> getUniqueAgentWiseContactList(String agent,long dateRange1, long dateRange2){
		
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		List<ChatSessionDoc> distinctIdList = mongoTemplate.getCollection(CHAT_SESSION).distinct("contactId",query.getQueryObject());
		return distinctIdList;
	}
	
	public List<ChatSessionDoc> getAgentWiseTotalMsgExchanged(String agent,long dateRange1, long dateRange2){
		
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		query.with(new Sort(new Order(Direction.ASC, "timestamp"))); 
		
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		return totalMsgDoc;
	}
	
	public List<ChatSessionDoc> getAgentWiseOpenConversation(String agent,long dateRange1, long dateRange2){
		List<ChatSessionDoc> totalOpenMsgDoc =new ArrayList<ChatSessionDoc>(); 
		long currentTimeStamp =System.currentTimeMillis();
		Query query = new Query();
		query.addCriteria(Criteria.where("assignedToAgent").is(agent).and("active").is(true));
		query.addCriteria(Criteria.where("assignedAgentStamp").gt(dateRange1).lt(dateRange2));
		List<ChatSessionDoc> totalMsgDoc = mongoTemplate.find(query, ChatSessionDoc.class, CHAT_SESSION);
		
		for(ChatSessionDoc chatDoc:totalMsgDoc) {
			long assignToAgent = chatDoc.getAssignedAgentStamp();
			long diffInMilliSeconds = currentTimeStamp-assignToAgent;
			int diffInHours = (int) (diffInMilliSeconds / (60 * 60 * 1000));
			if(diffInHours>OPEN_CONV_HR_LMT) {
				totalOpenMsgDoc.add(chatDoc);
			}
		}
		return totalOpenMsgDoc;
	}
	
	public long getConversationDuration(String agent,long startTime, long endTime){
		Map<String,Long> conVerMsgLst = new HashMap<String,Long>();
		Long maxEntryKeyValue =new Long(0);
		List<ChatSessionDoc> uniquContactIdLst =getUniqueAgentWiseContactList(agent,startTime,endTime);
		for(Object chatSession:uniquContactIdLst) {
			String conId = (String)chatSession;
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			long fistResponseStamp = chatSessionCon.getFistResponseStamp();
			long lastResponseStamp = chatSessionCon.getLastResponseStamp();
			Long converDuration = lastResponseStamp-fistResponseStamp;
			conVerMsgLst.put(conId, converDuration);
		}
		
		if(!conVerMsgLst.isEmpty() && ArgUtil.is(conVerMsgLst)) {
			 Object maxEntryKey = Collections.max(conVerMsgLst.entrySet(), Map.Entry.comparingByValue()).getKey();
	          maxEntryKeyValue =(Long)conVerMsgLst.get(maxEntryKey); 
		}
		
		return maxEntryKeyValue;
	}
	
	
	public double getStartLag(String agent,long dateRange1, long dateRange2){
		Map<String,Double> startLagMapLst = new HashMap<String,Double>();
		double startLag =0.0d;
		double percentageWithDecimal=0.0d;
		
		
	
		List<ChatSessionDoc> uniquContactIdLst =getUniqueAgentWiseContactList(agent,dateRange1,dateRange2);
		for(Object chatSession:uniquContactIdLst) {
			String conId = (String)chatSession;
			Query query = new Query();
			query.addCriteria(Criteria.where("contactId").is(conId));
			query.addCriteria(Criteria.where("assignedToAgent").is(agent));
			ChatSessionDoc chatSessionCon = mongoTemplate.findOne(query, ChatSessionDoc.class, CHAT_SESSION);
			long fistResponseStamp = chatSessionCon.getFistResponseStamp();
			long assignedDeptStamp = chatSessionCon.getAssignedDeptStamp();
			double converDuration = fistResponseStamp-assignedDeptStamp;
			Double diffInMin = (double) (converDuration / (60 * 1000));
			startLagMapLst.put(conId, diffInMin);
		}
		
		
		if(!startLagMapLst.isEmpty() && ArgUtil.is(startLagMapLst)) {
			 Object maxEntryKey = Collections.max(startLagMapLst.entrySet(), Map.Entry.comparingByValue()).getKey();
			 Double maxEntryKeyValue =startLagMapLst.get(maxEntryKey); 
			 startLag = maxEntryKeyValue;
			}
		
		
		return startLag;
	}
	
	
	public PeakLoadDto getAgentPeakLoadMsgCount(String agent, long startTime, long endTime) {
		 Aggregation agg = newAggregation(
				    match(Criteria.where("assignedAgentStamp").gt(startTime).lt(endTime)),
		            group("assignedAgentStamp").count().as("total"),
		            project("total").and("assignedAgentStamp").previousOperation(),
		            sort(Sort.Direction.DESC, "total","assignedAgentStamp")
		        );
		 	//Convert the aggregation result into a List
			 AggregationResults<PeakLoadDto> groupResults = mongoTemplate.aggregate(agg, CHAT_SESSION, PeakLoadDto.class);
			 PeakLoadDto peakLoadResult =null;
			 if(groupResults!=null && !groupResults.getMappedResults().isEmpty()) {
			  peakLoadResult = groupResults.getMappedResults().get(0);
			 }
			 return peakLoadResult;
		}
		
	
	/** fetch lead mesenger **/
	public LeadMessanger getLeadMessenger(Object contactype, long startTime, long endTime) {
		LeadMessanger leadMessanger = new LeadMessanger();
		double percentageWithDecimal=0.0;
		List<String> lst =adminDbMgr.getListOfContactType();
		Map<String,Integer> leasMsgLst = new HashMap<String,Integer>();
		for(String contactType: lst) {
			List<MessageDoc> msgDocLst = adminDbMgr.getTotalMsgCount(contactType, startTime, endTime);
			leasMsgLst.put(contactType, msgDocLst.size());
		}
		System.out.println("lead Msg :"+leasMsgLst.toString());
		if(ArgUtil.is(leasMsgLst)) {
		 Object maxEntryKey = Collections.max(leasMsgLst.entrySet(), Map.Entry.comparingByValue()).getKey();
         Integer maxEntryKeyValue =leasMsgLst.get(maxEntryKey); 
         Integer sumOfAllContactMsg = leasMsgLst.values().stream().mapToInt(i->i).sum();
         if(maxEntryKeyValue>0  && sumOfAllContactMsg >0) {
	         double percentage =((maxEntryKeyValue.doubleValue()/sumOfAllContactMsg.doubleValue())*100);
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
	
	public Map<Object,Object>  getHourWiseCount(List<ChatSessionDoc>  msgLst) {
		List<Integer> hourList = new ArrayList<Integer>();
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object,Object> mapLst = new HashMap<Object,Object>();
		for(ChatSessionDoc msg :msgLst) {
			 long timeStamp = msg.getAssignedAgentStamp();
			 Date date=new Date(timeStamp);  
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
		    System.out.println(key + ": " + Collections.frequency(hourList, key));
		}
		
		return mapLst;
	}
	/** date wise count **/
	public Map<Object,Object>  getDateWiseCount(List<ChatSessionDoc>  msgLst) {
		List<Object> dateWiseList = new ArrayList<Object>();
		Map<Object,Object> mapLst = new HashMap<Object,Object>();
		for(ChatSessionDoc msg :msgLst) {
			 long timeStamp = msg.getAssignedAgentStamp();
			 Date date=new Date(timeStamp);
	         String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
	         dateWiseList.add(ddMMyyyyFormat);
		}
		
		//Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(dateWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(dateWiseList, key));
		    System.out.println(key + ": " + Collections.frequency(dateWiseList, key));
		}
		
		return mapLst;
	}
	
	/** week wise count **/
	public Map<Object,Object>  getWeekWiseCount(List<ChatSessionDoc>  msgLst) {
		List<Object> weekWiseList = new ArrayList<Object>();
		Map<Object,Object> mapLst = new HashMap<Object,Object>();
		 Calendar cal = Calendar.getInstance();
		for(ChatSessionDoc msg :msgLst) {
			 long timeStamp = msg.getAssignedAgentStamp();
			 Date date=new Date(timeStamp);
	         String ddMMyyyyFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
	         cal.setTime(date);
	         String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).toUpperCase();
	         int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);
	         String str = month+" (WEEK) "+weekOfMonth;
	          weekWiseList.add(str);
		}
		
		//Datewise count
		Set<Object> dateWiseCount = new HashSet<Object>(weekWiseList);
		for (Object key : dateWiseCount) {
			mapLst.put(key, Collections.frequency(weekWiseList, key));
		    System.out.println(key + ": " + Collections.frequency(weekWiseList, key));
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
	
public Map<String,Integer> getDateDiff(long date1,long date2){
	
		
		   Map<String,Integer> dateDiffMap =new HashMap<String,Integer>();
		   // For thousand separator
	       DecimalFormat decimalFormatter = new DecimalFormat("###,###");
	       long diffInMilliSeconds =date2 -date1;
		
		   int diffInMin = (int) (diffInMilliSeconds / (60 * 1000));
	       System.out.println("difference in minutes: " + decimalFormatter.format(diffInMin));

	       int diffInHours = (int) (diffInMilliSeconds / (60 * 60 * 1000));

	       int diffInDays = (int) (diffInMilliSeconds / (24 * 60 * 60 * 1000));
	       
	       dateDiffMap.put("MINUTE", diffInMin);
	       dateDiffMap.put("HOUR", diffInHours);
	       dateDiffMap.put("DAYS", diffInDays);
	       
	       return dateDiffMap;
	}
public Map<Object, Object> mergerMapKyAndValue(Map<Object, Object> mergeMap,Map<Object, Object> map2){
	Map<Object, Object> mergeValue =mergeMap;
	   //Merge maps
	   map2.forEach(
	       (key, value) -> mergeValue.merge( key, value, (v1, v2) -> v1==v2 ? v1 : (Integer)v1 + (Integer)v2)
	   );
	return mergeValue;
}
	
}
