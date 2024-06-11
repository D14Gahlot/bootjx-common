package com.boot.jx.postman.manager;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.CustomerMasterFieldDoc;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Component
public class StarterDocKit {

	public static final Logger LOGGER = LoggerService.getLogger(StarterDocKit.class);

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
    CommonMongoTemplate commonMongoTemplate;

	private QuickMedia createTemplateReply(String name, String title, String category, String content, String url) {
		QuickMedia temp5 = mongoTemplate.findById(name, QuickMedia.class);
		if (ArgUtil.isEmpty(temp5)) {
			temp5 = new QuickMedia();
		}
		temp5.setId(name);
		temp5.setCode(name);
		temp5.setTitle(title);
		temp5.setType("IMAGE");
		temp5.setCategory(category);
		temp5.setUrl(url);
		temp5.setContent(content);
		return temp5;
	}

	private QuickReply createQuickReply(String id, String title, String category) {
		QuickReply temp5 = mongoTemplate.findById(id, QuickReply.class);
		if (ArgUtil.isEmpty(temp5)) {
			temp5 = new QuickReply();
		}
		temp5.setId(id);
		temp5.setTitle(title);
		temp5.setCategory(category);
		return temp5;
	}

	private void createClientApp(ClientAppConfigDoc clientAppConfig) {
		ClientAppConfigDoc app = mongoTemplate.findById(clientAppConfig.getId(), ClientAppConfigDoc.class);
		if (ArgUtil.isEmpty(app) || !ArgUtil.areEqual(app.getKeyVersion(), clientAppConfig.getKeyVersion())) {
			try {
				mongoTemplate.save(clientAppConfig);
			} catch (Exception e) {
				LOGGER.error("createClientAppErrror:" + clientAppConfig.getKeyName(), e);
			}
		}
	}
	
	private void createPredefinedMstField(CustomerMasterFieldDoc cusMasterFld) {
		
		
		CustomerMasterFieldDoc titelMstDoc= new CustomerMasterFieldDoc();
		titelMstDoc.setFieldCode("title");
		titelMstDoc.setFieldLabel("Title");
		titelMstDoc.setFieldType("String");
		titelMstDoc.setFieldDesc("Title");
		titelMstDoc.setIsactive(Constants.YES);
		titelMstDoc.setPredefined(true);
		titelMstDoc.setRequired(false);
		titelMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(titelMstDoc);
		
		Query qryQuery = new Query();
		Criteria criteria = Criteria.where("fieldCode").is(cusMasterFld.getFieldCode());
		qryQuery.addCriteria(criteria);
		CustomerMasterFieldDoc app = mongoTemplate.findOne(qryQuery, CustomerMasterFieldDoc.class);
		if (ArgUtil.isEmpty(app)) {
			try {
				commonMongoTemplate.save(cusMasterFld);
			} catch (Exception e) {
				LOGGER.error("createClientAppErrror:" + cusMasterFld.getFieldCode(), e);
			}
		}
	}
	
	

