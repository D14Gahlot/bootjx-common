package com.boot.jx.account.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.WabaAccountBalanceDoc;
import com.boot.utils.ArgUtil;

@Component
public class WabaAccountManager extends CommonMongoTemplateAbstract<WabaAccountManager> {


	@Autowired
	CommonMongoTemplate commonMongoTemplate;
	
	
	public WabaAccountBalanceDoc addEditAccountBalance(WabaAccountBalanceDoc reqDto) {
		WabaAccountBalanceDoc doc = new WabaAccountBalanceDoc(); 
		
		if(ArgUtil.is(reqDto.getId())){
			doc = commonMongoTemplate.findByIdSafeCheck(reqDto.getId(), WabaAccountBalanceDoc.class);
			
			 if(ArgUtil.is(doc)) {
				 WabaAccountBalanceDoc oldDoc=getOldDoc(doc, reqDto);
				 if(ArgUtil.is(doc.getOldVersion())) {
					 List<WabaAccountBalanceDoc> lstList =doc.getOldVersion();
							 lstList.add(oldDoc);
							 doc.setOldVersion(lstList);
				 }else {
					 List<WabaAccountBalanceDoc> lstList=new ArrayList<>();
					 lstList.add(oldDoc);
					 doc.setOldVersion(lstList);
				 }
				

			doc.setTenant(reqDto.getTenant()==null?doc.getTenant():reqDto.getTenant());
			doc.setDepositAmt(doc.getDepositAmt()+reqDto.getDepositAmt());
			doc.setCurrencyCode(doc.getCurrencyCode()==null?reqDto.getCurrencyCode():doc.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setWabaId(reqDto.getWabaId());
			doc.setUpdated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			commonMongoTemplate.save(doc);
			 }
		}else {
			doc.setWabaId(reqDto.getWabaId());
			doc.setTenant(reqDto.getTenant());
			doc.setDepositAmt(reqDto.getDepositAmt());
			doc.setCurrencyCode(reqDto.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setCreated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			doc.setOldVersion(new ArrayList<>());
			commonMongoTemplate.save(doc);
		}
		return doc;
	}
	
	
	private WabaAccountBalanceDoc getOldDoc(WabaAccountBalanceDoc doc,WabaAccountBalanceDoc reqDto) {
		double d =0d;
		WabaAccountBalanceDoc oldDoc=new WabaAccountBalanceDoc();
		 oldDoc.setId(doc.getId());
		 oldDoc.setBalanceAmt(ArgUtil.parseAsDouble(doc.getBalanceAmt(),reqDto.getBalanceAmt()));
		 oldDoc.setCurrencyCode(doc.getCurrencyCode());
		 oldDoc.setDepositAmt(ArgUtil.parseAsDouble(doc.getDepositAmt(),reqDto.getDepositAmt()));
		 oldDoc.setCreated(doc.getCreated());
		 oldDoc.setUpdated(doc.getUpdated());
		 oldDoc.setTotalMsgCost(ArgUtil.parseAsDouble(doc.getTotalMsgCost(),reqDto.getTotalMsgCost()));
		 oldDoc.setTenant(doc.getTenant());
		 oldDoc.setWabaId(doc.getWabaId());
		 
		 return oldDoc;
	}
	
	
	
}
