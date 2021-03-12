package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.client.GupShupNotifyClient;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = "GUPSHUPW")
public class WAGupShupConnector implements ConnectorHandler {

	@Autowired
	private GupShupChatClient gupShupChatClient;

	@Autowired
	private GupShupNotifyClient gupShupNotifyClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Override
	public void send(String lane, String to, OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(chatContactDoc.getChannelType());
		gupShupNotifyClient.sendMessage(outboxMessage);
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if (ArgUtil.is(templateReply)) {
				if ("image".equalsIgnoreCase(templateReply.getType())) {
					outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
							.mediaType(File.FileType.IMAGE.toString()));
					gupShupChatClient.sendMessage(outboxMessage);
				}
			} else {
				tmplClient.process(outboxMessage);
				gupShupChatClient.sendMessage(outboxMessage);
			}
		} else {
			gupShupChatClient.sendMessage(outboxMessage);
		}
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	@Override
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
		if (ArgUtil.is(inboxMessage.getOriginalMessage())) {
			GupShupInbound dm = JsonUtil.parse(inboxMessage.getOriginalMessage(), GupShupInbound.class);
			contact.setName(dm.getName());
			contact.setPhone(dm.getMobile());
		}
		return true;
	}

	public InboxMessage toInboxMessage(GupShupInbound inbound) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ContactType.WHATSAPP);
		inboxMessage.setChannel("GUPSHUPW");
		inboxMessage.from(inbound.getMobile());
		inboxMessage.setFromName(inbound.getName());
		inboxMessage.setMessage(inbound.getText());
		inboxMessage.setTo(inbound.getWaNumber());
		inboxMessage.setMessageIdExt(inbound.getReplyId());
		return inboxMessage;
	}
}
