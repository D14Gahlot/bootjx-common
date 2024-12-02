package com.boot.jx.admin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.amplify.model.JobStatus;
import com.boot.jx.admin.dto.ProfileSearchCriteria;
import com.boot.jx.admin.dto.ProfileSearchQuery;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.CONFIG_SETUP_KEY;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.doc.GroupDoc;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.common.dto.GroupSessionDto;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.QA;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.CommonServiceClient;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.ProfileFilterMasterDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.tunnel.ChronoScheduler;
import com.boot.jx.tunnel.TunnelService;
import com.boot.jx.tunnel.task.BatchJobExecuter;
import com.boot.jx.tunnel.task.JobTaskModel;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.jx.tunnel.task.JobTaskModel.JOB_STATUS;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.PhoneUtil;
import com.boot.utils.UniqueID;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.client.MongoCursor;

@Component
public class BulkMessageService extends BatchJobExecuter {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private AuditDetailProvider auditDetailProvider;

	@Autowired
	private PMEnvironment enviroment;

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	CommonServiceClient commonSerClient;
	
	@Autowired
	CustomerProfileService cusProfileService;

	public void registerJobAndTriggerSummary(BatchJob job) {
		registerJob(job);
		tunnelService.task("CAMPAIGN_CREATED",
				MapModel.createInstance().putAll(job.data()).put("bulkSessionId", job.getJobId()).toMap());
	}

	public void registerJob(BatchJob job, ChronoScheduler scheduler) {
		if (!ArgUtil.is(scheduler)) {
			registerJobAndTriggerSummary(job);
		} else if (ArgUtil.is(scheduler) && !StringUtils.isBlank(scheduler.getTopic())
				&& scheduler.getTopic().equalsIgnoreCase("CANCELLED")) {
			Map<String, Object> data = job.getData();
			data.put("jobId", job.getJobId());
			scheduler.setData(data);
			commonSerClient.schedule(scheduler);
		} else {
			scheduler = ArgUtil.nonEmpty(scheduler, ChronoScheduler.task());
			scheduler.setTopic("BulkMessageTask");
			tunnelService.schedule(job.scheduler(scheduler));
		}
	}

	public BulkSessionDoc send(OutboxMessage bulkMessage, ChronoScheduler scheduler) throws NumberParseException {

		String channelId = PostManUtil.CHANNEL_ID(bulkMessage.contact());

		ChannelConfig channelConfig = enviroment.config().channel(channelId);
		HSMTemplateDoc templateDoc = mongoTemplate.findById(bulkMessage.templateId(), HSMTemplateDoc.class);
		CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
		if (ArgUtil.is(templateDoc) && !ArgUtil.is(bulkMessage.getHsm().getCode())) {
			hsmTemp.setId(bulkMessage.templateId());
			hsmTemp.setCode(templateDoc.getCode());
			hsmTemp.setData(bulkMessage.getHsm().getData());
			bulkMessage.setHsm(hsmTemp);
		}
		BulkSessionDoc session = new BulkSessionDoc();
		session.setMessage(bulkMessage.getMessage());
		session.setTemplateId(bulkMessage.templateId());
		session.setTemplate(bulkMessage.templateCode());
		session.setMessageCount(bulkMessage.getTo().size());
		session.setContactType(bulkMessage.contact().getContactType());
		session.setLane(bulkMessage.contact().getLane());
		session.setCampaignTitle(bulkMessage.getCampaignTitle());
		session.setChannelId(channelId);
		session.setBulkSessionId(UniqueID.generateString62());
		session.setScheduler(scheduler);

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (String to : bulkMessage.getTo()) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMessage);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			to = PhoneUtil.addPlusSign(to);
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().phone(to);
			doc.setMessage(bulkMessage.getMessage());
			doc.setHsm(bulkMessage.getHsm());
			doc.setHsm(hsmTemp);
			doc.setTemplateId(bulkMessage.templateId());
			doc.setTemplate(bulkMessage.templateCode());
			doc.setAttachments(bulkMessage.getAttachments());
			doc.route().setQueueCode(adminApp.getQueue());
			doc.route().setSendMode(adminApp.getAppMode());
			doc.route().setSenderApp(adminApp.getAppType());
			doc.route().setSenderType(MESSAGE_SENDER_TYPE.ADMIN);
			doc.route().setSenderCode(auditDetailProvider.getAuditUser());

