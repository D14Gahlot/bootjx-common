package com.boot.jx.admin.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.JobsResponseDto;
import com.boot.jx.admin.dto.SearchQuery;
import com.boot.jx.admin.service.CustomerProfileService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class CustomerProfileContoller {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerProfileContoller.class);
	


	@Autowired
	CustomerProfileService cusProfileService;

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.POST })
	public ApiResponse<CustomerMasterFieldDto, Object> createUpdateCusMasFields(
			@RequestBody CustomerMasterFieldDto reqDto) {
		if (StringUtils.isBlank(reqDto.getId())) {
			cusProfileService.checkDupFieldCode(reqDto);
		}
		return ApiResponse.buildResults(cusProfileService.addEditCustomerMastFields(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.PATCH })
	public ApiResponse<CustomerMasterFieldDto, Object> updateCusMasFields(@RequestBody CustomerMasterFieldDto reqDto) {
		if (StringUtils.isBlank(reqDto.getId())) {
			cusProfileService.checkDupFieldCode(reqDto);
		}
		return ApiResponse.buildResults(cusProfileService.addEditCustomerMastFields(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.DELETE })
	public ApiResponse<CustomerMasterFieldDto, Object> deleteCusMasFields(@RequestBody CustomerMasterFieldDto reqDto) {
		return ApiResponse.buildResults(cusProfileService.deleteCustmerMasterFiled(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.GET })
	public ApiResponse<CustomerMasterFieldDto, Object> fetchCusMasFields(
			@RequestParam(value = "id", required = false) String id) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerMstFields(id));
	}

	@Autowired
	AWSFileStore fileStore;

	@RequestMapping(value = "/api/upload/pofile", method = { RequestMethod.POST })
	public ApiResponse<JobScheduledDoc, Object> uploadExcel(
			@RequestParam(name = "file", required = false) MultipartFile file) {
		CommonFile url = fileStore.upload1(file,
				String.format("%s/profileExcel/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
				file.getOriginalFilename());
		JobScheduledDoc jobSch = cusProfileService.uploadFile(url);
		
		
		return ApiResponse.buildResults(jobSch);

	}

	@RequestMapping(value = "/api/fetch/schdelued/jobs", method = { RequestMethod.GET })
	public ApiResponse<JobsResponseDto, Object> fetchCustomerProfileMasterDoc(
			@RequestParam(value = "id", required = false) String id) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerProfileMasterDoc(id));
	}

//	@RequestMapping(value = "/api/fetch/customer/contact/profile", method = { RequestMethod.GET })
//	public ApiResponse<JobsResponseDto, Object> fetchCustomerContactProfile(
//			@RequestParam(value = "id", required = true) String id) {
//		return ApiResponse.buildResults(cusProfileService.fetchCustomerContactProfile(id));
//	}

//	@RequestMapping(value = "/api/save/customer/upload/contact/details", method = { RequestMethod.POST })
//	public ApiResponse<CustomerContactDto, Object> fetchCustomerContactDetails(@RequestParam(value = "id", required = true) String id){
//			return ApiResponse.buildResults(cusProfileService.fetchCustomerContactDetails(id));
//		}

	@RequestMapping(value = "/api/agent/customer/contact/info", method = { RequestMethod.GET })
	public ApiResponse<CustomerProfileDoc, Object> fetchCustomerContactInfo(
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "customerId", required = false) String customerId,
			@RequestParam(value = "phoneno", required = false) String phoneno,
			@RequestParam(value = "emailid", required = false) String emailid) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerContactInfo(id, customerId, phoneno, emailid));
	}

//	@RequestMapping(value = "/api/save/jobs/output", method = { RequestMethod.POST })
//	public ApiResponse<JobsResponseDto, Object> saveJobsOutPut(@RequestParam(value = "id", required = true) String id,@RequestBody Map<String, List<Object>> maps){
//			return ApiResponse.buildResults(cusProfileService.saveJobsOutPut(id,maps));
//		}
//	
//
	@RequestMapping(value = "/api/fetch/jobs/output", method = { RequestMethod.GET })
	public ApiResponse<JobsResponseDto, Object> fetchJobsOutPut(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "jobid", required = false) String jobid) {
		return ApiResponse.buildResults(cusProfileService.fetchJobsOutPut(id, jobid));
	}

	@RequestMapping(value = "/api/save/customer/profile/contact/details", method = { RequestMethod.POST })
	public ApiResponse<CustomerProfileDoc, Object> saveCustomerProfile(
			@RequestParam(value = "id", required = true) String id) {
		return ApiResponse.buildResults(cusProfileService.saveCustomerProfile(id));
	}
//
//	@RequestMapping(value = "/api/customer/de-duplicate/save", method = { RequestMethod.POST })
//	public ApiResponse<CustomerProfileDoc, Object> deDeuplicateCheck(@RequestBody CustomerProfileRequest request) {
//		return ApiResponse.buildResults(cusProfileService.deDeuplicateCheck(request));
//	}

//	@RequestMapping(value = "/api/search/customer/profile", method = { RequestMethod.POST })
//	public ApiResponse<CustomerProfileDoc, Object> fetchCustomeProfile(@RequestBody SearchCustomerProfileDto search) {
//		return ApiResponse.buildResults(cusProfileService.fetchCustomeProfile(search));
//	}
	
	
	@RequestMapping(value = "/profile/filter", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> getProfiles(@RequestBody SearchQuery searchQry)  {
		List<CustomerProfileDoc> docs = cusProfileService.getProfileSearch(searchQry);
		return ApiResponse.buildResults(docs);
	}

	
	

}
