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
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(ContactType.TELEGRAM)
public class TelegramConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramConnector.class);

	@Autowired
	private TelegramClient telegramClient;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MongoTemplate mongoTemplate;

	public void send(String lane, String to, OutboxMessage outboxMessage) {
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if ("image".equalsIgnoreCase(mediaReply.getType())) {
				telegramClient.sendPhoto(lane, to, mediaReply.getUrl(), mediaReply.getTitle());
			}
		} else {
			telegramClient.sendReply(lane, to, outboxMessage.getMessage());
		}
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		this.send(inboxMessage.getLane(), inboxMessage.getFrom(), outboxMessage);
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	public InboxMessage toInboxMessage(Update update) {
		InboxMessage inboxMessage = new InboxMessage();

		inboxMessage.setFrom(ArgUtil.parseAsString(update.getMessage().getChatId()));

		if (ArgUtil.is(update.getMessage())) {
			inboxMessage.setMessageIdExt(ArgUtil.parseAsString(update.getMessage().getMessageId()));
			inboxMessage.setMessage(update.getMessage().getText());
		}

		inboxMessage.setOriginalMessage(update);
		inboxMessage.setContactType(ContactType.TELEGRAM);
		return inboxMessage;
	}

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		ChatContactDoc contact = sessionStore.getContact(inboxMessage);

		Update update = JsonUtil.parse(inboxMessage.getOriginalMessage(), Update.class);

		if (ArgUtil.is(update) && ArgUtil.is(update.getMessage()) && ArgUtil.is(update.getMessage().getFrom())
				&& ArgUtil.is(update.getMessage().getContact())) {

			if (ArgUtil.isEqual(update.getMessage().getFrom().getId(), update.getMessage().getContact().getUserID())) {
				contact.setName(update.getMessage().getFrom().getFirstName() + " "
						+ update.getMessage().getFrom().getLastName());
				contact.setPhone(update.getMessage().getContact().getPhoneNumber());
				sessionStore.save(contact);
			}

		}

		if (ArgUtil.isEmpty(contact.getPhone())) {
			telegramClient.promptShareNumber(inboxMessage.getContactId(), "Share your number >",
					inboxMessage.getLane());
			return false;
		}

		return true;
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		this.send(chatContactDoc.getLane(), chatContactDoc.getCsid(), outboxMessage);
	}

}
