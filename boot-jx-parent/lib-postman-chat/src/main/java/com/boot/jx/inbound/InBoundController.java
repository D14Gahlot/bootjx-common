package com.boot.jx.inbound;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatClientConfig;
import com.boot.jx.chat.ChatStatusReportService;
import com.boot.jx.connectors.FacebookConnector;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.Random;

@RestController
public class InBoundController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundController.class);

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private ChatClientConfig chatClientConfig;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/webhook/callback", method = RequestMethod.POST)
	public String setWebHook(@RequestParam(required = false) String callbackUrl) throws InterruptedException {
		chatClientConfig.setInboundForwardUrl(callbackUrl);
		return callbackUrl;
	}

	@RequestMapping(value = "/int/webhook/callback", method = RequestMethod.GET)
	public String getWebHook() throws InterruptedException {
		return chatClientConfig.getInboundForwardUrl();
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/int/inbound/callback", method = RequestMethod.POST)
	public InboxMessage onInboundCallback(@RequestBody InboxMessage inboxMessage,
			@RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
		// botService.arhive(inbound);
		if (PostManUtil.hasValidCheckSum(inboxMessage)) {
			inBoundService.invokeMethods(inboxMessage);
		}
		return inboxMessage;
	}

	static AtomicInteger counter = new AtomicInteger(1);

	@Autowired
	private ChatStatusReportService chatStatusReportService;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/status/callback", method = RequestMethod.POST)
	public List<MessageReport> onStatusCallback() throws InterruptedException {
		List<MessageReport> list = new LinkedList<MessageReport>();
		String ser = ArgUtil.parseAsString(counter.getAndIncrement());
		for (int i = 0; i < 5; i++) {
			MessageReport report = new MessageReport();
			report.setMessageIdRef(ArgUtil.parseAsString(ser));
			report.setMessageIdExt(ArgUtil.parseAsString(i));
			int statusint = Random.getInt(0, 5);
			report.setStatus(Message.Status.values()[statusint]);
			report.setTimestamp(Random.getInt(100, 999));
			list.add(report);
		}
		chatStatusReportService.offer(list);
		chatStatusReportService.process(ser);
		return list;
	}

	@ApiVendorHeaders
	@RequestMapping(value = ChatClient.PATH.ASSIGN_TO_AGENT, method = RequestMethod.POST)
	public ApiResponse<InboxMessage, ?> assignToAgent(@RequestBody InboxMessage inboxMessage)
			throws InterruptedException {
		return inBoundService.assignToAgent(inboxMessage);
	}

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private FacebookConnector facebookConnector;

	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.GET)
	public String get(@RequestParam(name = "hub.verify_token") String token,
			@RequestParam(name = "hub.challenge") String challenge, @RequestParam(required = false) String lane,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature) {
		return facebooClient.registerWebhook(token, challenge, lane);
	}

	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessage(@RequestBody FacebookHookRequest request,
			@RequestParam(required = false) String lane,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature) throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethods(event);
				// facebooClient.sendReply(event.getContactId(), "Helo", pageEntry.getId());
			});
		});
		return request;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback/{lane}", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessageLane(@RequestBody FacebookHookRequest request, @PathVariable String lane)
			throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethods(event);
			});
		});
		return request;
	}
}
