package com.boot.jx.postman.service;

import java.io.IOException;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.cache.ContactsCache;
import com.boot.jx.postman.model.TGMessage;
import com.boot.jx.tunnel.TunnelMessage;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;

@Configuration
@EnableScheduling
@Component
@Service
public class TelegramService {

	private static final String TELEGRAM_BACKUP_Q = "telegram_0";

	@Autowired
	private ContactsCache contactsCache;

	private TelegramBot defaultTelegramBot;

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private AuditService auditService;

	@Autowired(required = false)
	private RedissonClient redisson;

	@Autowired
	private PostManConfig postManConfig;

	private boolean flagTeleStarted = false;

	public boolean init() {
		if (!flagTeleStarted && postManConfig.isTelegramEnabled()) {
			try {
				ApiContextInitializer.init();
				TelegramBotsApi botsApi = new TelegramBotsApi();
				defaultTelegramBot = new TelegramBot().botUsername("almullabot")
						.botToken("1060793944:AAEqLlJK0tuHVw55bCtq4Ph-ZU59rLrUcEY")
						.contactsCache(contactsCache)
						.channel(TGMessage.Channel.DEFAULT)
						.set(tunnelService, auditService);
				botsApi.registerBot(defaultTelegramBot);
				flagTeleStarted = true;
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		return flagTeleStarted;
	}

	public TGMessage send(TGMessage message) {
		if (postManConfig.isTelegramEnabled() && init()) {
			if (isValid(message)) {
				for (String to : message.getTo()) {
					defaultTelegramBot.send(to, message.getMessage());
				}
			}
		} else {
			AppContext context = AppContextUtil.getContext();
			TunnelMessage<TGMessage> tunnelMessage = new TunnelMessage<TGMessage>(message, context);
			RQueue<TunnelMessage<TGMessage>> telegramQueue = redisson.getQueue(TELEGRAM_BACKUP_Q);
			telegramQueue.add(tunnelMessage);
		}
		return message;
	}

	private boolean isValid(TGMessage msg) {
		if (ArgUtil.isEmpty(msg.getTo()) || (msg.getTo().size() == 0) || ArgUtil.isEmpty(msg.getTo().get(0))
				|| (ArgUtil.isEmpty(msg.getMessage()) && ArgUtil.isEmpty(msg.getTemplate()))) {
			PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_WHATSAPP);
			pMGaugeEvent.setTo(msg.getTo());
			pMGaugeEvent.setMessage(msg.getMessage());
			pMGaugeEvent.setResult(Result.REJECTED);
			auditService.log(pMGaugeEvent);
			return false;
		}
		return true;
	}

	@Scheduled(fixedDelay = EmailService.RESEND_INTERVAL)
	public void doTask() throws IOException {
		if (!postManConfig.isTelegramEnabled()) {
			return;
		}

		if (redisson == null) {
			throw new PostManException("No Redisson Avaialble");
		}
		RQueue<TunnelMessage<TGMessage>> emailQueue = redisson.getQueue(TELEGRAM_BACKUP_Q);
		// AppContextUtil.getTraceTime();
		for (int i = 0; i < postManConfig.getEmailRetryBatch(); i++) {
			TunnelMessage<TGMessage> emailtask = emailQueue.poll();
			if (!ArgUtil.isEmpty(emailtask)) {
				AppContextUtil.setContext(emailtask.getContext());
				AppContextUtil.init();
				TGMessage message = emailtask.getData();
				message.setAttempt(message.getAttempt() + 1);
				this.send(message);
				AppContextUtil.clear();

			}
		}

	}
}
