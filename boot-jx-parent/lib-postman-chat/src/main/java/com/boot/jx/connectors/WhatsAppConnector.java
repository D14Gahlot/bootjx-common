package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.client.GupShupNotifyClient;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(ContactType.WHATSAPP)
public class WhatsAppConnector implements ConnectorHandler {

	@Autowired
	private GupShupChatClient gupShupChatClient;

	@Autowired
	private GupShupNotifyClient gupShupNotifyClient;

	@Autowired
	private PostManClient postManClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Override
	public void send(String lane, String to, OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(chatContactDoc.getChannelType());
		if (ArgUtil.isEqual(outboxMessage.getChannel(), Channel.GUPSHUP.toString())) {
			if (outboxMessage.isViaAgent() && ArgUtil.isEmpty(outboxMessage.getFiles())) {
				gupShupChatClient.sendMessage(chatContactDoc.getCsid(), outboxMessage.getMessage());
			} else if (outboxMessage.isTemplate() || outboxMessage.isQRButtons()) {
				gupShupNotifyClient.sendMessage(outboxMessage);
			} else {
				gupShupChatClient.sendMessage(outboxMessage);
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
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.GUPSHUP.toString())) {
			if (outboxMessage.isViaAgent() && ArgUtil.isEmpty(outboxMessage.getFiles())) {
				gupShupChatClient.sendViaAgent(inboxMessage, outboxMessage.getMessage());
			} else if (outboxMessage.isTemplate() || outboxMessage.isQRButtons()) {
				// gupShupNotifyClient.optIn(inboxMessage.getFrom());
				gupShupNotifyClient.sendMessage(outboxMessage);
			} else {
				gupShupChatClient.sendMessage(outboxMessage);
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
		if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.GUPSHUP.toString())) {
			gupShupChatClient.assignToAgent(inboxMessage.getTo(), inboxMessage.getFrom(),
					inboxMessage.session().getAssignedToDept());
		} else if (ArgUtil.isEqual(inboxMessage.getChannel(), Channel.DEFAULT.toString())) {
			Message<?> reply = inboxMessage.replyMessage("Call us @ " + gupShupConfig.getGupShupWaNumber());
			MessageBox mb = new MessageBox();
			mb.push(reply);
			postManClient.send(mb);
		}
		return inboxMessage;
	}

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		return true;
	}

}
