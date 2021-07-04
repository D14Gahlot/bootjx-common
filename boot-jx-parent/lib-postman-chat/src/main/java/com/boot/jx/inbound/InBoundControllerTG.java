package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.connectors.TelegramConnector;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.utils.ArgUtil;

import twitter4j.TwitterException;

@RestController
public class InBoundControllerTG {

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private TelegramConnector telegramConnector;

	@Autowired
	private TelegramClient telegramClient;

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tg/callback/{lane}", method = RequestMethod.POST)
	public Update onReceiveMessage(@PathVariable String lane, @RequestBody Update update) throws InterruptedException {
		InboxMessage event = telegramConnector.toInboxMessage(lane, update);
		inBoundService.invokeMethods(event);
		return update;
	}

	@RequestMapping(value = "/ext/inbound/tg/registerwebhook", method = RequestMethod.GET)
	public String registerwebhook(@RequestParam(required = false) String lane)
			throws InterruptedException, TwitterException {
		return telegramClient.registerWebhook(lane);
	}

	@Value("${postman.telegram.webhook.lanes}")
	private String[] webhookLanes;

	@Scheduled(fixedDelay = 5000)
	public void registerService() {
		for (String lane : webhookLanes) {
			if (ArgUtil.is(lane)) {
				telegramClient.registerWebhookOnce(lane);
			}
		}
	}

}
