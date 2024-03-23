package com.boot.jx.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.CustomerProfileMasterDto;
import com.boot.jx.admin.manager.CustomerMasterFldMgr;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.CustomerMasterFieldDoc;
import com.boot.jx.model.CommonFile;
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
	
	public List<CustomerProfileMasterDto> uploadFile(CommonFile comfile){
		return cmFieldMgr.uploadFile(comfile);
		
	}
	
	
	
	public List<CustomerProfileMasterDto> fetchCustomerProfileMasterDoc(String id){
		return cmFieldMgr.fetchCustomerProfileMasterDoc(id);
		
	}

}
