package com.boot.jx.account.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
		WabaAccountBalanceDoc oldDoc=null;
		if(ArgUtil.is(reqDto.getId())){
			doc = commonMongoTemplate.findByIdString(reqDto.getId(), WabaAccountBalanceDoc.class);
			 oldDoc=doc;
			 if(ArgUtil.is(doc.getOldVersions())) {
				 doc.oldVersion(oldDoc);
			 }else {
				 List<WabaAccountBalanceDoc> lstList=new ArrayList<>();
				 lstList.add(oldDoc);
				// doc.setOldVersions(lstList);
			 }
			doc.setTenant(reqDto.getTenant()==null?doc.getTenant():reqDto.getTenant());
			doc.setDepositAmt(doc.getDepositAmt()+reqDto.getDepositAmt());
			doc.setCurrencyCode(reqDto.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setWabaId(reqDto.getWabaId());
			doc.setUpdated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			commonMongoTemplate.save(doc);
		}else {
			doc.setWabaId(reqDto.getWabaId());
			doc.setTenant(reqDto.getTenant());
			doc.setDepositAmt(reqDto.getDepositAmt());
			doc.setCurrencyCode(reqDto.getCurrencyCode());
			doc.setTimeStamp(System.currentTimeMillis());
			doc.setCreated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			commonMongoTemplate.save(doc);
		}
		return doc;
	}
	
}
