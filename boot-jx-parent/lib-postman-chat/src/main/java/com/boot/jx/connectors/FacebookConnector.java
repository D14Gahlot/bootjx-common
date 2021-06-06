package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.FACEBOOK)
public class FacebookConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacebookConnector.class);

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	ExtUtilService extUtilService;

	public void send(OutboxMessage outboxMessage) {
		try {
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
				if (ArgUtil.is(mediaReply)) {
					if ("image".equalsIgnoreCase(mediaReply.getType())) {
						outboxMessage.attachment(
								new Attachment().mediaURL(mediaReply.getUrl()).mediaType(FileType.IMAGE.toString()));
					}
				} else {
					tmplClient.process(outboxMessage);
				}
			}
			facebooClient.send(outboxMessage);
			outboxMessage.updateStatus(Message.Status.SENT);
		} catch (Exception e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		}
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		this.reply(inboxMessage, new OutboxMessage().message("Our agent will get in touch with you"));
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
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
		FacebookUserProfile profile = facebooClient.getUserProfile(inboxMessage.getFrom(), inboxMessage.getLane());
		contact.setProfilePic(profile.getProfilePic());
		contact.setName(profile.getFirstName() + " " + profile.getLastName());
		contact.setEmail(profile.getEmail());
		return true;
	}

}
