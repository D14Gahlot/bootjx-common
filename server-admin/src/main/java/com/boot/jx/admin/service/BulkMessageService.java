package com.boot.jx.admin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.QA;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.tunnel.task.BatchJobExecuter;
import com.boot.jx.tunnel.task.JobTaskModel;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.jx.tunnel.task.JobTaskModel.JOB_STATUS;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
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

	public BulkSessionDoc send(OutboxMessage bulkMessage) throws NumberParseException {

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

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (String to : bulkMessage.getTo()) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMessage);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().setPhone(to);
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
				.data("lane", session.getLane()));

		return session;
	}

	public BulkSessionDoc sendMultiple(List<OutboxMessage> bulkMessages) throws NumberParseException {

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

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (OutboxMessage bulkMsg : bulkMessages) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMsg);
			String to = bulkMsg.getTo().get(0);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().setPhone(to);
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
				.data("lane", session.getLane()));

		return session;
	}

	public BulkSessionDoc sendToGroup(List<OutboxMessage> bulkMessages) throws NumberParseException {

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
		session.setGroupId(bulkMessage.getGroupId());
		session.setGroupTitle(bulkMessage.getGroupTitle());
		session.setGroupName(bulkMessage.getGroupName());

		auditDetailProvider.auditCreate(session);

		ClientApp adminApp = enviroment.config().clientApiKey(PMConstants.DEFAULT.ADMIN_QUEUE_CODE);
		String defaultRegion = enviroment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (OutboxMessage bulkMsg : bulkMessages) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMsg);
			String to = bulkMsg.getTo().get(0);
			doc.setContactId(null);
			doc.updateStatus(Status.SCHLD);
			doc.setBulkSessionId(session.getBulkSessionId());
			ConfigConstants.PHONE_NUMBER_UTIL.parse(to, defaultRegion, phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().setPhone(to);
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
				.data("lane", session.getLane()));

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
	public boolean read(BatchJob currentBatchJob) {
		BulkSessionDoc doc = mongoTemplate.findById(currentBatchJob.getJobId(), BulkSessionDoc.class);
		Query query = new Query().addCriteria(QueryCriteria.where("bulkSessionId").is(currentBatchJob.getJobId())
				.and("status").is(Status.SCHLD.toString())).limit(10);

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
			// System.out.println("Status.CRTD"+messageDoc.getContact().getPhone());
			messageStore.updateStatus(contactType, messageDoc, Status.CRTD, null);
			// messageStore.save(messageDoc, doc.getContactType());
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
			outboxMessage.contact().setPhone(msg.getContact().getPhone());
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
				Aggregation.group("statuss.k").count().as("count"));;

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
				|| !ArgUtil.is(doc.getStatus(), JOB_STATUS.COMPLETED.toString())
		){
			doc.setStatus(ArgUtil.parseAsString(currentBatchJob.getStatus(),doc.getStatus()));
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
			// List<Tutorial> tutorials = new ArrayList<Tutorial>();
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecords) {
				System.out.println("id :" + csvRecord.get("contacts"));
				// System.out.println("Title :"+ csvRecord.get("Title"));
				// System.out.println("Description :"+ csvRecord.get("Description"));
				// System.out.println("id :"+ csvRecord.get("Published"));

			}

		} catch (Exception e) {
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}
	}
}
