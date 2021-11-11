package com.boot.jx.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQB.CommonMongoCriteria;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.task.JobTaskModel;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.jx.tunnel.task.JobTaskModel.JOB_STATUS;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.jx.tunnel.task.QueuedTaskExecuter;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOptions.OutputMode;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Component
public class BulkMessageService extends QueuedTaskExecuter {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private AuditDetailProvider auditDetailProvider;

    @Autowired
    private PMEnvironment enviroment;

    public BulkSessionDoc send(OutboxMessage bulkMessage) throws NumberParseException {

	BulkSessionDoc session = new BulkSessionDoc();

	session.setMessage(bulkMessage.getMessage());
	session.setTemplateId(bulkMessage.getTemplateId());
	session.setTemplate(bulkMessage.getTemplate());
	session.setMessageCount(bulkMessage.getTo().size());
	session.setContactType(bulkMessage.contact().type());
	session.setLane(bulkMessage.contact().getLane());
	session.setBulkSessionId(UniqueID.generateString62());

	auditDetailProvider.audit(session);

	String defaultRegion = enviroment.keyEntry(ConfigConstants.KEY.POSTMAN_PHONEBOOK_REGION).asString("IN");

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
	    doc.setTemplateId(bulkMessage.getTemplateId());
	    doc.setTemplate(bulkMessage.getTemplate());
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
		.data("channel", bulkMessage.contact().getChannelType())
		// Lane for each message
		.data("lane", session.getLane()));

	return session;
    }

    @Override
    public boolean read(BatchJob currentBatchJob) {
	BulkSessionDoc doc = mongoTemplate.findById(currentBatchJob.getJobId(), BulkSessionDoc.class);
	Query query = new Query().addCriteria(CommonMongoCriteria.where("bulkSessionId").is(currentBatchJob.getJobId())
		.and("status").is(Status.SCHLD.toString())).limit(10);
	List<MessageDoc> msgs = messageStore.find(query, doc.getContactType());

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
	    messageStore.updateStatus(doc.getContactType(), messageDoc, Status.CRTD, null);
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
    private SessionStore sessionStore;

    @Override
    public void execute(BatchJob taskJob, Tasklet tasklet) {
	LOGGER.debug("execute(BatchJob {}, Tasklet {})", taskJob.getJobId(), tasklet.getTaskId());
	String messageId = tasklet.getTaskId();
	ContactType contactType = taskJob.data().entry("contactType").asEnum(ContactType.class);
	String channel = taskJob.data().entry("channel").asString();
	String lane = taskJob.data().entry("lane").asString();
	MessageDoc msg = messageStore.findByMessageId(messageId, contactType);

	OutboxMessage outboxMessage = new OutboxMessage();
	outboxMessage.setMessageId(msg.getMessageId());
	outboxMessage.setMessage(msg.getMessage());
	outboxMessage.setTemplate(msg.getTemplate());
	outboxMessage.setTemplateId(msg.getTemplateId());
	outboxMessage.contact().type(contactType);
	outboxMessage.contact().setChannelType(channel);
	outboxMessage.contact().setLane(lane);
	outboxMessage.contact().setEmail(msg.getContact().getEmail());
	outboxMessage.contact().setPhone(msg.getContact().getPhone());
	outboxMessage.contact().setContactId(msg.getContact().getContactId());

	ChatSessionDoc chatSessionDoc = sessionStore.linkSession(outboxMessage);
	if (ArgUtil.is(chatSessionDoc)) {
	    chatService.initSession(outboxMessage, chatSessionDoc);
	    chatService.send(chatSessionDoc, outboxMessage);
	} else {
	    messageStore.updateStatus(contactType, msg, Status.NSENT, "Cannot create session");
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

	List<DBObject> list = new ArrayList<DBObject>();
	list.add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))) // Match
		.toDBObject(Aggregation.DEFAULT_CONTEXT));
	list.add(Aggregation.group("status").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));

	DBCollection col = mongoTemplate.getCollection(MessageStore.getCollectionName(contactType));
	Cursor cursor = col.aggregate(list,
		AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build());

	long totalCount = 0;
	long doneCount = 0;
	// for (Map map : results) {
	while (cursor.hasNext()) {
	    DBObject object = cursor.next();
	    if (ArgUtil.is(object)) {
		Status status = ArgUtil.parseAsEnumT(object.get("_id"), Status.class);
		if (ArgUtil.is(status)) {
		    long count = ArgUtil.parseAsLong(object.get("count"), 0L);
		    doc.stats().put(ArgUtil.parseAsString(status), count);
		    totalCount = (totalCount + count);
		    // Done Count
		    if (status.ordinal() > Status.INIT.ordinal()) {
			doneCount = (doneCount + count);
		    }
		}
	    }

	}
	// }

	// System.out.println("TALLY : " + (totalCount == doneCount) + " -- "
	// +currentBatchJob.getDonePercent());
	boolean completed = (totalCount == doneCount) && (currentBatchJob.getDonePercent() == 100);

	if (completed) {
	    doc.setCompletedStamp(System.currentTimeMillis());
	}
	if (!ArgUtil.areEqual(currentBatchJob.getStatus(), doc.getStatus())) {
	    doc.setStatus(currentBatchJob.getStatus().toString());
	    if (completed) {
		doc.setStatus(JOB_STATUS.COMPLETED.toString());
	    }
	}
	mongoTemplate.save(doc);
	return completed;
    }

}
