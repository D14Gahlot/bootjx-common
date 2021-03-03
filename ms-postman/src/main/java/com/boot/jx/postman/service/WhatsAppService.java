package com.boot.jx.postman.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.async.ExecutorConfig;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditActor;
import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.events.UserInboxEvent;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.WAMessage;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.MapBuilder;
import com.boot.utils.UniqueID;

@Component
public class WhatsAppService {

	public static String WHATS_MESSAGES = "WHATS_MESSAGES";

	@Autowired(required = false)
	RedissonClient redisson;

	@Autowired
	ApiWhaService apiWhaService;

	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private PostManConfig postManConfig;

	@Autowired
	private FileService fileService;

	@Autowired
	private AuditService auditService;

	private RBlockingQueue<WAMessage> getQueue(BigDecimal queueId) {
		if (redisson == null) {
			throw new PostManException("No Redisson Avaialble");
		}
		if (ArgUtil.isEmpty(queueId) || queueId.equals(BigDecimal.ZERO)) {
			return redisson.getBlockingQueue(WHATS_MESSAGES + "_" + AppContextUtil.getTenant());
		}
		return redisson.getBlockingQueue(WHATS_MESSAGES + "_" + queueId + "_" + AppContextUtil.getTenant());
	}

	public WAMessage resolveTemplate(WAMessage waMessage) {
		if (ArgUtil.isEmpty(waMessage.getMessage()) && waMessage.getTemplate() != null) {
			Context context = new Context(postManConfig.getLocal(waMessage));
			context.setVariables(waMessage.getModel());

			File file = new File();
			file.setTemplate(waMessage.getTemplate());
			file.setModel(waMessage.getModel());
			file.setLang(waMessage.getLang());

			String msg = fileService.create(file, ContactType.WHATSAPP).getContent();
			waMessage.setMessage(msg);
		}

		if (ArgUtil.is(waMessage.getFiles()) && ArgUtil.is(waMessage.getFiles().get(0))
				&& ArgUtil.is(waMessage.getFiles().get(0).getUrl())) {
			String msg = waMessage.getMessage();
			msg = (msg + " Download File : " + waMessage.getFiles().get(0).getUrl());
			waMessage.setMessage(msg);
			waMessage.setFiles(null);
		}

		return waMessage;
	}

	@Async(ExecutorConfig.EXECUTER_DIAMOND)
	public WAMessage send(WAMessage message) {
		message.setId(AppContextUtil.getTraceId());
		message = resolveTemplate(message);
		if (isValid(message)) {
			WAMessage.Channel channelType = ArgUtil.parseAsEnumT(message.getChannel(), WAMessage.Channel.class);
			if (WAMessage.Channel.APIWHA == channelType) {
				apiWhaService.sendWAMessage(message);
			} else {
				RBlockingQueue<WAMessage> queue = getQueue(BigDecimal.ZERO);
				queue.add(message);
			}
		} else {
			message.setStatus(Status.NSENT);
		}
		return message;
	}

	@Async(ExecutorConfig.EXECUTER_DIAMOND)
	public List<WAMessage> send(List<WAMessage> msgs) {
		for (WAMessage waMessage : msgs) {
			send(waMessage);
		}
		return msgs;
	}

	@Async(ExecutorConfig.EXECUTER_DIAMOND)
	public WAMessage send(WAMessage message, BigDecimal queueId) {
		if (isValid(message)) {
			RBlockingQueue<WAMessage> queue = getQueue(queueId);
			message = resolveTemplate(message);
			queue.add(message);
		}
		return message;
	}

	public WAMessage poll(BigDecimal queueId) throws InterruptedException {
		RBlockingQueue<WAMessage> queue = getQueue(queueId);
		WAMessage message = queue.poll(1, TimeUnit.MINUTES);
		if (message == null) {
			RBlockingQueue<WAMessage> queue2 = getQueue(queueId.add(BigDecimal.ONE));
			message = queue2.poll(2, TimeUnit.SECONDS);
		}
		if (!isValid(message)) {
			message = null;
		}
		return message == null ? new WAMessage() : message;
	}

	public Map<String, Object> status(BigDecimal queueId) throws InterruptedException {
		RBlockingQueue<WAMessage> queue = getQueue(queueId);
		return MapBuilder.map().put("qName", queue.getName()).put("qSize", queue.size())
				.put("nextMessage", queue.peek()).build();
	}

	@Async(ExecutorConfig.EXECUTER_DIAMOND)
	public void onMessage(ListRequestModel<Map<String, String>> data, BigDecimal queueId) {
		List<Map<String, String>> messages = data.getValues();

		String sessionId = AppContextUtil.getSessionId(false, UniqueID.generateSessionId());

		for (Map<String, String> map : messages) {

			AppContextUtil.setSessionId(sessionId);
			AppContextUtil.getTraceId(true, true);
			AppContextUtil.resetTraceTime();
			AppContextUtil.init();

			PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.ON_WHATSAPP);

			try {
				String to = ArgUtil.parseAsString(map.get("to"), Constants.BLANK);
				String from = ArgUtil.parseAsString(map.get("from"), Constants.BLANK);

				UserInboxEvent userInboxEvent = new UserInboxEvent();
				userInboxEvent.setWaChannel(WAMessage.Channel.DEFAULT);
				userInboxEvent.setQueue(queueId);
				userInboxEvent.setFrom(from);
				userInboxEvent.setTo(to);
				userInboxEvent.setMessage(ArgUtil.parseAsString(map.get("text"), Constants.BLANK));

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
	}

	private boolean isValid(WAMessage msg) {
		if (ArgUtil.isEmpty(msg)) {
			return false;
		}
		if (ArgUtil.isEmpty(msg.getTo()) || (msg.getTo().size() == 0) || ArgUtil.isEmpty(msg.getTo().get(0))
				|| (ArgUtil.isEmpty(msg.getMessage()) && ArgUtil.isEmpty(msg.getTemplate()))) {
			PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_WHATSAPP).set(msg);
			pMGaugeEvent.setTo(msg.getTo());
			pMGaugeEvent.setMessage(msg.getMessage());
			pMGaugeEvent.setResult(Result.REJECTED);
			auditService.log(pMGaugeEvent);
			return false;
		}
		return true;
	}
}
