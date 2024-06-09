package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.CustomerContactDto;
import com.boot.jx.admin.dto.CustomerMasterFieldDto;
import com.boot.jx.admin.dto.JobsResponseDto;
import com.boot.jx.admin.dto.ProfileSearchCriteria;
import com.boot.jx.admin.dto.SearchCustomerProfileDto;
import com.boot.jx.admin.dto.ProfileSearchQuery;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.CustomerMasterFieldDoc;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.common.doc.JobsOutPutDoc;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.CustomerContactProfileDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.dto.CustomerProfileRequest;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.store.ContactStore;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;
import com.boot.utils.UniqueID;

@Component
public class CustomerMasterFldMgr {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerMasterFldMgr.class);
//	@Autowired
//	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;
//	@Autowired
//	AuditDetailProvider auditDetailProvider;

	@Autowired
	ExcelHelper excelHelper;

	@Autowired
	ContactStore contactStore;
	
	@Autowired
	PMEnvironment pmEnvironment;
	
	@Autowired
	private RestService restService;

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
				cmFieldDoc.setIsactive(ArgUtil.parseAsString(reqDto.getIsactive(), Constants.YES));
				cmFieldDoc.setAdditionalInfo(reqDto.getAdditionalInfo());
				cmFieldDoc.setRequired(reqDto.getIsRequired());
				commonMongoTemplate.save(cmFieldDoc);
			}
		} else {
			cmFieldDoc.setFieldCode(reqDto.getFieldCode());
			cmFieldDoc.setFieldLabel(reqDto.getFieldLabel());
			cmFieldDoc.setFieldDesc(reqDto.getFieldDesc());
			cmFieldDoc.setFieldType(reqDto.getFieldType());
			cmFieldDoc.setIsactive(ArgUtil.parseAsString(reqDto.getIsactive(), Constants.YES));
			cmFieldDoc.setRequired(reqDto.getIsRequired());
			cmFieldDoc.setAdditionalInfo(reqDto.getAdditionalInfo());
			commonMongoTemplate.save(cmFieldDoc);
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
			List<CustomerMasterFieldDoc> lstGropDocs = commonMongoTemplate.findAll(CustomerMasterFieldDoc.class);
			for (CustomerMasterFieldDoc doc : lstGropDocs) {
				CustomerMasterFieldDto dto = EntityDtoUtil.entityToDto(doc, new CustomerMasterFieldDto());
				if (ArgUtil.is(dto.getIsactive()) && !dto.getIsactive().equalsIgnoreCase(Constants.DELETED_SOFT))
					dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	public List<CustomerMasterFieldDto> deleteCustmerMasterFiled(CustomerMasterFieldDto reqDto) {
		if (ArgUtil.is(reqDto.getId())) {
			MongoQueryBuilder<CustomerMasterFieldDoc> builder = MongoQueryBuilder
					.collection(CustomerMasterFieldDoc.class).whereId(reqDto.getId());
			builder.set("isactive", ArgUtil.parseAsString(reqDto.getIsactive(), Constants.DELETED_SOFT));
			commonMongoTemplate.upsert(builder);
		}
		return fetchCustomerMasfields(null);
	}

	public CustomerMasterFieldDoc toCheckDupFieldCode(String fieldCode) {
		Query query = new Query();
		query.addCriteria(Criteria.where("fieldCode").is(fieldCode).and("active").is(Constants.YES));
		CustomerMasterFieldDoc mstDoc = commonMongoTemplate.findOne(query, CustomerMasterFieldDoc.class);
		return mstDoc;
	}

	public JobScheduledDoc uploadFile(CommonFile comfile) {
		JobScheduledDoc doc = new JobScheduledDoc();
		Map<String, List<Object>> input = new HashMap<>();
		List<Object> files = new ArrayList<>();

		try {
			comfile.setBody(null);
			files.add(comfile);
			input.put("files", files);
			doc.setInput(input);
			doc.setIsactive(Constants.YES);
			doc.setJobtype("customer_profile_bulk_upload");
			doc.setTime(TimeStampIndex.now());
			doc.setStatus(ArgUtil.parseAsString(Status.CRTD));

			commonMongoTemplate.save(doc);
			SafeKeyHashMap<Object> globalVars = pmEnvironment.local().globalVars();
			String nodeUrl = globalVars.keyEntry("cp_node_url").asString();
			LOGGER.info("isSchedular :"+nodeUrl);
			
			
			/** to call node API **/
			//MapModel data =new MapModel();
			HashMap<String, Object> data =new HashMap<>();
			BuilderMap mapBuilder = MapBuilder.map();
			mapBuilder.put("id", doc.getId());
			
			data.put("name","cust_profiles_bulk_upload");
			data.put("desc","Deduplication and saving the bulk uploaded customer profiles to the database");
			data.put("data",mapBuilder.toMap());
			
			 String jsonStr = JsonUtil.toJson(data);
			LOGGER.info("post data :"+jsonStr);
			MapModel resp= restService.ajax(nodeUrl).postJson(data).asMapModel();
			LOGGER.info("JSON UTIL:"+JsonUtil.toJsonPrettyPrint(resp));
			
			

			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<JobsResponseDto> fetchCustomerProfileMasterDoc(String id) {

		List<JobsResponseDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;
		JobsResponseDto dto = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				dto = EntityDtoUtil.entityToDto(cmProfileDoc, new JobsResponseDto());
				dtoLst.add(dto);
			}
		} else {
			List<JobScheduledDoc> lstProfileDocs = commonMongoTemplate.findAll(JobScheduledDoc.class);
			for (JobScheduledDoc doc : lstProfileDocs) {
				dto = EntityDtoUtil.entityToDto(doc, new JobsResponseDto());
				dtoLst.add(dto);
			}
		}

		return dtoLst;
	}

	/** read customer contacts from s3 bucket -excel **/

//	public List<JobsResponseDto> fetchCustomerContactProfile(String id) {
//		List<JobsResponseDto> dtoLst = new ArrayList<>();
//		JobScheduledDoc cmProfileDoc = null;
//		String url = null;
//		if (ArgUtil.is(id)) {
//			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
//			if (ArgUtil.is(cmProfileDoc)) {
//				// url = cmProfileDoc.getFileUploadMap().
//				JobsResponseDto dto = EntityDtoUtil.entityToDto(cmProfileDoc, new JobsResponseDto());
//				dtoLst.add(dto);
//			}
//		}else {
//			List<JobScheduledDoc> lstAllDocs = commonMongoTemplate.findAll(null);
//		}
//
//		System.out.println("url :" + url);
//
//		return dtoLst;
//
//	}

	@Deprecated
	public List<CustomerContactDto> fetchCustomerContactDetails(String id) {
		List<CustomerContactDto> dtoLst = new ArrayList<>();
		JobScheduledDoc cmProfileDoc = null;

		List<CustomerProfileDoc> lstCusProMap = new ArrayList<>();

		String url = null;
		if (ArgUtil.is(id)) {
			cmProfileDoc = commonMongoTemplate.findByIdString(id, JobScheduledDoc.class);
			if (ArgUtil.is(cmProfileDoc)) {
				List<Map<String, Object>> maps = null;
				// url = cmProfileDoc.getFiles().getUrl();
				CustomerContactDto dto = new CustomerContactDto();
				dto.setId(id);
				dto.setSuccessMaps(null);
				dto.setUploadUrl(url);
				dto.setSuccessUrl(url);
				dto.setErrorUrl(url);

				try {
					maps = excelHelper.convertExcelToFormattedString();
					lstCusProMap = excelHelper.createCustomerProfile();
					dto.setSuccessMaps(maps);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				saveCustomerContactProfile(id, maps);
				// saveCustomerProfileMaster(id, lstCusProMap);
				dtoLst.add(dto);
			}
		}

		System.out.println("url :" + url);

		return dtoLst;

	}

	public List<CustomerProfileDoc> saveCustomerProfileMaster(String id) {
		List<CustomerProfileDoc> docs = new ArrayList<>();
		List<CustomerProfileDoc> lstCusProMap = null;
		try {
			lstCusProMap = excelHelper.createCustomerProfile();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ArgUtil.is(id) && lstCusProMap != null && !lstCusProMap.isEmpty()) {
			LOGGER.info("saveCustomerProfileMaster size :" + lstCusProMap.size());
			for (CustomerProfileDoc doc : lstCusProMap) {
				commonMongoTemplate.save(doc);
				docs.add(doc);
			}
		}

		return docs;
	}

	@Deprecated
	public void saveCustomerContactProfile(String id, List<Map<String, Object>> maps) {
		if (ArgUtil.is(id) && maps != null && !maps.isEmpty()) {
			CustomerContactProfileDoc cusprdoc = new CustomerContactProfileDoc();
			cusprdoc.setContactIdRef(id);
			cusprdoc.setContactmap(maps);
			commonMongoTemplate.save(cusprdoc);

		}

	}

	public List<CustomerProfileDoc> fetchCustomerContactInfo(String refId, String customerId, String phoneno,
			String emailid) {
		List<CustomerProfileDoc> cpLst = new ArrayList<>();
		CustomerProfileDoc profileDoc = new CustomerProfileDoc();
		List<Criteria> orOperator = new LinkedList<Criteria>();

		if (ArgUtil.is(refId)) {
			profileDoc = commonMongoTemplate.findOne(new Query(Criteria.where("contactIdRef").is(refId)),
					CustomerProfileDoc.class);

		}
		if (ArgUtil.is(phoneno)) {
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

	public List<JobsResponseDto> saveJobsOutPut(String id, Map<String, List<Object>> maps) {
		if (ArgUtil.is(maps)) {
			JobsOutPutDoc jobsOpDoc = new JobsOutPutDoc();
			jobsOpDoc.setJobid(id);
			jobsOpDoc.setOutput(jobsOpDoc.getOutput());
			jobsOpDoc.setIsactive(Constants.YES);
			jobsOpDoc.setJobtype("customer_profile_bulk_output");
			jobsOpDoc.setTime(TimeStampIndex.now());
			jobsOpDoc.setStatus(ArgUtil.parseAsString(Status.SCHLD));
			commonMongoTemplate.save(jobsOpDoc);
		}
		return null;
	}

	public List<JobsResponseDto> fetchJobsOutPut(String id, String jobid) {
		List<JobsResponseDto> lstDtos = new ArrayList<>();
		List<JobsOutPutDoc> lstDocs = new ArrayList<>();
		if (ArgUtil.is(id) && ArgUtil.isEmptyString(jobid)) {
			Query qryQuery = new Query();
			Criteria criteria = Criteria.where("id").is(id);
			qryQuery.addCriteria(criteria);
			lstDocs = commonMongoTemplate.find(qryQuery, JobsOutPutDoc.class);
		}else if (ArgUtil.is(jobid) && ArgUtil.isEmptyString(id)) {
			Query qryQuery = new Query();
			Criteria criteria = Criteria.where("jobid").is(jobid);
			qryQuery.addCriteria(criteria);
			lstDocs = commonMongoTemplate.find(qryQuery, JobsOutPutDoc.class);
		}else if (ArgUtil.is(id) && ArgUtil.is(jobid)) {
			Query qryQuery = new Query();
			Criteria criteria = new Criteria().andOperator(Criteria.where("id").is(id),
					Criteria.where("jobid").is(jobid));
			qryQuery.addCriteria(criteria);

			lstDocs = commonMongoTemplate.find(qryQuery, JobsOutPutDoc.class);
		} else {
			lstDocs = commonMongoTemplate.findAll(JobsOutPutDoc.class);

		}

		if (lstDocs != null && !lstDocs.isEmpty()) {
			for (JobsOutPutDoc op : lstDocs) {
				JobsResponseDto dto = EntityDtoUtil.entityToDto(op, new JobsResponseDto());
				lstDtos.add(dto);
			}

		}

		return lstDtos;

	}
	// PBPhone ph = parsePhone(new PBPhone().phone(phone));

	public List<CustomerProfileDoc> deDeuplicateCheck(CustomerProfileRequest request) {
		List<CustomerProfileDoc> cpLst = new ArrayList<>();
		List<Criteria> orOperator = new LinkedList<Criteria>();

		Set<PBPhone> setPbPhone = new TreeSet<PBPhone>();
		Set<PBEmail> setPbEmail = new TreeSet<PBEmail>();
		CustomerProfileDoc profileDoc = null;
//		if (ArgUtil.is(refId)) {
//			profileDoc = mongoTemplate.findOne(new Query(Criteria.where("contactIdRef").is(refId)),
//					CustomerProfileDoc.class);
//
//		}
		PBPhone ph = new PBPhone();
		PBEmail pEmail = new PBEmail();
		if (ArgUtil.is(request.getPhone())) {
			profileDoc = contactStore.findProfileByPhone(request.getPhone());
			if (profileDoc != null) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("phone").codeKey("ValidNameDuplicate")
						.description("Field code already exists"));
			}

			ph = contactStore.parsePhone(new PBPhone().phone(request.getPhone()));
			orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
					.and("countryCallingCode").is(ph.countryCallingCode)));
		}

		if (ArgUtil.is(request.getEmail())) {

			profileDoc = contactStore.findProfileByEmail(request.getEmail());
			if (profileDoc != null) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("email").codeKey("ValidNameDuplicate")
						.description("Field code already exists"));
			}
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(request.getEmail())));
		}

		CustomerProfileDoc cProfileDoc = new CustomerProfileDoc();
		cProfileDoc.setName(request.getName());
		if (ArgUtil.is(ph)) {
			ph.setUuid(UniqueID.generateString());
			setPbPhone.add(ph);
		}

		if (ArgUtil.is(request.getEmail())) {
			pEmail.setUuid(UniqueID.generateString());
			pEmail.setEmail(request.getEmail());
			pEmail.setLabel("Email");
			setPbEmail.add(pEmail);

		}
		cProfileDoc.setPhones(setPbPhone);
		cProfileDoc.setEmails(setPbEmail);
		cProfileDoc.setCode(request.getCode());
		// cProfileDoc.setCreatedStamp(System.currentTimeMillis());
		// cProfileDoc.setCreatedBy(auditDetailProvider.getAuditUser());
		commonMongoTemplate.save(cProfileDoc);
		cpLst.add(cProfileDoc);
		return cpLst;
	}

	public List<CustomerProfileDoc> fetchCustomeProfile(SearchCustomerProfileDto search) {
		List<Criteria> orOperator = new LinkedList<Criteria>();

		if (ArgUtil.is(search.getChatContactId())) {
			orOperator.add(Criteria.where("chatcontactId")
					.elemMatch(Criteria.where("chatcontactId").is(search.getChatContactId())));

		}

		if (ArgUtil.is(search.getEmailId())) {
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(search.getEmailId())));
		}
		if (ArgUtil.is(search.getPhone())) {
			PBPhone ph = contactStore.parsePhone(new PBPhone().phone(search.getPhone()));
			orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
					.and("countryCallingCode").is(ph.countryCallingCode)));
		}

		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(new Criteria().orOperator(orOperator.toArray(new Criteria[orOperator.size()])));
		return contactStore.find(qb);
	}

	/** profile search **/
	
	public List<CustomerProfileDoc> getProfileSearch(ProfileSearchQuery searchQry) {
		int limit = searchQry.getPageSize() == 0 ? 25 :searchQry.getPageSize();
		String sortDir =ArgUtil.parseAsString(searchQry.getSortBy(), "asc");
		List<ProfileSearchCriteria> searchCriterias =searchQry.getSearchCriterias(); 
		
		List<Criteria> criterias =new LinkedList<Criteria>();
		
		for(ProfileSearchCriteria src :searchCriterias) {
			criterias.add(createCriteria(src.getKey(),src.getOperation(),src.getValue()));
		}
		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()]))).sortBy(sortDir).limit(limit);
		return contactStore.find(qb);
	}
	
	private Criteria createCriteria(String key, String operation, Object value) {
        switch (operation) {
            case "=":
            case "EQ":	
                return Criteria.where(key).is(value);
            case ">":
            case "GT":	
                return Criteria.where(key).gt(value);
            case "<":
            case "LT":		
                return Criteria.where(key).lt(value);
            case ">=":
            case "GTE":
                return Criteria.where(key).gte(value);
            case "<=":
            case "LTE":		
                return Criteria.where(key).lte(value);
            case "!=":
            case "NE":	
                return Criteria.where(key).ne(value);
            case "STARTS_WITH": // Criteria for name starts with a specific prefix
            	return Criteria.where(key).regex("^" + value, "i"); // Case-insensitive search
            case "END_WITH":  // Criteria for name ends with a specific suffix
            	return Criteria.where(key).regex(value + "$", "i"); // Case-insensitive search
            case "ne": // Criteria for field is not empty
            	return Criteria.where(key).ne("").and(key).ne(null);
            case "ANY_MATCH":	// Criteria for matching any or all elements
            	return  Criteria.where(key).in(value);	
            case "ALL_MATCH":	// Criteria for matching all elements
            	return  Criteria.where(key).all(value);	
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    
	}

}