	@PostConstruct
	public void init() {

		try {
			createDefaultTemplats();

		} catch (Exception e) {
			e.printStackTrace();
		}

		ClientAppConfigDoc agentApp = new ClientAppConfigDoc();
		agentApp.setId(PMConstants.DEFAULT.AGENT_QUEUE_CODE);
		agentApp.setKeyName("Agent Desk");
		agentApp.setQueue(PMConstants.DEFAULT.AGENT_QUEUE_CODE);
		agentApp.setAppType(APP_TYPE.AGENT.name());
		agentApp.setKey(PostManUtil.UNIQUE_API_KEY());
		agentApp.setKeyVersion("v3");
		agentApp.setShared(true);
		createClientApp(agentApp);

		ClientAppConfigDoc botApp = new ClientAppConfigDoc();
		botApp.setId(PMConstants.DEFAULT.BOT_QUEUE_CODE);
		botApp.setKeyName("Basic Bot");
		botApp.setQueue(PMConstants.DEFAULT.BOT_QUEUE_CODE);
		botApp.setAppType(APP_TYPE.BOT.name());
		botApp.setKey(PostManUtil.UNIQUE_API_KEY());
		botApp.setKeyVersion("v3");
		botApp.setShared(true);
		createClientApp(botApp);

		ClientAppConfigDoc adminApp = new ClientAppConfigDoc();
		adminApp.setId(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		adminApp.setKeyName("Admin App");
		adminApp.setQueue(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		adminApp.setAppType(APP_TYPE.DEFAULT.name());
		adminApp.setKey(PostManUtil.UNIQUE_API_KEY());
		adminApp.setKeyVersion("v4");
		adminApp.setShared(true);
		createClientApp(adminApp);

		ClientAppConfigDoc feedbackApp = new ClientAppConfigDoc();
		feedbackApp.setId(PMConstants.DEFAULT.FEEDBACK_QUEUE_CODE);
		feedbackApp.setKeyName("Feedback Collector");
		feedbackApp.setQueue(PMConstants.DEFAULT.FEEDBACK_QUEUE_CODE);
		feedbackApp.setAppType(APP_TYPE.FEEDBACK.name());
		feedbackApp.setKey(PostManUtil.UNIQUE_API_KEY());
		feedbackApp.setKeyVersion("v4");
		feedbackApp.setShared(true);
		createClientApp(feedbackApp);
		
		/** pre-defined master flds**/
		CustomerMasterFieldDoc titelMstDoc= new CustomerMasterFieldDoc();
		titelMstDoc.setFieldCode("title");
		titelMstDoc.setFieldLabel("Title");
		titelMstDoc.setFieldType("String");
		titelMstDoc.setFieldDesc("Title");
		titelMstDoc.setIsactive(Constants.YES);
		titelMstDoc.setPredefined(true);
		titelMstDoc.setRequired(false);
		titelMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(titelMstDoc);
		
		
		CustomerMasterFieldDoc dobMstDoc= new CustomerMasterFieldDoc();
		dobMstDoc.setFieldCode("dob");
		dobMstDoc.setFieldLabel("Date of Birth");
		dobMstDoc.setFieldType("timestamp");
		dobMstDoc.setFieldDesc("Date of Birth");
		dobMstDoc.setIsactive(Constants.YES);
		dobMstDoc.setPredefined(true);
		dobMstDoc.setRequired(false);
		dobMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(dobMstDoc);
		
		CustomerMasterFieldDoc genderMstDoc= new CustomerMasterFieldDoc();
		genderMstDoc.setFieldCode("gender");
		genderMstDoc.setFieldLabel("Gender");
		genderMstDoc.setFieldType("string");
		genderMstDoc.setFieldDesc("Gender");
		genderMstDoc.setIsactive(Constants.YES);
		genderMstDoc.setPredefined(true);
		genderMstDoc.setRequired(false);
		genderMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(genderMstDoc);
		
		CustomerMasterFieldDoc altPhoneMstDoc= new CustomerMasterFieldDoc();
		altPhoneMstDoc.setFieldCode("alt_phones");
		altPhoneMstDoc.setFieldLabel("Alternate Phone number");
		altPhoneMstDoc.setFieldType("string");
		altPhoneMstDoc.setFieldDesc("Alternate Phone number");
		altPhoneMstDoc.setIsactive(Constants.YES);
		altPhoneMstDoc.setPredefined(true);
		altPhoneMstDoc.setRequired(false);
		altPhoneMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(altPhoneMstDoc);
		
		CustomerMasterFieldDoc altEmailMstDoc= new CustomerMasterFieldDoc();
		altEmailMstDoc.setFieldCode("alt_emails");
		altEmailMstDoc.setFieldLabel("Alternate email id");
		altEmailMstDoc.setFieldType("string");
		altEmailMstDoc.setFieldDesc("Alternate email id");
		altEmailMstDoc.setIsactive(Constants.YES);
		altEmailMstDoc.setPredefined(true);
		altEmailMstDoc.setRequired(false);
		altEmailMstDoc.setCreated(TimeStampIndex.now());
		createPredefinedMstField(altEmailMstDoc);
		
		
		
		
		
		
	}

	private void createDefaultTemplats() {
		mongoTemplate.save(createTemplateReply("GIRL_AND_BIKE", "Girl and bike", "Gallery1", "See this Nice Pic",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688334/samples/bike.jpg"));

		mongoTemplate.save(createTemplateReply("OFFICE_N_WORK", "Office & Work", "Gallery1", "Work environment",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688339/samples/imagecon-group.jpg"));

		mongoTemplate.save(createTemplateReply("KITTEN_PLAYING", "Kitten Playing", "Animals", "Happy Kitten",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688341/samples/animals/kitten-playing.gif"));

		mongoTemplate.save(createTemplateReply("THREE_DOGS", "Three Dogs", "Animals", "Gang of Dogs",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688335/samples/animals/three-dogs.jpg"));

		mongoTemplate.save(createTemplateReply("REINDEER", "Reindeer", "Animals", "In Snow",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688331/samples/animals/reindeer.jpg"));

		mongoTemplate.save(createTemplateReply("CAT", "Cat", "Animals", "Bad Cat",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688330/samples/animals/cat.jpg"));

		mongoTemplate.save(createTemplateReply("ACCESSORIES BAG", "Accessories Bag", "Ecommerce", "Cool bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/accessories-bag.jpg"));

		mongoTemplate.save(createTemplateReply("LEATHER BAG GRAY", "Leather Bag Gray", "Ecommerce", "Formal bag",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688338/samples/ecommerce/leather-bag-gray.jpg"));

		mongoTemplate.save(createTemplateReply("SHOES", "Shoes", "Ecommerce", "Purple Shoes",
				"https://res.cloudinary.com/www-mehery-com/image/upload/v1611688333/samples/ecommerce/shoes.png"));

		// Quick Replies
		mongoTemplate.save(createQuickReply("0", "Hello", "greeting"));
		mongoTemplate.save(createQuickReply("1", "Very Good Morning", "greeting-morning"));
		mongoTemplate.save(createQuickReply("2", "Very Good After Noon", "greeting-afternoon"));
		mongoTemplate.save(createQuickReply("3", "Very Good Evening", "greeting-evening"));
		mongoTemplate.save(createQuickReply("4", "Nice talking too.", "conversation-complete"));
		mongoTemplate.save(createQuickReply("5", "You're welcome.", "conversation-complete"));
	}

	public void domain() {
		/**
		 * Required to create index
		 * 
		 * @param contactType
		 */
		for (ContactType contactType : ContactType.values()) {
			MessageDoc wa = MessageDoc.instance(contactType);
			mongoTemplate.save(wa);
			mongoTemplate.remove(wa);
		}

	}

}
