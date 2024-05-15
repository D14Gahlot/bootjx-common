package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.attoparser.trace.MarkupTraceEvent.NonMinimizedStandaloneElementEndTraceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.CustomerContactDto;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.JobsResponseDto;
import com.boot.jx.admin.dto.SearchCustomerProfileDto;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.CustomerMasterFieldDoc;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.common.doc.JobsOutPutDoc;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.CustomerContactProfileDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.dto.CustomerProfileRequest;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.store.ContactStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.UniqueID;

@Component
public class CustomerMasterFldMgr {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerMasterFldMgr.class);
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;
	@Autowired
	AuditDetailProvider auditDetailProvider;

	@Autowired
	ExcelHelper excelHelper;
	
	@Autowired
	ContactStore contactStore;

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
		doc.setStatus(ArgUtil.parseAsString(Status.CRTD));
		commonMongoTemplate.save(doc);
	
		return doc;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public List<JobsResponseDto> fetchCustomerProfileMasterDoc(String id) {

		List<JobsResponseDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				JobsResponseDto dto = EntityDtoUtil.entityToDto(cmProfileDoc, new JobsResponseDto());
				dtoLst.add(dto);
			}
		} else {
			List<JobScheduledDoc> lstProfileDocs = mongoTemplate.findAll(JobScheduledDoc.class);
			for (JobScheduledDoc doc : lstProfileDocs) {
				JobsResponseDto dto = EntityDtoUtil.entityToDto(doc, new JobsResponseDto());
				dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	/** read customer contacts from s3 bucket -excel **/

	public List<JobsResponseDto> fetchCustomerContactProfile(String id) {
		List<JobsResponseDto> dtoLst = new ArrayList<>();
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
		
		List<CustomerProfileDoc> lstCusProMap = new ArrayList<>();
		
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
					lstCusProMap =excelHelper.createCustomerProfile();
					dto.setSuccessMaps(maps);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				saveCustomerContactProfile(id, maps);
				//saveCustomerProfileMaster(id, lstCusProMap);
				dtoLst.add(dto);
			}
		}

		System.out.println("url :" + url);

		return dtoLst;

	}

	public List<CustomerProfileDoc> saveCustomerProfileMaster(String id) {
		List<CustomerProfileDoc> docs=new ArrayList<>();
		List<CustomerProfileDoc> lstCusProMap =null;
		try {
			lstCusProMap =excelHelper.createCustomerProfile();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (ArgUtil.is(id) && lstCusProMap != null && !lstCusProMap.isEmpty()) {
			LOGGER.info("saveCustomerProfileMaster size :"+lstCusProMap.size());
			for(CustomerProfileDoc doc :lstCusProMap) {
				commonMongoTemplate.save(doc);
				docs.add(doc);
			}
		}
		
		return docs;
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

	public List<CustomerProfileDoc> fetchCustomerContactInfo(String refId, String customerId, String phoneno, String emailid) {
		List<CustomerProfileDoc> cpLst = new ArrayList<>(); 
		CustomerProfileDoc profileDoc = null;
		List<Criteria> orOperator = new LinkedList<Criteria>();
		
		if (ArgUtil.is(refId)) {
			profileDoc = mongoTemplate.findOne(new Query(Criteria.where("contactIdRef").is(refId)),
					CustomerProfileDoc.class);

		}if(ArgUtil.is(phoneno)) {
				PBPhone ph = contactStore.parsePhone(new PBPhone().phone(phoneno));
				orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
						.and("countryCallingCode").is(ph.countryCallingCode)));
		}
		
		if (ArgUtil.is(emailid)) {
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(emailid)));
		}

		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(new Criteria().orOperator(orOperator.toArray(new Criteria[orOperator.size()])));
		cpLst = contactStore.find(qb);
		return cpLst;
		
	}
	
	public List<JobsResponseDto> saveJobsOutPut(String id,List<Map<String,Object>> maps) {
		if(ArgUtil.is(maps)) {
			JobsOutPutDoc jobsOpDoc = new JobsOutPutDoc();
			jobsOpDoc.setJobid(id);
			jobsOpDoc.setOutputLst(maps);
			jobsOpDoc.setIsactive(Constants.YES);
			jobsOpDoc.setCreateBy(auditDetailProvider.getAuditUser());
			jobsOpDoc.setCreatedStamp(System.currentTimeMillis());
			jobsOpDoc.setJobtype("customer_profile_bulk_upload");
			jobsOpDoc.setTime(TimeStampIndex.now());
			jobsOpDoc.setStatus(ArgUtil.parseAsString(Status.SCHLD));
			commonMongoTemplate.save(jobsOpDoc);
		}
		return null;
	}
	public List<JobsResponseDto> fetchJobsOutPut(String id) {
		List<JobsResponseDto> lstDtos = new ArrayList<>();
		JobsOutPutDoc jobsOpDoc =null;
		if (ArgUtil.is(id)) {
			jobsOpDoc = commonMongoTemplate.findByIdString(id, JobsOutPutDoc.class);
			if(ArgUtil.is(jobsOpDoc)) {
				JobsResponseDto dto = EntityDtoUtil.entityToDto(jobsOpDoc, new JobsResponseDto());
				dto.setInputLst(jobsOpDoc.getOutputLst());
				lstDtos.add(dto);
			}
			
		}else {
			List<JobsOutPutDoc> lstDocs = commonMongoTemplate.findAll(JobsOutPutDoc.class);
			for(JobsOutPutDoc op:lstDocs) {
				JobsResponseDto dto = EntityDtoUtil.entityToDto(jobsOpDoc, new JobsResponseDto());
				dto.setInputLst(op.getOutputLst());
				lstDtos.add(dto);
			}
		}
		return lstDtos;
		
	}

	public List<CustomerProfileDoc> deDeuplicateCheck(CustomerProfileRequest request) {
		List<CustomerProfileDoc> cpLst = new ArrayList<>(); 
		List<Criteria> orOperator = new LinkedList<Criteria>();
		
		Set<PBPhone> setPbPhone =  new TreeSet<PBPhone>();
		Set<PBEmail> setPbEmail =  new TreeSet<PBEmail>();
		
//		if (ArgUtil.is(refId)) {
//			profileDoc = mongoTemplate.findOne(new Query(Criteria.where("contactIdRef").is(refId)),
//					CustomerProfileDoc.class);
//
//		}
		PBPhone ph = new PBPhone();
		PBEmail pEmail = new PBEmail();
		if(ArgUtil.is(request.getPhone())) {
				 ph = contactStore.parsePhone(new PBPhone().phone(request.getPhone()));
				orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
						.and("countryCallingCode").is(ph.countryCallingCode)));
		}
		
		if (ArgUtil.is(request.getEmail())) {
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(request.getEmail())));
		}

		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(new Criteria().orOperator(orOperator.toArray(new Criteria[orOperator.size()])));
		cpLst = contactStore.find(qb);
		if(cpLst!=null && !cpLst.isEmpty() && cpLst.size()>1) {
			
			ApiResponseUtil.throwInputException(new ApiFieldError().field("phone").codeKey("ValidNameDuplicate")
					.description("Field code already exists"));
		}
		
		CustomerProfileDoc cProfileDoc = new CustomerProfileDoc();
		cProfileDoc.setName(request.getName());
		if(ArgUtil.is(ph)) {
			ph.setUuid(UniqueID.generateString());
			setPbPhone.add(ph);
		}
		
		if(ArgUtil.is(request.getEmail())) {
			pEmail.setUuid(UniqueID.generateString());
			pEmail.setEmail(request.getEmail());
			pEmail.setLabel("Email");
			setPbEmail.add(pEmail);
			
		}
		cProfileDoc.setPhones(setPbPhone);
		cProfileDoc.setEmails(setPbEmail);
		cProfileDoc.setCode(request.getCode());
		cProfileDoc.setCreatedStamp(System.currentTimeMillis());
		cProfileDoc.setCreatedBy(auditDetailProvider.getAuditUser());
		mongoTemplate.save(cProfileDoc);
		cpLst.add(cProfileDoc);
		return cpLst;
	}
	
}
	
