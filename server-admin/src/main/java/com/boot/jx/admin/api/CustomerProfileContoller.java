package com.boot.jx.admin.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.JobsResponseDto;
import com.boot.jx.admin.dto.ProfileSearchQuery;
import com.boot.jx.admin.service.CustomerProfileService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.config.CustomerFieldMasterDoc;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class CustomerProfileContoller {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerProfileContoller.class);

	@Autowired
	CustomerProfileService cusProfileService;

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.POST })
	public ApiResponse<CustomerFieldMasterDoc, Object> createUpdateCusMasFields(
			@RequestBody CustomerFieldMasterDoc reqDto) {
		if (StringUtils.isBlank(reqDto.getId())) {
			cusProfileService.checkDupFieldCode(reqDto);
		}
		return ApiResponse.buildResults(cusProfileService.addEditCustomerMastFields(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.PATCH })
	public ApiResponse<CustomerFieldMasterDoc, Object> updateCusMasFields(@RequestBody CustomerFieldMasterDoc reqDto) {
		if (StringUtils.isBlank(reqDto.getId())) {
			cusProfileService.checkDupFieldCode(reqDto);
		}
		return ApiResponse.buildResults(cusProfileService.addEditCustomerMastFields(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.DELETE })
	public ApiResponse<CustomerFieldMasterDoc, Object> deleteCusMasFields(
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "active", required = false) String active){
			//@Request CustomerFieldMasterDoc reqDto) {
	CustomerFieldMasterDoc reqDto= new CustomerFieldMasterDoc();
	reqDto.setId(id);
	reqDto.setActive(false);
		return ApiResponse.buildResults(cusProfileService.deleteCustmerMasterFiled(reqDto));
	}

	@RequestMapping(value = "/api/fetch/customer/master/fields", method = { RequestMethod.GET })
	public ApiResponse<CustomerFieldMasterDoc, Object> fetchCusMasFields(
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "active", required = false) Boolean active,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false,defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "created") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerMstFields(id,active,pageSize,pageNo,sortBy,sortDir));
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
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "pageSize", required = false,defaultValue = "25") int pageSize,
			@RequestParam(value = "pageNo", required = false,defaultValue = "0") int pageNo,
			@RequestParam(value = "sortBy", required = false,defaultValue = "_id") String sortBy) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerProfileMasterDoc(id,pageSize,pageNo,sortBy));
	}
	@RequestMapping(value = "/api/agent/customer/contact/info", method = { RequestMethod.GET })
	public ApiResponse<CustomerProfileDoc, Object> fetchCustomerContactInfo(
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "customerId", required = false) String customerId,
			@RequestParam(value = "phoneno", required = false) String phoneno,
			@RequestParam(value = "emailid", required = false) String emailid) {
		return ApiResponse.buildResults(cusProfileService.fetchCustomerContactInfo(id, customerId, phoneno, emailid));
	}


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

	@RequestMapping(value = "/api/profile/filter/search", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> getProfiles(@RequestBody ProfileSearchQuery searchQry) {
		List<CustomerProfileDoc> docs = cusProfileService.getProfileSearch(searchQry);
		return ApiResponse.buildResults(docs);
	}

}
