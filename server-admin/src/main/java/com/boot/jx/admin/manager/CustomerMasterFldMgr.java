package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.CustomerContactDto;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.JobScheduledDto;
import com.boot.jx.common.doc.CustomerMasterFieldDoc;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.CustomerContactProfileDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.EntityDtoUtil;

@Component
public class CustomerMasterFldMgr {
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;
	@Autowired
	AuditDetailProvider auditDetailProvider;

	@Autowired
	ExcelHelper excelHelper;

	public List<CustomerMasterFieldDto> addAndEditMasterfield(CustomerMasterFieldDto reqDto) {

		CustomerMasterFieldDoc cmFieldDoc = new CustomerMasterFieldDoc();
		if (ArgUtil.is(reqDto.getId())) {
			cmFieldDoc = commonMongoTemplate.findByIdString(reqDto.getId(), CustomerMasterFieldDoc.class);
			if (ArgUtil.is(cmFieldDoc)) {
				cmFieldDoc.setId(cmFieldDoc.getId());
				cmFieldDoc.setFieldCode(
						reqDto.getFieldCode() == null ? cmFieldDoc.getFieldCode() : reqDto.getFieldCode());
				cmFieldDoc.setFieldLabel(
						reqDto.getFieldLabel() == null ? cmFieldDoc.getFieldLabel() : reqDto.getFieldLabel());
				cmFieldDoc.setFieldDesc(
						reqDto.getFieldDesc() == null ? cmFieldDoc.getFieldDesc() : reqDto.getFieldDesc());
				cmFieldDoc.setFieldType(
						reqDto.getFieldType() == null ? cmFieldDoc.getFieldType() : reqDto.getFieldType());
				cmFieldDoc.setIsactive(reqDto.getIsactive());
				cmFieldDoc.setModifiedBy(auditDetailProvider.getAuditUser());
				cmFieldDoc.setModifiedStamp(System.currentTimeMillis());
				mongoTemplate.save(cmFieldDoc);
			}
		} else {
			cmFieldDoc.setFieldCode(reqDto.getFieldCode());
			cmFieldDoc.setFieldLabel(reqDto.getFieldLabel());
			cmFieldDoc.setFieldDesc(reqDto.getFieldDesc());
			cmFieldDoc.setFieldType(reqDto.getFieldType());
			cmFieldDoc.setIsactive(reqDto.getIsactive());
			cmFieldDoc.setCreateBy(auditDetailProvider.getAuditUser());
			cmFieldDoc.setCreatedStamp(System.currentTimeMillis());
			mongoTemplate.save(cmFieldDoc);
		}

		return fetchCustomerMasfields(cmFieldDoc.getId());
	}

