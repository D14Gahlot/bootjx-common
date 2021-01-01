package com.boot.jx.postman.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.WAMessage;
import com.boot.jx.postman.service.WhatsAppService;
import com.boot.utils.ArgUtil;

@RestController
public class WhatsAppController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WhatsAppController.class);
	private static final long MESSAGE_TIMEOUT = 6 * 60 * 60 * 1000;

	@Autowired
	WhatsAppService whatsAppService;

	@Autowired
	AuditService auditService;

	@RequestMapping(value = PostManUrls.WHATS_APP_SEND_BULK, method = RequestMethod.POST)
	public ApiResponse<WAMessage, Object> sendWhatsAppBulk(@RequestBody List<WAMessage> msgs)
			throws PostManException {
		return ApiResponse.buildList(whatsAppService.send(msgs));
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_SEND, method = RequestMethod.POST)
	public ApiResponse<WAMessage, Object> sendWhatsApp(@RequestBody WAMessage msg) throws PostManException {
		return ApiResponse.build(whatsAppService.send(msg));
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_SEND, method = RequestMethod.GET)
	public WAMessage sendWhatsAppGet(@RequestParam String to, @RequestParam String message,
			@RequestParam(required = false) WAMessage.Channel channel) throws PostManException {
		WAMessage msg = new WAMessage();
		msg.addTo(to);
		msg.setMessage(message);
		msg.setIChannel(channel);
		return whatsAppService.send(msg);
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_SEND, method = {
			RequestMethod.POST }, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public WAMessage sendWhatsAppForm(@RequestParam String to, @RequestParam String message,
			@RequestParam(required = false) WAMessage.Channel channel) throws PostManException {
		WAMessage msg = new WAMessage();
		msg.addTo(to);
		msg.setMessage(message);
		msg.setIChannel(channel);
		return whatsAppService.send(msg);
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_POLL, method = RequestMethod.GET)
	public WAMessage pollWhatsApp(@RequestParam BigDecimal q)
			throws PostManException, InterruptedException {
		return whatsAppService.poll(ArgUtil.parseAsBigDecimal(q, BigDecimal.ZERO));
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_POLL, method = RequestMethod.POST)
	public WAMessage pollWhatsApp(@RequestParam BigDecimal q, @RequestBody ListRequestModel<Map<String, String>> data)
			throws PostManException, InterruptedException {
		if (data.getValues().size() > 0) {
			whatsAppService.onMessage(data, q);
		}
		return new WAMessage();
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_STATS, method = RequestMethod.GET)
	public Map<String, Object> statsWhatsApp(BigDecimal q) throws PostManException, InterruptedException {
		return whatsAppService.status(ArgUtil.parseAsBigDecimal(q, BigDecimal.ZERO));
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_RESEND, method = RequestMethod.POST)
	public ApiResponse<WAMessage, Object> resendWhatsApp(@RequestBody WAMessage msg, @RequestParam BigDecimal q)
			throws PostManException {
		long ageOfMessage = System.currentTimeMillis() - msg.getTimestamp();

		if (ageOfMessage < MESSAGE_TIMEOUT || msg.getAttempt() < 5) {
			msg.setAttempt(msg.getAttempt() + 1);
			whatsAppService.send(msg, ArgUtil.parseAsBigDecimal(q, BigDecimal.ZERO));
		} else {
			msg.setStatus(Status.FAILED);
			return statusWhatsApp(msg, "TIMEOUT");
		}
		return ApiResponse.build(msg);
	}

	@RequestMapping(value = PostManUrls.WHATS_APP_STATUS, method = RequestMethod.POST)
	public ApiResponse<WAMessage, Object> statusWhatsApp(@RequestBody WAMessage msg,
			@RequestParam(required = false) String reason) throws PostManException {
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_WHATSAPP).set(msg);
		;
		pMGaugeEvent.setTo(msg.getTo());
		pMGaugeEvent.setMessage(msg.getMessage());
		if (msg.getStatus() == WAMessage.Status.SENT) {
			pMGaugeEvent.setResult(Result.DONE);
		} else {
			pMGaugeEvent.setResponseText(reason);
			pMGaugeEvent.setResult(Result.FAIL);
		}
		auditService.log(pMGaugeEvent);
		return ApiResponse.build(msg);
	}

}
