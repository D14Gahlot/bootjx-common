package com.boot.jx.admin.manager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.WabaBalanceDto;
import com.boot.jx.admin.dto.WabaDateWiseBalanceDto;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.WabaAccountBalanceDoc;
import com.boot.jx.postman.doc.WabaAnalyticsDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.DateUtil;
import com.boot.utils.JsonUtil;

@Component
public class WabaAccountManeger extends CommonMongoTemplateAbstract<WabaAccountManeger> {
	
	@Autowired
	CommonMongoTemplate commonMongoTemplate;
	
	@Autowired(required = false)
	protected AuditDetailProvider auditDetailProvider;


	public WabaAccountBalanceDoc addEditAccountBalance(WabaAccountBalanceDoc reqDto) {
		WabaAccountBalanceDoc doc = new WabaAccountBalanceDoc(); 
		if(ArgUtil.is(reqDto.getId())){
			doc = commonMongoTemplate.findByIdString(reqDto.getId(), WabaAccountBalanceDoc.class);
			doc.setDepositAmt(reqDto.getDepositAmt());
			doc.setCurrencyCode(reqDto.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setUpdated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			commonMongoTemplate.save(doc);
		}else {
			doc.setDepositAmt(reqDto.getDepositAmt());
			doc.setCurrencyCode(reqDto.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setCreated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			commonMongoTemplate.save(doc);
		}
		return doc;
	}
	

	
	public WabaDateWiseBalanceDto getWabaCostAnalytics(long timestamp) {
		WabaDateWiseBalanceDto dto=new WabaDateWiseBalanceDto();
		
		String month=DateUtil.monthNameByTimestamp(timestamp);
		long startTStamp=DateUtil.startTStampForaMonth(timestamp);
		long endTStamp=DateUtil.endTStampForaMonth(timestamp);
		
		Query query = new Query();
		query.addCriteria(Criteria.where("start").gt(startTStamp).and("end").lt(endTStamp));
		List<WabaAnalyticsDoc> wabaAnaLst=commonMongoTemplate.find(query,WabaAnalyticsDoc.class);
		Map<String, Long> countMap=new HashMap<>();
		Map<String, Double> costMap=new HashMap<>();
		long totalCount=0l;
		double totalCost=0;
		DecimalFormat df = new DecimalFormat("####0.000");
		
		if(ArgUtil.isNotEmpty(wabaAnaLst)) {
			for(WabaAnalyticsDoc doc:wabaAnaLst) {
				dto.setWabaId(doc.getWabaId());
				dto.setNumber(doc.getNumber());
				String category=doc.getConversation_category();
				long count=doc.getConversation();
				totalCount+=count;
				double cost =doc.getCost();
				
				cost = Double.valueOf(df.format(cost));
				totalCost+=cost;
				if(countMap.containsKey(category)) {
					long sum =countMap.get(category);
					countMap.put(category, sum+count);
				}else {
					countMap.put(category, count);
				}
				
				
				if(costMap.containsKey(category)) {
					double sum =costMap.get(category);
					sum = Double.valueOf(df.format(sum));
					costMap.put(category, sum+cost);
				}else {
					costMap.put(category, cost);
				}
			}
			
		}
		
		 // List of all expected categories
        List<String> expectedKeys = Arrays.asList("MARKETING", "UTILITY", "SERVICE", "AUTHENTICATION");
        
        expectedKeys.forEach(key -> countMap.putIfAbsent(key, Long.valueOf(0)));
        
        expectedKeys.forEach(key -> costMap.putIfAbsent(key, Double.valueOf(0)));
        
		getWabaCostAnalyticsV1(timestamp);
		return dto;
	}
	
	
	 public WabaBalanceDto getWabaCostAnalyticsV1(long timestamp) {
		 
		 WabaBalanceDto wDto=new WabaBalanceDto();
		 List<WabaDateWiseBalanceDto> lstList = new ArrayList<>();
		 DecimalFormat df = new DecimalFormat("####0.000");
		 
		 List<ChannelConfigDoc>  chDocs=getListChannelCongigFowWa();
		 for(ChannelConfigDoc chdoc:chDocs) {
			System.out.println("chdoc JSON {--}"+JsonUtil.toJson(chdoc));
	        // Get the month, start, and end timestamp using your DateUtil utility
	        String month = DateUtil.monthNameByTimestamp(timestamp);
	        long startTStamp = DateUtil.startTStampForaMonth(timestamp);
	        long endTStamp = DateUtil.endTStampForaMonth(timestamp);
	        if(ArgUtil.is(chdoc.getWacfb())) {
	        String wabaId =chdoc.getWacfb().getWabaId();//"430589913462237";
	        String number =chdoc.getWacfb().getNumber();
	        String tnt=chdoc.getDomain();
	        
	        
	        WabaDateWiseBalanceDto dto = new WabaDateWiseBalanceDto();
	        Integer totalConvCnt=0;
	        Double totalConvCost=0.0;
	       
	        
	        // Define the aggregation pipeline
	        Aggregation aggregation = Aggregation.newAggregation(
	            // $match stage to filter by wabaId, start, and end
	            Aggregation.match(Criteria.where("wabaId").is(wabaId)
	                .and("start").gt(startTStamp)
	                .and("end").lt(endTStamp)),

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
	        AggregationResults<Document> results = commonMongoTemplate.aggregate(aggregation, "TP_WABA_ANALYTICS", Document.class);

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
			if(ArgUtil.is(wabaId)) {
			 waAccBal=getAccountBalance(wabaId);
			}
			double deposiTamt =0.0;
			if(ArgUtil.is(waAccBal)) { 
				deposiTamt=waAccBal.getDepositAmt();
			}
			dto.setDepostAmt(deposiTamt);
			dto.setTotalCount(totalConvCnt);
			dto.setTotalCost(totalConvCost);
			dto.setBalanceAmt(deposiTamt-totalConvCost);
			dto.setTnt(ArgUtil.parseAsString(tnt,AppContextUtil.getTenant()));
			lstList.add(dto);
			
		 }
		 }
		wDto.setDateWiseBaL(lstList);
			
			
	        return wDto; // Return the final list of maps
	    }

	

	public WabaBalanceDto fetchWabaAccountBalance(long timestamp) {
		WabaDateWiseBalanceDto dto =getWabaCostAnalytics(timestamp);
		
		List<WabaAccountBalanceDoc> doc = null; 
		Query query=new Query();
		if(ArgUtil.is(dto)) {
		query.addCriteria(Criteria.where("wabaId").is(dto.getWabaId()));
		doc = commonMongoTemplate.find(query, WabaAccountBalanceDoc.class);
		}
		double balanceAmt =0.0;
		double depostAmt=0.0;
		if(ArgUtil.isNotEmpty(doc)) {
		 depostAmt =doc.get(0).getDepositAmt();
		 balanceAmt = depostAmt-dto.getTotalCost();
		}
		
		WabaBalanceDto wDto=new WabaBalanceDto();
		// TODO Auto-generated method stub
		wDto =getWabaCostAnalyticsV1(timestamp);
		return wDto;
	}
	
	
	public WabaAccountBalanceDoc getAccountBalance(String wabaId) {
		WabaAccountBalanceDoc doc = null; 
		MongoQueryBuilder<WabaAccountBalanceDoc> qb=null;
		Query query=new Query();
		if(ArgUtil.is(wabaId)) {
			//query.addCriteria(Criteria.where("wabaId").is(wabaId));
			 qb = CommonMongoQueryBuilder.collection(WabaAccountBalanceDoc.class)
					.where(Criteria.where("wabaId").is(wabaId));
			//doc = commonMongoTemplate.find(query, WabaAccountBalanceDoc.class);
		}
		
		return findOne(qb);
	}
	
	public List<ChannelConfigDoc> getListChannelCongigFowWa() {
		Query query = new Query();
		query.addCriteria(Criteria.where("isDisabled").is(false).and("contactType").is(ContactType.WHATSAPP.name()));
		query.fields().include("domain").include("wacfb.number").include("wacfb.wabaId").include("contactType").include("isDisabled");
		List<ChannelConfigDoc> cofigDocLst = mongoTemplate.find(query, ChannelConfigDoc.class, "CONFIG_CHANNEL");
		return cofigDocLst;
	}

}
