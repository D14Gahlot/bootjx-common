package com.boot.jx.postman.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocStore;
import com.boot.jx.postman.doc.ContactDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.IMessage;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class MessageStore extends CommonDocStore {

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

	private MessageDoc createMessageDoc(InboxMessage inboxMessage) {
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(inboxMessage));
		doc.setType("I");
		doc.setTimestamp(System.currentTimeMillis());
		ContactDoc contact = new ContactDoc();
		contact.setMobile(inboxMessage.getFrom());
		contact.setContactType(inboxMessage.getContactType());
		doc.setContact(contact);
		doc.setMessage(inboxMessage.getMessage());
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setTags(inboxMessage.getTags());
		doc.setMessageIdExt(inboxMessage.getMessageIdExt());
		return doc;
	}

	private MessageDoc findOrCreateMessageDoc(InboxMessage inboxMessage) {
		MessageDoc doc = null;
		if (ArgUtil.is(inboxMessage.getMessageId())) {
			doc = mongoTemplate.findById(inboxMessage.getMessageId(), MessageDoc.class,
					getCollectionName(inboxMessage.getContactType()));
		}
		if (!ArgUtil.is(doc)) {
			return createMessageDoc(inboxMessage);
		}
		return doc;
	}

	public MessageDoc create(InboxMessage inboxMessage) {
		MessageDoc doc = createMessageDoc(inboxMessage);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
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

	public MessageDoc find(InboxMessage inboxMessage) {
		return findOrCreateMessageDoc(inboxMessage);
	}

	public MessageDoc log(IMessage inboxMessage, EVENTS eventName, String... logMessage) {
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
		doc.setAgent(inboxMessage.session().getAgent());
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
		return doc;
	}

	// Out Going Messages
	private void update(OutboxMessage outMessage, MessageDoc doc) {
		doc.setLogs(outMessage.getLogs());
		doc.setMessageIdExt(outMessage.getMessageIdExt());
		doc.setStatus(ArgUtil.parseAsString(outMessage.getStatus()));
	}

	private MessageDoc createMessageDoc(OutboxMessage outMessage) {
		String to = CollectionUtil.getOne(outMessage.getTo());
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(outMessage));

		if (ArgUtil.is(outMessage.getAction())) {
			doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "A"));
			doc.setAction(outMessage.getAction());
		} else {
			doc.setType(ArgUtil.nonEmpty(outMessage.getType(), "O"));
		}

		doc.setTimestamp(System.currentTimeMillis());
		ContactDoc contact = new ContactDoc();
		contact.setMobile(to);
		contact.setContactType(outMessage.getContactType());
		doc.setContact(contact);
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
		
		update(outMessage, doc);

		return doc;
	}

	public MessageDoc find(OutboxMessage outMessage) {
		return mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
				getCollectionName(outMessage.getContactType()));
	}

	private MessageDoc findOrCreateMessageDoc(OutboxMessage outMessage) {
		MessageDoc doc = null;
		if (ArgUtil.is(outMessage.getMessageId())) {
			doc = mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
					getCollectionName(outMessage.getContactType()));
		}
		if (!ArgUtil.is(doc)) {
			doc = createMessageDoc(outMessage);
		}

		update(outMessage, doc);

		return doc;
	}

	public MessageDoc update(OutboxMessage outMessage) {
		MessageDoc doc = findOrCreateMessageDoc(outMessage);
		mongoTemplate.save(doc, getCollectionName(outMessage.getContactType()));
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc create(OutboxMessage outMessage) {
		MessageDoc doc = createMessageDoc(outMessage);
		mongoTemplate.save(doc, getCollectionName(outMessage.getContactType()));
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

}
