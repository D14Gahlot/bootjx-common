package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.gupshup.GupShupClientChat;
import com.boot.jx.postman.gupshup.GupShupClientNotify;
import com.boot.jx.postman.gupshup.GupShupConfigClient;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.gupshup.GupShupResp;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = "GUPSHUPW")
public class WAGupShupConnector implements ConnectorHandler {

	@Autowired
	private GupShupClientChat gupShupChatClient;

	@Autowired
	private GupShupClientNotify gupShupNotifyClient;

	@Autowired
	protected GupShupConfigClient gupShupConfig;

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
		gupShupNotifyClient.sendMessage(outboxMessage, chatContactDoc.getLane());
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		GupShupResp resp = null;
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if (ArgUtil.is(templateReply)) {
				if ("image".equalsIgnoreCase(templateReply.getType())) {
					outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
							.mediaType(File.FileType.IMAGE.toString()));
					resp = gupShupChatClient.sendMessage(outboxMessage, inboxMessage.getLane());
				}
			} else {
				tmplClient.process(outboxMessage);
				resp = gupShupChatClient.sendMessage(outboxMessage, inboxMessage.getLane());
			}
		} else {
			resp = gupShupChatClient.sendMessage(outboxMessage, inboxMessage.getLane());
		}

		if (!ArgUtil.is(resp) || !ArgUtil.is(resp.getResponse())) {
			outboxMessage.setStatus(Message.Status.SENT_ERR);
			outboxMessage.logs().add("No Response Object");
		} else if (ArgUtil.isEqual(resp.getResponse().getStatus(), "error")) {
			outboxMessage.setStatus(Message.Status.SENT_ERR);
			outboxMessage.logs()
					.add(String.format("%s : %s", resp.getResponse().getId(), resp.getResponse().getDetails()));
		} else {
			if (ArgUtil.is(resp.getResponse().getId()))
				outboxMessage.setMessageIdExt(resp.getResponse().getId());
			outboxMessage.setStatus(Message.Status.SENT);
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
		inboxMessage.setLane(inbound.getWaNumber());
		return inboxMessage;
	}
	
}
