package com.boot.jx.postman.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ErrorObject;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.scope.ThreadScoped;
import com.boot.jx.utils.PostManUtil;

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
    private Contactable contactable;

    // DOCs
    private ChatContactDoc chatContactDoc;

    // QUERYs
    private ChatContactQuery chatContactQuery;

    public void setMessage(IMessage message) {
	this.message = message;
    }

    private Contactable getContactable() {
	if (this.contactable == null) {
	    if (message != null) {
		this.contactable = PostManUtil.getContactMeta(message.contact());
	    }
	}
	return this.contactable;
    }

    public ChatContactDoc getChatContactDoc() {
	if (this.chatContactDoc == null) {
	    Contactable c = getContactable();
	    this.chatContactDoc = commonMongoTemplate.findById(c.getContactId(), ChatContactDoc.class);
	}
	return this.chatContactDoc;
    }

    public ChatContactQuery getChatContactQuery() {
	if (this.chatContactQuery == null) {
	    ChatContactDoc chatContactDoc = this.getChatContactDoc();
	    this.chatContactQuery = new ChatContactQuery(chatContactDoc);
	}
	return this.chatContactQuery;
    }

    public void commitChatContactQuery() {
	if (this.chatContactQuery != null) {
	    commonMongoTemplate.updateFirst(this.chatContactQuery);
	    this.chatContactDoc = null;
	}
    }

    public void log(ErrorObject error) {
	commonMongoTemplate.save(error);
    }

}
