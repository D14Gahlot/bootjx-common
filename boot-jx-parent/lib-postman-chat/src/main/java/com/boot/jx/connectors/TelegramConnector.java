package com.boot.jx.connectors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.MapModel;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
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

	public void send(String lane, String to, OutboxMessage outboxMessage) {
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if (ArgUtil.is(mediaReply)) {
				if ("image".equalsIgnoreCase(mediaReply.getType())) {
					outboxMessage.attachment(
							new Attachment().mediaURL(mediaReply.getUrl()).mediaType(File.FileType.IMAGE.toString()));
					telegramClient.sendPhoto(lane, to, mediaReply.getUrl(), mediaReply.getTitle());
				}
			} else {
				tmplClient.process(outboxMessage);

				SendMessage sendMessage = new SendMessage();
				sendMessage.setText(outboxMessage.getMessage());
				if (outboxMessage.options().containsKey("buttons")) {

					List<TmplElement> buttons = new MapModel(outboxMessage.options()).entry("buttons")
							.asList(new TmplElement());

					ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
					replyKeyboardMarkup.setSelective(true);
					replyKeyboardMarkup.setResizeKeyboard(true);
					replyKeyboardMarkup.setOneTimeKeyboard(true);

					List<KeyboardRow> keyboard = new ArrayList<>();
					KeyboardRow keyboardFirstRow = new KeyboardRow();

					for (TmplElement button : buttons) {
						keyboardFirstRow.add(button.getLabel());
					}

					keyboard.add(keyboardFirstRow);

					/**
					 * KeyboardRow keyboardSecondRow = new KeyboardRow();
					 * keyboardSecondRow.add(getAlertsCommand(language));
					 * keyboardSecondRow.add(getBackCommand(language));
					 * keyboard.add(keyboardSecondRow);
					 **/

					replyKeyboardMarkup.setKeyboard(keyboard);
					sendMessage.setReplyMarkup(replyKeyboardMarkup);
				}
				telegramClient.sendReply(lane, to, sendMessage);
			}
		} else {
			telegramClient.sendReply(lane, to, outboxMessage.getMessage());
		}
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		this.send(chatContactDoc.getLane(), chatContactDoc.getCsid(), outboxMessage);
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		this.send(inboxMessage.getLane(), inboxMessage.getFrom(), outboxMessage);
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
			telegramClient.promptShareNumber(inboxMessage.getContactId(),
					"Confirm that you would like to share your contact number and continue, by clicking on the button below",
					inboxMessage.getLane());
			return false;
		}

		return true;
	}

}
