package com.boot.jx.admin.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.CustomerContactDto;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.CustomerProfileMasterDto;
import com.boot.jx.admin.service.CustomerProfileService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.doc.CustomerContactProfileDoc;

@RestController
public class CustomerProfileContoller {
	
	@Autowired
	CustomerProfileService cusProfileService;

	@RequestMapping(value = "/api/add-update-customer-mast-field", method = { RequestMethod.POST })
	public ApiResponse<CustomerMasterFieldDto, Object> createAndUpdateCustmerMasterFiled(@RequestBody CustomerMasterFieldDto reqDto){
		if(StringUtils.isBlank(reqDto.getId())){
			cusProfileService.checkDupFieldCode(reqDto);
		}	
		return ApiResponse.buildResults(cusProfileService.addEditCustomerMastFields(reqDto));
	}
	
	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.GET })
	public ApiResponse<CustomerMasterFieldDto, Object> fetchCusMasFields(@RequestParam(value = "id", required = false) String id){
			return ApiResponse.buildResults(cusProfileService.fetchCustomerMstFields(id));
		}
	
	@Autowired
	AWSFileStore fileStore;
	@RequestMapping(value = "/api/upload/pofile", method = { RequestMethod.POST })
	public ApiResponse<CommonFile, Object> uploadExcel(
			@RequestParam(name = "file", required = false) MultipartFile file) {
//		CommonFile url = fileStore.upload1(file,
//				String.format("%s%s%s", AppContextUtil.getTenant(), UUID.randomUUID()),
//				file.getOriginalFilename());
		
		CommonFile url = fileStore.upload1(file,
				String.format("%s/profileExcel/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
				file.getOriginalFilename());
		cusProfileService.uploadFile(url);
		return ApiResponse.buildResults(url);
	
	}
 
	
	@RequestMapping(value = "/api/fetch/customer/master/profile", method = { RequestMethod.GET })
	public ApiResponse<CustomerProfileMasterDto, Object> fetchCustomerProfileMasterDoc(@RequestParam(value = "id", required = false) String id){
			return ApiResponse.buildResults(cusProfileService.fetchCustomerProfileMasterDoc(id));
		}
	
	@RequestMapping(value = "/api/fetch/customer/upload/profile", method = { RequestMethod.GET })
	public ApiResponse<CustomerProfileMasterDto, Object> fetchCustomerContactProfile(@RequestParam(value = "id", required = true) String id){
			return ApiResponse.buildResults(cusProfileService.fetchCustomerContactProfile(id));
		}
	
	@RequestMapping(value = "/api/save/customer/upload/contact/details", method = { RequestMethod.POST })
	public ApiResponse<CustomerContactDto, Object> fetchCustomerContactDetails(@RequestParam(value = "id", required = true) String id){
			return ApiResponse.buildResults(cusProfileService.fetchCustomerContactDetails(id));
		}
	
	@RequestMapping(value = "/api/agent/customer/contact/info", method = { RequestMethod.GET })
	public ApiResponse<CustomerContactProfileDoc, Object> fetchCustomerContactInfo(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "customerId", required = false) String customerId,
			@RequestParam(value = "phoneno", required = false) String phoneno){
			return ApiResponse.buildResult(cusProfileService.fetchCustomerContactInfo(id,customerId,phoneno));
		}
	
	
}