			docs.add(doc);
		}

		session.setStatus("CREATED");
		mongoTemplate.save(session);
		messageStore.insert(docs, bulkMessage.contact().type());

		registerJob(// Create batch Job to pass
				JobTaskModel.newBatchJob()
						// Set Unique Job Id
						.jobId(session.getBulkSessionId())
						// Contact Type for each message
						.data("contactType", session.getContactType())
						// Channel for each message
						.data("channelType", channelConfig.getChannelType())
						// Lane for each message
						.data("lane", session.getLane())

				, scheduler);

		return session;
	}

	public BulkSessionDoc sendMultiple(List<OutboxMessage> bulkMessages, ChronoScheduler scheduler)
			throws NumberParseException {

		OutboxMessage bulkMessage = bulkMessages.get(0);
		String channelId = PostManUtil.CHANNEL_ID(bulkMessage.contact());
		ChannelConfig channelConfig = enviroment.config().channel(channelId);
		BulkSessionDoc session = new BulkSessionDoc();
		session.setMessage(bulkMessage.getMessage());
		session.setTemplateId(bulkMessage.templateId());
		session.setTemplate(bulkMessage.templateCode());
		session.setMessageCount(bulkMessage.getTo().size());
		session.setContactType(bulkMessage.contact().getContactType());
		session.setLane(bulkMessage.contact().getLane());
		session.setChannelId(channelId);
		session.setBulkSessionId(UniqueID.generateString62());
		session.setCampaignTitle(bulkMessage.getCampaignTitle());
		session.setScheduler(scheduler);
		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (OutboxMessage bulkMsg : bulkMessages) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMsg);
			String to = bulkMsg.getTo().get(0);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			to = PhoneUtil.addPlusSign(to);
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().phone(to);
			doc.setMessage(bulkMsg.getMessage());
			doc.setHsm(bulkMsg.getHsm());
			doc.setTemplateId(bulkMsg.templateId());
			doc.setTemplate(bulkMsg.templateCode());
			doc.setAttachments(bulkMsg.getAttachments());

			doc.route().setQueueCode(adminApp.getQueue());
			doc.route().setSendMode(adminApp.getAppMode());
			doc.route().setSenderApp(adminApp.getAppType());
			doc.route().setSenderType(MESSAGE_SENDER_TYPE.ADMIN);
			doc.route().setSenderCode(auditDetailProvider.getAuditUser());

			docs.add(doc);
		}

		session.setStatus("CREATED");
		mongoTemplate.save(session);
		messageStore.insert(docs, bulkMessage.contact().type());
		registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()), scheduler);

		return session;
	}

	public BulkSessionDoc sendToGroup(List<OutboxMessage> bulkMessages, ChronoScheduler scheduler)
			throws NumberParseException {
		
		OutboxMessage bulkMessage = bulkMessages.get(bulkMessages.size()-1);
		String channelId = PostManUtil.CHANNEL_ID(bulkMessage.contact());
		ChannelConfig channelConfig = enviroment.config().channel(channelId);
		BulkSessionDoc session = new BulkSessionDoc();
		session.setMessage(bulkMessage.getMessage());
		session.setTemplateId(bulkMessage.templateId());
		session.setTemplate(bulkMessage.templateCode());
		session.setMessageCount(bulkMessage.getTo().size());
		session.setContactType(bulkMessage.contact().getContactType());
		session.setLane(bulkMessage.contact().getLane());
		session.setChannelId(channelId);
		session.setBulkSessionId(UniqueID.generateString62());
		session.setGroupId(bulkMessage.getGroupId());
		session.setCampaignTitle(bulkMessage.getCampaignTitle());
		session.setGroupName(bulkMessage.getGroupName());
		session.setScheduler(scheduler);
		session.setGroups(bulkMessage.getGroups());
		List<String> toLst = bulkMessage.getTo();

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		//for (OutboxMessage bulkMsg : bulkMessages) {
		   for (String to : toLst) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMessage);
			//String to = bulkMsg.getTo().get(0);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			to = PhoneUtil.addPlusSign(to);
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().phone(to);
			doc.setMessage(bulkMessage.getMessage());
			doc.setHsm(bulkMessage.getHsm());
			doc.setTemplateId(bulkMessage.templateId());
			doc.setTemplate(bulkMessage.templateCode());
			doc.setAttachments(bulkMessage.getAttachments());
			doc.route().setQueueCode(adminApp.getQueue());
			doc.route().setSendMode(adminApp.getAppMode());
			doc.route().setSenderApp(adminApp.getAppType());
			doc.route().setSenderType(MESSAGE_SENDER_TYPE.ADMIN);
			doc.route().setSenderCode(auditDetailProvider.getAuditUser());

			docs.add(doc);
			
		}
		   
		session.setStatus("CREATED");
		mongoTemplate.save(session);
		messageStore.insert(docs, bulkMessage.contact().type());
		registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()), scheduler);

		return session;
	}

	@Override
	public BatchJob resetJob(String jobId) {
		BatchJob oldJob = stopJob(jobId);
		BulkSessionDoc session = mongoTemplate.findById(jobId, BulkSessionDoc.class);
		session.setStatus("CREATED");
		mongoTemplate.save(session);

		String channelId = ArgUtil.nonEmpty(session.getChannelId(),
				PostManUtil.CHANNEL_ID(session.getContactType(), "", session.getLane()));

		ChannelConfig channelConfig = enviroment.config().channel(channelId);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		Query query = new Query().addCriteria(
				QueryCriteria.where("bulkSessionId").is(oldJob.getJobId()).and("stamps.SENT").exists(false));
		builder.set("status", Status.SCHLD.toString());

		messageStore.updateMulti(query, builder.update(), MessageStore.getCollectionName(session.getContactType()));

		return registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()));
	}
	
	@Override
	public BatchJob stopJob(String jobId) {
		BatchJob oldJob = stopJob(jobId);
		BulkSessionDoc session = mongoTemplate.findById(jobId, BulkSessionDoc.class);
		session.setStatus(Status.STOPPED.toString());
		mongoTemplate.save(session);

		String channelId = ArgUtil.nonEmpty(session.getChannelId(),
				PostManUtil.CHANNEL_ID(session.getContactType(), "", session.getLane()));

		ChannelConfig channelConfig = enviroment.config().channel(channelId);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		Query query = new Query().addCriteria(
				QueryCriteria.where("bulkSessionId").is(oldJob.getJobId()).and("stamps.SENT").exists(false));
		builder.set("status", Status.STOPPED.toString());

		messageStore.updateMulti(query, builder.update(), MessageStore.getCollectionName(session.getContactType()));

		return registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()));
	}
	

	@Override
	public boolean read(BatchJob currentBatchJob) {
		BulkSessionDoc doc = mongoTemplate.findById(currentBatchJob.getJobId(), BulkSessionDoc.class);
		Query query = new Query().addCriteria(QueryCriteria.where("bulkSessionId").is(currentBatchJob.getJobId())
				.and("status").is(Status.SCHLD.toString())).limit(25);

		ContactType contactType = doc.contactType();
		List<MessageDoc> msgs = messageStore.find(query, contactType);

		if (!ArgUtil.is(msgs) || msgs.size() == 0) {
			return true; // Reading is Finished
		}

		if (!ArgUtil.areEqual(currentBatchJob.getStatus(), doc.getStatus())) {
			doc.setStatus(currentBatchJob.getStatus().toString());
			mongoTemplate.save(doc);
		}

		for (MessageDoc messageDoc : msgs) {
			push(JobTaskModel.newTasklet(currentBatchJob).taskId(messageDoc.getMessageId()));
			messageDoc.updateStatus(Status.CRTD);
			messageStore.updateStatus(contactType, messageDoc, Status.CRTD, null);
		}
		return false;
	}

	@Scheduled(fixedDelay = 1000)
	public void scheduler2() {
		this.execute();
	}

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Override
	public void execute(BatchJob taskJob, Tasklet tasklet) {
		LOGGER.debug("execute(BatchJob {}, Tasklet {})", taskJob.getJobId(), tasklet.getTaskId());
		String messageId = tasklet.getTaskId();
		ContactType contactType = taskJob.data().entry("contactType").asEnum(ContactType.class);
		String channelType = taskJob.data().entry("channelType").asString();
		String lane = taskJob.data().entry("lane").asString();
		MessageDoc msg = messageStore.findByMessageId(messageId, contactType);

		if (ArgUtil.is(msg) && !msg.stamps().containsKey("SENT")) {
			OutboxMessage outboxMessage = new OutboxMessage();
			outboxMessage.setMessageId(msg.getMessageId());
			outboxMessage.setMessage(msg.getMessage());
			outboxMessage.setHsm(msg.getHsm());
			outboxMessage.template(msg.getTemplate());
			outboxMessage.templateId(msg.getTemplateId());
			outboxMessage.setAttachments(msg.getAttachments());
			outboxMessage.contact().type(contactType);
			outboxMessage.contact().setChannelType(channelType);
			outboxMessage.contact().setLane(lane);
			outboxMessage.contact().setEmail(msg.getContact().getEmail());
			outboxMessage.contact().phone(msg.getContact().phone());
			outboxMessage.contact().setContactId(msg.getContact().getContactId());
			outboxMessage.setRoute(msg.getRoute());

			ChatSessionDoc chatSessionDoc = chatSessionFactory.linkSession(outboxMessage);
			if (ArgUtil.is(chatSessionDoc)) {
				chatSessionService.initSession(outboxMessage, chatSessionDoc);
				chatService.send(chatSessionDoc, outboxMessage);
			} else {
				messageStore.updateStatus(contactType, msg, Status.NSENT, "Cannot create session");
			}
		}
	}

	@Override
	public boolean tally(BatchJob currentBatchJob) {
		BulkSessionDoc doc = mongoTemplate.findById(currentBatchJob.getJobId(), BulkSessionDoc.class);

		ContactType contactType = currentBatchJob.data().entry("contactType").asEnum(ContactType.class);

//		Aggregation agg = Aggregation.newAggregation(
//				Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))), // Match
//				Aggregation.group("status").count().as("count") // Group By Status
//		);
//		AggregationResults<Map> results = mongoTemplate.aggregate(agg, MessageStore.getCollectionName(contactType),
//				Map.class);

		QA list = new QA().add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))),
				QA.project("statuss", QA.objectToArray("stamps")), Aggregation.unwind("statuss"),
				Aggregation.group("statuss.k").count().as("count"));
		;

		// list.add(Aggregation.group("status").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));
