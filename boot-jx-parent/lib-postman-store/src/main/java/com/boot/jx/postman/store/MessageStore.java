package com.boot.jx.postman.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ContactDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class MessageStore {

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	private String getCollectionName(ContactType contactType) {
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

	public void setHandler(InboxMessage inboxMessage, String handler) {
		MessageDoc doc = findOrCreateMessageDoc(inboxMessage);
		doc.setHandler(handler);
		mongoTemplate.save(doc, getCollectionName(inboxMessage.getContactType()));
		inboxMessage.setMessageId(doc.getMessageId());
	}

	// Out Going Messages
	private MessageDoc createMessageDoc(Message<?> outMessage) {
		String to = CollectionUtil.getOne(outMessage.getTo());
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(outMessage));
		doc.setType("O");
		doc.setTimestamp(System.currentTimeMillis());
		ContactDoc contact = new ContactDoc();
		contact.setMobile(to);
		contact.setContactType(outMessage.getContactType());
		doc.setContact(contact);

		if (ArgUtil.is(outMessage.getTemplate())) {
			doc.setTemplate(outMessage.getTemplate());
		} else {
			doc.setMessage(outMessage.getMessage());
		}
		return doc;
	}

	private MessageDoc findOrCreateMessageDoc(Message<?> outMessage) {
		MessageDoc doc = null;
		if (ArgUtil.is(outMessage.getMessageId())) {
			doc = mongoTemplate.findById(outMessage.getMessageId(), MessageDoc.class,
					getCollectionName(outMessage.getContactType()));
		}
		if (!ArgUtil.is(doc)) {
			return createMessageDoc(outMessage);
		}
		return doc;
	}

	public MessageDoc get(Message<?> outMessage) {
		MessageDoc doc = findOrCreateMessageDoc(outMessage);
		mongoTemplate.save(doc, getCollectionName(outMessage.getContactType()));
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc create(Message<?> outMessage) {
		MessageDoc doc = createMessageDoc(outMessage);
		mongoTemplate.save(doc, getCollectionName(outMessage.getContactType()));
		outMessage.setMessageId(doc.getMessageId());
		return doc;
	}

	public MessageDoc save(MessageDoc messageDoc) {
		mongoTemplate.save(messageDoc, getCollectionName(messageDoc.getContact().getContactType()));
		return messageDoc;
	}
}
