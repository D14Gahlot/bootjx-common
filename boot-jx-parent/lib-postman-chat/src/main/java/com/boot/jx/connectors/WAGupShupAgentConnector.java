package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.gupshup.GupShupClientAgent;
import com.boot.jx.postman.gupshup.GupShupClientChat;
import com.boot.jx.postman.gupshup.GupShupConfigClient;
import com.boot.jx.postman.gupshup.GupShupInboundV2;
import com.boot.jx.postman.gupshup.GupShupClientNotify;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = "GUPSHUPAGENT")
public class WAGupShupAgentConnector implements ConnectorHandler {

	@Autowired
	private GupShupClientChat gupShupChatClient;

	@Autowired
	private GupShupClientNotify gupShupNotifyClient;

	@Autowired
	private GupShupClientAgent gupShupAgentClient;

	@Autowired
	private PostManClient postManClient;

	@Autowired
	protected GupShupConfigClient gupShupConfig;

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(chatContactDoc.getChannelType());
		outboxMessage.setLane(chatContactDoc.getLane());
		if (ArgUtil.isEqual(outboxMessage.getChannel(), Channel.GUPSHUPAGENT.toString())) {
			if (outboxMessage.isViaAgent() && ArgUtil.isEmpty(outboxMessage.getFiles())) {
				gupShupChatClient.sendMessage(chatContactDoc.getCsid(), outboxMessage.getMessage());
			} else if (outboxMessage.isTemplateMsg() || outboxMessage.isQRButtons()) {
				gupShupNotifyClient.send(outboxMessage);
			} else {
				gupShupChatClient.send(outboxMessage);
			}
		} else if (ArgUtil.isEqual(outboxMessage.getChannel(), Channel.DEFAULT.toString())) {
			MessageBox mb = new MessageBox();
			mb.push(outboxMessage);
			postManClient.send(mb);
		}
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(inboxMessage.getChannel());
		outboxMessage.setLane(inboxMessage.getLane());
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.GUPSHUPAGENT.toString())) {
			if (outboxMessage.isViaAgent() && ArgUtil.isEmpty(outboxMessage.getFiles())) {
				gupShupAgentClient.sendViaAgent(inboxMessage, outboxMessage.getMessage());
			} else if (outboxMessage.isTemplateMsg() || outboxMessage.isQRButtons()) {
				// gupShupNotifyClient.optIn(inboxMessage.getFrom());
				gupShupNotifyClient.send(outboxMessage);
			} else {
				gupShupChatClient.send(outboxMessage);
			}
		} else if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.DEFAULT.toString())) {
			Message<?> reply = inboxMessage.replyMessage(outboxMessage.getMessage());
			MessageBox mb = new MessageBox();
			mb.push(reply);
			postManClient.send(mb);
		}
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.GUPSHUPAGENT.toString())) {
			gupShupAgentClient.assignToAgent(inboxMessage.getTo(), inboxMessage.getFrom(),
					inboxMessage.session().getDept());
		} else if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.DEFAULT.toString())) {
			Message<?> reply = inboxMessage.replyMessage("Call us @ " + gupShupConfig.getGupShupWaNumber());
			MessageBox mb = new MessageBox();
			mb.push(reply);
			postManClient.send(mb);
		}
		return inboxMessage;
	}

	@Override
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
		return true;
	}

	public InboxMessage toInboxMessage(GupShupInboundV2 inboundV2) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ContactType.WHATSAPP);
		inboxMessage.setChannel(Channel.GUPSHUPAGENT.toString());
		inboxMessage.from(inboundV2.getMessages().get(0).getFrom());
		inboxMessage.setFromName(inboundV2.getContacts().get(0).getProfile().getName());

		if (ArgUtil.areEqual(inboundV2.getMessages().get(0).getType(), "button")) {
			inboxMessage.setMessage(inboundV2.getMessages().get(0).getButton().getText());
		} else {
			inboxMessage.setMessage(inboundV2.getMessages().get(0).getText().getBody());
		}

		inboxMessage.setTo(inboundV2.getContacts().get(0).getWaId());
		inboxMessage.setMessageIdExt(inboundV2.getMessages().get(0).getId());
		return inboxMessage;
	}

	@Override
	public void send(OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub

	}
}
