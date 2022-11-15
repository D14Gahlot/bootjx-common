package com.boot.jx.contak.api;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.dto.CompanyDTO;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.manager.FirebaseManager;
import com.boot.jx.contak.manager.UserRegistrationManager;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.phonebook.manager.PhoneBookManager;
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
	public ApiResponse<CompanyDoc,Object> save(@RequestBody CompanyDTO msg){
		CompanyDoc  compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("number").is(msg.number)));
		if(ArgUtil.is(compoc)) {
			ApiResponseUtil.throwDuplicateInputException(new ApiFieldError().field("number"));
		}
		CompanyDoc companyDoc = new CompanyDoc();
		companyDoc.setActive(true);
		companyDoc.setLegalBusinessName(msg.legalBusinessName);
		companyDoc.setDisplayName(msg.displayName);
		companyDoc.setCountryOfOperation(msg.countryOfOperation);
		companyDoc.setAddress(msg.address);
		companyDoc.setWebsiteUrl(msg.websiteUrl);
		
		if(ArgUtil.is(msg.coiFile)) {	
			CommonFile commonfile = fileStore.upload1(msg.coiFile,
					String.format("%s/quickmedia/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					msg.coiFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setCoiFileUrl(url);
		}
		
		if(ArgUtil.is(msg.gstFile)) {	
			CommonFile commonfile = fileStore.upload1(msg.gstFile,
					String.format("%s/quickmedia/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					msg.gstFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setGstFileUrl(url);
		}
		
		if(ArgUtil.is(msg.panFile)) {	
			CommonFile commonfile = fileStore.upload1(msg.panFile,
					String.format("%s/quickmedia/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					msg.panFile.getOriginalFilename());
			com.boot.jx.dict.FileType fileType = commonfile.getFileType();
			FileFormat fileFormat = commonfile.getFileFormat();
			String url = commonfile.getUrl();
			companyDoc.setPanFileUrl(url);
		}
		
		companyDoc.setContactPersonName(msg.contactPersonName);
		companyDoc.setContactPhoneNumber(msg.contactPhoneNumber);
		companyDoc.setContactPersonEmailId(msg.contactPersonEmailId);
		
		

		try {
	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(msg.password.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        companyDoc.setPassword(sb.toString());
	    } catch (java.security.NoSuchAlgorithmException e) {
	    	
	    }
		
		
		
		companyDoc.setApiKey(UUID.randomUUID().toString());
		companyDoc.setCompanyTimeZone(msg.timezone);
		companyDoc.setCreatedAt(TimeStampIndex.now());
		companyDoc.setNumber(msg.number);	
		companyDoc.setLogoUrl(msg.logoUrl);
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
	
	
	@RequestMapping(value = "/api/v1/company", method = { RequestMethod.GET })
	public ApiResponse<List<CompanyDoc>,Object> get(){	
		List<CompanyDoc> companyDocList = commonMongoTemplate.findAll(CompanyDoc.class);
		return ApiResponse.buildResult(companyDocList);
	}

}
