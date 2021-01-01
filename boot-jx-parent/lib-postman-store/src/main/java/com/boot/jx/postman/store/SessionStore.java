package com.boot.jx.postman.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class SessionStore {

	@Autowired
	MongoTemplate mongoTemplate;

	@Value("${postman.chat.session.timeout}")
	String chatSessionTimeout;

	public ChatSessionDoc createSession(InboxMessage inboxMessage) {
		String contactId = PostManUtil.createContactId(inboxMessage);
		inboxMessage.setContactId(contactId);

		String sessionId = inboxMessage.getSessionId();

		ChatContactDoc chatContactDoc = null;
		ChatSessionDoc chatSessionDoc = null;

		if (ArgUtil.isEmpty(sessionId)) {
			chatContactDoc = mongoTemplate.findById(contactId, ChatContactDoc.class);
			if (ArgUtil.is(chatContactDoc)) {
				sessionId = chatContactDoc.getSessionId();
			}
		}

		if (ArgUtil.is(sessionId)) {
			chatSessionDoc = mongoTemplate.findById(sessionId, ChatSessionDoc.class);
		}

		if (ArgUtil.isEmpty(chatSessionDoc)
				|| TimeUtils.isExpired(chatSessionDoc.getLastInComingStamp(), chatSessionTimeout)) {

			// SESSION CREATION
			chatSessionDoc = new ChatSessionDoc();
			chatSessionDoc.setContactId(contactId);

			// SESSION UPDATE
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			mongoTemplate.save(chatSessionDoc);

			// CONTACT CREATION
			if (ArgUtil.isEmpty(chatContactDoc)) {
				chatContactDoc = new ChatContactDoc();
				chatContactDoc.setContactId(contactId);
				chatContactDoc.setContactType(ArgUtil.parseAsString(inboxMessage.getContactType()));
			}
			// CONTACT UPDATE
			chatContactDoc.setSessionId(chatSessionDoc.getSessionId());
			mongoTemplate.save(chatContactDoc);

		} else {
			// SESSION UPDATE
			chatSessionDoc.setLastInComingStamp(System.currentTimeMillis());
			mongoTemplate.save(chatSessionDoc);
		}

		inboxMessage.setSessionId(chatSessionDoc.getSessionId());
		return chatSessionDoc;
	}

}
