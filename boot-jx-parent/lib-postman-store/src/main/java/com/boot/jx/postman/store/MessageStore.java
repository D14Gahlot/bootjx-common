package com.boot.jx.postman.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonDocStore;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.doc.ContactDetailDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.TimeUtils;
import com.google.common.collect.Lists;

@Component
public class MessageStore extends CommonDocStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageStore.class);

    public static enum EVENTS {
	ASGND_TO_DEPT, ASGND_TO_AGENT, UNASGND, PICKED_BY_AGENT, CLOSED_BY_AGENT, LABEL_ADDED, LABEL_REMOVED,
	STATUS_CHANGED,TAG_ADDED
    }

    @Autowired
    MongoTemplate mongoTemplate;

    @Value("${postman.chat.session.timeout}")
    String chatSessionTimeout;

    public static String getCollectionName(Object contactType) {
	return (MessageDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(contactType, "OTHERS"));
    }

    private MessageDoc updateMessageDoc(InboxMessage inboxMessage, MessageDoc doc) {
	doc.setMessage(inboxMessage.getMessage());
	doc.setSessionId(inboxMessage.getSessionId());
	doc.setTags(inboxMessage.getTags());
	doc.setMessageIdExt(inboxMessage.getMessageIdExt());

	// Additonals
	doc.setAttachments(inboxMessage.getAttachments());

	return doc;
    }

    private MessageDoc createMessageDoc(InboxMessage inboxMessage) {
	MessageDoc doc = new MessageDoc();
	doc.setContactId(PostManUtil.createContactId(inboxMessage));
	doc.setType("I");
	doc.setTimestamp(System.currentTimeMillis());

	ContactDetailDoc contact = new ContactDetailDoc();
	contact.setPhone(inboxMessage.getFrom());
	contact.setContactType(ArgUtil.parseAsString(inboxMessage.contact().type()));
	doc.setContact(contact);

	updateMessageDoc(inboxMessage, doc);

	return doc;
    }

    public MessageDoc findByMessageId(String messageId, Object contactType) {
	return mongoTemplate.findById(messageId, MessageDoc.class, getCollectionName(contactType));
    }

    private MessageDoc findMessageDoc(InboxMessage inboxMessage) {
	if (ArgUtil.is(inboxMessage.getMessageId())) {
	    return mongoTemplate.findById(inboxMessage.getMessageId(), MessageDoc.class,
		    getCollectionName(inboxMessage.contact().type()));
	} else if (ArgUtil.is(inboxMessage.getMessageIdExt())) {
	    CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
	    builder.where("messageIdExt", inboxMessage.getMessageIdExt());
	    return mongoTemplate.findOne(builder.getQuery(), MessageDoc.class,
		    getCollectionName(inboxMessage.contact().type()));
	} else {
	    return null;
	}
    }

    public MessageDoc findOrCreateMessageDoc(InboxMessage inboxMessage) {
	MessageDoc doc = findMessageDoc(inboxMessage);
	if (!ArgUtil.is(doc)) {
	    return createMessageDoc(inboxMessage);
	}
	return doc;
    }

    public MessageDoc findAndUpdateMessageDoc(InboxMessage inboxMessage) {
	MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
	if (ArgUtil.is(doc.getMessageId())) {
	    doc = updateMessageDoc(inboxMessage, doc);
	}
	mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
	inboxMessage.setMessageId(doc.getMessageId());
	return doc;
    }

    public MessageDoc createOrUpdate(InboxMessage inboxMessage) {
	MessageDoc doc = findAndUpdateMessageDoc(inboxMessage);
	inboxMessage.setMessageId(doc.getMessageId());
	return doc;
    }

    public void setTemplate(InboxMessage inboxMessage, String template) {
	MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
	doc.setTemplate(template);
	mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
	inboxMessage.setMessageId(doc.getMessageId());
    }

    public void setTags(InboxMessage inboxMessage, TagDocument tags) {
	MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
	doc.setTags(tags);
	mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
	inboxMessage.setMessageId(doc.getMessageId());
    }

    public void setHandler(InboxMessage inboxMessage, String handler) {
	MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
	doc.setHandler(handler);
	mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
	inboxMessage.setMessageId(doc.getMessageId());
    }

    public MessageDoc log(IMessage inboxMessage, String actorAgent, EVENTS eventName, String... logMessage) {
	MessageDoc doc = new MessageDoc();
	doc.setContactId(PostManUtil.createContactId(inboxMessage));
	doc.setType("L");
	doc.setTimestamp(System.currentTimeMillis());
	if (ArgUtil.is(logMessage)) {
	    for (String string : logMessage) {
		doc.logs().add(string);
	    }
	}
	doc.setAction(ArgUtil.parseAsString(eventName));
	doc.setSessionId(inboxMessage.getSessionId());
	doc.setAgent(actorAgent);
	mongoTemplate.save(doc, getCollectionName(inboxMessage.contact().type()));
	return doc;
    }

    // Out Going Messages
    private MessageDoc updateMessageDoc(OutboxMessage outMessage, MessageDoc doc) {
	doc.setAgent(outMessage.session().getAgent());
	// if (ArgUtil.is(outMessage.getTemplate())) {
	doc.setTemplate(outMessage.getTemplate());
	doc.setModel(outMessage.getModel());
	// } else {
	doc.setMessage(outMessage.getMessage());
	// }
	doc.setAttachments(outMessage.getAttachments());

	doc.setSessionId(outMessage.getSessionId());
	doc.setMessageIdRef(outMessage.getMessageIdRef());

	doc.setLogs(outMessage.getLogs());
	doc.setMessageIdExt(outMessage.getMessageIdExt());
	doc.setStatus(ArgUtil.parseAsString(outMessage.getStatus()));

	doc.stamps().putAll(outMessage.stamps());
	doc.meta().putAll(outMessage.meta());

	return doc;
    }

    public MessageDoc createMessageDoc(OutboxMessage outMessage) {
	MessageDoc doc = new MessageDoc();
	if (ArgUtil.is(outMessage.getAction())) {
	    doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "A"));
	    doc.setAction(outMessage.getAction());
	} else {
	    doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "O"));
	}
	doc.setTimestamp(System.currentTimeMillis());

	String to = CollectionUtil.getOne(outMessage.getTo());
	doc.setContactId(PostManUtil.createContactId(outMessage));

	ContactDetailDoc contact = new ContactDetailDoc();
	contact.setPhone(to);
	contact.setMobile(to);
	contact.setContactType(ArgUtil.parseAsString(outMessage.contact().getContactType()));
	doc.setContact(contact);

	updateMessageDoc(outMessage, doc);
	return doc;
    }

    public MessageDoc findMessageDoc(OutboxMessage outMessage) {
	if (ArgUtil.is(outMessage.getMessageId())) {
	    return mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
		    getCollectionName(outMessage.contact().type()));
	}
	return null;
    }

    private MessageDoc findOrCreateMessageDoc(OutboxMessage outMessage) {
	MessageDoc doc = findMessageDoc(outMessage);
	if (!ArgUtil.is(doc)) {
	    doc = createMessageDoc(outMessage);
	}
	return doc;
    }

    public MessageDoc findAndUpdateMessageDoc(OutboxMessage outMessage) {
	MessageDoc doc = findOrCreateMessageDoc(outMessage);
	if (ArgUtil.is(doc.getMessageId())) {
	    doc = updateMessageDoc(outMessage, doc);
	}
	mongoTemplate.save(doc, getCollectionName(outMessage.contact().type()));
	outMessage.setMessageId(doc.getMessageId());
	return doc;
    }

    public MessageDoc createOrUpdate(OutboxMessage outMessage) {
	MessageDoc doc = findAndUpdateMessageDoc(outMessage);
	outMessage.setMessageId(doc.getMessageId());
	return doc;
    }

    public MessageDoc note(OutboxMessage outboxMessage, String agent) {
	MessageDoc doc = createMessageDoc(outboxMessage);
	doc.setType("N");
	doc.setAgent(agent);
	mongoTemplate.save(doc, getCollectionName(outboxMessage.contact().type()));
	return doc;
    }

    public List<MessageDoc> findBySessionId(String sessionId, String contactType) {
	Query query2 = new Query();
	query2.addCriteria(Criteria.where("sessionId").is(sessionId));
	List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
	return messages;
    }

    public List<MessageDoc> findByBulkSessionId(String bulkSessionId, ContactType contactType) {
	Query query2 = new Query();
	query2.addCriteria(Criteria.where("bulkSessionId").is(bulkSessionId));
	List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
	return messages;
    }

    public void applyPatch(MessageDoc messageDoc) {
	applyPatch(messageDoc, getCollectionName(messageDoc.getContact().getContactType()));
    }

    public void updateStatus(ContactType contactType, MessageDoc messageDoc, Status status, String reason) {
	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

	if (ArgUtil.is(messageDoc.getMessageId())) {
	    builder.whereIdSafe(messageDoc.getMessageId());
	} else if (ArgUtil.is(messageDoc.getMessageIdExt())) {
	    builder.where("messageIdExt", messageDoc.getMessageIdExt());
	} else if (ArgUtil.is(messageDoc.getMessageIdRef())) {
	    builder.where("messageIdRef", messageDoc.getMessageIdRef());
	} else {
	    return;
	}

	builder.set("status", status);
	builder.set("stamps." + status.toString(), System.currentTimeMillis());
	if (ArgUtil.is(reason)) {
	    builder.update().push("logs", reason);
	}
	mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
		getCollectionName(contactType));
    }

    public void updateStatus(MessageReport messageReport) {

	LOGGER.debug("updateStatus {} {} {}", messageReport.getMessageId(), messageReport.getContactType(),
		messageReport.getStatus());

	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

	boolean multi = false;
	if (ArgUtil.is(messageReport.getMessageId())) {
	    builder.whereIdSafe(messageReport.getMessageId());
	} else if (ArgUtil.is(messageReport.getMessageIdExt())) {
	    builder.where("messageIdExt", messageReport.getMessageIdExt());
	} else if (ArgUtil.is(messageReport.getMessageIdRef())) {
	    builder.where("messageIdRef", messageReport.getMessageIdRef());
	} else if (ArgUtil.is(messageReport.contact().getCsid()) && messageReport.getWatermarkStamp() > 0L) {
	    String contactId = PostManUtil.CONTACT_ID(messageReport.contact());
	    builder.with(
		    // Main Condition
		    Criteria.where("contactId").is(contactId).and("stamps." + messageReport.getStatus().toString())
			    .exists(false).andOperator(
				    // Range
				    Criteria.where("timestamp").lt(messageReport.getWatermarkStamp()),
				    Criteria.where("timestamp").gt(TimeUtils.beforeTimeMillis("24hr"))));
	    multi = true;
	} else {
	    return;
	}

	if (ArgUtil.is(messageReport.getStatus())) {
	    builder.set("status", messageReport.getStatus());
	    builder.set("stamps." + messageReport.getStatus().toString(), messageReport.getChangeStamp());

	    if (ArgUtil.is(messageReport.getReason())) {
		builder.update().push("logs", messageReport.getReason());
	    }

	    if (multi) {
		mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
			getCollectionName(messageReport.getContactType()));
	    } else {
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
			getCollectionName(messageReport.getContactType()));
	    }
	    // LOGGER.info(JsonUtil.toJson(builder));
	}
    }

    public void insert(List<MessageDoc> messages, ContactType contactType) {
	/**
	 * for (MessageDoc messageDoc : messages) { mongoTemplate.save(messageDoc,
	 * MessageStore.getCollectionName(contactType)); //System.out.println("phone:
	 * "+messageDoc.getContact().getPhone()); } return;
	 **/
	int n = 500;
	// Calculate the total number of partitions of size `n` each
	int m = messages.size() / n;
	if (messages.size() % n != 0) {
	    m++;
	}
	// partition the list into sublists of size `n` each
	List<List<MessageDoc>> itr = Lists.partition(messages, n);
	for (int i = 0; i < m; i++) {
	    mongoTemplate.insert(itr.get(i), MessageStore.getCollectionName(contactType));
	}

    }

    public List<MessageDoc> find(Query query, ContactType contactType) {
	return mongoTemplate.find(query, MessageDoc.class, MessageStore.getCollectionName(contactType));
    }

    public MessageDoc save(MessageDoc msg, ContactType contactType) {
	mongoTemplate.save(msg, MessageStore.getCollectionName(contactType));
	return msg;
    }

}
