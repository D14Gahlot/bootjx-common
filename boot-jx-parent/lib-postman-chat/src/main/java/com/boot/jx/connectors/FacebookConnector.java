package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.AbstractConnector;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.FACEBOOK)
public class FacebookConnector extends AbstractConnector {

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
				QuickMedia mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
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
		event.contact().setChannel("PAGE");
		event.setFrom(id);
		event.contact().setCsid(id);
		event.setMessage(m.getMessage().getText());
		event.to().add(m.getRecipient().get("id"));
		event.contact().type(ContactType.FACEBOOK);
		event.contact().setLane(lane);
		return event;
	}

	@Override
	public boolean initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		FacebookUserProfile profile = facebooClient.getUserProfile(inboxMessage.getFrom(),
				inboxMessage.contact().getLane());
		ChatContactQuery contactQuery = messageContext.getChatContactQuery();
		contactQuery.setProfilePic(profile.getProfilePic());
		contactQuery.setName(profile.getFirstName() + " " + profile.getLastName());
		contactQuery.setEmail(profile.getEmail());
		return true;
	}

}
