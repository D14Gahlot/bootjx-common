package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(ContactType.TELEGRAM)
public class TelegramConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramConnector.class);

	@Autowired
	private TelegramClient telegramClient;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
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

		if (ArgUtil.is(update.getMessage().getContact())) {
			Contact contact = update.getMessage().getContact();
			inboxMessage.data().put("contact", contact);
		}
		inboxMessage.setOriginalMessage(update);
		return inboxMessage;
	}

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		ChatContactDoc contact = sessionStore.getContact(inboxMessage);

		if (ArgUtil.is(inboxMessage.getForm())) {
			if (ArgUtil.is(inboxMessage.getForm().get("name"))) {
				contact.setName(ArgUtil.parseAsString(inboxMessage.getForm().get("name")));
			}
			if (ArgUtil.is(inboxMessage.getForm().get("email"))) {
				contact.setEmail(ArgUtil.parseAsString(inboxMessage.getForm().get("email")));
			}
			sessionStore.save(contact);
		}

		if (ArgUtil.isEmpty(contact.getName())) {
			reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage(null).template("pm-user-login-form"));
			return false;
		}

		if (ArgUtil.isEmpty(contact.getEmail())) {
			reply(inboxMessage, (OutboxMessage) inboxMessage.replyMessage(null).template("pm-user-login-form"));
			return false;
		}

		return true;
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
	}

	@Scheduled(fixedDelay = 5000)
	public void registerService() {
		telegramClient.initWebhook();
	}

}
