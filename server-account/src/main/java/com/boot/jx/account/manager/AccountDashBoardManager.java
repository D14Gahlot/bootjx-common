package com.boot.jx.account.manager;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import com.boot.jx.postman.PMConstants;
import com.boot.utils.ArgUtil;
import com.boot.utils.DateUtil;

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
			listContactType = contactTypeSet.stream().filter(x -> !x.isEmpty() && x.startsWith(PMConstants.COLLECTION_NAME))
					.collect(Collectors.toList());
		}
		if (!listContactType.isEmpty()) {
			listContactType.removeAll(lstOfConRemo);
		}
		return listContactType;
	}
	
	
	
	
}
