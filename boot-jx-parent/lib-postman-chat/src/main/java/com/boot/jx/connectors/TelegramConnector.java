package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.TELEGRAM)
public class TelegramConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramConnector.class);

	@Autowired
	private TelegramClient telegramClient;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	public void send(OutboxMessage outboxMessage) {
		try {
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
				if (ArgUtil.is(mediaReply)) {
					if ("image".equalsIgnoreCase(mediaReply.getType())) {
						outboxMessage.attachment(new Attachment().mediaURL(mediaReply.getUrl())
								.mediaType(FileType.IMAGE.toString()).mediaCaption(mediaReply.getTitle()));
						telegramClient.send(outboxMessage);
					}
				} else {
					tmplClient.process(outboxMessage);
					telegramClient.send(outboxMessage);
				}
			} else {
				telegramClient.send(outboxMessage);
			}
			outboxMessage.updateStatus(OutboxMessage.Status.SENT);
		} catch (Exception e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		}

	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	public InboxMessage toInboxMessage(String lane, Update update) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setFrom(ArgUtil.parseAsString(update.getMessage().getChatId()));
		if (ArgUtil.is(update.getMessage())) {
			inboxMessage.setMessageIdExt(ArgUtil.parseAsString(update.getMessage().getMessageId()));
			inboxMessage.setMessage(update.getMessage().getText());
		}
		inboxMessage.setOriginalMessage(update);
		inboxMessage.setContactType(ContactType.TELEGRAM);
		inboxMessage.setLane(lane);
		return inboxMessage;
	}

	@Override
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {

		Update update = JsonUtil.parse(inboxMessage.getOriginalMessage(), Update.class);

		if (ArgUtil.is(update) && ArgUtil.is(update.getMessage()) && ArgUtil.is(update.getMessage().getFrom())
				&& ArgUtil.is(update.getMessage().getContact())) {

			if (ArgUtil.isEqual(update.getMessage().getFrom().getId(), update.getMessage().getContact().getUserID())) {
				contact.setName(update.getMessage().getFrom().getFirstName() + " "
						+ update.getMessage().getFrom().getLastName());
				contact.setPhone(update.getMessage().getContact().getPhoneNumber());
			}

		}

		if (ArgUtil.isEmpty(contact.getPhone())) {
			telegramClient.promptShareNumber(inboxMessage.getFrom(),
					"Confirm that you would like to share your contact number and continue, by clicking on the button below",
					inboxMessage.getLane());
			return false;
		}

		return true;
	}

}
