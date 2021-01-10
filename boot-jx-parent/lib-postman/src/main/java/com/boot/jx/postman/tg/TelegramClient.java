package com.boot.jx.postman.tg;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-telegram.properties")
@EnableEncryptableProperties
public class TelegramClient {

	@Value("${postman.telegram.webhook.url}")
	private String telegramWebhookUrl;
	@Value("${postman.telegram.webhook.path}")
	private String telegramWebhooPath;

	@Value("${postman.telegram.default.lane}")
	private String defaultLane;

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramClient.class);

	public static class PATH {
		public static final String URL = "https://api.telegram.org";
		public static final String BOT = "/bot/{accessToken}";
		public static final String BOT_SET_WEBHOOK = BOT + "/setWebHook";
	}

	@Autowired
	RestService restService;

	@Autowired
	private Environment environment;

	public String registerWebhook(String callbackURL, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String accessToken = environment.getProperty("postman.telegram.lane." + lane + ".accessToken");
		return restService.ajax(PATH.URL).path(PATH.BOT_SET_WEBHOOK).pathParam("accessToken", accessToken)
				.field("url", callbackURL + telegramWebhooPath).post().asString();
	}

	public void sendReply(String id, String text, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String accessToken = environment.getProperty("postman.telegram.lane." + lane + ".accessToken");
		LOGGER.info("Message result to {} : {}", id, lane);

	}

	@PostConstruct
	public void init() {
		if (ArgUtil.is(telegramWebhookUrl)) {
			LOGGER.info("WebHook registered to {}", registerWebhook(telegramWebhookUrl, defaultLane));
		}
	}
}
