package com.boot.jx.xms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.postman.ChannelConfig;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.xms.dto.OutBoundMsgBasic.OutBoundMsg;
import com.boot.jx.xms.dto.OutBoundReciept;
import com.boot.utils.ArgUtil;

@Component
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private PMEnvironment pmEnvironment;

    public OutBoundReciept send(OutBoundMsg message) {
	ChannelConfig channel = pmEnvironment.config().channels(message.getChannelId());

	OutboxMessage outboxMessage = new OutboxMessage();

	if (message.type == "text") {
	    outboxMessage.setMessage(message.getText().body);
	}

	if (message.type == "template") {
	    outboxMessage.setTemplateId(message.getTemplate().id);
	    outboxMessage.setTemplate(message.getTemplate().code);
	    outboxMessage.setLang(message.getTemplate().lang);
	}

	outboxMessage.contact().type(channel.getContactType());
	outboxMessage.contact().setChannel(channel.getChannel());
	outboxMessage.contact().setLane(channel.getLane());

	outboxMessage.contact().copyFrom(message.getToContact());

	ChatSessionDoc chatSessionDoc = sessionStore.linkSession(outboxMessage);
	if (ArgUtil.is(chatSessionDoc)) {
	    chatService.initSession(outboxMessage, chatSessionDoc);
	    chatService.send(chatSessionDoc, outboxMessage);
	}

	return new OutBoundReciept();
    }

}
