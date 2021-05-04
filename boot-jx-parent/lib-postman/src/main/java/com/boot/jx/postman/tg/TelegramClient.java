package com.boot.jx.postman.tg;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.TGMessage;
import com.boot.jx.postman.tg.TelegramModels.TGSendDocument;
import com.boot.jx.postman.tg.TelegramModels.TGSendPhoto;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-telegram.properties")
@EnableEncryptableProperties
public class TelegramClient {

	boolean isRegistered;

	@Value("${postman.telegram.webhook.url}")
	private String telegramWebhookUrl;
	@Value("${postman.telegram.webhook.path}")
	private String telegramWebhooPath;

	@Value("${postman.telegram.default.lane}")
	private String defaultLane;

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramClient.class);

	public static class PATH {
		public static final String URL = "https://api.telegram.org";
		public static final String BOT = "/bot{accessToken}";
		public static final String BOT_SET_WEBHOOK = BOT + "/setWebHook";
		public static final String BOT_SEND_MESSAGE = BOT + "/sendMessage";
	}

	@Autowired
	RestService restService;

	@Autowired
	private PMEnvironment environment;

	private String getAccessToken(String lane) {
		TelegramConfig config = environment.get().telegram(lane);
		return config.getAccessToken();
	}

	public String registerWebhook(String callbackURL, String lane) {
		TelegramConfig config = environment.get().telegram(lane);
		return restService.ajax(PATH.URL).path(PATH.BOT_SET_WEBHOOK).pathParam("accessToken", config.getAccessToken())
				.field("url", callbackURL + telegramWebhooPath)
				.queryParam("url", callbackURL + telegramWebhooPath + "/" + lane).post().asString();
	}

	public TGMessage sendReply(String lane, String id, SendMessage sendMessage) {
		SendMessage message = sendMessage; // Create a SendMessage object with mandatory fields
		sendMessage.setChatId(id);
		return restService.ajax(PATH.URL).path(PATH.BOT_SEND_MESSAGE).pathParam("accessToken", getAccessToken(lane))
				.post(message).as(TGMessage.class);
	}

	public TGMessage sendReply(String lane, String id, String text) {
		SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
				.setChatId(id).setText(text);
		return restService.ajax(PATH.URL).path(PATH.BOT_SEND_MESSAGE).pathParam("accessToken", getAccessToken(lane))
				.post(message).as(TGMessage.class);
	}

	public TGMessage sendPhoto(String lane, String id, String photo, String caption) {
		SendPhoto message = new TGSendPhoto() // Create a SendMessage object with mandatory fields
				.setChatId(id).setPhoto(photo).setCaption(caption);
		return restService.ajax(PATH.URL).path(PATH.BOT).path("/sendPhoto")
				.pathParam("accessToken", getAccessToken(lane)).post(message).as(TGMessage.class);
	}

	public TGMessage sendDocument(String lane, String id, String document, String caption) {
		SendDocument message = new TGSendDocument() // Create a SendMessage object with mandatory fields
				.setChatId(id).setDocument(document).setCaption(caption);
		return restService.ajax(PATH.URL).path(PATH.BOT).path("/sendDocument")
				.pathParam("accessToken", getAccessToken(lane)).post(message).as(TGMessage.class);
	}

	public String promptShareNumber(String id, String text, String lane) {
		SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
				.setChatId(id);
		message.setText(text);

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
		keyboardButton.setText(text).setRequestContact(true);
		keyboardFirstRow.add(keyboardButton);
		// add array to list
		keyboard.add(keyboardFirstRow);
		// add list to our keyboard
		replyKeyboardMarkup.setKeyboard(keyboard);

		return restService.ajax(PATH.URL).path(PATH.BOT_SEND_MESSAGE).pathParam("accessToken", getAccessToken(lane))
				.post(message).asString();

	}

	public String registerWebhook(String lane) {
		try {
			TelegramConfig config = environment.get().telegram(lane);
			if (ArgUtil.is(config.getWebhookUrl())) {
				String resp = registerWebhook(config.getWebhookUrl(), lane);
				LOGGER.info("WebHook registered to {}", resp);
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerWebhookOnce(String lane) {
		try {
			if (!isRegistered) {
				registerWebhook(lane);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
