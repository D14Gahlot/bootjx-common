package com.boot.jx.admin.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.dto.CustomerContactDto;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.JobsResponseDto;
import com.boot.jx.admin.manager.CustomerMasterFldMgr;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.CustomerMasterFieldDoc;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.doc.CustomerContactProfileDoc;
import com.boot.utils.ArgUtil;

@Service
public class CustomerProfileService {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	CustomerMasterFldMgr cmFieldMgr;
	
	public List<CustomerMasterFieldDto> addEditCustomerMastFields(CustomerMasterFieldDto req) {
		List<CustomerMasterFieldDto> lstCmfields = cmFieldMgr.addAndEditMasterfield(req);
		return lstCmfields;
	}

	public List<CustomerMasterFieldDto> fetchCustomerMstFields(String id) {

		List<CustomerMasterFieldDto> lstCmfields = cmFieldMgr.fetchCustomerMasfields(id);
		return lstCmfields;
	}
	
	public void checkDupFieldCode(CustomerMasterFieldDto req) {
		if (ArgUtil.is(req.getId())) {
			CustomerMasterFieldDoc groupDoc = cmFieldMgr.toCheckDupFieldCode(req.getFieldCode());
			if (ArgUtil.is(groupDoc)) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidNameDuplicate")
						.description("Field code already exists"));

			}
		}
	}
	
	public JobScheduledDoc uploadFile(CommonFile comfile){
		return cmFieldMgr.uploadFile(comfile);
		
	}
	
	
	
	public List<JobsResponseDto> fetchCustomerProfileMasterDoc(String id){
		return cmFieldMgr.fetchCustomerProfileMasterDoc(id);
		
	}
	
	public List<JobsResponseDto> fetchCustomerContactProfile(String id){
		return cmFieldMgr.fetchCustomerContactProfile(id);
		
	}
	

	public List<CustomerContactDto> fetchCustomerContactDetails(String id){
		return cmFieldMgr.fetchCustomerContactDetails(id);
		
	}
	
	public CustomerContactProfileDoc fetchCustomerContactInfo(String id,String customerId,String phoneno){
		return cmFieldMgr.fetchCustomerContactInfo(id,customerId,phoneno);
		
	}
	
	
	
	public List<JobsResponseDto> saveJobsOutPut(String id,List<Map<String,Object>> maps){
		return cmFieldMgr.saveJobsOutPut(id,maps);
		
	}
	
	

}
