package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.tw.TwitterClient;

@Component
@ConnectorMapping(ContactType.TWITTER)
public class TwitterConnector implements ConnectorHandler {

	@Autowired
	private TwitterClient twitterClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	//@Override
	//public void sendReply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		//twitterClient.sendReply(inboxMessage.getFrom(), outboxMessage.getMessage(), inboxMessage.getLane());
	//}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		twitterClient.sendReply(inboxMessage.getFrom(), "Call us @ " + gupShupConfig.getGupShupWaNumber(),
				inboxMessage.getLane());
		return inboxMessage;
	}

	public InboxMessage toInboxMessage(FacebookMessaging m, String lane) {
		String id = m.getSender().get("id");
		InboxMessage event = new InboxMessage();
		event.setChannel(Channel.GUPSHUP.toString());
		event.from(id);
		event.setMessage(m.getMessage().getText());
		event.setTo(m.getRecipient().get("id"));
		event.setContactType(ContactType.TWITTER);
		event.setLane(lane);
		return event;
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub
		
	}

}
