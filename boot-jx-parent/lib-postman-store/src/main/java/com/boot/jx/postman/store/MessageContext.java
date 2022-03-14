package com.boot.jx.postman.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ErrorObject;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.scope.ThreadScoped;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
@ThreadScoped
public class MessageContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageContext.class);

    @Autowired
    public MongoTemplate mongoTemplate;

    @Autowired
    public CommonMongoTemplate commonMongoTemplate;

    // DTOs
    private IMessage message;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private PMDomainConfig pmDomainConfig;

    // QUERYs
    private ChatContactQuery chatContactQuery;
    private ChatSessionQuery chatSessionQuery;
    @Autowired
    private SessionStore sessionStore;

    public void setMessage(IMessage message) {
	this.message = message;
    }

    private Contactable getContactable() {
	if (message != null) {
	    return PostManUtil.getContactMeta(message.contact());
	}
	return null;
    }

    private ChatContactDoc getChatContactDoc() {
	Contactable c = getContactable();
	return commonMongoTemplate.findById(c.getContactId(), ChatContactDoc.class);
    }

    public ChatContactQuery contact() {
	if (this.chatContactQuery == null) {
	    ChatContactDoc chatContactDoc = this.getChatContactDoc();
	    this.chatContactQuery = new ChatContactQuery(chatContactDoc);
	}
	return this.chatContactQuery;
    }

    public ChatSessionQuery session() {
	if (chatSessionQuery == null) {
	    ChatSessionDoc chatSessionDoc;
	    chatSessionDoc = sessionStore.getSession(message.getSessionId());
	    chatSessionQuery = new ChatSessionQuery(chatSessionDoc);
	}
	return chatSessionQuery;
    }

    public void commitChatContactQuery() {
	if (this.chatContactQuery != null) {
	    commonMongoTemplate.updateFirst(this.chatContactQuery);
	}
    }

    public void log(ErrorObject error) {
	commonMongoTemplate.save(error);
    }

    public ClientApp clientApp(String assignedQueue, Contactable contactable) {
	ClientApp defaultClient = null;
	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.config().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }
	}

	if (!ArgUtil.is(contactable)) {
	    return defaultClient;
	}

	assignedQueue = pmDomainConfig.getDefaultInboundQueue(contactable);

	if (ArgUtil.is(assignedQueue)) {
	    defaultClient = pmEnvironment.config().clientApiKey(assignedQueue);

	    if (ArgUtil.is(defaultClient)) {
		return defaultClient;
	    }
	}

	return defaultClient;
    }

    public ClientApp clientApp() {
	if (ArgUtil.is(message)) {
	    return this.clientApp(message.session().getQueue(), message.contact());
	}
	return this.clientApp(null, null);
    }

}
