package com.boot.jx.connectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.store.SessionStore;

@Component
@ConnectorMapping(ContactType.FACEBOOK)
public class FacebookConnector implements ConnectorHandler {

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public void sendReply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		facebooClient.sendReply(inboxMessage.getFrom(), outboxMessage.getMessage(), inboxMessage.getLane());
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		facebooClient.sendReply(inboxMessage.getFrom(), "Call us @ " + gupShupConfig.getGupShupWaNumber(),
				inboxMessage.getLane());
		return inboxMessage;
	}

	public InboxMessage toInboxMessage(FacebookMessaging m, String lane) {
		String id = m.getSender().get("id");
		InboxMessage event = new InboxMessage();
		event.setChannel("PAGE");
		event.from(id);
		event.setMessage(m.getMessage().getText());
		event.setTo(m.getRecipient().get("id"));
		event.setContactType(ContactType.FACEBOOK);
		event.setLane(lane);
		return event;
	}

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		ChatContactDoc contact = sessionStore.getContact(inboxMessage);
		FacebookUserProfile profile = facebooClient.getUserProfile(inboxMessage.getFrom(), inboxMessage.getLane());

		contact.setProfilePic(profile.getProfilePic());
		contact.setName(profile.getFirstName() + " " + profile.getLastName());

		sessionStore.save(contact);

		return true;
	}

}
