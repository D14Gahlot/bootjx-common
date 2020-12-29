package com.boot.jx.postman.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.AuditActor;
import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.AuditService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.cache.ContactsCache;
import com.boot.jx.postman.events.UserInboxEvent;
import com.boot.jx.postman.model.TGMessage;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.UniqueID;

public class TelegramBot extends TelegramLongPollingBot {

	private static Logger LOGGER = LoggerService.getLogger(TelegramBot.class);

	public String botUsername;
	public String botToken;
	public ContactsCache contactsCache;
	private TGMessage.Channel channel;

	public TelegramBot botUsername(String botUsernamem) {
		this.botUsername = botUsernamem;
		return this;
	}

	public TelegramBot botToken(String botToken) {
		this.botToken = botToken;
		return this;
	}

	public TelegramBot contactsCache(ContactsCache contactsCache) {
		this.contactsCache = contactsCache;
		return this;
	}

	public TelegramBot channel(TGMessage.Channel channel) {
		this.channel = channel;
		return this;
	}

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private AuditService auditService;

	public TelegramBot set(TunnelService tunnelService, AuditService auditService) {
		this.tunnelService = tunnelService;
		this.auditService = auditService;
		return this;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			LOGGER.info("TG TEXT: {} {}", update.getMessage().getChatId(), update.getMessage().getText());
			if (update.getMessage().getText().equals("/start")
					|| update.getMessage().getText().equals("/link")) {
				shareNumber(update);
			} else if (update.getMessage().getText().equals("/mycontact")) {
				SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
						.setChatId(update.getMessage().getChatId());
				message.setText(update.getMessage().getContact().getPhoneNumber());
				executeWithCatch(message);
			} else {
				String from = contactsCache.get("_" + update.getMessage().getFrom().getId());
				if (ArgUtil.is(from)) {
					onMessageLocal(from, update);
				} else {
					shareNumber(update);
				}
			}

		} else {
			LOGGER.info("TG NOTEXT : {}", update.getMessage().getChatId());
		}
		if (ArgUtil.is(update.getMessage().getContact())) {
			if (ArgUtil.is(update.getMessage().getFrom().getId())) {
				update.getMessage().getFrom().getId().equals(update.getMessage().getContact().getUserID());
				LOGGER.info("TG CONTACT : {}", update.getMessage().getChatId());
				String phone = update.getMessage().getContact().getPhoneNumber();
				String phoneKey = channel.name() + "#" + phone;
				// Cache ChatId against PhoneNumber
				contactsCache.put(phoneKey,
						ArgUtil.parseAsString(update.getMessage().getChatId()));
				// Cache PhoneNumber against userid
				contactsCache.put("_" + update.getMessage().getFrom().getId(),
						update.getMessage().getContact().getPhoneNumber());
				// send(phone, "You have successfully subscribed to updates");
			}
		}
	}

	private void shareNumber(Update update) {
		SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
				.setChatId(update.getMessage().getChatId());
		message.setText("Share your number >");

		// create keyboard
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		message.setReplyMarkup(replyKeyboardMarkup);
		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(true);

		// new list
		List<KeyboardRow> keyboard = new ArrayList<>();

		// first keyboard line
		KeyboardRow keyboardFirstRow = new KeyboardRow();
		KeyboardButton keyboardButton = new KeyboardButton();
		keyboardButton.setText("Share your number >").setRequestContact(true);
		keyboardFirstRow.add(keyboardButton);
		// add array to list
		keyboard.add(keyboardFirstRow);
		// add list to our keyboard
		replyKeyboardMarkup.setKeyboard(keyboard);
		executeWithCatch(message);
	}

	private void onMessageLocal(String from, Update update) {
		String sessionId = AppContextUtil.getSessionId(false, UniqueID.generateSessionId());
		AppContextUtil.setSessionId(sessionId);
		AppContextUtil.getTraceId(true, true);
		AppContextUtil.resetTraceTime();
		AppContextUtil.init();
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.ON_TG);
		try {

			UserInboxEvent userInboxEvent = new UserInboxEvent();
			userInboxEvent.setTgChannel(TGMessage.Channel.DEFAULT);
			userInboxEvent.setQueue(BigDecimal.ZERO);
			userInboxEvent.setFrom(from);
			userInboxEvent.setMessage(update.getMessage().getText());

			AppContextUtil.setActorId(new AuditActor(AuditActor.ActorType.W, from));
			pMGaugeEvent.setTo(CollectionUtil.getList(userInboxEvent.getFrom()));
			pMGaugeEvent.setMessage(userInboxEvent.getMessage());
			pMGaugeEvent.setResult(Result.DONE);

			tunnelService.task(userInboxEvent);
			auditService.gauge(pMGaugeEvent.set(Result.DONE));

		} catch (Exception e) {
			auditService.excep(pMGaugeEvent.set(Result.DONE), e);
		} finally {
			AppContextUtil.clear();
		}
	}

	public void send(String phone, String text) {
		String chatId = contactsCache.get(channel.name() + "#" + phone);
		if (ArgUtil.is(chatId)) {
			SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
					.setChatId(chatId)
					.setText(text);
			executeWithCatch(message);
		} else {
			PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_TG);
			pMGaugeEvent.setTo(CollectionUtil.getList(phone));
			pMGaugeEvent.setMessage(text);
			pMGaugeEvent.setResult(Result.FAIL);
			auditService.log(pMGaugeEvent);
		}
	}

	private void executeWithCatch(SendMessage message) {
		try {
			execute(message); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}
}