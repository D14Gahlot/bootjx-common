package com.boot.jx.postman.store;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.ModelPatch;
import com.boot.jx.model.ModelPatch.ModelPatchCommand;
import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder.SimpleDocQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.config.CustomerFieldMasterDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBWebsite;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.UtilityModels.UniqueIndex;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.boot.utils.UniqueID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
public class ContactStore extends CommonMongoTemplateAbstract<ContactStore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactStore.class);

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	@Autowired
	public PMClientConfig pmClientConfig;

	@Autowired
	protected PMEnvironment environment;
	
	@Autowired
	CommonMongoTemplate cMongoTemplate;
	
	@Autowired(required = false)
	protected AuditDetailProvider auditDetailProvider;

	public PBPhone parsePhone(PBPhone pbPhone) {
		String defaultRegion = environment.keyEntry("postman.phonebook.region").asString("IN");
		if (ArgUtil.not(pbPhone.phone)) {
			pbPhone.phone = String.format("+%s%s", pbPhone.countryCallingCode, pbPhone.nationalNumber);
		}
		pbPhone.phone = pbPhone.phone.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();
		try {
			PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse("+" + pbPhone.phone, defaultRegion);
			pbPhone.nationalNumber = ArgUtil.parseAsString(phoneNumber.getNationalNumber());
			pbPhone.countryCallingCode = ArgUtil.parseAsString(phoneNumber.getCountryCode());
			pbPhone.phone = String.format("+%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			pbPhone.country = PHONE_NUMBER_UTIL.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
		} catch (NumberParseException e) {

		}

		return pbPhone;
	}

	public ChatContactDoc findContact(Contactable contactMeta) {
		Contactable contact = PostManUtil.getContactMeta(contactMeta);
		if (ArgUtil.isEmpty(contact.getContactId())) {
			return null;
		}
		return findById(contact.getContactId(), ChatContactDoc.class);
	}

	public List<ChatContactDoc> searchContacts(String search, String lane) {
		// TODO:-- Optimize Search
		// Query query =
		// TextQuery.queryText(TextCriteria.forDefaultLanguage().matching(search)).sortByScore()

		Criteria c = Criteria.where("lane").is(lane); // Lane should be fixed

		if (ArgUtil.is(search)) {
			c = c.orOperator(
					// Check all fields
					Criteria.where("name").regex("" + search + "", "i"),
					Criteria.where("phone").regex("" + search + "", "i"),
					Criteria.where("email").regex("" + search + "", "i"));
		}
		Query query = new Query()
				// New Criteria
				.addCriteria(c);
		return find(query, ChatContactDoc.class);

	}

	public CustomerProfileDoc findProfileByPhone(String phone) {
		PBPhone ph = parsePhone(new PBPhone().phone(phone));
		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
						.and("countryCallingCode").is(ph.countryCallingCode)));
		return findOne(qb);
	}

	public CustomerProfileDoc findProfileByEmail(String email) {
		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(Criteria.where("emails").elemMatch(Criteria.where("email").is(email)));
		return findOne(qb);
	}

	public List<CustomerProfileDoc> findProfileByContactId(String contactId) {
		ChatContactDoc contact = findById(contactId, ChatContactDoc.class);

		List<Criteria> orOperator = new LinkedList<Criteria>();

		if (ArgUtil.is(contact.getEmail())) {
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(contact.getEmail())));
		}
		if (ArgUtil.is(contact.phone())) {
			PBPhone ph = parsePhone(new PBPhone().phone(contact.phone()));
			orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
					.and("countryCallingCode").is(ph.countryCallingCode)));
		}

		if (ArgUtil.is(contact.user().getCode())) {
			orOperator.add(Criteria.where("code").is(contact.user().getCode()));
		}

		if (ArgUtil.is(contact.user().getEmail())) {
			orOperator.add(Criteria.where("emails").elemMatch(Criteria.where("email").is(contact.user().getEmail())));
		}

		if (ArgUtil.is(contact.user().getMobile())) {
			PBPhone ph = parsePhone(new PBPhone().phone(contact.user().getMobile()));
			orOperator.add(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
					.and("countryCallingCode").is(ph.countryCallingCode)));
		}

		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(new Criteria().orOperator(orOperator.toArray(new Criteria[orOperator.size()])));
		return find(qb);
	}

	public void linkProfile(ChatContactQuery contactQuery, CustomerProfileDoc profile) {
		ChatContactDoc contact = contactQuery.getDoc();
		contact.profile().setId(profile.getId());
		contact.profile().setCode(profile.code);
		contact.profile().setName(profile.name.getFormattedName());
		contactQuery.set("profile", contact.profile());
	}

	public ChatContactDoc linkProfile(String contactId, String profileId) {
		CustomerProfileDoc profile = findById(profileId, CustomerProfileDoc.class);
		ChatContactDoc contact = findById(contactId, ChatContactDoc.class);
		ChatContactQuery query = new ChatContactQuery(contact);
		linkProfile(query, profile);
		update(query);
		return contact;
	}

	public ChatContactDoc delinkProfile(String contactId) {
		ChatContactDoc contact = findById(contactId, ChatContactDoc.class);
		ChatContactQuery query = new ChatContactQuery(contact);
		query.unset("profile");
		contact.setProfile(null);
		update(query);
		return contact;
	}

	public static <T extends UniqueIndex<T>> Set<T> patch(ModelPatchCommand command, Set<T> items, T item) {
		switch (command) {
		case ADD:
		case UPDATE:
			Optional<T> found = Optional.empty();
			if (ArgUtil.is(item.uuid())) {
				found = items.stream().filter(itm -> ArgUtil.is(item.uuid(), itm.uuid())).findFirst();
			}
			if (found.isPresent()) {
				found.get().update(item);
			} else {
				item.uuid(UniqueID.generateString());
			}
			items.add(item);
			break;
		case REMOVE:
			items.remove(item);
			break;
		case DELETE:
			return null;
		default:
			break;
		}
		return items;
	}

	public CustomerProfileDoc patchCustomerProfile(ModelPatches req) {
		CustomerProfileDoc doc = findById(req.getId(), CustomerProfileDoc.class);
		SimpleDocQueryBuilder qb = SimpleDocQueryBuilder.doc(doc);
		for (ModelPatch patch : req.getPatches()) {
			switch (patch.getField()) {
			case "email":
			case "emails":
				PBEmail email = patch.value().as(PBEmail.class);
				qb.setunset("emails", patch(patch.getCommand(), doc.emails(), email));
				break;
			case "phone":
			case "phones":
			case "mobile":
			case "mobiles":
				PBPhone phone = parsePhone(patch.value().as(PBPhone.class));
				qb.setunset("phones", patch(patch.getCommand(), doc.phones(), phone));
				break;
			case "address":
			case "addresses":
				PBAddress address = patch.value().as(PBAddress.class);
				qb.setunset("addresses", patch(patch.getCommand(), doc.addresses(), address));
				break;
			case "url":
			case "urls":
				PBWebsite url = patch.value().as(PBWebsite.class);
				qb.setunset("urls", patch(patch.getCommand(), doc.urls(), url));
				break;
			case "name":
				PBName name = patch.value().as(PBName.class);
				qb.setunset("name", name.fix());
				break;
			case "code":
				qb.setunset("code", patch.value().asString());
				break;
			case "rmCode":
				qb.setunset("rmCode", patch.value().asString());
				break;
				
			case "additionalInfo.alt_phones":
			case "alt_phones":
				PBPhone alt_phone = parsePhone(patch.value().as(PBPhone.class));
				qb.setunset("additionalInfo.alt_phones", patch(patch.getCommand(), doc.phones(), alt_phone));
				break;
			case "additionalInfo.alt_emails":
			case "alt_emails":
				PBEmail alt_email =patch.value().as(PBEmail.class);
				qb.setunset("additionalInfo.alt_emails", patch(patch.getCommand(), doc.emails, alt_email));
				break;	
				
			default:
				// TODO:-  Check additonal validty in Masters and its type in master,
				// then based on type of field do conversion below and update instead
				// using.asString() for all
				Object objType=checkFieldType(getFileName(patch.getField()), patch.value());
				if(ArgUtil.is(objType)) {
					if(patch.getCommand().equals(ModelPatchCommand.REMOVE)) {
						qb.setunset(patch.getField(),ArgUtil.parseAsT(Constants.BLANK,objType,false));
					}else {
						qb.setunset(patch.getField(),ArgUtil.parseAsT(patch.getValue(),objType,false));
					}
				}
				break;
			}
			update(qb);
			doc = findById(req.getId(), CustomerProfileDoc.class);
		}

		return doc;
	}

	public CustomerProfileDoc findProfileByCode(String code) {
		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(Criteria.where("code").is(code));
		return findOne(qb);
	}

	public void checkDuplicate(ModelPatches req) {
		CustomerProfileDoc cusPfDoc = null;

		for (ModelPatch patch : req.getPatches()) {
			if (patch.getCommand().equals(ModelPatchCommand.ADD)) {
				switch (patch.getField()) {
				case "email":
				case "emails":
					PBEmail email = patch.value().as(PBEmail.class);
					cusPfDoc = findProfileByEmail(email.getEmail());
					if (ArgUtil.is(cusPfDoc)) {
						ApiResponseUtil.throwInputException(new ApiFieldError().field(patch.getField())
								.codeKey("ValidEmailDuplicate").description(patch.getField() + " already exists"));
					}
					break;
				case "phone":
				case "phones":
					PBPhone phone = parsePhone(patch.value().as(PBPhone.class));
					cusPfDoc = findProfileByPhone(phone.getPhone());
					if (ArgUtil.is(cusPfDoc)) {
						ApiResponseUtil.throwInputException(new ApiFieldError().field(patch.getField())
								.codeKey("ValidPhoneDuplicate").description(patch.getField() + " already exists"));
					}
					break;
				case "code":
					cusPfDoc = findProfileByCode(patch.value().asString());
					if (ArgUtil.is(cusPfDoc)) {
						ApiResponseUtil.throwInputException(new ApiFieldError().field(patch.getField())
								.codeKey("ValidCodeDuplicate").description(patch.getField() + " already exists"));
					}
					break;
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	public CustomerProfileDoc createprofile(CustomerProfileDoc req) {
		CustomerProfileDoc doc = findById(req.getId(), CustomerProfileDoc.class);
		if(ArgUtil.is(doc)) {
			updateProfile(req,doc);
		}else {
			doc= new CustomerProfileDoc();
			
			if(ArgUtil.is(req.getName())) {
				PBName pbName=new PBName();
				pbName.setFirstName(req.getName().getFirstName());
				pbName.setLastName(req.getName().getLastName());
				pbName.setMiddleName(req.getName().getMiddleName());
				pbName.setFormattedName(req.getName().getFormattedName());
				pbName.fix();
				doc.setName(pbName);
			}
			
			
			if(ArgUtil.is(findProfileByCode(req.getCode()))){
				ApiResponseUtil.throwInputException(new ApiFieldError().obzect("code").field("code")
						.codeKey("ValidCodeDuplicate").description(req.getCode() + " already exists"));
			}
			
			doc.setCode(req.getCode());
			doc.setRmCode(req.getRmCode());
			 Set<PBEmail> emails = new HashSet<>();
			if(req.getEmails()!=null && !req.getEmails().isEmpty()){
				 Set<PBEmail> reqEmails =req.getEmails();
				 
				for(PBEmail pbEmail:reqEmails) {
					PBEmail pb =new PBEmail();
					if(ArgUtil.is(findProfileByEmail(pbEmail.getEmail()))){
						ApiResponseUtil.throwInputException(new ApiFieldError().obzect("email").field("email")
								.codeKey("ValidEmailDuplicate").description(pbEmail.getEmail() + " already exists"));
					}
					pb.setUuid(ArgUtil.parseAsString(pb.getUuid(), UniqueID.generateString()));
					emails.add(pb.update(pbEmail));
				}
				doc.setEmails(emails);
			}
			Set<PBPhone> phones = new HashSet<>();
			if(req.getPhones()!=null && !req.getPhones().isEmpty()) {
				Set<PBPhone> reqPhones =req.getPhones();
				for(PBPhone phone:reqPhones) {
					if(ArgUtil.is(findProfileByPhone(phone.getPhone()))){
						ApiResponseUtil.throwInputException(new ApiFieldError().obzect("phone").field("phone")
								.codeKey("ValidPhoneDuplicate").description(phone.getPhone() + " already exists"));
					}
					PBPhone pb =parsePhone(phone);
					pb.setUuid(ArgUtil.parseAsString(pb.getUuid(), UniqueID.generateString()));
					phones.add(pb);
				}
				doc.setPhones(phones);
			}
			if(req.getAddresses()!=null && !req.getAddresses().isEmpty()) {
				Set<PBAddress> pAddresses=new HashSet<>();
				for(PBAddress adre:req.getAddresses()) {
					PBAddress pa =new PBAddress();
					pa.setUuid(ArgUtil.parseAsString(adre.getUuid(), UniqueID.generateString()));
					pa.update(adre);
					pAddresses.add(pa);
					
				}
				doc.setAddresses(pAddresses);
				
			}
			
			if(req.getWorks()!=null && !req.getWorks().isEmpty()) {
				doc.setWorks(req.getWorks());
			}
			
			if(req.getUrls()!=null && !req.getUrls().isEmpty()) {
				Set<PBWebsite> pws=new HashSet<>();
				for(PBWebsite ws:req.getUrls()) {
					PBWebsite pWebsite=new PBWebsite();
					pWebsite.setUuid(ArgUtil.parseAsString(ws.getUuid(), UniqueID.generateString()));
					pws.add(pWebsite.update(ws));
					
				}
				doc.setUrls(pws);
			}
			Map<String,Object> addInfoMap=new HashMap<String,Object>();
			if(req.getAdditionalInfo()!=null && !req.getAdditionalInfo().isEmpty()) {
				Map<String,Object> addInfo=req.getAdditionalInfo();
				 /** Retrieve all the key-value pairs from the map **/
		        Set<Map.Entry<String, Object>> entries = addInfo.entrySet();
		        for (Map.Entry<String, Object> entry : entries) {
		            LOGGER.info("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		           switch (entry.getKey()) {
		           case "emails":
		            case "alt_emails":
		            	List<Object> emailtList=(List<Object>)entry.getValue();
		            	Set<PBEmail> pbEmails =new TreeSet<PBEmail>();
		            	for(Object obj:emailtList) {
		            		Map<String, Object> pMap=JsonUtil.toJsonMap(obj);
		            		PBEmail pbEmail=new PBEmail();
		            		for(Map.Entry<String, Object> eMapmail : pMap.entrySet()) {
		            		 if(eMapmail.getKey().contains("email")) {
		            			 pbEmail.setEmail(ArgUtil.parseAsString(eMapmail.getValue(), Constants.BLANK));
		            		 }else if(eMapmail.getKey().contains("label")) {
		            			 pbEmail.setLabel(ArgUtil.parseAsString(eMapmail.getValue(), Constants.BLANK));
		            		 }else if(eMapmail.getKey().contains("type")) {
		            			 pbEmail.setType(ArgUtil.parseAsString(eMapmail.getValue(), Constants.BLANK));
		            		 }
		            		}
		            		 pbEmail.setUuid(UniqueID.generateString());
		            		 pbEmails.add(pbEmail);
		            		
		            	}
		            	addInfoMap.put(entry.getKey(),pbEmails);	
		 			  break;
		            case "phones":
		            case "alt_phones":
		            	List<Object> phoneLstList=(List<Object>)entry.getValue();
		            	Set<PBPhone> pbPhones =new TreeSet<PBPhone>();
		            	for(Object obj:phoneLstList) {
		            		Map<String, Object> pMap=JsonUtil.toJsonMap(obj);
		            		PBPhone ph = parsePhone(new PBPhone().phone(pMap.get("phone").toString()));
		            		ph.setUuid(ArgUtil.parseAsString(ph.getUuid(), UniqueID.generateString()));
		            		if(ArgUtil.is(ph))
		            		 pbPhones.add(ph);
		            	}
		            	addInfoMap.put(entry.getKey(),pbPhones);	
		 			  break;
		            case "title":
		            case "Title": 
		            	addInfoMap.put(entry.getKey(), entry.getValue());
		            break;	
		            case "gender":
		            case "Gender": 
		            	addInfoMap.put(entry.getKey(), entry.getValue());
		            break;
		            case "dob":
		            case "DOB": 
		            	addInfoMap.put(entry.getKey(), entry.getValue());
		            break;
		            default:
		            	Object object=checkFieldType(entry.getKey(),entry.getValue());
		            	if(ArgUtil.isEmpty(object)){
		            		LOGGER.info("Json Util else  :"+JsonUtil.toJson(object)+"\t key-value :"+entry.getKey()+"-"+JsonUtil.toJson(entry.getValue()));
		            	}else {
		            		addInfoMap.put(entry.getKey(), entry.getValue());
		            	}
		            }
		        }
		        
			}
			doc.setAdditionalInfo(addInfoMap);
			
			doc.setCreated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
			mongoTemplate.save(doc);
			
		}
		return doc;
	}

	public Object checkFieldType(String code,Object value) {
		Object objType=null;
		if (ArgUtil.is(code) && ArgUtil.is(value)) {
			Query qryQuery=new Query();
			qryQuery.addCriteria(Criteria.where("code").is(code).and("active").is(true));
			List<CustomerFieldMasterDoc> cmFieldDoc = cMongoTemplate.find(qryQuery, CustomerFieldMasterDoc.class);
			if(cmFieldDoc!=null && !cmFieldDoc.isEmpty()) {
				Object fldType=cmFieldDoc.get(0).getType();
				if(ArgUtil.is(fldType)) {
					objType =ArgUtil.parseAsT(fldType,new String(),false);
				}
			}
		return objType;
		}
		return objType;
	}
	
	private String getFileName(String additionalInfo) {
		  String fldCode = Arrays.stream(additionalInfo.split("\\."))
                .skip(1)
                .findFirst()
                .orElse(additionalInfo);
		  return fldCode;
	}
	
	@SuppressWarnings("unchecked")
	public void updateProfile(CustomerProfileDoc req, CustomerProfileDoc doc) {
		ObjectMapper objectMapper = new ObjectMapper();
		
		if(ArgUtil.is(req.getCode())){
			CustomerProfileDoc docCode =findProfileByCode(req.getCode());
			if(ArgUtil.is(docCode) && !docCode.getId().equalsIgnoreCase(doc.getId())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().obzect("code").field("code")
						.codeKey("ValidCodeDuplicate").description(req.getCode() + " already exists"));
			}else {
				doc.setCode(ArgUtil.parseAsString(req.getCode(), doc.getCode()));
			}
		}

		doc.setRmCode(ArgUtil.parseAsString(req.getRmCode(), doc.getRmCode()));
		if (ArgUtil.is(req.getName())) {
			PBName pbName = new PBName();
			pbName.setFirstName(ArgUtil.parseAsString(req.getName().getFirstName(), doc.getName().getFirstName()));
			pbName.setLastName(ArgUtil.parseAsString(req.getName().getMiddleName(), doc.getName().getMiddleName()));
			pbName.setMiddleName(ArgUtil.parseAsString(req.getName().getLastName(), doc.getName().getLastName()));
			pbName.setFormattedName(
					ArgUtil.parseAsString(req.getName().getFormattedName(), doc.getName().getFormattedName()));
			pbName.fix();
			doc.setName(pbName);
		}

		if (req.getPhones() != null && !req.getPhones().isEmpty()) {
			Set<PBPhone> reqPhones = req.getPhones();
			for (PBPhone reqph : reqPhones) {
				Optional<PBPhone> found = Optional.empty();
				String uuid = reqph.getUuid();
				found = doc.getPhones().stream().filter(phone -> phone.getUuid().equals(uuid)).findFirst();
				if (found.isPresent()) {
					found.get().update(reqph);
				} else {
					PBPhone pb = parsePhone(reqph);
					pb.setUuid(ArgUtil.parseAsString(pb.getUuid(), UniqueID.generateString()));
					doc.getPhones().add(pb);
				}

			}
		}
		if (req.getEmails() != null && !req.getEmails().isEmpty()) {
			Set<PBEmail> reqPbEmails = req.getEmails();
			for (PBEmail reqEm : reqPbEmails) {
				Optional<PBEmail> found = Optional.empty();
				String uuid = reqEm.getUuid();
				found = doc.getEmails().stream().filter(email -> email.getUuid().equals(uuid)).findFirst();
				if (found.isPresent()) {
					found.get().update(reqEm);
				} else {
					PBEmail pbEm = new PBEmail();
					pbEm.setUuid(ArgUtil.parseAsString(pbEm.getUuid(), UniqueID.generateString()));
					pbEm.update(reqEm);
					doc.getEmails().add(pbEm);
				}

			}

		}

		Map<String, Object> addInfoMap = req.getAdditionalInfo();
		if (ArgUtil.is(addInfoMap)) {
			for (Map.Entry<String, Object> entry : addInfoMap.entrySet()) {
				LOGGER.info("Key: " + entry.getKey() + ", Value: " + entry.getValue());
				switch (entry.getKey()) {
				case "title":
				case "Title":
					addInfoMap.put(entry.getKey(), ArgUtil.parseAsString(entry.getValue(),
							doc.getAdditionalInfo().get(entry.getKey())==null?Constants.BLANK:doc.getAdditionalInfo().get(entry.getKey()).toString()));
					break;
				case "gender":
				case "Gender":
					addInfoMap.put(entry.getKey(), ArgUtil.parseAsString(entry.getValue(),
							doc.getAdditionalInfo().get(entry.getKey())==null?Constants.BLANK:doc.getAdditionalInfo().get(entry.getKey()).toString()));
					break;
				case "dob":
				case "DOB":
					addInfoMap.put(entry.getKey(), ArgUtil.parseAsString(entry.getValue(),
							doc.getAdditionalInfo().get(entry.getKey())==null?Constants.BLANK:doc.getAdditionalInfo().get(entry.getKey()).toString()));
					break;
				case "emails":
				case "alt_emails":
					List<PBEmail> pbEmails=objectMapper.convertValue(entry.getValue(), new TypeReference<List<PBEmail>>() {});
					Set<PBEmail> spbmails =addUpdateEmail(pbEmails,doc);
					addInfoMap.put(entry.getKey(),spbmails);
					break;
				case "phone":
				case "alt_phones":
					List<PBPhone> pbPhones = objectMapper.convertValue(entry.getValue(), new TypeReference<List<PBPhone>>() {});
					Set<PBPhone> spbPhone =addUpdatePhone(pbPhones, doc);
					addInfoMap.put(entry.getKey(),spbPhone);
					break;	
					
				default:
					Object object = checkFieldType(entry.getKey(), entry.getValue());
					if (ArgUtil.isEmpty(object)) {
						LOGGER.info("Json Util else  :" + JsonUtil.toJson(object) + "\t key-value :" + entry.getKey()
								+ "-" + JsonUtil.toJson(entry.getValue()));
					} else {
						addInfoMap.put(entry.getKey(),
								ArgUtil.parseAsT(entry.getValue(), doc.getAdditionalInfo().get(entry.getKey()), false));
					}
				}
			}

		}
		doc.setAdditionalInfo(addInfoMap);
		
		doc.setUpdated(TimeStampIndex.now().by(auditDetailProvider.getAuditUser()));
		mongoTemplate.save(doc);
	}

	private Set<PBPhone> addUpdatePhone(List<PBPhone> reqPhones, CustomerProfileDoc doc) {
	    Set<PBPhone> setPbPhone = new TreeSet<>();
	    for (PBPhone reqph : reqPhones) {
	        Optional<PBPhone> found = Optional.empty();
	        String uuid = reqph.getUuid();
	        found = doc.getPhones().stream()
	                   .filter(phone -> phone.getUuid().equals(uuid))
	                   .findFirst();
	        if (found.isPresent()) {
	            found.get().update(reqph);
	            setPbPhone.add(found.get());  // Add the updated phone
	        } else {
	            PBPhone pb = parsePhone(reqph);
	            pb.setUuid(ArgUtil.parseAsString(pb.getUuid(), UniqueID.generateString()));
	            doc.getPhones().add(pb);
	            setPbPhone.add(pb);  // Add the new phone
	        }
	    }
	    return setPbPhone;
	}

	
	private Set<PBEmail> addUpdateEmail(List<PBEmail> reqEmail, CustomerProfileDoc doc) {
		Set<PBEmail> setPbEmail = new TreeSet<>();
		for (PBEmail reqEm : reqEmail) {
			Optional<PBEmail> found = Optional.empty();
			String uuid = reqEm.getUuid();
			found = doc.getEmails().stream().filter(email -> email.getUuid().equals(uuid)).findFirst();
			if (found.isPresent()) {
				found.get().update(reqEm);
				setPbEmail.add(found.get());  // Add the updated phone
			} else {
				PBEmail pbEm = new PBEmail();
				pbEm.setUuid(ArgUtil.parseAsString(pbEm.getUuid(), UniqueID.generateString()));
				pbEm.update(reqEm);
				setPbEmail.add(pbEm);
			}

		}
		return setPbEmail;
	}
}