	public List<CustomerMasterFieldDto> fetchCustomerMasfields(String id) {
		List<CustomerMasterFieldDto> dtoLst = new ArrayList<>();
		CustomerMasterFieldDoc cmFieldDoc = null;
		if (ArgUtil.is(id)) {
			cmFieldDoc = commonMongoTemplate.findByIdString(id, CustomerMasterFieldDoc.class);
			if (ArgUtil.is(cmFieldDoc)) {
				CustomerMasterFieldDto dto = EntityDtoUtil.entityToDto(cmFieldDoc, new CustomerMasterFieldDto());
				dtoLst.add(dto);
			}
		} else {
			List<CustomerMasterFieldDoc> lstGropDocs = mongoTemplate.findAll(CustomerMasterFieldDoc.class);
			for (CustomerMasterFieldDoc doc : lstGropDocs) {
				CustomerMasterFieldDto dto = EntityDtoUtil.entityToDto(doc, new CustomerMasterFieldDto());
				dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	public CustomerMasterFieldDoc toCheckDupFieldCode(String fieldCode) {
		CustomerMasterFieldDoc mstDoc = mongoTemplate.findOne(new Query(Criteria.where("fieldCode").is(fieldCode)),
				CustomerMasterFieldDoc.class);
		return mstDoc;
	}

public JobScheduledDoc uploadFile(CommonFile comfile) {
		JobScheduledDoc doc = new JobScheduledDoc();
		List<Map<String,Object>> lstMaps = new ArrayList<>();
		try {
//		InputFile inputFile = new InputFile();
//		inputFile.setPath(comfile.getPath());
//		inputFile.setTitle(comfile.getTitle());
//		inputFile.setFileFormat(comfile.getFileFormat());
//		inputFile.setExtension(comfile.getExtension());
//		inputFile.setUrl(comfile.getUrl());
//		inputFile.setContentLength(comfile.getContentLength());
		comfile.setBody(null);	
		Map<String, Object> mapObj = new HashMap<String,Object>();
		mapObj.put("file", comfile);
		lstMaps.add(mapObj);
		//doc.setInput(mapObj);
		doc.setInputLst(lstMaps);
		doc.setIsactive(Constants.YES);
		doc.setCreateBy(auditDetailProvider.getAuditUser());
		doc.setCreatedStamp(System.currentTimeMillis());
		doc.setJobtype("customer_profile_bulk_upload");
		doc.setTime(TimeStampIndex.now());
		doc.setStatus(ArgUtil.parseAsString(Status.SCHLD));
		commonMongoTemplate.save(doc);
	
		return doc;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public List<JobScheduledDto> fetchCustomerProfileMasterDoc(String id) {

		List<JobScheduledDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				JobScheduledDto dto = EntityDtoUtil.entityToDto(cmProfileDoc, new JobScheduledDto());
				dtoLst.add(dto);
			}
		} else {
			List<JobScheduledDoc> lstProfileDocs = mongoTemplate.findAll(JobScheduledDoc.class);
			for (JobScheduledDoc doc : lstProfileDocs) {
				JobScheduledDto dto = EntityDtoUtil.entityToDto(doc, new JobScheduledDto());
				dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	/** read customer contacts from s3 bucket -excel **/

	public List<JobScheduledDto> fetchCustomerContactProfile(String id) {
		List<JobScheduledDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;
		String url = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				//url = cmProfileDoc.getFileUploadMap().
				//JobScheduledDto dto = EntityDtoUtil.entityToDto(cmProfileDoc, new JobScheduledDto());

				//dtoLst.add(dto);
			}
		}

		System.out.println("url :" + url);

		return dtoLst;

	}

	public List<CustomerContactDto> fetchCustomerContactDetails(String id) {
		List<CustomerContactDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;
		String url = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				List<Map<String, Object>> maps = null;
				//url = cmProfileDoc.getFiles().getUrl();
				CustomerContactDto dto = new CustomerContactDto();
				dto.setId(id);
				dto.setSuccessMaps(null);
				dto.setUploadUrl(url);
				dto.setSuccessUrl(url);
				dto.setErrorUrl(url);

				try {
					maps = excelHelper.convertExcelToFormattedString();
					dto.setSuccessMaps(maps);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				saveCustomerContactProfile(id, maps);
				dtoLst.add(dto);
			}
		}

		System.out.println("url :" + url);

		return dtoLst;

	}

	public void saveCustomerContactProfile(String id, List<Map<String, Object>> maps) {
		if (ArgUtil.is(id) && maps != null && !maps.isEmpty()) {
			CustomerContactProfileDoc cusprdoc = new CustomerContactProfileDoc();
			cusprdoc.setContactIdRef(id);
			cusprdoc.setContactmap(maps);
			cusprdoc.setCreateBy(auditDetailProvider.getAuditUser());
			cusprdoc.setCreatedStamp(System.currentTimeMillis());
			commonMongoTemplate.save(cusprdoc);

		}

	}

	public CustomerContactProfileDoc fetchCustomerContactInfo(String refId, String customerId, String phoneno) {
		CustomerContactProfileDoc profileDoc = null;
		if (ArgUtil.is(refId)) {
			profileDoc = mongoTemplate.findOne(new Query(Criteria.where("contactIdRef").is(refId)),
					CustomerContactProfileDoc.class);

		}
		return profileDoc;
	}
}