//				MongoCollection<Document> col = mongoTemplate.getCollection(MessageStore.getCollectionName(contactType));
//				MongoCursor<Document> cursor = col.aggregate(list).iterator();
//				System.out.println(JsonUtil.toJson(list.piplines()));

		MongoCursor<Document> cursor = mongoTemplate.collection(MessageStore.getCollectionName(contactType))
				.aggregate(list).iterator();

		long totalCount = 0;
		long doneCount = 0;
		// for (Map map : results) {
		while (cursor.hasNext()) {
			Document object = cursor.next();
			if (ArgUtil.is(object)) {
				Status status = ArgUtil.parseAsEnumT(object.get("_id"), Status.class);
				if (ArgUtil.is(status)) {
					long count = ArgUtil.parseAsLong(object.get("count"), 0L);
					doc.stats().put(ArgUtil.parseAsString(status), count);
					if (ArgUtil.isEqual(status, Status.SCHLD)) {
						totalCount = Math.max(totalCount, count);
					}
					// Done Count
					if (ArgUtil.isEqual(status, Status.CRTD, Status.INIT, Status.SENT)) {
						doneCount = Math.max(doneCount, count);
					}
				}
			}
		}
		// }

		// System.out.println("TALLY : " + (totalCount == doneCount) + " -- "
		// +currentBatchJob.getDonePercent());
		boolean completed = (totalCount == doneCount);

		if (completed) {
			doc.setCompletedStamp(System.currentTimeMillis());
		}
		if (!ArgUtil.areEqual(currentBatchJob.getStatus(), doc.getStatus())
				|| !ArgUtil.is(doc.getStatus(), JOB_STATUS.COMPLETED.toString())) {
			doc.setStatus(ArgUtil.parseAsString(currentBatchJob.getStatus(), doc.getStatus()));
			if (completed) {
				doc.setStatus(JOB_STATUS.COMPLETED.toString());
			}
		}
		doc.setJob(currentBatchJob);
		mongoTemplate.save(doc);
		return completed;
	}

	/** Parsing csv file **/
	public BulkSessionDoc uploadFile(MultipartFile file) throws NumberParseException {
		try {
			readFile(file.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String TYPE = "text/csv";

	public static boolean hasCSVFormat(MultipartFile file) {
		if (!TYPE.equals(file.getContentType())) {
			return false;
		}
		return true;
	}

	public void readFile(InputStream is) {
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
				CSVParser csvParser = new CSVParser(fileReader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecords) {
				// System.out.println("id :" + csvRecord.get("contacts"));
			}

		} catch (Exception e) {
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}
	}

	public BulkSessionDoc cancelScheduleJob(BulkSessionDoc bulkDoc) {
		try {
			ChronoScheduler cSch = bulkDoc.getScheduler();
			LOGGER.info("The interval is a valid future date." + cSch.getInterval());
			cSch.setTopic("CANCELLED");
			bulkDoc.setStatus(JobStatus.CANCELLED.toString());
			
			
			MongoQueryBuilder<BulkSessionDoc> builderU = MongoQueryBuilder.collection(BulkSessionDoc.class).whereId(bulkDoc.getBulkSessionId());
			builderU.set("status", JobStatus.CANCELLED.toString());
			mongoTemplate.upsert(builderU);
			
			registerJob(bulkDoc.getJob(), bulkDoc.getScheduler());
			stopJob(bulkDoc.getJob().getJobId());
			CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
			Query query = new Query().addCriteria(QueryCriteria.where("bulkSessionId").is(bulkDoc.getJob().getJobId())
					.and("stamps.SENT").exists(false));
			builder.set("status", JobStatus.CANCELLED.toString());
			messageStore.updateMulti(query, builder.update(), MessageStore.getCollectionName(bulkDoc.getContactType()));
		} catch (Exception e) {
			e.getMessage();
			throw new RuntimeException("The bulk message cancel job could not be found");
		}
		return bulkDoc;
	}
	
	public BulkSessionDoc reSchedule(BulkSessionDoc bulkDoc,ChronoScheduler schedular) {
		BatchJob job = bulkDoc.getJob();
		String jobId = job.getJobId();
		//BatchJob oldJob = stopJob(jobId);
		BulkSessionDoc session = mongoTemplate.findById(jobId, BulkSessionDoc.class);
		session.setStatus("CREATED");
		session.setScheduler(schedular);
		mongoTemplate.save(session);

		String channelId = ArgUtil.nonEmpty(session.getChannelId(),
				PostManUtil.CHANNEL_ID(session.getContactType(), "", session.getLane()));

		ChannelConfig channelConfig = enviroment.config().channel(channelId);

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		Query query = new Query().addCriteria(
				QueryCriteria.where("bulkSessionId").is(job.getJobId()).and("stamps.SENT").exists(false));
		builder.set("status", Status.SCHLD.toString());
		messageStore.updateMulti(query, builder.update(), MessageStore.getCollectionName(session.getContactType()));
		registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()), schedular);
		return session;
	}
	
	public BulkSessionDoc reSend(OutboxMessage bulkMessage,BulkSessionDoc bulkDoc) throws Exception{
		if (ArgUtil.is(bulkMessage.getScheduler())) {
			bulkMessage.templateId(bulkDoc.getTemplateId());
			bulkMessage.message(bulkDoc.getMessage());
			bulkMessage.contact().setLane(bulkDoc.getLane());
			bulkMessage.contact().setContactId(bulkDoc.getChannelId());
			bulkMessage.contact().setContactType(bulkDoc.getContactType());
			bulkMessage.setCampaignTitle(bulkDoc.getCampaignTitle());
			if (ArgUtil.is(bulkDoc.getGroupId()) || ArgUtil.is(bulkDoc.getGroups())) {
				bulkMessage.setGroupId(bulkDoc.getGroupId());
				if (ArgUtil.isEmpty(bulkDoc.getGroups())) {
					bulkMessage.setGroups(Arrays.asList(bulkDoc.getGroupId()));
				}
				List<OutboxMessage> lstOutBoxMsg = getGroupDetailsV1(bulkMessage);
				return sendToGroup(lstOutBoxMsg, bulkMessage.getScheduler());
			}else if(ArgUtil.is(bulkDoc.getFilters())) {
				bulkMessage.setFilters(bulkDoc.getFilters());
				List<OutboxMessage> lstOutBoxMsg = getFilterDetails(bulkMessage);
				return sendToFilterGroup(lstOutBoxMsg, bulkMessage.getScheduler());
			}else {
				Query queryAll = new Query();
				queryAll.addCriteria(Criteria.where("type").in("O"));
				queryAll.addCriteria(Criteria.where("bulkSessionId").is(bulkDoc.getBulkSessionId()));
				queryAll.fields().include("contact.phone").include("messageId");
				List<MessageDoc> msgDoc = mongoTemplate.find(queryAll, MessageDoc.class,
						MessageDoc.COLLECTION_NAME + "_" + bulkDoc.getContactType().toString());
				List<String> to = new ArrayList<>();
				if (ArgUtil.is(msgDoc)) {
					msgDoc.forEach(doc -> to.add(doc.getContact().getPhone()));
					bulkMessage.setTo(to);
				}
				return send(bulkMessage, bulkMessage.getScheduler());
			}
		}
		return null;
	}
		
		
		

	public BulkSessionDoc sendToFilterGroup(List<OutboxMessage> bulkMessages, ChronoScheduler scheduler) throws NumberParseException {
		OutboxMessage bulkMessage = bulkMessages.get(bulkMessages.size()-1);
		String channelId = PostManUtil.CHANNEL_ID(bulkMessage.contact());
		ChannelConfig channelConfig = enviroment.config().channel(channelId);
		BulkSessionDoc session = new BulkSessionDoc();
		session.setMessage(bulkMessage.getMessage());
		session.setTemplateId(bulkMessage.templateId());
		session.setTemplate(bulkMessage.templateCode());
		session.setMessageCount(bulkMessage.getTo().size());
		session.setContactType(bulkMessage.contact().getContactType());
		session.setLane(bulkMessage.contact().getLane());
		session.setChannelId(channelId);
		session.setBulkSessionId(UniqueID.generateString62());
		session.setCampaignTitle(bulkMessage.getCampaignTitle());
		session.setGroupName(bulkMessage.getGroupName());
		session.setScheduler(scheduler);
		session.setFilters(bulkMessage.getFilters());
		
		List<String> toLst = bulkMessage.getTo();

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		//for (OutboxMessage bulkMsg : bulkMessages) {
		   for (String to : toLst) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMessage);
			//String to = bulkMsg.getTo().get(0);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			to = PhoneUtil.addPlusSign(to);
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().phone(to);
			doc.setMessage(bulkMessage.getMessage());
			doc.setHsm(bulkMessage.getHsm());
			doc.setTemplateId(bulkMessage.templateId());
			doc.setTemplate(bulkMessage.templateCode());
			doc.setAttachments(bulkMessage.getAttachments());
			doc.route().setQueueCode(adminApp.getQueue());
			doc.route().setSendMode(adminApp.getAppMode());
			doc.route().setSenderApp(adminApp.getAppType());
			doc.route().setSenderType(MESSAGE_SENDER_TYPE.ADMIN);
			doc.route().setSenderCode(auditDetailProvider.getAuditUser());

			docs.add(doc);
			
		}
		   
		session.setStatus("CREATED");
		mongoTemplate.save(session);
		messageStore.insert(docs, bulkMessage.contact().type());
		registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channelType", channelConfig.getChannelType())
				// Lane for each message
				.data("lane", session.getLane()), scheduler);

		return session;
	}
	
	public List<OutboxMessage> getGroupDetailsV1(OutboxMessage outboxMessage) {

		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();

		if (outboxMessage != null) {
			List<String> groups = outboxMessage.getGroups();
			if (ArgUtil.isEmpty(groups)) {
				groups = new ArrayList<>();
				groups.add(outboxMessage.getGroupId());
			}
			String groupTitle = outboxMessage.getCampaignTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			String hsmTemplateCode = null;
			StringBuilder concatGroupNames = new StringBuilder();
			Set<String> uniquePhoneNumbers = new HashSet<>();
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}
			if (ArgUtil.is(groups)) {
				for (String groupId : groups) {
					GroupDoc groupDoc = mongoTemplate.findById(groupId, GroupDoc.class);

					if (ArgUtil.is(groupDoc)) {
						OutboxMessage outboxMsg = new OutboxMessage();
						if (concatGroupNames.length() > 0) {
							concatGroupNames.append(" , "); // Add a comma separator
						}
						concatGroupNames.append(groupDoc.getGroupName());

						List<GroupSessionDto> lstDto = groupDoc.getSessions();
						CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
						hsmTemp.setId(hsmId);
						hsmTemp.setCode(hsmTemplateCode);
						hsmTemp.setData(otBoxMsg.getHsm().data());

						outboxMsg.setGroupId(groupId);
						outboxMsg.setCampaignTitle(groupTitle);
						outboxMsg.setMessage(otBoxMsg.getMessage());

						outboxMsg.setAttachments(otBoxMsg.getAttachments());
						outboxMsg.setContact(otBoxMsg.getContact());
						outboxMsg.setHsm(hsmTemp);
						outboxMsg.setGroupName(concatGroupNames.toString());

						for (GroupSessionDto dto : lstDto) {
							outboxMsg.setTo(Arrays.asList(dto.getPhone()));
							uniquePhoneNumbers.add(dto.getPhone());
						}
						List<String> toLst = new ArrayList<>(uniquePhoneNumbers);
						outboxMsg.setTo(toLst);
						outboxMsg.setGroups(otBoxMsg.getGroups());
						listOfOutboxMsg.add(outboxMsg);
					}

				}

			}

		}
		return listOfOutboxMsg;
	}

	private List<OutboxMessage> getFilterDetails(OutboxMessage outboxMessage) {

		List<OutboxMessage> listOfOutboxMsg = new ArrayList<>();

		if (outboxMessage != null && ArgUtil.is(outboxMessage.getFilters())) {
			List<String> filters = outboxMessage.getFilters();
			String campTitle = outboxMessage.getCampaignTitle();
			OutboxMessage otBoxMsg = outboxMessage;
			String hsmId = otBoxMsg.getHsm().getId();
			String hsmTemplateCode = null;
			
			StringBuilder concatFilterpNames = new StringBuilder();
			Set<String> uniquePhoneNumbers = new HashSet<>();
			HSMTemplateDoc templateDoc = mongoTemplate.findById(hsmId, HSMTemplateDoc.class);
			if (ArgUtil.is(templateDoc)) {
				hsmTemplateCode = templateDoc.getCode();
			}

			for (String filterId : filters) {
				ProfileFilterMasterDoc profileFilter = mongoTemplate.findById(filterId, ProfileFilterMasterDoc.class);

				if (ArgUtil.is(profileFilter) && ArgUtil.is(profileFilter.get_filterCriteria())) {
					OutboxMessage outboxMsg = new OutboxMessage();
					List<List<Object>> filterCri = profileFilter.get_filterCriteria();

					List<List<ProfileSearchCriteria>> searCri = getSearchCriteria(filterCri);
					ProfileSearchQuery profSerarch = new ProfileSearchQuery();
					profSerarch.setSearchCriterias(searCri);
					
					List<CustomerProfileDoc> docs = null;
					if(ArgUtil.is(searCri)){
						docs =cusProfileService.getProfileSearch(profSerarch);
					}
					if (ArgUtil.is(docs)) {

						if (concatFilterpNames.length() > 0) {
							concatFilterpNames.append(" , "); // Add a comma separator
						}
						concatFilterpNames.append(profileFilter.getFilterName());
						for (CustomerProfileDoc profielDoc : docs) {
							Set<PBPhone> lstDto = profielDoc.getPhones();
							CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
							hsmTemp.setId(hsmId);
							hsmTemp.setCode(hsmTemplateCode);
							hsmTemp.setData(otBoxMsg.getHsm().data());

							outboxMsg.setAttachments(otBoxMsg.getAttachments());
							outboxMsg.setContact(otBoxMsg.getContact());
							outboxMsg.setHsm(hsmTemp);
							outboxMsg.setGroupName(concatFilterpNames.toString());
							outboxMsg.setCampaignTitle(campTitle);
							for (PBPhone dto : lstDto) {
								outboxMsg.setTo(Arrays.asList(dto.getPhone()));
								uniquePhoneNumbers.add(dto.getPhone());
							}
							List<String> toLst = new ArrayList<>(uniquePhoneNumbers);
							outboxMsg.setTo(toLst);
							outboxMsg.setFilters(filters);
							listOfOutboxMsg.add(outboxMsg);
						}
					}
				}
			}

		}
		return listOfOutboxMsg;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<List<ProfileSearchCriteria>> getSearchCriteria(List<List<Object>> filterCri) {
		return  filterCri.stream()
                .map(innerList -> innerList.stream()
                        .filter(obj -> obj instanceof Map) // Ensure the object is a Map
                        .map(obj -> (Map<String, String>) obj) // Cast to Map<String, String>
                        .map(map -> new ProfileSearchCriteria(
                                map.get("key"),
                                map.get("operator"),
                                map.get("value")
                        ))
                        .collect(Collectors.toList())) // Collect as List<ProfileSearchCriteria>
                .collect(Collectors.toList());
	}
}
