package com.boot.jx.contak.api;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.manager.FirebaseManager;
import com.boot.jx.contak.manager.UserRegistrationManager;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.utils.ArgUtil;

@RestController
@RequestMapping("/comp")
public class CompanyController {
	
	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	PhoneBookManager phoneBookManager;
	
	@Autowired
	UserRegistrationManager userRegistrationManager;
	
	
	@Autowired
	FirebaseManager firebaseManager;
	
	@Autowired
	AWSFileStore fileStore;
	
	
	@RequestMapping(value = "/api/v1/company/register", method = { RequestMethod.POST })
	public ApiResponse<CompanyDoc,Object> save(@RequestParam(name = "coiFile") MultipartFile coiFile,
			@RequestParam(name = "gstFile") MultipartFile gstFile,
			@RequestParam(name = "panFile") MultipartFile panFile,
			@RequestParam(name = "logoUrl") MultipartFile logoUrl,
			@RequestParam String number,
			@RequestParam String legalBusinessName,
			@RequestParam String displayName,
			@RequestParam String countryOfOperation,
			@RequestParam String address,
			@RequestParam String websiteUrl,
			@RequestParam String contactPersonName,
			@RequestParam String contactPhoneNumber,
			@RequestParam String contactPersonEmailId,
			@RequestParam String timezone,		
			@RequestParam String password){
		CompanyDoc  compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("number").is(number)));
		if(ArgUtil.is(compoc)) {
			ApiResponseUtil.throwDuplicateInputException(new ApiFieldError().field("number"));
		}
		CompanyDoc companyDoc = new CompanyDoc();
		companyDoc.setActive(true);
		companyDoc.setLegalBusinessName(legalBusinessName);
		companyDoc.setDisplayName(displayName);
		companyDoc.setCountryOfOperation(countryOfOperation);
		companyDoc.setAddress(address);
		companyDoc.setWebsiteUrl(websiteUrl);
		
		if(ArgUtil.is(coiFile)) {	
			CommonFile commonfile = fileStore.upload1(coiFile,
					String.format("%s/oafiles/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					coiFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setCoiFileUrl(url);
		}
		
		if(ArgUtil.is(gstFile)) {	
			CommonFile commonfile = fileStore.upload1(gstFile,
					String.format("%s/oafiles/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					gstFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setGstFileUrl(url);
		}
		
		if(ArgUtil.is(panFile)) {	
			CommonFile commonfile = fileStore.upload1(panFile,
					String.format("%s/oafiles/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					panFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setPanFileUrl(url);
		}
		
		companyDoc.setContactPersonName(contactPersonName);
		companyDoc.setContactPhoneNumber(contactPhoneNumber);
		companyDoc.setContactPersonEmailId(contactPersonEmailId);
		
		

		try {
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(password.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        companyDoc.setPassword(sb.toString());
	    } catch (java.security.NoSuchAlgorithmException e) {
	    	
	    }
		
		
		
		companyDoc.setApiKey(UUID.randomUUID().toString());
		companyDoc.setCompanyTimeZone(timezone);
		companyDoc.setCreatedAt(TimeStampIndex.now());
		companyDoc.setNumber(number);	
		if(ArgUtil.is(logoUrl)) {	
			CommonFile commonfile = fileStore.upload1(logoUrl,
					String.format("%s/oafiles/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					logoUrl.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setLogoUrl(url);
		}
		commonMongoTemplate.save(companyDoc);
		return ApiResponse.buildResult(companyDoc);
	}
	
	@RequestMapping(value = "/api/v1/company/get", method = { RequestMethod.GET })
	public ApiResponse<CompanyDoc,Object> getById(@RequestParam String id){	
		CompanyDoc  compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("companyId").is(id)));
		if(!ArgUtil.is(compoc)) {
			ApiResponseUtil.throwException("Company does not exists");
		}
		return ApiResponse.buildResult(compoc);
	}
	
	@RequestMapping(value = "/api/v1/company/get/auth", method = { RequestMethod.GET })
	public ApiResponse<CompanyDoc,Object> getByAuthKey(@RequestParam String authKey){	
		CompanyDoc  compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("apiKey").is(authKey)));
		if(!ArgUtil.is(compoc)) {
			ApiResponseUtil.throwException("Company does not exists");
		}
		return ApiResponse.buildResult(compoc);
	}
	
	
	@RequestMapping(value = "/api/v1/company", method = { RequestMethod.GET })
	public ApiResponse<List<CompanyDoc>,Object> get(){	
		List<CompanyDoc> companyDocList = commonMongoTemplate.findAll(CompanyDoc.class);
		return ApiResponse.buildResult(companyDocList);
	}

}
