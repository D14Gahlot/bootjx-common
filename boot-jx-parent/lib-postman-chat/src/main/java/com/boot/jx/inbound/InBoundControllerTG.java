package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.connectors.TelegramConnector;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;

@RestController
public class InBoundControllerTG {

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private TelegramConnector telegramConnector;

	@Autowired
	private TelegramClient telegramClient;

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tg/callback", method = RequestMethod.POST)
	public Update onReceiveMessage(@RequestBody Update update) throws InterruptedException {
		InboxMessage event = telegramConnector.toInboxMessage(update);
		inBoundService.invokeMethods(event);
		return update;
	}

	@Scheduled(fixedDelay = 5000)
	public void registerService() {
		telegramClient.initWebhook();
	}

}
