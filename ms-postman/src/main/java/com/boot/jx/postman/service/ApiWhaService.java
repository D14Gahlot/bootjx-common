package com.boot.jx.postman.service;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.AuditService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.events.UserInboxEvent;
import com.boot.jx.postman.model.PostManFile;
import com.boot.jx.postman.model.WAMessage;
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Component
public class ApiWhaService {

	private static Logger LOGGER = LoggerService.getLogger(ApiWhaService.class);

	@Value("${apiwha.api.key}")
	String apiWhaKey;

	@Autowired
	private RestService restService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FileService fileService;
	@Autowired
	private TunnelService tunnelService;

	@Autowired
	private PostManConfig postManConfig;

	public WAMessage sendWAMessage(WAMessage message) {

		String to = null;
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_WHATSAPP);
		try {
			to = message.getTo() != null ? message.getTo().get(0) : null;
			if (message.getTemplate() != null) {
				Context context = new Context(postManConfig.getLocal(message));
				context.setVariables(message.getModel());
				PostManFile file = new PostManFile();
				file.setTemplate(message.getTemplate());
				file.setModel(message.getModel());
				file.setLang(message.getLang());
				message.setMessage(fileService.create(file).getContent());
			}

			if (ArgUtil.isEmpty(to)) {
				auditService.gauge(pMGaugeEvent.set(message).result(Result.REJECTED));
			} else {
				String responseText = restService.ajax("http://panel.apiwha.com/send_message.php")
						.field("apikey", apiWhaKey).field("number",
								message.getTo())
						.postForm().asString();
				auditService.gauge(pMGaugeEvent.responseText(responseText).set(message));
			}
		} catch (Exception e) {
			auditService.excep(pMGaugeEvent.set(message).result(Result.ERROR), e);
		}
		return message;
	}

	public void onMessage(Map<String, Object> dataMap) {
		String event = ArgUtil.parseAsString(dataMap.get("event"), Constants.BLANK);
		if ("INBOX".equals(event)) {
			UserInboxEvent userInboxEvent = new UserInboxEvent();
			userInboxEvent.setWaChannel(WAMessage.Channel.APIWHA);
			userInboxEvent.setFrom(ArgUtil.parseAsString(dataMap.get("from"), Constants.BLANK));
			userInboxEvent.setTo(ArgUtil.parseAsString(dataMap.get("to"), Constants.BLANK));
			userInboxEvent.setMessage(ArgUtil.parseAsString(dataMap.get("text"), Constants.BLANK));
			tunnelService.task(userInboxEvent);
		}
	}

}
