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

import com.boot.jx.mongo.CommonDocStore;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.doc.ContactDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.IMessage;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class MessageStore extends CommonDocStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageStore.class);

	public static enum EVENTS {
		ASGND_TO_DEPT, ASGND_TO_AGENT, UNASGND, PICKED_BY_AGENT, CLOSED_BY_AGENT, LABEL_ADDED, LABEL_REMOVED
	}

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	private String getCollectionName(Object contactType) {
		return (MessageDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(contactType, "OTHERS"));
	}

	private MessageDoc updateMessageDoc(InboxMessage inboxMessage, MessageDoc doc) {
		doc.setMessage(inboxMessage.getMessage());
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setTags(inboxMessage.getTags());
		doc.setMessageIdExt(inboxMessage.getMessageIdExt());
		return doc;
	}

	private MessageDoc createMessageDoc(InboxMessage inboxMessage) {
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(inboxMessage));
		doc.setType("I");
		doc.setTimestamp(System.currentTimeMillis());

		ContactDoc contact = new ContactDoc();
		contact.setMobile(inboxMessage.getFrom());
		contact.setContactType(inboxMessage.getContactType());
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
					getCollectionName(inboxMessage.getContactType()));
		} else if (ArgUtil.is(inboxMessage.getMessageIdExt())) {
			CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();
			builder.where("messageIdExt", inboxMessage.getMessageIdExt());
			return mongoTemplate.findOne(builder.getQuery(), MessageDoc.class,
					getCollectionName(inboxMessage.getContactType()));
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
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
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
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	public void setTags(InboxMessage inboxMessage, TagDocument tags) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setTags(tags);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	public void setHandler(InboxMessage inboxMessage, String handler) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setHandler(handler);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	public MessageDoc log(IMessage inboxMessage, String agent, EVENTS eventName, String... logMessage) {
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
		doc.setAgent(agent);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
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
		return doc;
	}

	private MessageDoc createMessageDoc(OutboxMessage outMessage) {
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

		ContactDoc contact = new ContactDoc();
		contact.setMobile(to);
		contact.setContactType(outMessage.getContactType());
		doc.setContact(contact);

		updateMessageDoc(outMessage, doc);
		return doc;
	}

	public MessageDoc findMessageDoc(OutboxMessage outMessage) {
		if (ArgUtil.is(outMessage.getMessageId())) {
			return mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
					getCollectionName(outMessage.getContactType()));
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
		mongoTemplate.save(doc, getCollectionName(outMessage.getContactType()));
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc createOrUpdate(OutboxMessage outMessage) {
		MessageDoc doc = findAndUpdateMessageDoc(outMessage);
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc save(MessageDoc messageDoc) {
		mongoTemplate.save(messageDoc, getCollectionName(messageDoc.getContact().getContactType()));
		return messageDoc;
	}

	public List<MessageDoc> findBySessionId(String sessionId, String contactType) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("sessionId").is(sessionId));
		List<MessageDoc> messages = mongoTemplate.find(query2, MessageDoc.class, getCollectionName(contactType));
		return messages;
	}

	public void applyPatch(MessageDoc messageDoc) {
		applyPatch(messageDoc, getCollectionName(messageDoc.getContact().getContactType()));
	}

	public void updateStatus(MessageReport messageReport) {

		LOGGER.debug("updateStatus {} {} {}", messageReport.getMessageId(), messageReport.getContactType(),
				messageReport.getStatus());

		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		if (ArgUtil.is(messageReport.getMessageId())) {
			builder.whereIdSafe(messageReport.getMessageId());
		} else if (ArgUtil.is(messageReport.getMessageIdExt())) {
			builder.where("messageIdExt", messageReport.getMessageIdExt());
		} else if (ArgUtil.is(messageReport.getMessageIdRef())) {
			builder.where("messageIdRef", messageReport.getMessageIdRef());
		} else {
			return;
		}

		if (ArgUtil.is(messageReport.getStatus())) {
			builder.set("status", messageReport.getStatus());
			builder.set("stamps." + messageReport.getStatus().toString(), messageReport.getTimestamp());

			if (ArgUtil.is(messageReport.getReason())) {
				builder.update().push("logs", messageReport.getReason());
			}

			mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
					getCollectionName(messageReport.getContactType()));
			// LOGGER.info(JsonUtil.toJson(builder));
		}
	}

}
