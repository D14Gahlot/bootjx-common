package com.boot.jx.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoQueryBuilder.CommonMongoCriteria;
import com.boot.jx.postman.doc.BulkSessionDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.tunnel.task.JobTaskModel;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.jx.tunnel.task.QueuedTaskExecuter;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
public class BulkMessageService extends QueuedTaskExecuter {

	private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MessageStore messageStore;

	public BulkSessionDoc send(OutboxMessage bulkMessage) throws NumberParseException {

		BulkSessionDoc session = new BulkSessionDoc();

		session.setMessage(bulkMessage.getMessage());
		session.setTemplateId(bulkMessage.getTemplateId());
		session.setTemplate(bulkMessage.getTemplate());
		session.setMessageCount(bulkMessage.getTo().size());
		session.setContactType(bulkMessage.contact().type());
		session.setLane(bulkMessage.contact().getLane());
		session.setBulkSessionId(UniqueID.generateString62());

		PhoneNumber phoneNumber = new PhoneNumber();
		List<MessageDoc> docs = new ArrayList<MessageDoc>();
		for (String to : bulkMessage.getTo()) {
			MessageDoc doc = messageStore.createMessageDoc(bulkMessage);
			doc.setContactId(null);
			doc.updateStatus(Status.CRTD);
			doc.setBulkSessionId(session.getBulkSessionId());
			PHONE_NUMBER_UTIL.parse(to, "IN", phoneNumber);
			to = String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			doc.getContact().setPhone(to);
			doc.setMessage(bulkMessage.getMessage());
			doc.setTemplateId(bulkMessage.getTemplateId());
			doc.setTemplate(bulkMessage.getTemplate());
			docs.add(doc);
		}

		mongoTemplate.save(session);
		messageStore.insert(docs, bulkMessage.contact().type());
		registerJob(JobTaskModel.newBatchJob()
				// Set Unique Job Id
				.jobId(session.getBulkSessionId())
				// Contact Type for each message
				.data("contactType", session.getContactType())
				// Channel for each message
				.data("channel", bulkMessage.contact().getChannel())
				// Lane for each message
				.data("lane", session.getLane()));

		return session;
	}

	@Override
	public BatchJob read(BatchJob batchJob) {
		BulkSessionDoc doc = mongoTemplate.findById(batchJob.getJobId(), BulkSessionDoc.class);
		Query query = new Query()
				.addCriteria(
						CommonMongoCriteria.where("bulkSessionId").is(batchJob.getJobId()).and("status").is("CRTD"))
				.limit(50);
		List<MessageDoc> msgs = messageStore.find(query, doc.getContactType());

		if (!ArgUtil.is(msgs) || msgs.size() == 0) {
			return null;
		}
		for (MessageDoc messageDoc : msgs) {
			push(JobTaskModel.newTasklet(batchJob).taskId(messageDoc.getMessageId()));
		}
		return batchJob;
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
		outboxMessage.contact().setChannel(channel);
		outboxMessage.contact().setLane(lane);
		outboxMessage.contact().setEmail(msg.getContact().getEmail());
		outboxMessage.contact().setPhone(msg.getContact().getPhone());
		outboxMessage.contact().setContactId(msg.getContact().getContactId());

		ChatSessionDoc chatSessionDoc = sessionStore.linkSession(outboxMessage);
		if (ArgUtil.is(chatSessionDoc)) {
			chatService.send(chatSessionDoc, outboxMessage);
		} else {
			msg.updateStatus(Status.NSENT);
			messageStore.save(msg, contactType);
		}
	}

}
